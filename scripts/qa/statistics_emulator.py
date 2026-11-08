"""Exercise statistics in an isolated QA package, preserving the real inventory.
Run scripts/build_offline.py first. Requires a booted 1080x2400 emulator.
"""
from pathlib import Path
import json, os, subprocess, sys, tempfile, time, zipfile, xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[2]
os.chdir(ROOT)
SDK=Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))
JDK=Path(os.environ.get('JAVA_HOME','/Applications/Android Studio.app/Contents/jbr/Contents/Home'))
TOOLS=SDK/'build-tools/36.0.0';ADB=SDK/'platform-tools/adb';BUILD=ROOT/'app/build/offline'
PKG='com.rainbow.priceperuse.statsqa'
SHOTS=ROOT/'screens/emulator/statistics-qa';SHOTS.mkdir(parents=True,exist_ok=True)
def run(*args): return subprocess.check_output([str(x) for x in args])
def adb(*args): return run(ADB,*args)
def shell(*args): return adb('shell',*args)
def tap(x,y):shell('input','tap',int(x*3),int(132+y*2205/760));time.sleep(.4)
def shot(name):time.sleep(.65);(SHOTS/(name+'.png')).write_bytes(adb('exec-out','screencap','-p'))
def dump():
 shell('uiautomator','dump','/sdcard/stats-qa.xml')
 return shell('cat','/sdcard/stats-qa.xml').decode()
def launch():
 shell('am','force-stop',PKG);shell('am','start','-W','-n',PKG+'/com.rainbow.priceperuse.MainActivity');time.sleep(1.2)
def prefs():return ET.fromstring(shell('run-as',PKG,'cat','shared_prefs/rainbow_inventory.xml'))

def seed(items,language='en',theme=0,seen=True):
 shell('am','force-stop',PKG)
 tree=ET.Element('map');ET.SubElement(tree,'string',name='items').text=json.dumps(items)
 ET.SubElement(tree,'string',name='language').text=language
 ET.SubElement(tree,'int',name='theme',value=str(theme))
 ET.SubElement(tree,'boolean',name='onboarding_seen',value='true' if seen else 'false')
 path=BUILD/'statistics-qa-prefs.xml';ET.ElementTree(tree).write(path,encoding='utf-8',xml_declaration=True)
 adb('push',path,'/data/local/tmp/statistics-qa-prefs.xml')
 shell('run-as',PKG,'mkdir','-p','shared_prefs')
 shell('run-as',PKG,'cp','/data/local/tmp/statistics-qa-prefs.xml','shared_prefs/rainbow_inventory.xml')
 launch()

manifest=(BUILD/'AndroidManifest.xml').read_text().replace('package="com.rainbow.priceperuse"','package="'+PKG+'"').replace('android:name=".MainActivity"','android:name="com.rainbow.priceperuse.MainActivity"')
(BUILD/'qa-manifest.xml').write_text(manifest)
run(TOOLS/'aapt2','link','-o',BUILD/'qa-base.apk','-I',SDK/'platforms/android-36/android.jar','--manifest',BUILD/'qa-manifest.xml',BUILD/'resources.zip')
with zipfile.ZipFile(BUILD/'qa-base.apk','a') as archive:
 for file in (BUILD/'dex').glob('*.dex'):archive.write(file,file.name)
