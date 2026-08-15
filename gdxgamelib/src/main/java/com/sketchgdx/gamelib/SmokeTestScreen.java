package com.sketchgdx.gamelib;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Level-2 test: real libGDX/GL smoke test with a visible pass/fail screen,
 * so you can tell at a glance on the device whether the whole pipeline
 * (Sketchware Pro -> local .aar -> GLSurfaceView -> AssetManager -> render)
 * actually works, without needing logcat.
 *
 * GREEN screen = everything initialized and the test asset loaded.
 * RED screen   = something threw; the exception message is drawn on screen.
 * Tap anywhere on a green (pass) screen to continue into MenuScreen.
 */
public class SmokeTestScreen implements Screen {

    private final GdxGame game;
    private SpriteBatch batch;
    private BitmapFont font;
    private AssetManager assets;
    private Texture testTexture;

    private boolean passed = false;
    private String statusLine1 = "";
    private String statusLine2 = "";

    public SmokeTestScreen(GdxGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        try {
            batch = new SpriteBatch();
            font = new BitmapFont();
            font.getData().setScale(1.5f);

            assets = new AssetManager();
            assets.load("badlogic.jpg", Texture.class);
            assets.finishLoading();
            testTexture = assets.get("badlogic.jpg", Texture.class);

            statusLine1 = "GDX OK  -  libGDX " + com.badlogic.gdx.Version.VERSION;
            statusLine2 = "GL: " + Gdx.gl.glGetString(com.badlogic.gdx.graphics.GL20.GL_RENDERER)
                    + "  |  asset load: OK  |  tap to continue";
            passed = true;
        } catch (Throwable t) {
            passed = false;
            statusLine1 = "GDX FAILED";
            statusLine2 = t.getClass().getSimpleName() + ": " + t.getMessage();
            Gdx.app.error("SmokeTest", "libGDX smoke test failed", t);
        }
    }

    @Override
    public void render(float delta) {
        if (passed) {
            ScreenUtils.clear(0.15f, 0.55f, 0.2f, 1f); // green
        } else {
            ScreenUtils.clear(0.6f, 0.1f, 0.1f, 1f); // red
        }

        batch.begin();
        font.draw(batch, statusLine1, 40, Gdx.graphics.getHeight() - 60);
        font.draw(batch, statusLine2, 40, Gdx.graphics.getHeight() - 100);
        if (passed && testTexture != null) {
            batch.draw(testTexture, 40, 40);
        }
        batch.end();

        if (passed && Gdx.input.justTouched()) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (assets != null) assets.dispose();
    }
}
