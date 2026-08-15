package com.sketchgdx.gamelib;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** A minimal but real 2D gameplay loop: move a sprite with arrow keys /
 *  on-screen drag, keep it on screen. Replace the update()/draw() logic
 *  with your actual game — this is the shape (input -> update -> render)
 *  every 2D screen in the project should follow. */
public class PlayScreen2D implements Screen {

    private final GdxGame game;
    private final AssetManager assets;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Texture player;
    private final Vector2 pos = new Vector2(50, 50);
    private static final float SPEED = 260f;

    public PlayScreen2D(GdxGame game, AssetManager assets) {
        this.game = game;
        this.assets = assets;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        player = assets.get("badlogic.jpg", Texture.class);
    }

    @Override
    public void render(float delta) {
        handleInput(delta);

        ScreenUtils.clear(0.05f, 0.15f, 0.08f, 1f);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(player, pos.x, pos.y);
        batch.end();
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) pos.x -= SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) pos.x += SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) pos.y += SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) pos.y -= SPEED * delta;

        if (Gdx.input.isTouched()) {
            Vector2 touch = viewport.unproject(
                new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            pos.set(touch.x - player.getWidth() / 2f, touch.y - player.getHeight() / 2f);
        }

        pos.x = com.badlogic.gdx.math.MathUtils.clamp(pos.x, 0, 800 - player.getWidth());
        pos.y = com.badlogic.gdx.math.MathUtils.clamp(pos.y, 0, 480 - player.getHeight());
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        assets.dispose(); // last screen holding it: safe to dispose here
    }
}
