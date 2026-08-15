package com.sketchgdx.gamelib;

/**
 * Level-1 test: proves the .aar's classes are actually reachable from your
 * Sketchware Pro project (classloader / dex merge worked) WITHOUT touching
 * libGDX's GL renderer at all. If this crashes or the Toast never shows,
 * the problem is in the library import step, not in the game code.
 *
 * Call this straight from "Add Source Directly" in MainActivity's onCreate,
 * or from a button click block via a Java class action block — no libGDX
 * runtime, no GLSurfaceView, so it's safe to call from anywhere at any time.
 */
public class GdxLibCheck {

    /** @return a short human-readable status string. Never throws. */
    public static String ping() {
        try {
            String gdxVersion = com.badlogic.gdx.Version.VERSION;
            return "gdxgamelib OK - libGDX " + gdxVersion + " classes loaded";
        } catch (Throwable t) {
            // Even the libGDX core jar itself failed to load/merge correctly
            return "gdxgamelib FAILED: " + t.getClass().getSimpleName() + " - " + t.getMessage();
        }
    }
}
