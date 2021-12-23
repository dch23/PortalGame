package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;

public class RayHitInfo {
    public Fixture fixture;
    public Vector2 point;
    public Vector2 normal;
    public float fraction;

    public RayHitInfo(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
        this.fixture = fixture;
        this.point = point;
        this.normal = normal;
        this.fraction = fraction;
    }

    public void print() {
        System.out.println("Info of " + this);
        System.out.println("\tFixture: " + fixture);
        System.out.println("\tPoint: " + point);
        System.out.println("\tNormal: " + normal);
        System.out.println("\tFraction: " + fraction);
        System.out.println();
    }
}
