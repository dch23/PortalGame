package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;


public class BossEntity extends EnemyEntity {
    static public ArrayList<BossEntity> BossEntities = new ArrayList<>();
    ArrayList<RayHitInfo> raysHitInfo;
    RayHitInfo closestRayHitInfo;
    float closeEnoughCollisionRange = 0.02f;
    int wanderdirection = 1;
    float maxRayDistance = 100;

    public BossEntity(String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);
//        animationTextureSizeScale = 4f;
//        addAnimation("Move", );

    }

    public boolean hitWall() {
        raysHitInfo = new ArrayList<>();            // refresh the rays information list
        closestRayHitInfo = null;                   // reset the closest ray to nothing
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
        int xDirection = (int) (this.body.getLinearVelocity().x / Math.abs(this.body.getLinearVelocity().x));
        world.rayCast(callback, this.body.getPosition(), new Vector2(maxRayDistance * xDirection, this.body.getPosition().y));

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
        {
            float distanceFromWall = PMath.magnitude(PMath.subVector2(closestRayHitInfo.point, this.body.getPosition())) - this.size.x / 2f;
            return distanceFromWall < closeEnoughCollisionRange;
        }

        static public void operate() {
            for (BossEntity enemy : BossEntity) {
                if(enemy.hitWall()) {
                    enemy.wanderdirection *= -1;
                }
    }
}
