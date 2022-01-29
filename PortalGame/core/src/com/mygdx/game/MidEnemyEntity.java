package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class MidEnemyEntity extends EnemyEntity{
    static public ArrayList<MidEnemyEntity> midEnemyEntities = new ArrayList<>();
    public static Vector2 regularSize = new Vector2(0.3f,0.5f);

    float closeEnoughCollisionRange = 0.02f;
    int wanderDirection = 1;
    float initialSpeed = 0.5f;
    float doubleSpeed = 1f;
    ArrayList<RayHitInfo> raysHitInfo;
    RayHitInfo closestRayHitInfo;

    float maxRayDistance = 100;



    public MidEnemyEntity(String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);
        this.speed = initialSpeed;
        animationTextureSizeScale = 3f;
        addAnimation("Walk", "Characters/imp_axe_demon/imp_axe_demon/demon_axe_red/ezgif.com-gif-maker.gif", 6, true, 0.16f);
        addAnimation("Run", "Characters/imp_axe_demon/imp_axe_demon/demon_axe_red/axe_demon_run.gif", 6, true, 0.3f);
        currentAnimation = "Walk";

        midEnemyEntities.add(this);
    }

    public static Vector2 getRegularSize() {
        return regularSize;
    }

    private boolean seeEnemy() {
        int xDirection = getBody().getLinearVelocity().x == 0 ? 1
                : (int)(this.body.getLinearVelocity().x/Math.abs(this.body.getLinearVelocity().x));
        RayHitInfo sightRay = PMath.getClosestRayHitInfo(world, getPosition(), new Vector2(xDirection*100,0), maxRayDistance, false);
        if (sightRay == null) return false;
        Entity entity = Entity.entityFromBody(sightRay.fixture.getBody());
        String sight = entity.getName();

        return sight.equals("Player");

    }
    public static void operate() {
        for (MidEnemyEntity enemy : midEnemyEntities) {
            if (enemy.getBody() == null) return;
            if (enemy.hitWall()) {
                enemy.wanderDirection *= -1;
                enemy.speed = enemy.initialSpeed;
                enemy.currentAnimation = "Walk";
            }
            if (enemy.seeEnemy()) {
                enemy.speed = enemy.doubleSpeed;
                enemy.currentAnimation = "Run";
            }
            enemy.body.setLinearVelocity(enemy.speed * enemy.wanderDirection, enemy.body.getLinearVelocity().y);
            enemy.horizontalFaceDirection = enemy.wanderDirection;

            // reflection
            enemy.updateReflection(((Player) Entity.entityFromName("Player")).portals);
        }
    }
}
