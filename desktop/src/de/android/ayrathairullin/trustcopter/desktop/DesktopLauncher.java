package de.android.ayrathairullin.trustcopter.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import de.android.ayrathairullin.trustcopter.ThrustCopter;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.useGL30 = false;
		config.title = "Thrust Copter";
		config.width = 800;
		config.height = 480;
		new LwjglApplication(new ThrustCopter(), config);
	}
}
