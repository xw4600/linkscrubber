#!/usr/bin/env bash
# Builds build/linkscrubber.apk with the raw Android SDK tools (no Gradle).
set -euo pipefail
cd "$(dirname "$0")"

SDK=${ANDROID_SDK:-$HOME/tools/android-sdk}
export JAVA_HOME=${JAVA_HOME:-$HOME/tools/jdk17}
export PATH="$JAVA_HOME/bin:$PATH"

BT=$SDK/build-tools/34.0.0
ANDROID_JAR=$SDK/platforms/android-34/android.jar
KEYSTORE=$HOME/.android/linkscrubber.keystore
APP=app/src/main
OUT=build

rm -rf "$OUT"
mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/dex" "$OUT/test"

echo "== unit tests (scrub logic)"
javac -d "$OUT/test" "$APP/java/com/linkscrubber/Scrubber.java" \
    "$APP/java/com/linkscrubber/ScrubRules.java" test/com/linkscrubber/ScrubberTest.java
java -cp "$OUT/test" com.linkscrubber.ScrubberTest

echo "== resources"
"$BT/aapt2" compile --dir "$APP/res" -o "$OUT/res.zip"
"$BT/aapt2" link -o "$OUT/base.apk" -I "$ANDROID_JAR" \
    --manifest "$APP/AndroidManifest.xml" --java "$OUT/gen" \
    --min-sdk-version 26 --target-sdk-version 34 \
    --version-code 1 --version-name 1.0 "$OUT/res.zip"

echo "== compile"
find "$APP/java" "$OUT/gen" -name '*.java' > "$OUT/sources.txt"
javac -Xlint:-options -source 8 -target 8 \
    -bootclasspath "$ANDROID_JAR" -classpath "$ANDROID_JAR" \
    -d "$OUT/classes" @"$OUT/sources.txt"

echo "== dex"
"$BT/d8" --lib "$ANDROID_JAR" --min-api 26 --output "$OUT/dex" $(find "$OUT/classes" -name '*.class')
(cd "$OUT/dex" && zip -q ../base.apk classes.dex)

echo "== align + sign"
"$BT/zipalign" -f -p 4 "$OUT/base.apk" "$OUT/aligned.apk"
if [ ! -f "$KEYSTORE" ]; then
    mkdir -p "$(dirname "$KEYSTORE")"
    keytool -genkeypair -keystore "$KEYSTORE" -storepass linkscrubber -keypass linkscrubber \
        -alias linkscrubber -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=LinkScrubber, O=Personal"
fi
"$BT/apksigner" sign --ks "$KEYSTORE" --ks-key-alias linkscrubber \
    --ks-pass pass:linkscrubber --key-pass pass:linkscrubber \
    --out "$OUT/linkscrubber.apk" "$OUT/aligned.apk"
"$BT/apksigner" verify "$OUT/linkscrubber.apk"
echo "Built $(pwd)/$OUT/linkscrubber.apk"
