package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class MenuButton {
    static MenuButton play, quit;
    static Sound clickSound = Gdx.audio.newSound(Gdx.files.internal("music/click.mp3"));
    private static float volume = 1;

    static InputAdapter input = new InputAdapter() {


        @Override
        public boolean touchDown(int x, int y, int pointer, int button) {
            if (play == null || quit == null) return true;

            Vector2 point = new Vector2(x*MyGdxGame.GAME_SCALE, (MyGdxGame.SCENE_HEIGHT-y) * MyGdxGame.GAME_SCALE);

            boolean playInBounds = PMath.inBounds(point, play.bounds[0], play.bounds[1]),
                    quitInBounds = PMath.inBounds(point, quit.bounds[0], quit.bounds[1]);
            if (playInBounds) {
                AudioManager.playSound(clickSound, volume, false, true);
                MyGdxGame.changeLevel(MyGdxGame.currentLevel+1);
            }
            else if (quitInBounds) {
//                System.out.println("quit!");
                AudioManager.playSound(clickSound, volume, false, true);
                Gdx.app.exit();
            }

            return true;
        }
        public boolean mouseMoved(int x, int y) {
            return true;
        }
    };

    String name;
    Vector2[] bounds;

    static void setup() {
        Gdx.input.setInputProcessor(input);
    }

    public MenuButton(String name, Vector2 a, Vector2 b) {
        this.name = name;
        bounds = new Vector2[] {a, b};

//        System.out.println("name: " + name);
        switch (name) {
            case "play":
                play = this;
//                System.out.println("SET PLAY BUTTON");
                break;
            case "quit":
                quit = this;
                break;
        }
    }
}
