"""Capture responsive screens in an isolated package on a selected emulator."""
from pathlib import Path
import argparse,json,os,re,subprocess,time,xml.etree.ElementTree as ET,zipfile
ROOT=Path(__file__).resolve().parents[2]
os.chdir(ROOT)
SDK=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))
JDK=Path(os.environ.get('JAVA_HOME','/Applications/Android Studio.app/Contents/jbr/Contents/Home'))
ADB=SDK/'platform-tools/adb';TOOLS=SDK/'build-tools/36.0.0';BUILD=ROOT/'app/build/offline'
PKG='com.rainbow.priceperuse.compatqa'
parser=argparse.ArgumentParser();parser.add_argument('--legacy',action='store_true');args=parser.parse_args()
WORK=BUILD/('compat-api21' if args.legacy else 'compat-latest');WORK.mkdir(parents=True,exist_ok=True)
OUT=ROOT/'screens/compatibility'/('api21' if args.legacy else 'latest');OUT.mkdir(parents=True,exist_ok=True)
REPORT=ROOT/'docs/compatibility';REPORT.mkdir(parents=True,exist_ok=True)
def run(*a,check=True):return subprocess.run([str(v) for v in a],stdout=subprocess.PIPE,stderr=subprocess.PIPE,check=check).stdout
def adb(*a,**kw):return run(ADB,*a,**kw)
def sh(*a,**kw):return adb('shell',*a,**kw)
def rect(s):return tuple(map(int,re.findall(r'\d+',s)))
viewport=(0,0,1080,2200)
checks=[]
def tree():
 global viewport
 for attempt in range(3):
  result=sh('uiautomator','dump','/data/local/tmp/worthbit-compat.xml',check=False)
  if b'dumped to' not in result:
   time.sleep(.4);continue
  try:
   root=ET.fromstring(sh('cat','/data/local/tmp/worthbit-compat.xml'))
   for node in root.iter('node'):
    if node.attrib.get('package')==PKG and node.attrib.get('class')=='android.view.View' and node.attrib.get('content-desc'):
     viewport=rect(node.attrib['bounds']);break
   return root
  except (ET.ParseError,subprocess.CalledProcessError):time.sleep(.5)
 raise RuntimeError('Cannot inspect UI: '+repr(result))
def expect_description(part):
 root=tree()
 assert any(part in n.attrib.get('content-desc','') for n in root.iter('node')),(part,ET.tostring(root).decode()[:300])
def tap(x,y):
 l,t,r,b=viewport
 sh('input','tap',round(l+x*(r-l)/360),round(t+y*(b-t)/760));time.sleep(.25)
def screenshot(name):
 time.sleep(.2);(OUT/(name+'.png')).write_bytes(adb('exec-out','screencap','-p'))
