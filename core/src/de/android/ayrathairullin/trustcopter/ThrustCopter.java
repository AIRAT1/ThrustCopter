package de.android.ayrathairullin.trustcopter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ThrustCopter extends ApplicationAdapter {
	private FPSLogger fpsLogger;
	private SpriteBatch batch;

	@Override
	public void create () {
		fpsLogger = new FPSLogger();
		batch = new SpriteBatch();
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

	}

	private void drawScene() {

	}

	@Override
	public void dispose () {
		batch.dispose();
	}
}
