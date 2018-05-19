package de.android.ayrathairullin.trustcopter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ThrustCopter extends ApplicationAdapter {
	private FPSLogger fpsLogger;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private TextureRegion backgroundRegion, terrainBelow, terrainAbove;
	private float terrainOffset, planeAnimTime;
	private Animation<TextureRegion> plane;

	@Override
	public void create () {
		fpsLogger = new FPSLogger();
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800,480);
		backgroundRegion = new TextureRegion(new Texture("background.png"));
		terrainBelow = new TextureRegion(new Texture("groundGrass.png"));
		terrainAbove = new TextureRegion(terrainBelow);
		terrainAbove.flip(true, true);
		plane = new Animation<TextureRegion>(.05f, new TextureRegion(new Texture("planeRed1.png")),
				new TextureRegion(new Texture("planeRed2.png")),
				new TextureRegion(new Texture("planeRed3.png")),
				new TextureRegion(new Texture("planeRed2.png")));
		plane.setPlayMode(Animation.PlayMode.LOOP);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		fpsLogger.log();
		updateScene();
		drawScene();
	}

	private void updateScene() {
		terrainOffset -= 200 * Gdx.graphics.getDeltaTime();
		planeAnimTime += Gdx.graphics.getDeltaTime();
	}

	private void drawScene() {
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.disableBlending();
		batch.draw(backgroundRegion, 0, 0);
		batch.enableBlending();

		batch.draw(terrainBelow, terrainOffset, 0);
		batch.draw(terrainBelow, terrainOffset + terrainBelow.getRegionWidth(), 0);
		batch.draw(terrainAbove, terrainOffset, 480 - terrainAbove.getRegionHeight());
		batch.draw(terrainAbove, terrainOffset + terrainAbove.getRegionWidth(), 480 - terrainAbove.getRegionHeight());

		batch.draw(plane.getKeyFrame(planeAnimTime), 350, 200);

		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
	}
}
