package com.decoder.jacky.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.decoder.jacky.AzurLaneSpineCharacterDecoder;

public class DesktopLauncher {
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new AzurLaneSpineCharacterDecoder(args), config);
	}
}
