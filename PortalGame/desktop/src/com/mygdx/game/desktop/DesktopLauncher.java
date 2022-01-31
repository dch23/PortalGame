package com.mygdx.game.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.MyGdxGame;

import java.awt.*;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		// changing config information
		config.title = "Portal Game";
//		config.width = 1920;
//		config.height = 1080;
//		config.fullscreen = true;
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		config.width = size.width;
		config.height = size.height;

//		config.width = Gdx.graphics.getWidth();
//		config.height = Gdx.graphics.getHeight();

		new LwjglApplication(new MyGdxGame(size.width, size.height), config);
	}
}
