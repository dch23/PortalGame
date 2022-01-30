package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

import java.util.ArrayList;

public class Fireball extends Entity {
    private static ArrayList<Fireball> fireballs = new ArrayList<>();

    public Fireball(Vector2 position, Vector2 size, Vector2 direction) {
        super("fireball", position, size, BodyDef.BodyType.DynamicBody, null, 10, 0.1f, false, null);


        fireballs.add(this);
    }

    public static void operate() {
        for (Fireball ball : fireballs) {
        
        }
    }

    public static void disposeAll() {
        fireballs = new ArrayList<>();
    }
}
