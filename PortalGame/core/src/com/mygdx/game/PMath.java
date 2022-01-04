package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

public class PMath {
    static public float magnitude(Vector2 v) {
        float x = Math.abs(v.x);
        float y = Math.abs(v.y);
        if (x + y == 0f) return 0;
        return (float) Math.sqrt((double) (x*x + y*y));
    }
    static public Vector2 multVector2(Vector2 a, float scale) {
        return new Vector2(a.x * scale, a.y * scale);
    }
    static public Vector2 multVector2(Vector2 a, Vector2 b) {
        return new Vector2(a.x * b.x, a.y * b.y);
    }
    static public Vector2 addVector2(Vector2 a, Vector2 b) {
        return new Vector2(a.x + b.x, a.y + b.y);
    }
    static public Vector2 subVector2(Vector2 a, Vector2 b) {
        return new Vector2(a.x - b.x, a.y - b.y);
    }
    static public Vector2 divideVector2(Vector2 a, float scale) {
        return new Vector2(a.x/scale, a.y/scale);
    }
    static public Vector2 normalizeVector2(Vector2 v) {
        return divideVector2(v, magnitude(v));
    }

    static public Vector2 absoluteVector2(Vector2 v) {
        return new Vector2(Math.abs(v.x), Math.abs(v.y));
    }

    static public float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

}


