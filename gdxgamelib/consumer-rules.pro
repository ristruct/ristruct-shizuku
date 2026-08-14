# Keep everything from libGDX itself and our own bridge classes.
# Without this, an app that turns on minifyEnabled/proguard can strip
# methods libGDX calls via JNI/reflection, which fails silently at runtime.
-keep class com.badlogic.gdx.** { *; }
-keep interface com.badlogic.gdx.** { *; }
-keep class com.sketchgdx.gamelib.** { *; }
-dontwarn com.badlogic.gdx.**
