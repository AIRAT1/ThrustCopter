package de.android.ayrathairullin.trustcopter;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ThrustCopter extends Game {
    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 480;

    TextureAtlas atlas;
    FPSLogger fpsLogger;
    OrthographicCamera camera;
    Viewport viewport;
    SpriteBatch batch;

    public ThrustCopter() {
        fpsLogger = new FPSLogger();
        camera = new OrthographicCamera();
        camera.position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        atlas = new TextureAtlas(Gdx.files.internal("ThrustCopter.pack"));
        setScreen(new ThrustCopterScene(this));
    }

    @Override
    public void render() {
        fpsLogger.log();
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        atlas.dispose();
    }
}
