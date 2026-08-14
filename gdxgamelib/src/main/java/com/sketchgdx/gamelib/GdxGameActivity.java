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
 * In Sketchware Pro:
 *   1. Import Library -> Local Library -> gdxgamelib.aar
 *   2. Add the <activity> entry shown in AndroidManifest.xml (this module)
 *      into your project's AndroidManifest via the Manifest editor.
 *   3. On a Button click block: More Block -> Intent -> "Start Activity" ->
 *      set component to "com.sketchgdx.gamelib.GdxGameActivity" and, if you
 *      want to change the image, putExtra("image_path", "sprites/player.png").
 *   4. When the game finishes (back button), it calls finish() and, if it
 *      was started with startActivityForResult, returns RESULT_OK with the
 *      "score" extra so your block screen can read it in onActivityResult.
 */
public class GdxGameActivity extends AndroidApplication {

    public static final String EXTRA_IMAGE_PATH = "image_path";
    public static final String EXTRA_RESULT_SCORE = "score";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String imagePath = "badlogic.jpg";
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_IMAGE_PATH)) {
            imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH);
        }

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGyroscope = false;

        initialize(new BaseGame(imagePath), config);
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
