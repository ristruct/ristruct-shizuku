package com.sketchgdx.gamelib;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Simple "tap to start" menu screen. Loads its own assets, switches to
 *  PlayScreen2D on tap. Copy this file as a starting point for any menu,
 *  pause screen, or game-over screen. */
public class MenuScreen implements Screen {

    private final GdxGame game;
    private final AssetManager assets = new AssetManager();
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;

    public MenuScreen(GdxGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont(); // swap for a loaded .fnt via AssetManager for real UI text
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        assets.load("badlogic.jpg", Texture.class);
        assets.finishLoading(); // small file: fine to block here
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.12f, 0.12f, 0.18f, 1f);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        font.draw(batch, "TAP TO START", 300, 260);
        batch.end();

        if (Gdx.input.justTouched()) {
            game.setScreen(new PlayScreen2D(game, assets));
            // PlayScreen2D now owns `assets`; don't dispose() it here.
            return;
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        // NOTE: assets is intentionally NOT disposed here — ownership passes
        // to whichever screen we switch to. Dispose it in the screen that
        // is showing when the game actually closes.
    }
}
