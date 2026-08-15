package com.sketchgdx.gamelib;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Bare-minimum 3D pipeline example: PerspectiveCamera + Environment/lighting
 * + ModelBatch, spinning a procedurally-built cube (no .obj/.g3db needed, so
 * it runs with zero extra assets). For real models, load them through
 * AssetManager the same way PlayScreen2D loads its texture, using
 * G3dModelLoader / ObjLoader instead of ModelBuilder.
 */
public class PlayScreen3D implements Screen {

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Model cubeModel;
    private ModelInstance cubeInstance;
    private Environment environment;
    private float angle = 0f;

    @Override
    public void show() {
        modelBatch = new ModelBatch();

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(3f, 3f, 3f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        ModelBuilder modelBuilder = new ModelBuilder();
        cubeModel = modelBuilder.createBox(2f, 2f, 2f,
            new Material(ColorAttribute.createDiffuse(Color.ORANGE)),
            Usage.Position | Usage.Normal);
        cubeInstance = new ModelInstance(cubeModel);
    }

    @Override
    public void render(float delta) {
        angle += delta * 45f; // degrees/sec
        cubeInstance.transform.setToRotation(Vector3.Y, angle);

        ScreenUtils.clear(0.02f, 0.02f, 0.05f, 1f, true);
        camera.update();

        modelBatch.begin(camera);
        modelBatch.render(cubeInstance, environment);
        modelBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        modelBatch.dispose();
        cubeModel.dispose();
    }
}
