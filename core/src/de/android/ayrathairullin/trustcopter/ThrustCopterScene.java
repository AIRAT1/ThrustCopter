package de.android.ayrathairullin.trustcopter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class ThrustCopterScene extends ScreenAdapter {
	private static final int TOUCH_IMPULSE = 500;
	private static final float TAP_DRAW_TIME_MAX = 1.0f;
	private static final int METEOR_SPEED = 6; // TODO default value 60

	enum GameState {
		INIT, ACTION, GAME_OVER
	}

	private ThrustCopter game;
	private GameState gameState = GameState.INIT;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private TextureRegion backgroundRegion, terrainBelow, terrainAbove, tapIndicator, tap1, gameOver,
	pillarUp, pillarDown, selectedMeteorTexture;
	private float terrainOffset, planeAnimTime, tapDrawTime, deltaPosition, nextMeteorIn;
	private Animation<TextureRegion> plane;
	private Vector2 planeVelocity = new Vector2();
	private Vector2 planePosition = new Vector2();
	private Vector2 planeDefaultPosition = new Vector2();
	private Vector2 gravity = new Vector2();
	private Vector2 tmpVector = new Vector2();
	private Vector2 scrollVelocity = new Vector2();
	private Vector2 lastPillarPosition = new Vector2();
	private Vector2 meteorPosition = new Vector2();
	private Vector2 meteorVelocity = new Vector2();
	private static final Vector2 damping = new Vector2(.99f, .99f);
	private TextureAtlas atlas;
	private Vector3 touchPosition = new Vector3();
	private Array<Vector2> pillars = new Array<Vector2>();
	private Rectangle planeRect = new Rectangle();
	private Rectangle obstacleRect = new Rectangle();
	private Array<TextureAtlas.AtlasRegion> meteorTextures = new Array<TextureAtlas.AtlasRegion>();
	private boolean meteorInScene;
	private Music music;
	private Sound tapSound, crashSound, spawnSound;

	public ThrustCopterScene (ThrustCopter thrustCopter) {
		game = thrustCopter;
		batch = game.batch;
		camera = game.camera;
		camera.position.set(400, 240, 0);
		atlas = game.atlas;
		backgroundRegion = atlas.findRegion("background");
		terrainBelow = atlas.findRegion("groundGrass");
		terrainAbove = new TextureRegion(terrainBelow);
		terrainAbove.flip(true, true);
		tapIndicator = atlas.findRegion("tap2");
		tap1 = atlas.findRegion("tap1");
		gameOver = new TextureRegion(new Texture("gameover.png"));
		pillarUp = atlas.findRegion("rockGrassUp");
		pillarDown = atlas.findRegion("rockGrassDown");
		meteorTextures.add(atlas.findRegion("meteorBrown_med1"));
		meteorTextures.add(atlas.findRegion("meteorBrown_med2"));
		meteorTextures.add(atlas.findRegion("meteorBrown_small1"));
		meteorTextures.add(atlas.findRegion("meteorBrown_small2"));
		meteorTextures.add(atlas.findRegion("meteorBrown_tiny1"));
		meteorTextures.add(atlas.findRegion("meteorBrown_tiny2"));
		plane = new Animation<TextureRegion>(.05f, atlas.findRegion("planeRed1"),
				atlas.findRegion("planeRed2"),
				atlas.findRegion("planeRed3"),
				atlas.findRegion("planeRed2"));
		plane.setPlayMode(Animation.PlayMode.LOOP);
		resetScene();

		music = Gdx.audio.newMusic(Gdx.files.internal("sounds/journey.mp3"));
		music.setLooping(true);
		music.play();

		tapSound = Gdx.audio.newSound(Gdx.files.internal("sounds/pop.ogg"));
		crashSound = Gdx.audio.newSound(Gdx.files.internal("sounds/crash.ogg"));
		spawnSound = Gdx.audio.newSound(Gdx.files.internal("sounds/alarm.ogg"));
	}

	private void resetScene() {
		terrainOffset = 0;
		planeAnimTime = 0;
		planeVelocity.set(400, 0);
		scrollVelocity.set(4, 0);
		gravity.set(0, -2);
		planeDefaultPosition.set(100 - 88 / 2, 240 - 273 / 2); // TODO 100 was 400 default
		planePosition.set(planeDefaultPosition.x, planeDefaultPosition.y);
		pillars.clear();
		addPillar();
		meteorInScene = false;
		nextMeteorIn = (float)Math.random() * 5;
	}

	private void addPillar() {
		Vector2 pillarPosition = new Vector2();
		if (pillars.size == 0) {
			pillarPosition.x = (float)(800 + Math.random() * 600);
		}else {
			pillarPosition.x = lastPillarPosition.x + (float)(600 + Math.random() * 600);
		}
		if (MathUtils.randomBoolean()) {
			pillarPosition.y = 1;
		}else {
			pillarPosition.y = -1;
		}
		lastPillarPosition = pillarPosition;
		pillars.add(pillarPosition);
	}

	@Override
	public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		updateScene();
		drawScene();
	}

	private void updateScene() {
		if (Gdx.input.justTouched()) {
			tapSound.play();
			if (gameState == GameState.INIT) {
				gameState = GameState.ACTION;
				return;
			}
			if (gameState == GameState.GAME_OVER) {
				gameState = GameState.INIT;
				resetScene();
				return;
			}
			touchPosition.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPosition);
			tmpVector.set(planePosition.x, planePosition.y);
			tmpVector.sub(touchPosition.x, touchPosition.y).nor();
			planeVelocity.mulAdd(tmpVector,
					TOUCH_IMPULSE - MathUtils.clamp(Vector2.dst(touchPosition.x,
							touchPosition.y, planePosition.x, planePosition.y), 0, TOUCH_IMPULSE));
			tapDrawTime = TAP_DRAW_TIME_MAX;
		}
		if (gameState == GameState.INIT || gameState == GameState.GAME_OVER) {
			return;
		}
		float deltaTime = Gdx.graphics.getDeltaTime();
		tapDrawTime -= deltaTime;

		planeAnimTime += deltaTime;
		planeVelocity.scl(damping);
		planeVelocity.add(gravity);
		planeVelocity.add(scrollVelocity);
		planePosition.mulAdd(planeVelocity, deltaTime);
		deltaPosition = planePosition.x - planeDefaultPosition.x;
		terrainOffset -= deltaPosition;
		planePosition.x = planeDefaultPosition.x;
		if (terrainOffset * - 1 > terrainBelow.getRegionWidth()) {
			terrainOffset = 0;
		}
		if (terrainOffset > 0) {
			terrainOffset = - terrainBelow.getRegionWidth();
		}

		if (meteorInScene) {
			meteorPosition.mulAdd(meteorVelocity, deltaTime);
			meteorPosition.x -= deltaPosition;
			if (meteorPosition.x < - 10) {
				meteorInScene = false;
			}
			obstacleRect.set(meteorPosition.x + 2, meteorPosition.y + 2,
					selectedMeteorTexture.getRegionWidth() - 4,
					selectedMeteorTexture.getRegionHeight() - 4);
			if (planeRect.overlaps(obstacleRect)) {
				if (gameState != GameState.GAME_OVER) {
					tapDrawTime = 0;
					crashSound.play();
					gameState = GameState.GAME_OVER;
				}
			}
		}
		nextMeteorIn = - deltaTime;
		if (nextMeteorIn <= 0) {
			launchMeteor();
		}

		planeRect.set(planePosition.x + 16, planePosition.y, 50, 73);
		for (Vector2 vec : pillars) {
			vec.x -= deltaPosition;
			if (vec.x + pillarUp.getRegionWidth() < - 10) {
				pillars.removeValue(vec, false);
			}
			if (vec.y == 1) {
				obstacleRect.set(vec.x + 10, 0, pillarUp.getRegionWidth() - 20,
						pillarUp.getRegionHeight() - 10);
			}else {
				obstacleRect.set(vec.x + 10, 480 - pillarDown.getRegionHeight() + 10,
						pillarUp.getRegionWidth() - 20, pillarUp.getRegionHeight());
			}
			if (planeRect.overlaps(obstacleRect)) {
				if (gameState != GameState.GAME_OVER) {
					tapDrawTime = 0;
					crashSound.play();
					gameState = GameState.GAME_OVER;
				}
			}
		}
		if (lastPillarPosition.x < 400) {
			addPillar();
		}

		if (planePosition.y < terrainBelow.getRegionHeight() - 35 ||
				planePosition.y + 73 > 480 - terrainBelow.getRegionHeight() + 35) {
			if (gameState != GameState.GAME_OVER) {
				tapDrawTime = 0;
				crashSound.play();
				gameState = GameState.GAME_OVER;
			}
		}
	}

	private void launchMeteor() {
		nextMeteorIn = 1.5f + (float)Math.random() * 5;
		if (meteorInScene) {
			return;
		}
		spawnSound.play();
		meteorInScene = true;
		int id = (int)Math.random() * meteorTextures.size;
		selectedMeteorTexture = meteorTextures.get(id);
		meteorPosition.x = 810;
		meteorPosition.y = (float)(80 + Math.random() * 320);
		Vector2 destination = new Vector2();
		destination.x = - 10;
		destination.y = (float)(80 + Math.random() * 320);
		destination.sub(meteorPosition).nor();
		meteorVelocity.mulAdd(destination, METEOR_SPEED);
	}

	private void drawScene() {
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.disableBlending();
		batch.draw(backgroundRegion, 0, 0);
		batch.enableBlending();

		for (Vector2 vec : pillars) {
			if (vec.y == 1) {
				batch.draw(pillarUp, vec.x, 0);
			}else {
				batch.draw(pillarDown, vec.x, 480 - pillarDown.getRegionHeight());
			}
		}
		batch.draw(terrainBelow, terrainOffset, 0);
		batch.draw(terrainBelow, terrainOffset + terrainBelow.getRegionWidth(), 0);
		batch.draw(terrainAbove, terrainOffset, 480 - terrainAbove.getRegionHeight());
		batch.draw(terrainAbove, terrainOffset + terrainAbove.getRegionWidth(), 480 - terrainAbove.getRegionHeight());

		batch.draw(plane.getKeyFrame(planeAnimTime), planePosition.x, planePosition.y);

		if (tapDrawTime > 0) {
			batch.draw(tapIndicator, touchPosition.x - 29.5f, touchPosition.y - 29.5f);
		}

		if (gameState == GameState.INIT) {
			batch.draw(tap1, planePosition.x, planePosition.y - 80);
		}

		if (gameState == GameState.GAME_OVER) {
			batch.draw(gameOver, 400 - 206, 240 - 80);
		}

		if (meteorInScene) {
			batch.draw(selectedMeteorTexture, meteorPosition.x, meteorPosition.y);
		}

		batch.end();
	}

	@Override
	public void dispose () {
		music.dispose();
		pillars.clear();
		meteorTextures.clear();
		tapSound.dispose();
		crashSound.dispose();
		spawnSound.dispose();
	}
}
