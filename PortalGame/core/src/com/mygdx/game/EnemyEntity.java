package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;
import java.util.HashMap;

public class EnemyEntity extends Entity {
    static private ArrayList<EnemyEntity> enemies = new ArrayList<>();

    static private final HashMap<String, Boolean> ignoreMap = new HashMap<String, Boolean>() {{
        put("portal collider", true);
    }};


    protected float closeEnoughCollisionRange = 0.02f;

    protected float maxRayDistance = 100;
    protected float speed = 1.0f;
    protected float frictionMagnitude = 0.6f;


    // death vars
    protected float alpha = 1;
    private float fadeSpeed = 0.02f;

    private float groundDistance = 0f;

    public EnemyEntity(String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);
        this.body.setFixedRotation(true);
        enemies.add(this);
    }

    private boolean onGround() {
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                groundDistance = body.getPosition().y - point.y;
                return 0;
            }
        };
        Vector2 bottom = body.getPosition();
//        Vector2 bottom = new Vector2(body.getPosition().x, body.getPosition().y - size.y/2f);
        Vector2 endRay = new Vector2(bottom);
        endRay.add(0, -100f);

        world.rayCast(callback, bottom, endRay);
        return true;
    }

    protected boolean hitWall() {
        if (this.body.getLinearVelocity().x == 0) return false;
        int xDirection = (int)(this.body.getLinearVelocity().x/Math.abs(this.body.getLinearVelocity().x));

        RayHitInfo[] rays = new RayHitInfo[] {
                PMath.getClosestRayHitInfo(world, getPosition(), new Vector2(xDirection, 0), maxRayDistance, true, ignoreMap),
                PMath.getClosestRayHitInfo(world, PMath.addVector2(getPosition(), new Vector2(0, getSize().y/2f)), new Vector2(xDirection, 0), maxRayDistance, true, ignoreMap),
                PMath.getClosestRayHitInfo(world, PMath.addVector2(getPosition(), new Vector2(0, -getSize().y/2f)), new Vector2(xDirection, 0), maxRayDistance, true, ignoreMap)
        };
        RayHitInfo closestRayHitInfo = null;
        for (RayHitInfo ray : rays) {
            if (ray == null) continue;
            if (closestRayHitInfo == null) closestRayHitInfo = ray;
            else {
                float distance1 = PMath.magnitude(PMath.subVector2(closestRayHitInfo.point, getPosition()));
                float distance2 = PMath.magnitude(PMath.subVector2(ray.point, getPosition()));
                if (distance2 < distance1) closestRayHitInfo = ray;
            }
        }

        if (closestRayHitInfo == null) return false;
        float distanceFromWall = PMath.magnitude(PMath.subVector2(closestRayHitInfo.point, this.body.getPosition())) - this.size.x/2f;
        return distanceFromWall < closeEnoughCollisionRange;
    }


    protected void die() {
        if (alpha <= 0) {
            dispose();
            alpha = 0;
        }
        else alpha -= fadeSpeed;
        currentAnimation = "Death";
    }
}

