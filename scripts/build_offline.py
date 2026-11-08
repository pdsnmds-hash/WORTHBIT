"""Build with cached Gradle dependencies and preserve the existing debug certificate."""
from pathlib import Path
import os,re,subprocess,zipfile,shutil

ROOT=Path(__file__).resolve().parents[1]
os.chdir(ROOT)
HOME=Path.home()
SDK=Path(os.environ.get('ANDROID_HOME',HOME/'Library/Android/sdk'))
JDK=Path(os.environ.get('JAVA_HOME','/Applications/Android Studio.app/Contents/jbr/Contents/Home'))
BUILD=ROOT/'app/build/offline'
BUILD.mkdir(parents=True,exist_ok=True)
TOOLS=SDK/'build-tools/36.0.0'
config=(ROOT/'app/build.gradle.kts').read_text()
def number(name):return re.search(rf'{name}\s*=\s*(\d+)',config).group(1)
def run(args):subprocess.run([str(x) for x in args],check=True,env={**os.environ,'JAVA_HOME':str(JDK),'ANDROID_HOME':str(SDK)})
run([ROOT/'gradlew','--offline',':app:assembleDebug',':app:testDebugUnitTest',':app:lintDebug','--no-daemon'])
version_name=re.search(r'versionName\s*=\s*"([^"]+)"',config).group(1)
manifest=(ROOT/'app/src/main/AndroidManifest.xml').read_text().replace('<manifest ',f'<manifest package="com.rainbow.priceperuse" android:versionCode="{number("versionCode")}" android:versionName="{version_name}" ')
manifest=manifest.replace('<application ',f'<uses-sdk android:minSdkVersion="{number("minSdk")}" android:targetSdkVersion="{number("targetSdk")}"/><application android:debuggable="true" ')
(BUILD/'AndroidManifest.xml').write_text(manifest)
resources=BUILD/'resources.zip'
resources.unlink(missing_ok=True)
run([TOOLS/'aapt2','compile','--dir',ROOT/'app/src/main/res','-o',resources])
apk=ROOT/'app/build/outputs/apk/debug/app-debug.apk'
dex=BUILD/'dex';dex.mkdir(exist_ok=True)
for path in dex.glob('*.dex'):path.unlink()
with zipfile.ZipFile(apk) as archive:
    for name in archive.namelist():
        if re.fullmatch(r'classes(?:\d+)?\.dex',name):(dex/name).write_bytes(archive.read(name))
key=BUILD/'debug.keystore'
if not key.exists():key=HOME/'.android/debug.keystore'
if not key.exists():raise FileNotFoundError('No existing debug signing key is available')
out=ROOT/'deliverables/worthbit-debug.apk'
run([JDK/'bin/java','-jar',TOOLS/'lib/apksigner.jar','sign','--ks',key,'--ks-pass','pass:android','--out',out,apk])
run([JDK/'bin/java','-jar',TOOLS/'lib/apksigner.jar','verify','--verbose',out])
shutil.copyfile(out,ROOT/'deliverables/rainbow-price-debug.apk')
print('APK:',out)
