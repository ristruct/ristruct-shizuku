#!/usr/bin/env bash
set -euo pipefail

VERSION="13.1.5"
OUT="dist/RistructShizuku"
WORK="dist/.work"
rm -rf dist
mkdir -p "$OUT/classes-fat" "$OUT/res" "$WORK"

AAR_URL="https://repo1.maven.org/maven2/dev/rikka/shizuku"
for artifact in api provider shared aidl; do
  curl -fsSL "$AAR_URL/$artifact/$VERSION/$artifact-$VERSION.aar" -o "$WORK/$artifact.aar"
  unzip -q -o "$WORK/$artifact.aar" -d "$WORK/$artifact"
  if [ -f "$WORK/$artifact/classes.jar" ]; then
    unzip -q -o "$WORK/$artifact/classes.jar" -d "$OUT/classes-fat"
  fi
  if [ -d "$WORK/$artifact/res" ]; then
    cp -R "$WORK/$artifact/res/." "$OUT/res/"
  fi
done

# Build the RistructShizuku library first.
cp library/build/outputs/aar/library-release.aar "$OUT/RistructShizuku.aar"
unzip -q -o library/build/outputs/aar/library-release.aar -d "$WORK/ours"
unzip -q -o "$WORK/ours/classes.jar" -d "$OUT/classes-fat"

rm -rf "$OUT/classes-fat/META-INF"
(cd "$OUT/classes-fat" && jar cf ../classes.jar .)

ANDROID_JAR="$ANDROID_HOME/platforms/android-36/android.jar"
D8="$ANDROID_HOME/build-tools/35.0.0/d8"
mkdir -p "$OUT/dex"
"$D8" --lib "$ANDROID_JAR" --min-api 24 --output "$OUT/dex" "$OUT/classes.jar"
cp "$OUT/dex/classes.dex" "$OUT/classes.dex"
rm -rf "$OUT/classes-fat" "$OUT/dex" "$WORK"

# Match the Sketchware Master-style package demonstrated by the user.
cat > "$OUT/AndroidManifest.xml" <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.RistructShizuku">
    <uses-permission android:name="moe.shizuku.manager.permission.API_V23" />
    <uses-permission android:name="rikka.shizuku.permission.API" />
    <application>
        <provider
            android:name="rikka.shizuku.ShizukuProvider"
            android:authorities="${applicationId}.shizuku"
            android:multiprocess="false"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.INTERACT_ACROSS_USERS_FULL" />
        <meta-data
            android:name="moe.shizuku.client.V3_SUPPORT"
            android:value="true" />
    </application>
</manifest>
XML
printf '%s\n' 'com.RistructShizuku' > "$OUT/config"

(cd dist && zip -qr RistructShizuku.zip RistructShizuku)
