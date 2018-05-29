package de.android.ayrathairullin.trustcopter;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
    AssetManager manager;

    public ThrustCopter() {
        manager = new AssetManager();

        fpsLogger = new FPSLogger();
        camera = new OrthographicCamera();
        camera.position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
    }

    @Override
    public void create() {
        manager.load("gameover.png", Texture.class);
        manager.load("life.png", Texture.class);
        manager.load("sounds/journey.mp3", Music.class);
        manager.load("sounds/pop.ogg", Sound.class);
        manager.load("sounds/crash.ogg", Sound.class);
        manager.load("sounds/alarm.ogg", Sound.class);
        manager.load("sounds/star.ogg", Sound.class);
        manager.load("sounds/shield.ogg", Sound.class);
        manager.load("sounds/fuel.ogg", Sound.class);
        manager.load("ThrustCopter.pack", TextureAtlas.class);
        manager.load("impact-40.fnt", BitmapFont.class);
        manager.finishLoading();

        batch = new SpriteBatch();
        atlas = manager.get("ThrustCopter.pack", TextureAtlas.class);
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
