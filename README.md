# RailBoard

A lightweight Android app for live UK National Rail departure boards, written in plain Java (no external app dependencies).

- Live departures from National Rail's Darwin data via the free Huxley2 community proxy (no API key)
- Station search plus a "nearest station" finder using phone location
- Live-tiles style board with platform, operator, delays and cancellations
- Home screen widget that caches the last viewed board
- Material 3 style dark UI

## Build

The APK is built with the standard Android toolchain (aapt2 + javac + d8, minSdk 24, targetSdk 34):

    aapt2 compile --dir app/src/main/res -o res.zip
    aapt2 link -o res-linked.zip -I android.jar --manifest app/src/main/AndroidManifest.xml -R res.zip --java gen
    javac --release 8 -classpath android.jar -d classes $(find app/src/main/java gen -name '*.java')
    d8 --min-api 24 --release --lib android.jar --output . $(find classes -name '*.class')

## Notes

- Signing keystore is kept out of the repo on purpose.
- Data is provided by the National Rail Darwin feed; availability depends on the community proxy.
