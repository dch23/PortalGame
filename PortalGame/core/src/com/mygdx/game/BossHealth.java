package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.bullet.collision._btMprSupport_t;



public class BossHealth {
    static int maxHealth = 100;
    static private int health = maxHealth;
    static Sprite sprite = new Sprite(new Texture("sprites/bossBar.png"));
    static Renderer renderer;

    private static float sizeScale = 0.003f;
    private static Vector2 size, barSize;
    private static Vector2 position, barPos;
    private static Vector2 offset = new Vector2(0,0);
    private static float length;

    private static Color barColor = new Color(207/255f, 8/255f, 8/255f, 1f);

    static void set() {
        renderer = MyGdxGame.entityRenderer;

        size = new Vector2(sprite.getWidth() * sizeScale, sprite.getHeight() * sizeScale);
        position = new Vector2(MyGdxGame.gameBounds.x/2f - size.x/2f, MyGdxGame.gameBounds.y - size.y/2f - 0.16f);
        length = size.x;

//        barPos = PMath.subVector2(position, PMath.divideVector2(size, 2f));
        barPos = new Vector2(position);
        barSize = new Vector2(size.x, size.y/2f + 0.03f);
    }

    static private void renderBar() {
        barSize.x = length;
        renderer.renderRectangle(barPos, barSize, barColor);

//        renderer.renderRectangle(new Vector2(0,0), new Vector2(1,1), Color.RED);
//        System.out.println(barPos + " " + barSize);
    }

    static public void damageBoss(int damage) {
        if (Boss.boss == null) return;
        if (!Boss.boss.alive) return;

        health = (health - damage > 0 ? health - damage : 0 );
        length = ((float) health / (float) maxHealth) * (size.x);
        if (health <= 0) {
            Boss.boss.alive = false;
            return;
        }

    }

    static public void render() {
        if (MyGdxGame.currentLevel != MyGdxGame.maps.size()-1) return;
        renderBar();
        renderer.renderSprite(sprite, position, size, offset, 0);

    }

    public static void resetHealth() {
        health = maxHealth;
    }
}
