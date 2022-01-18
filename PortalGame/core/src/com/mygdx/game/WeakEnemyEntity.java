package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;

public class WeakEnemyEntity extends EnemyEntity {
    float closeEnoughCollisionRange = 0.02f;
    int wanderDirection = 1;
    ArrayList<RayHitInfo> raysHitInfo;
    RayHitInfo closestRayHitInfo;

    float maxRayDistance = 100;

    public WeakEnemyEntity(World world, String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(world, name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);
    }
    static public void initialize(World world){
//        world.setContactListener(new WeakEnemyCollisionListener());
    }
    private boolean hitWall() {
        raysHitInfo = new ArrayList<>();            // refresh the rays information list
        closestRayHitInfo = null;                   // reset the closest ray to nothing

        // shooting a ray is done by ray callbacks, read about rays on libgdx docs, learn about Vector2 normal, most likely dont need to know about fraction variable
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture == null || point == null || normal == null) return 0;
                // Multiple hits
                raysHitInfo.add(new RayHitInfo(fixture, new Vector2(point), new Vector2(normal), fraction));
                return 1;
            }
        };

        // look at the world.rayCast function on the libgdx docs and see what parameters you must provide
        int xDirection = (int)(this.body.getLinearVelocity().x/Math.abs(this.body.getLinearVelocity().x));
        world.rayCast(callback, this.body.getPosition(), new Vector2(maxRayDistance*xDirection, this.body.getPosition().y));

        // Finding the closest ray hit through a searching algorithm
        if (raysHitInfo != null) {
            if (raysHitInfo.size() == 0) return false;
            for (RayHitInfo rayHitInfo : raysHitInfo) {
                if (!rayHitInfo.fixture.isSensor()) if (closestRayHitInfo == null) closestRayHitInfo = rayHitInfo;
                if (closestRayHitInfo == null) continue;

                float distance1 = PMath.magnitude(PMath.subVector2(closestRayHitInfo.point, this.body.getPosition()));
                float distance2 = PMath.magnitude(PMath.subVector2(rayHitInfo.point, this.body.getPosition()));
                if (distance2 < distance1 && !rayHitInfo.fixture.isSensor()) {
                    closestRayHitInfo = rayHitInfo;
                }
            }
        }
        if (closestRayHitInfo == null) return false;
        float distanceFromWall = PMath.magnitude(PMath.subVector2(closestRayHitInfo.point, this.body.getPosition())) - this.size.x/2f;
        return distanceFromWall < closeEnoughCollisionRange;
    }


    public void operate() {
        if(hitWall()) {
            wanderDirection *= -1;
        }
        this.body.setLinearVelocity(this.speed * wanderDirection, this.body.getLinearVelocity().y);
    }
}