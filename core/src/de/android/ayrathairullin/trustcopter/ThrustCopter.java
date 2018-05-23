package de.android.ayrathairullin.trustcopter;


import com.badlogic.gdx.Game;

public class ThrustCopter extends Game {
    @Override
    public void create() {
        setScreen(new ThrustCopterScene());
    }
}