def click_text(choices):
 for n in tree().iter('node'):
  if n.attrib.get('text','').casefold() in [c.casefold() for c in choices]:
   l,t,r,b=rect(n.attrib['bounds']);assert r>l and b>t
   sh('input','tap',(l+r)//2,(t+b)//2);time.sleep(.2);return
 raise AssertionError('Missing text: '+str(choices))
def back():sh('input','keyevent','4');time.sleep(.3)
def current_items():
 root=ET.fromstring(sh('run-as',PKG,'cat','shared_prefs/rainbow_inventory.xml'))
 return json.loads(next(n.text for n in root if n.attrib.get('name')=='items'))
fixtures=[dict(id='coffee',name='Coffee machine, home edition',cents=1200000,uses=25,icon=0),dict(id='bag',name='Backpack',cents=12000,uses=60,icon=1),dict(id='book',name='Book',cents=3000,uses=30,icon=10),dict(id='headphones',name='Headphones',cents=24000,uses=80,icon=5),dict(id='bike',name='Bicycle',cents=60000,uses=100,icon=11),dict(id='box',name='Box',cents=1500,uses=0,icon=2)]
def launch():
 sh('am','force-stop',PKG);sh('am','start','-W','-n',PKG+'/com.rainbow.priceperuse.MainActivity');time.sleep(1.8)
def seed(lang):
 sh('am','force-stop',PKG)
 xml=ET.Element('map');ET.SubElement(xml,'string',name='items').text=json.dumps(fixtures)
 ET.SubElement(xml,'string',name='language').text=lang
 ET.SubElement(xml,'int',name='theme',value='0')
 ET.SubElement(xml,'boolean',name='onboarding_seen',value='true')
 path=WORK/'compatibility-prefs.xml';ET.ElementTree(xml).write(path,encoding='utf-8',xml_declaration=True)
 adb('push',path,'/data/local/tmp/worthbit-compatibility-prefs.xml')
 sh('run-as',PKG,'mkdir','-p','shared_prefs')
 sh('run-as',PKG,'cp','/data/local/tmp/worthbit-compatibility-prefs.xml','shared_prefs/rainbow_inventory.xml')
 launch();expect_description('WorthBit')
def assert_fields():
 fields=[n for n in tree().iter('node') if n.attrib.get('class')=='android.widget.EditText']
 assert len(fields)==2
 l,t,r,b=viewport;sx=(r-l)/360;sy=(b-t)/760
 for node,y in zip(fields,[190,290]):
  a,c,d,e=rect(node.attrib['bounds'])
  assert l+17*sx<=a<d<=l+343*sx
  assert t+(y-3)*sy<=c<e<=t+(y+48)*sy
 return fields

def package():
 manifest=(BUILD/'AndroidManifest.xml').read_text().replace('package="com.rainbow.priceperuse"','package="'+PKG+'"').replace('android:name=".MainActivity"','android:name="com.rainbow.priceperuse.MainActivity"')
 (WORK/'compat-manifest.xml').write_text(manifest)
 run(TOOLS/'aapt2','link','-o',WORK/'compat-base.apk','-I',SDK/'platforms/android-36/android.jar','--manifest',WORK/'compat-manifest.xml',BUILD/'resources.zip')
 with zipfile.ZipFile(WORK/'compat-base.apk','a') as archive:
  for p in (BUILD/'dex').glob('*.dex'):archive.write(p,p.name)
 run(TOOLS/'zipalign','-f','4',WORK/'compat-base.apk',WORK/'compat-aligned.apk')
 run(JDK/'bin/java','-jar',TOOLS/'lib/apksigner.jar','sign','--ks',BUILD/'debug.keystore','--ks-pass','pass:android','--out',WORK/'compat.apk',WORK/'compat-aligned.apk')
 adb('install','--no-incremental','-r',WORK/'compat.apk')

package()
settings_names=['animator_duration_scale','transition_animation_scale','window_animation_scale']
settings_before={key:sh('settings','get','global',key).decode().strip() for key in settings_names}
for key in settings_names:sh('settings','put','global',key,'0')
try:
 launch()
 sizes=[(411,891),(380,823),(320,568)] if not args.legacy else [(320,568)]
 for width,height in sizes:
  sh('wm','size',f'{width*3}x{height*3}');sh('wm','density','480');time.sleep(.8)
  for lang in ['en','it']:
   prefix=f'{width}-{lang}'
   seed(lang);screenshot(prefix+'-home')
   assert abs((viewport[2]-viewport[0])/3-width)<=1
   tap(180,522);expect_description('All things' if lang=='en' else 'Tutte le cose');screenshot(prefix+'-inventory')
   l,t,r,b=viewport;sh('input','swipe',(l+r)//2,round(t+(b-t)*.82),(l+r)//2,round(t+(b-t)*.26),500);time.sleep(.3)
   screenshot(prefix+'-inventory-bottom');tap(180,550);expect_description('Box');screenshot(prefix+'-detail-unused');back()
   expect_description('All things' if lang=='en' else 'Tutte le cose');tap(36,32)
   tap(180,220);expect_description('Coffee machine');screenshot(prefix+'-detail')
   tap(325,33);screenshot(prefix+'-actions');tap(210,82);assert_fields();screenshot(prefix+'-edit')
   tap(120,210);sh('input','keyevent','KEYCODE_MOVE_END');sh('input','text','%sXL');time.sleep(.4)
   screenshot(prefix+'-keyboard');back();assert_fields()
   tap(225,470);screenshot(prefix+'-icons');click_text(['Cancel','Annulla'])
   tap(180,700);expect_description('Coffee machine');tap(180,535)
   assert next(i for i in current_items() if i['id']=='coffee')['uses']==26
   tap(275,620);assert next(i for i in current_items() if i['id']=='coffee')['uses']==25
   back();tap(180,700);fields=assert_fields();assert all(n.attrib.get('text','') in ['',n.attrib.get('hint',''),'e.g. Coffee machine','Es. Macchina del caffè','Es. Macchina del caff?','0.00'] for n in fields);screenshot(prefix+'-add-empty')
   tap(120,210);sh('input','text','New%sQA%sitem');back()
   tap(120,310);sh('input','text','149.99');back();assert_fields();screenshot(prefix+'-add-filled');tap(180,700)
   assert next(i for i in current_items() if i['name']=='New QA item')['cents']==14999
   back();tap(275,139);expect_description('Collection statistics' if lang=='en' else 'Statistiche della collezione');screenshot(prefix+'-stats-uses')
   tap(255,285);screenshot(prefix+'-stats-value');back();expect_description('WorthBit')
   tap(325,33);expect_description('Settings' if lang=='en' else 'Impostazioni');screenshot(prefix+'-settings')
   for name,y in [('about',465),('privacy',545),('version',620)]:
    tap(180,y)
    root=tree();close=next(n for n in root.iter('node') if n.attrib.get('text') in ['Close','Chiudi'])
    a,c,d,e=rect(close.attrib['bounds']);assert a>=0 and c>=0 and d<=width*3 and e<=height*3
    screenshot(prefix+'-'+name)
    if name=='privacy':
     area=next(n for n in root.iter('node') if n.attrib.get('class')=='android.widget.ScrollView');x1,y1,x2,y2=rect(area.attrib['bounds'])
     sh('input','swipe',(x1+x2)//2,y2-10,(x1+x2)//2,y1+10,500);screenshot(prefix+'-privacy-scrolled')
     newer=next(n for n in tree().iter('node') if n.attrib.get('text') in ['Close','Chiudi']);assert newer.attrib['bounds']==close.attrib['bounds']
    click_text(['Close','Chiudi'])
   tap(180,681);expect_description('step 1' if lang=='en' else 'passo 1');screenshot(prefix+'-onboarding-1')
   tap(180,690);screenshot(prefix+'-onboarding-2');tap(180,690);screenshot(prefix+'-onboarding-3');tap(305,33)
   expect_description('Settings' if lang=='en' else 'Impostazioni')
   checks.append({'width_dp':width,'height_dp':height,'language':lang,'screens':len(list(OUT.glob(prefix+'-*.png'))),'result':'PASS'})
   print('PASS',prefix,flush=True)
 device={name:sh('getprop',name).decode().strip() for name in ['ro.build.version.sdk','ro.build.version.release','ro.product.cpu.abilist']}
 (REPORT/('legacy-ui.json' if args.legacy else 'responsive-ui.json')).write_text(json.dumps({'device':device,'checks':checks},indent=2)+'\n')
 print('PASS: compatibility UI checks',flush=True)
except Exception:
 screenshot('failure')
 try:(OUT/'failure.xml').write_bytes(ET.tostring(tree()))
 except Exception:pass
 raise
finally:
 sh('am','force-stop',PKG,check=False);adb('uninstall',PKG,check=False)
 for key,value in settings_before.items():
  if value=='null':sh('settings','delete','global',key,check=False)
  else:sh('settings','put','global',key,value,check=False)
 sh('wm','size','reset',check=False);sh('wm','density','reset',check=False)
 sh('rm','-f','/data/local/tmp/worthbit-compatibility-prefs.xml',check=False)
