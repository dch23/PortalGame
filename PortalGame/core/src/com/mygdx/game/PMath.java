package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

public class PMath {
    static public float magnitude(Vector2 v) {
        float x = Math.abs(v.x);
        float y = Math.abs(v.y);
        if (x + y == 0f) return 0;
        return (float) Math.sqrt((double) (x*x + y*y));
    }
}
