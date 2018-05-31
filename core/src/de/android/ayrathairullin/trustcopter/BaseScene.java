package de.android.ayrathairullin.trustcopter;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;

import static com.badlogic.gdx.Input.Keys;

public class BaseScene extends ScreenAdapter{
    protected ThrustCopter game;
    private boolean keyHandled;

    public BaseScene (ThrustCopter thrustCopter) {
        game = thrustCopter;
        keyHandled = false;
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (Gdx.input.isKeyPressed(Keys.BACK)) {
            if (keyHandled) {
                return;
            }
            handleBackPress();
            keyHandled = true;
        }else {
            keyHandled = false;
        }
    }

    protected void handleBackPress() {
        System.out.println("back");
    }
}
