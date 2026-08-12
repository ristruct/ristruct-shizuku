#!/usr/bin/env bash
set -euo pipefail

VERSION="13.1.5"
ANNOTATION_VERSION="1.3.0"
OUT_ROOT="dist"
OUT="$OUT_ROOT/RistructShizuku"
WORK="$OUT_ROOT/.work"

rm -rf "$OUT_ROOT"
mkdir -p "$OUT" "$WORK" "$WORK/classes-fat" "$OUT/res"

fetch_artifact() {
  local group="$1"
  local artifact="$2"
  local version="$3"
  local base="$4"
  local aar_url="$base/$group/$artifact/$version/$artifact-$version.aar"
  local jar_url="$base/$group/$artifact/$version/$artifact-$version.jar"
  local aar_out="$WORK/$artifact.aar"

  if curl --fail --silent --show-error --location --retry 3 --retry-delay 1 "$aar_url" -o "$aar_out"; then
    unzip -q -o "$aar_out" -d "$WORK/$artifact"
    if [ -f "$WORK/$artifact/classes.jar" ]; then
      unzip -q -o "$WORK/$artifact/classes.jar" -d "$WORK/classes-fat"
    fi
    if [ -d "$WORK/$artifact/res" ]; then
      cp -R "$WORK/$artifact/res/." "$OUT/res/"
    fi
  else
    # Not published as .aar (e.g. androidx.annotation is a plain .jar) - fall back.
    local jar_out="$WORK/$artifact.jar"
    curl --fail --silent --show-error --location --retry 3 --retry-delay 1 "$jar_url" -o "$jar_out"
    unzip -q -o "$jar_out" -d "$WORK/classes-fat"
  fi
}

# Build our library first.
gradle :library:assembleRelease --no-daemon --stacktrace

# Include the Shizuku runtime/API into the Sketchware package so the consumer
# can use RistructShizuku from Add source directly without adding Maven deps.
fetch_artifact "dev/rikka/shizuku" "api" "$VERSION" "https://repo1.maven.org/maven2"
fetch_artifact "dev/rikka/shizuku" "provider" "$VERSION" "https://repo1.maven.org/maven2"
fetch_artifact "dev/rikka/shizuku" "shared" "$VERSION" "https://repo1.maven.org/maven2"
fetch_artifact "dev/rikka/shizuku" "aidl" "$VERSION" "https://repo1.maven.org/maven2"
fetch_artifact "androidx/annotation" "annotation" "$ANNOTATION_VERSION" "https://repo1.maven.org/maven2"

# Merge our compiled classes.
unzip -q -o library/build/outputs/aar/library-release.aar -d "$WORK/ours"
unzip -q -o "$WORK/ours/classes.jar" -d "$WORK/classes-fat"

rm -rf "$WORK/classes-fat/META-INF"
find "$WORK/classes-fat" -name "module-info.class" -delete
(cd "$WORK/classes-fat" && jar cf "$OUT/classes.jar" .)

ANDROID_JAR="$ANDROID_HOME/platforms/android-34/android.jar"
D8="$ANDROID_HOME/build-tools/35.0.0/d8"
mkdir -p "$WORK/dex"
"$D8" --lib "$ANDROID_JAR" --min-api 24 --output "$WORK/dex" "$OUT/classes.jar"
cp "$WORK/dex/classes.dex" "$OUT/classes.dex"

# Export only the exact Sketchware-style package contents.
cat > "$OUT/AndroidManifest.xml" <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<manifest
    xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.RistructShizuku">

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

# Keep the five-item Sketchware package clean.
rm -rf "$WORK"

# Standalone AAR for normal Android consumers.
cp library/build/outputs/aar/library-release.aar "$OUT_ROOT/RistructShizuku.aar"

(cd "$OUT_ROOT" && zip -qr RistructShizuku.zip RistructShizuku)

printf '\nExport complete:\n'
find "$OUT" -maxdepth 1 -type f -printf '%f\n' | sort
printf '\nAAR: %s\n' "$OUT_ROOT/RistructShizuku.aar"
printf 'Sketchware ZIP: %s\n' "$OUT_ROOT/RistructShizuku.zip"
