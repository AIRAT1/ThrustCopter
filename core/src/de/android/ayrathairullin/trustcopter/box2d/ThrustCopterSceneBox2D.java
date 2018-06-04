package de.android.ayrathairullin.trustcopter.box2d;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import de.android.ayrathairullin.trustcopter.BaseScene;
import de.android.ayrathairullin.trustcopter.Pickup;
import de.android.ayrathairullin.trustcopter.ThrustCopter;

import static com.badlogic.gdx.graphics.g2d.Animation.*;
import static com.badlogic.gdx.physics.box2d.BodyDef.*;

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
    Array<Body> pickupsInScene = new Array<Body>();
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
    Vector2 tmpVector = new Vector2();
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

    private void resetScene() {
        meteorInScene = false;
        nextMeteorIn = (float)Math.random() * 5;
        pickupTiming.x = 1 + (float)Math.random() * 2;
        pickupTiming.y = 3 + (float)Math.random() * 2;
        pickupTiming.z = 1 + (float)Math.random() * 3;
        terrainOffset = 0;
        planeAnimTime = 0;
        tapDriveTime = 0;
        starCount = 0;
        score = 0;
        shieldCount = 15;
        fuelCount = 100;
        fuelPercentage = 114;
        planeDefaultPosition.set(250 - 88 / 2, 240 - 73 / 2);
        planePosition.set(planeDefaultPosition.x, planeDefaultPosition.y);
        smoke.setPosition(planePosition.x + 20, planePosition.y + 30);;
        if (gameState == GameState.GameOver) {
            resetPhysics();
        }
    }

    private void resetPhysics() {
        for (Body vec : pillars) {
            world.destroyBody(vec);
        }
        pillars.clear();
        for (Body vec : pickupsInScene) {
            world.destroyBody(vec);
        }
        pickupsInScene.clear();
        tmpVector.set(800, 500);
        meteorBody.setTransform(tmpVector, 0);
        tmpVector.set(planePosition);
        planeBody.setTransform(tmpVector.x / BOX2D_TO_CAMERA, tmpVector.y / BOX2D_TO_CAMERA, 0);
        planeBody.setAwake(true);
        box2dCam.position.set(40, 24, 0);
        previousCamXPos = 40;
        terrainBodyUp.setTransform(box2dCam.position.x + .4f, 44.5f, 0);
        terrainBodyDown.setTransform(box2dCam.position.x + .4f, 3.5f, 0);
        lastPillarBody = null;
        addPillar();
    }

    private void initPhysics() {
        world = new World(new Vector2(8, - 10), true);
        debugRenderer = new Box2DDebugRenderer();
        box2dCam = new OrthographicCamera(80, 48);
        box2dCam.position.set(40, 24, 0);
        previousCamXPos = 40;

        planeBody = createPhysicsObjectFromGraphics(plane.getKeyFrame(0), planePosition, BodyType.DynamicBody);
//        planeBody.setSleepingAllowed(false);
        terrainBodyUp = createPhysicsObjectFromGraphics(terrainAbove, new Vector2(
                terrainAbove.getRegionWidth() / 2,
                480 - terrainAbove.getRegionHeight() / 2), BodyType.StaticBody);
        terrainBodyDown = createPhysicsObjectFromGraphics(terrainBelow, new Vector2(
                terrainBelow.getRegionWidth() / 2,
                terrainBelow.getRegionHeight() / 2), BodyType.StaticBody);
        meteorBody = createPhysicsObjectFromGraphics(selectedMeteorTexture, new Vector2(
                800, 500), BodyType.KinematicBody);

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {

            }

            @Override
            public void endContact(Contact contact) {
                bodyA = contact.getFixtureA().getBody();
                bodyB = contact.getFixtureB().getBody();
                boolean planeFound = false;
                if (bodyA.equals(planeBody)) {
                    planeFound = true;
                    unknownBody = bodyB;
                }else if (bodyB.equals(planeBody)) {
                    planeFound = true;
                    unknownBody = bodyA;
                }
                if (planeFound) {
                    ItemType itemType = getItemType(unknownBody);
                    if (itemType == ItemType.Terrain) {
                        endGame();
                    }else if (shieldCount <= 0 && itemType == ItemType.Meteor
                            || itemType == ItemType.Pillar) {
                        endGame();
                    }else if (itemType == ItemType.Pickup) {
                        pickIt(unknownBody);
                    }
                }
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                bodyA = contact.getFixtureA().getBody();
                bodyB = contact.getFixtureB().getBody();
                boolean planeFound = false;
                if (bodyA.equals(planeBody)) {
                    planeFound = true;
                    unknownBody = bodyB;
                }else  if (bodyB.equals(planeBody)) {
                    planeFound = true;
                    unknownBody = bodyA;
                }
                if (planeFound) {
                    ItemType itemType = getItemType(unknownBody);
                    if (shieldCount > 0 && (itemType == ItemType.Meteor
                    || itemType == ItemType.Pillar)) {
                        contact.setEnabled(false);
                    }else if (itemType == ItemType.Pickup) {
                        contact.setEnabled(false);
                    }
                }
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
                // unused
            }
        });
    }

    private Body createPhysicsObjectFromGraphics(TextureRegion region,
                                                 Vector2 position, BodyType bodyType) {
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = bodyType;
        boxBodyDef.position.x = position.x / BOX2D_TO_CAMERA;
        boxBodyDef.position.y = position.y / BOX2D_TO_CAMERA;
        Body boxBody = world.createBody(boxBodyDef);
        PolygonShape boxPoly = new PolygonShape();
        boxPoly.setAsBox(region.getRegionWidth() / (2 * BOX2D_TO_CAMERA),
                region.getRegionHeight() / (2 * BOX2D_TO_CAMERA));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = boxPoly;
        fixtureDef.density = 1;
        fixtureDef.restitution = .2f;
        boxBody.createFixture(fixtureDef);

//        boxBody.createFixture(boxPoly, 1);
        boxPoly.dispose();
        boxBody.setUserData(region);
        return boxBody;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (gamePaused) {
            return;
        }
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateSceneBox2D(delta);
        drawSceneBox2D();
        if (DRAW_BOX2D_DEBUG) {
            box2dCam.update();
            debugRenderer.render(world, box2dCam.combined);
        }
    }

    private void updateSceneBox2D(float delta) {
        if (Gdx.input.justTouched()) {
            if (game.soundEnabled) {
                tapSound.play(game.soundVolume);
            }
            if (gameState == GameState.Init) {
                gameState = GameState.Action;
                return;
            }
            if (gameState == GameState.GameOver) {
                resetScene();
                gameState = GameState.Init;
                return;
            }
            if (fuelCount > 0) {
                touchPosition.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                touchPositionBox2D.set(touchPosition);
                box2dCam.unproject(touchPositionBox2D);
                tmpVector.set(planeBody.getPosition());
                tmpVector.sub(touchPositionBox2D.x, touchPositionBox2D.y).nor();
                tmpVector.scl(TOUCH_IMPULSE - MathUtils.clamp(25 * Vector2.dst(
                        touchPositionBox2D.x, touchPositionBox2D.y,
                        planeBody.getPosition().x, planeBody.getPosition().y), 0, TOUCH_IMPULSE));
                planeBody.applyLinearImpulse(tmpVector, planeBody.getPosition(), true);
                tapDriveTime = TAP_DRAW_TIME_MAX;
                camera.unproject(touchPosition);
            }
        }
        smoke.update(delta);
        if (gameState == GameState.Init || gameState == GameState.GameOver) {
            if (gameState == GameState.GameOver) {
                explosion.update(delta);
            }
            return;
        }
        planeAnimTime += delta;
        deltaPosition = (box2dCam.position.x - previousCamXPos) * BOX2D_TO_CAMERA;
        previousCamXPos = box2dCam.position.x;
        terrainOffset -= deltaPosition;
        if (terrainOffset * - 1 > terrainBelow.getRegionWidth()) {
            terrainOffset = 0;
        }
        if (terrainOffset > 0) {
            terrainOffset = - terrainBelow.getRegionWidth();
        }
        if (meteorInScene) {
            meteorPosition = meteorBody.getPosition();
            if (meteorPosition.x - box2dCam.position.x < - 42) {
                meteorInScene = false;
            }
            meteorPosition.scl(BOX2D_TO_CAMERA);
        }
        for (Body vec : pillars) {
            if (vec.getPosition().x + 5.4 - box2dCam.position.x < - 42) {
                pillars.removeValue(vec, false);
                world.destroyBody(vec);
            }
        }
        for (Body pickup : pickupsInScene) {
            if (pickup.getPosition().x + 1.9 - box2dCam.position.x < - 42) {
                pickupsInScene.removeValue(pickup, false);
                world.destroyBody(pickup);
            }
        }
        if (lastPillarBody.getPosition().x < box2dCam.position.x) {
            addPillar();
        }
        tapDriveTime -= delta;
        nextMeteorIn -= delta;
        if (nextMeteorIn <= 0) {
            launchMeteor();
        }
        checkAndCreatePickup(delta);
        fuelCount -= 6 * delta;
        fuelPercentage = (int)(114 * fuelCount) / 100;
        shieldCount -= delta;
        score += delta;

        world.step(delta, 8, 3);
        box2dCam.position.x = planeBody.getPosition().x + 19.4f;
        terrainBodyUp.setTransform(box2dCam.position.x + .4f, 44.5f, 0);
        terrainBodyDown.setTransform(box2dCam.position.x + .4f, 3.5f, 0);

        if (setForRemoval.size > 0) {
            for (Body pickup : setForRemoval) {
                world.destroyBody(pickup);
            }
            setForRemoval.clear();
        }
//        planePosition = planeBody.getPosition();

    }
}
