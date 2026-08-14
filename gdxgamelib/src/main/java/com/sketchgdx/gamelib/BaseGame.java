package com.sketchgdx.gamelib;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Minimal, self-contained libGDX game core.
 *
 * This class is what Sketchware Pro users add/edit as an "extra Java class"
 * inside their project (Design -> Java files -> Add). It is intentionally
 * simple: it shows the AssetManager loading pattern (async load with a
 * progress screen) and a moving sprite once loading is done, so you can
 * confirm the whole pipeline (Sketchware Pro -> local .aar -> libGDX ->
 * assets/) actually renders on a device before building your real game.
 *
 * Put your game images inside:
 *   app/src/main/assets/   (Sketchware Pro's own assets folder)
 * libGDX's AssetManager on Android reads from that same assets/ folder,
 * so anything you drop there in Sketchware Pro's file manager is reachable
 * with a plain relative path, e.g. "sprites/player.png".
 */
public class BaseGame extends ApplicationAdapter {

    private AssetManager assetManager;
    private SpriteBatch batch;
    private Texture logoTexture;

    private float x = 100f;
    private float speed = 220f;

    /** Path (relative to assets/) of the image to load and bounce around. */
    private final String imagePath;

    public BaseGame() {
        this("badlogic.jpg");
    }

    public BaseGame(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public void create() {
        assetManager = new AssetManager();
        assetManager.load(imagePath, Texture.class);
        batch = new SpriteBatch();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!assetManager.update()) {
            // still loading assets, could draw a progress bar here using
            // assetManager.getProgress()
            return;
        }

        if (logoTexture == null) {
            logoTexture = assetManager.get(imagePath, Texture.class);
        }

        x += speed * Gdx.graphics.getDeltaTime();
        if (x < 0 || x + logoTexture.getWidth() > Gdx.graphics.getWidth()) {
            speed = -speed;
            x = MathUtils.clamp(x, 0, Gdx.graphics.getWidth() - logoTexture.getWidth());
        }

        batch.begin();
        batch.draw(logoTexture, x, 100);
        batch.end();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (assetManager != null) assetManager.dispose();
    }
}
