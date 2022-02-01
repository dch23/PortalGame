package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

import java.util.ArrayList;

public class ChargeEnemyEntity extends EnemyEntity {
    static public ArrayList<ChargeEnemyEntity> chargeEnemyEntities = new ArrayList<>();
    public static Vector2 regularSize = new Vector2(0.3f,0.4f);

    int wanderDirection = 1;
    float initialSpeed = 0.6f;
    float doubleSpeed = 2f;

    float maxRayDistance = 100;



    public ChargeEnemyEntity(String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);
        this.speed = initialSpeed;
        animationTextureSizeScale = 4f;
        addAnimation("Walk", "Characters/NightBorne/NightBorne_run.gif", 6, true, 0.16f);
        addAnimation("Run", "Characters/NightBorne/NightBorne_run.gif", 6, true, 0.3f);
        addAnimation("Death", "Characters/NightBorne/NightBorne_death..gif", 0, false, 0.3f);

        currentAnimation = "Walk";

        chargeEnemyEntities.add(this);
        sounds.put("AngryScream", Gdx.audio.newSound(Gdx.files.internal("Characters/imp_axe_demon/imp_axe_demon/demon_axe_red/sounds/angryenemy.mp3")));
    }


    public static Vector2 getRegularSize() {
        return regularSize;
    }

    private boolean findEnemy() {
        int xDirection = getBody().getLinearVelocity().x == 0 ? 1
                : (int)(this.body.getLinearVelocity().x/Math.abs(this.body.getLinearVelocity().x));
        RayHitInfo sightRay = PMath.getClosestRayHitInfo(world, getPosition(), new Vector2(xDirection*100,0), maxRayDistance, false);
        if (sightRay == null) return false;
        Entity entity = Entity.entityFromBody(sightRay.fixture.getBody());
        String sight = entity.getName();

        return sight.equals("Player");

    }
    public static void operate() {
        // delete dead dudes
        for (int i=chargeEnemyEntities.size()-1; i>=0; i--) {
            ChargeEnemyEntity enemy = chargeEnemyEntities.get(i);
            if (!enemy.alive) {
                enemy.die();
                if (enemy.alpha == 0) chargeEnemyEntities.remove(i);
            }
        }

        for (ChargeEnemyEntity enemy : chargeEnemyEntities) {
            if (enemy.getBody() == null) continue;
            if (!enemy.alive) continue;
            if (enemy.hitWall()) {
                System.out.println("AT WALl");
                enemy.wanderDirection *= -1;
                enemy.speed = enemy.initialSpeed;
                enemy.currentAnimation = "Walk";
            }
            if (enemy.findEnemy()) {
                enemy.speed = enemy.doubleSpeed;
                enemy.currentAnimation = "Run";
            }
            enemy.body.setLinearVelocity(enemy.speed * enemy.wanderDirection, enemy.body.getLinearVelocity().y);
            enemy.horizontalFaceDirection = enemy.wanderDirection;

            // reflection
            enemy.updateReflection(Player.player.portals);
        }
    }
}
