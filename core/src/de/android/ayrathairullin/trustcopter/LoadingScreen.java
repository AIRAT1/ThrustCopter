package de.android.ayrathairullin.trustcopter;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LoadingScreen extends ScreenAdapter{
    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 480;
    private static final float PROGRESS_BAR_WIDTH = 100;
    private static final float PROGRESS_BAR_HEIGHT = 25;

    private ShapeRenderer shapeRenderer;
    private Viewport viewport;
    private Camera camera;

    private float progress = 0;

    private ThrustCopter game;

    public LoadingScreen(ThrustCopter thrustCopter) {
        game = thrustCopter;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void show() {
        super.show();
        camera = new OrthographicCamera();
        camera.position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
        camera.update();
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();

        game.manager.load("gameover.png", Texture.class);
        game.manager.load("life.png", Texture.class);
        game.manager.load("sounds/journey.mp3", Music.class);
        game.manager.load("sounds/pop.ogg", Sound.class);
        game.manager.load("sounds/crash.ogg", Sound.class);
        game.manager.load("sounds/alarm.ogg", Sound.class);
        game.manager.load("sounds/star.ogg", Sound.class);
        game.manager.load("sounds/shield.ogg", Sound.class);
        game.manager.load("sounds/fuel.ogg", Sound.class);
        game.manager.load("ThrustCopter.pack", TextureAtlas.class);
        game.manager.load("impact-40.fnt", BitmapFont.class);
        game.manager.load("Smoke", ParticleEffect.class);
        game.manager.load("Explosion", ParticleEffect.class);
        game.manager.finishLoading();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        update();
        clearScreen();
        draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeRenderer.dispose();
    }

    private void update() {
        if (game.manager.update()) {
            game.atlas = game.manager.get("ThrustCopter.pack", TextureAtlas.class);
            game.font = game.manager.get("impact-40.fnt", BitmapFont.class);
            game.setScreen(new ThrustCopterScene(game));
        }else {
            progress = game.manager.getProgress();
        }
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void draw() {
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(SCREEN_WIDTH / 2 - PROGRESS_BAR_WIDTH / 2,
                SCREEN_HEIGHT / 2 - PROGRESS_BAR_HEIGHT / 2,
                progress * PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        shapeRenderer.end();
    }
}