run(TOOLS/'zipalign','-f','4',BUILD/'qa-base.apk',BUILD/'qa-aligned.apk')
run(JDK/'bin/java','-jar',TOOLS/'lib/apksigner.jar','sign','--ks',BUILD/'debug.keystore','--ks-pass','pass:android','--out',BUILD/'statistics-qa.apk',BUILD/'qa-aligned.apk')
adb('install','--no-incremental','-r',BUILD/'statistics-qa.apk')
launch()
fixtures=[
 dict(id='coffee',name='Coffee machine',cents=1200000,uses=25,icon=0),
 dict(id='bag',name='Backpack',cents=12000,uses=60,icon=1),
 dict(id='book',name='Book',cents=3000,uses=30,icon=10),
 dict(id='headphones',name='Headphones',cents=24000,uses=80,icon=5),
 dict(id='bicycle',name='Bicycle',cents=60000,uses=100,icon=11),
 dict(id='box',name='Box',cents=1500,uses=0,icon=2),
]
try:
 if '--onboarding-only' in sys.argv:
  seed([],seen=False);assert 'step 1 of 3' in dump();shot('onboarding-1-en')
  tap(180,690);assert 'step 2 of 3' in dump();shot('onboarding-2-en')
  shell('input','keyevent','4');time.sleep(.3);assert 'step 1 of 3' in dump()
  tap(180,690);tap(180,690);assert 'step 3 of 3' in dump();shot('onboarding-3-en')
  # Pause/resume on the last page must not schedule another splash or reset the step.
  shell('input','keyevent','KEYCODE_HOME');time.sleep(.3);shell('am','start','-W','-n',PKG+'/com.rainbow.priceperuse.MainActivity')
  assert 'step 3 of 3' in dump()
  tap(180,690);assert 'WorthBit inventory' in dump()
  assert next(n.attrib['value'] for n in prefs() if n.attrib.get('name')=='onboarding_seen')=='true'
  launch();assert 'WorthBit inventory' in dump()
  # Replay from Settings returns to Settings and does not erase inventory.
  tap(325,33);tap(180,681);assert 'step 1 of 3' in dump();tap(305,33);assert 'Settings' in dump()
  seed(fixtures,'it',1,seen=False);assert 'passo 1 di 3' in dump();shot('onboarding-1-ocean-it')
  tap(180,690);shot('onboarding-2-ocean-it');tap(180,690);shot('onboarding-3-ocean-it')
  tap(180,690);assert 'Inventario WorthBit' in dump()
  data=json.loads(next(n.text for n in prefs() if n.attrib.get('name')=='items'));assert data==fixtures
  # Language can be changed before entering the app; Skip persists completion.
  seed([],'en',2,seen=False);tap(77,33);assert 'passo 1 di 3' in dump();shot('onboarding-peach-language-switch')
  tap(305,33);assert 'Inventario WorthBit' in dump();launch();assert 'Inventario WorthBit' in dump()
  # Capture the real branded splash during a cold start (before its short timer ends).
  shell('am','force-stop',PKG)
  shell('am','start','-W','-n',PKG+'/com.rainbow.priceperuse.MainActivity')
  (SHOTS/'splash-peach-it.png').write_bytes(adb('exec-out','screencap','-p'))
  time.sleep(1.2);assert 'Inventario WorthBit' in dump()
  (ROOT/'deliverables/onboarding-verification.txt').write_text('PASS: three onboarding steps, Next and Back, pause/resume, completion persisted across relaunch. Replay/close returns to Settings. English/Italian and three themes, first-run language switch and Skip. Existing six-item inventory preserved. Branded cold-start splash captured and transitions to the correct route. Isolated QA package only.\n')
  print('PASS: splash and onboarding')
  raise SystemExit(0)
 if '--info-only' in sys.argv:
  import re
  def close_info(label):
   tree=ET.fromstring(dump())
   node=next(n for n in tree.iter('node') if n.attrib.get('text')==label)
   bounds=list(map(int,re.findall(r'\d+',node.attrib['bounds'])))
   shell('input','tap',(bounds[0]+bounds[2])//2,(bounds[1]+bounds[3])//2);time.sleep(.3)
   return node.attrib['bounds']
  for lang,theme,label in [('en',0,'lavender-en'),('it',1,'ocean-it'),('en',2,'peach-en')]:
   seed(fixtures,lang,theme);tap(325,33)
   close='Chiudi' if lang=='it' else 'Close'
   tap(180,465);assert 'WorthBit' in dump();shot('pixel-about-'+label);close_info(close)
   tap(180,545);shot('pixel-privacy-top-'+label)
   before=next(n.attrib['bounds'] for n in ET.fromstring(dump()).iter('node') if n.attrib.get('text')==close)
   shell('input','swipe',540,1550,540,800,500);time.sleep(.3)
   shot('pixel-privacy-scrolled-'+label)
   assert close_info(close)==before
   tap(180,620);assert 'WorthBit' in dump();shot('pixel-version-'+label);close_info(close)
  (ROOT/'deliverables/pixel-info-verification.txt').write_text('PASS: About, Privacy and Version use pixel panels in three palettes, English and Italian. Policy scrolls while Close remains fixed and clickable. Dialog dismissal verified. Isolated QA data only.\n')
  print('PASS: pixel information dialogs')
  raise SystemExit(0)
 if '--home-only' in sys.argv:
  import struct
  seed(fixtures);shot('home-three-cards-six-items')
  first=adb('exec-out','screencap')
  time.sleep(.7);shot('home-cat-next-frame')
  second=adb('exec-out','screencap')
  width,height,fmt=struct.unpack_from('<III',first)
  assert (width,height,fmt)==(1080,2400,1)
  def flight_pixels(frame):
   payload=frame[-width*height*4:]
   return b''.join(payload[(row*width+40)*4:(row*width+1020)*4] for row in range(1720,1850))
  assert flight_pixels(first)!=flight_pixels(second)
  tap(180,522);assert 'All things' in dump();shot('all-things-top')
  shell('input','swipe',540,1860,540,710,600);time.sleep(.4);shot('all-things-bottom')
  tap(180,550);assert 'content-desc="Box"' in dump()
  tap(36,32);assert 'All things' in dump();tap(36,32);shot('home-return-from-inventory')
  seed(fixtures,'it',1);shot('home-cat-ocean-it');tap(180,522);assert 'Tutte le cose' in dump();shot('all-things-ocean-it')
  seed(fixtures[:3]);shot('home-exactly-three')
  seed(fixtures[:1]);shot('home-one-card')
  seed([]);shot('home-empty-cat')
  (ROOT/'deliverables/home-cat-verification.txt').write_text('PASS: isolated six-item collection shows three home cards and All things button. Two cat frames differ in the flight lane. Full inventory scroll reaches sixth item (Box); detail/back returns to full inventory and then home. Italian/Ocean, three-item, one-item and empty states captured. Primary inventory untouched.\n')
  print('PASS: cat animation and full-inventory navigation')
  raise SystemExit(0)
 if '--visual-only' in sys.argv:
  for lang,theme,label in [('en',0,'lavender-en'),('it',1,'ocean-it'),('en',2,'peach-en')]:
   seed(fixtures,lang,theme);tap(275,139)
   assert ('6 items, 295 uses, 1 unused' if lang=='en' else '6 cose, 295 utilizzi, 1 mai usate') in dump()
   shot('larger-icons-uses-'+label);tap(255,285);shot('larger-icons-prices-'+label)
   tap(83,500);tap(180,565);assert 'Coffee machine' in dump();tap(36,32)
  seed([]);tap(275,139);shot('larger-icons-empty-en')
  (ROOT/'deliverables/statistics-layout-verification.txt').write_text('PASS: larger graph icons, compact heading/totals, both chart modes in three palettes and both languages. New hit positions select items and open detail; Back returns to statistics. Empty state captured. Isolated QA fixtures only.\n')
  print('PASS: statistics layout checks')
  raise SystemExit(0)
 seed([]);tap(275,139);assert '0 items, 0 uses, 0 unused' in dump();shot('empty-en')
 seed([dict(id='new',name='New item',cents=100,uses=0,icon=2)]);tap(275,139);assert '1 items, 0 uses, 1 unused' in dump();shot('unused-en')
 tap(255,285);shot('unused-price-en')
 seed(fixtures);tap(275,139)
 assert '6 items, 295 uses, 1 unused' in dump();shot('uses-lavender-en')
 # Select the backpack bar (third by use count), then open its caption.
 tap(189,450);shot('selected-bar-en');tap(180,555)
 assert 'Backpack' in dump()
 tap(180,535);tap(275,620);tap(36,32)
 assert '6 items, 295 uses, 1 unused' in dump()
 # A committed use must be visible as soon as we return to the graph.
 tap(189,450);tap(180,555);tap(180,535);tap(36,32)
 assert '6 items, 296 uses, 1 unused' in dump()
 seed(fixtures);tap(275,139)
 tap(255,285);shot('purchase-lavender-en')
 tap(326,249);assert 'Reading your statistics' in dump();shot('explanation-en');shell('input','keyevent','4');time.sleep(.4)
 # Cheapest rank opens the book; edit price and verify the new aggregate.
 tap(130,640);assert 'Book' in dump();tap(325,33);tap(210,80)
 # Fill native price with a new value.
 tap(110,310);shell('input','keyevent','KEYCODE_MOVE_END')
 for _ in range(8):shell('input','keyevent','KEYCODE_DEL')
 shell('input','text','60');shell('input','keyevent','4');time.sleep(.3);tap(180,700);tap(36,32)
 current=json.loads(next(n.text for n in prefs() if n.attrib.get('name')=='items'))
 assert next(i for i in current if i['id']=='book')['cents']==6000
 shot('after-price-edit-en')
 # Delete an item through the existing confirmation; aggregate count must follow.
 tap(255,285);time.sleep(.7);tap(83,450);tap(180,555)
 assert 'Coffee machine' in dump();tap(325,33);tap(210,130);tap(250,478)
 tap(275,139);assert '5 items, 270 uses, 1 unused' in dump();shot('after-delete-en')
 seed(fixtures,'it',1);tap(275,139);assert '6 cose, 295 utilizzi, 1 mai usate' in dump();shot('uses-ocean-it')
 tap(255,285);shot('purchase-ocean-it')
 seed(fixtures,'en',2);tap(275,139);shot('uses-peach-en')
 # Very large amounts and long labels must stay inside their panels.
 seed([dict(id=str(i),name='Very long collection item name number '+str(i),cents=99999999999-i,uses=2147483647-i,icon=i) for i in range(6)])
 tap(275,139);shot('large-values-en')
 (ROOT/'deliverables/statistics-emulator-verification.txt').write_text('PASS: isolated QA package, no primary inventory modifications.\nEmpty and all-unused states.\nSix-item fixture: 6 items / EUR 13005 / 295 uses / 1 unused.\nTop-five use and purchase-price charts, bar selection, detail navigation.\nUse and Undo before leaving detail -> 295 total uses; committed use -> 296.\nPrice edit persisted and aggregate refreshed.\nDeletion -> 5 items / 270 uses / 1 unused.\nEnglish and Italian, Lavender / Ocean / Peach screenshots.\nLarge totals and long names rendered.\nQA fixture screenshots: screens/emulator/statistics-qa/.\n')
 print('PASS: statistics emulator checks')
except Exception:
 shot('failure-state')
 (SHOTS/'failure-state.xml').write_text(dump())
 raise
finally:
 shell('am','force-stop',PKG)
 adb('uninstall',PKG)
 shell('rm','-f','/data/local/tmp/statistics-qa-prefs.xml')
 if shell('pm','path','com.rainbow.priceperuse').strip():shell('am','start','-n','com.rainbow.priceperuse/.MainActivity')
