package de.android.ayrathairullin.trustcopter.box2d;


import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

import de.android.ayrathairullin.trustcopter.BaseScene;
import de.android.ayrathairullin.trustcopter.ThrustCopter;

public class ThrustCopterSceneBox2D extends BaseScene{
    private Box2DDebugRenderer debugRenderer;

    public ThrustCopterSceneBox2D(ThrustCopter thrustCopter) {
        super(thrustCopter);
    }
}
