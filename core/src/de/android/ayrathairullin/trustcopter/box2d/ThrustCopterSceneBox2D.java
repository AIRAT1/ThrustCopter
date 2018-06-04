package de.android.ayrathairullin.trustcopter.box2d;


import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import de.android.ayrathairullin.trustcopter.BaseScene;
import de.android.ayrathairullin.trustcopter.Pickup;
import de.android.ayrathairullin.trustcopter.ThrustCopter;

import static com.badlogic.gdx.graphics.g2d.Animation.*;

public class ThrustCopterSceneBox2D extends BaseScene{
    private static final boolean DRAW_BOX2D_DEBUG = true;
    private static final int BOX2D_TO_CAMERA = 10;
    private static final int TOUCH_IMPULSE = 1000;
    private static final float TAP_DRAW_TIME_MAX = 1;
    private static final int METEOR_SPEED = 50;

    private Box2DDebugRenderer debugRenderer;
    private World world;
    private OrthographicCamera box2dCam;
    private Body planeBody, terrainBodyDown, terrainBodyUp, meteorBody,
    lastPillarBody, bodyA, bodyB, unknownBody, hitBody;
    Vector2 meteorPosition = new Vector2();
    Array<Body> pockupsInScene = new Array<Body>();
    Array<Body> pillars = new Array<Body>();
    Array<Body> setForRemoval = new Array<Body>();

    TextureRegion bgRegion, terrainBelow, terrainAbove, tap2, tap1,
    pillarUp, pillarDown, toDraw,selectedMeteorTexture;
    Texture fuelIndicator, gameOver;
    float tapDriveTime, terrainOffset, planeAnimTime, previousCamXPos,
    deltaPosition, nextMeteorIn;
    Animation<TextureRegion> plane;
    Vector2 planePosition = new Vector2();
    Vector2 planeDefaultPosition = new Vector2();
    Vector2 gravity = new Vector2();
    Vector2 tempVector = new Vector2();
    Vector3 touchPosition = new Vector3();
    Vector3 touchPositionBox2D = new Vector3();
    Vector3 pickupTiming = new Vector3();
    GameState gameState = GameState.Init;
    Rectangle planeRect = new Rectangle();
    Rectangle obstacleRect = new Rectangle();
    boolean meteorInScene;
    Music music;
    Sound tapSound, crashSound, spawnSound;
    SpriteBatch batch;
    OrthographicCamera camera;
    TextureAtlas atlas;
    Pickup tempPickup;
    int starCount, fuelPercentage;
    float fuelCount, shieldCount, score;
    Animation<TextureRegion> shield;
    BitmapFont font;
    ParticleEffect smoke, explosion;
    private boolean gamePaused = false;

    static enum GameState {
        Init, Action, GameOver
    }

    static enum ItemType {
        Pickup, Terrain, Meteor, Pillar
    }

    public ThrustCopterSceneBox2D(ThrustCopter thrustCopter) {
        super(thrustCopter);
        batch = game.batch;
        camera = game.camera;
        atlas = game.atlas;
        font = game.font;
        fuelIndicator = game.manager.get("life.png", Texture.class);
        bgRegion = atlas.findRegion("background");
        terrainBelow = atlas.findRegion("groundGrass");
        tap2 = atlas.findRegion("tap2");
        tap1 = atlas.findRegion("tap1");
        pillarUp = atlas.findRegion("rockGrassUp");
        pillarDown = atlas.findRegion("rockGrassDown");
        gameOver = game.manager.get("gameover.png", Texture.class);
        terrainAbove = new TextureRegion(terrainBelow);
        terrainAbove.flip(true, true);

        plane = new Animation<TextureRegion>(.05f, atlas.findRegion("planeRed1"),
                atlas.findRegion("planeRed2"),
                atlas.findRegion("planeRed3"),
                atlas.findRegion("planeRed2"));
        plane.setPlayMode(PlayMode.LOOP);
        shield = new Animation<TextureRegion>(.1f, atlas.findRegion("shield1"),
                atlas.findRegion("shield2"),
                atlas.findRegion("shield3"),
                atlas.findRegion("shield2"));
        shield.setPlayMode(PlayMode.LOOP);

        selectedMeteorTexture = atlas.findRegion("meteorBrown_med1");
        if (game.soundEnabled) {
            music = game.manager.get("sounds/journey.mp3", Music.class);
            music.setLooping(true);
            music.play();
            music.setVolume(game.soundVolume);

            tapSound = game.manager.get("sounds/pop.ogg", Sound.class);
            crashSound = game.manager.get("sounds/crash.ogg", Sound.class);
            spawnSound = game.manager.get("sounds/alarm.ogg", Sound.class);
        }

        smoke = game.manager.get("Smoke", ParticleEffect.class);
        explosion = game.manager.get("Explosion", ParticleEffect.class);

        resetScene();
        initPhysics();
        addPillar();
    }
}
