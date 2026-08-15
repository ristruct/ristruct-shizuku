package com.sketchgdx.gamelib;

import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

/**
 * Launch point that a Sketchware Pro block-based screen can start with a
 * plain "Intent -> Start Activity" (or "Start Activity for Result") block.
 * Sketchware Pro can't add custom Activity classes through the block editor
 * itself, so this class -- shipped inside gdxgamelib.aar -- IS the Activity;
 * you only reference its class name from blocks.
 *
 * Recommended setup (most stable — doesn't touch the manifest sections
 * Sketchware Pro's IDE regenerates on its own):
 *   1. Import Library -> Local Library -> gdxgamelib.aar
 *   2. Manifest editor -> add the <activity> entry from this module's
 *      AndroidManifest.xml (leave MainActivity's own LAUNCHER entry as-is).
 *   3. In MainActivity's onCreate event: add block Intent -> "Start Activity"
 *      with component "com.sketchgdx.gamelib.GdxGameActivity", then block
 *      Activity -> "Finish Activity" right after it. The app still boots
 *      through Sketchware Pro's own MainActivity (so the IDE never fights
 *      you over the manifest), it just hands off to the game in the same
 *      frame, with no visible flicker.
 *   4. When the game finishes (e.g. a "game over" screen calls
 *      finishWithScore(...)), it hands RESULT_OK + a "score" extra back to
 *      whatever screen started it with startActivityForResult.
 */
public class GdxGameActivity extends AndroidApplication {

    public static final String EXTRA_RESULT_SCORE = "score";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGyroscope = false;

        // GdxGame -> MenuScreen -> PlayScreen2D / PlayScreen3D. Add your own
        // Screens (levels, pause, game-over, ...) next to those and switch
        // between them with game.setScreen(...) from within any Screen.
        initialize(new GdxGame(), config);
    }

    /** Call this from your game logic (e.g. a "game over" screen) to close
     *  and hand a result back to the Sketchware Pro block screen that
     *  started this Activity with "Start Activity for Result". */
    public void finishWithScore(int score) {
        Intent result = new Intent();
        result.putExtra(EXTRA_RESULT_SCORE, score);
        setResult(RESULT_OK, result);
        finish();
    }
}
