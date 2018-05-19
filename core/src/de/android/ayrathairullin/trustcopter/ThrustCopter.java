package de.android.ayrathairullin.trustcopter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ThrustCopter extends ApplicationAdapter {
	private FPSLogger fpsLogger;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private TextureRegion backgroundRegion, terrainBelow, terrainAbove;
	private float terrainOffset, planeAnimTime;
	private Animation<TextureRegion> plane;
	private Vector2 planeVelocity = new Vector2();
	private Vector2 planePosition = new Vector2();
	private Vector2 planeDefaultPosition = new Vector2();
	private Vector2 gravity = new Vector2();
	private static final Vector2 damping = new Vector2(.99f, .99f);
	private TextureAtlas atlas;
	private Viewport viewport;

	@Override
	public void create () {
		fpsLogger = new FPSLogger();
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
//		camera.setToOrtho(false, 800,480);
		camera.position.set(400, 240, 0);
		viewport = new FitViewport(800, 480, camera);
		atlas = new TextureAtlas(Gdx.files.internal("ThrustCopter.pack"));
		backgroundRegion = atlas.findRegion("background");
		terrainBelow = atlas.findRegion("groundGrass");
		terrainAbove = new TextureRegion(terrainBelow);
		terrainAbove.flip(true, true);
		plane = new Animation<TextureRegion>(.05f, atlas.findRegion("planeRed1"),
				atlas.findRegion("planeRed2"),
				atlas.findRegion("planeRed3"),
				atlas.findRegion("planeRed2"));
		plane.setPlayMode(Animation.PlayMode.LOOP);
		resetScene();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	private void resetScene() {
		terrainOffset = 0;
		planeAnimTime = 0;
		planeVelocity.set(400, 0);
		gravity.set(0, -2);
		planeDefaultPosition.set(400 - 88 / 2, 240 - 273 / 2);
		planePosition.set(planeDefaultPosition.x, planeDefaultPosition.y);
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
		float deltaTime = Gdx.graphics.getDeltaTime();
		planeAnimTime += deltaTime;
		planeVelocity.scl(damping);
		planeVelocity.add(gravity);
		planePosition.mulAdd(planeVelocity, deltaTime);
		terrainOffset -= planePosition.x - planeDefaultPosition.x;
		planePosition.x = planeDefaultPosition.x;
		if (terrainOffset * - 1 > terrainBelow.getRegionWidth()) {
			terrainOffset = 0;
		}
		if (terrainOffset > 0) {
			terrainOffset = - terrainBelow.getRegionWidth();
		}
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

		batch.draw(plane.getKeyFrame(planeAnimTime), planePosition.x, planePosition.y);

		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
	}
}
