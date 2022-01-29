package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

import java.util.ArrayList;

public class Fireball extends Entity {
    private static ArrayList<Fireball> fireballs = new ArrayList<>();

    public Fireball(String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);
        fireballs.add(this);
    }


    public static void operate() {
        for (Fireball ball : fireballs) {
            
        }
    }
}
