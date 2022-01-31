package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.HashMap;

public class WeakEnemyEntity extends EnemyEntity {
    static public ArrayList<WeakEnemyEntity> weakEnemyEntities = new ArrayList<>();
    static private Vector2 regularSize = new Vector2(0.2f,0.35f);
    int wanderDirection = 1;

    public WeakEnemyEntity(String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);
        animationTextureSizeScale = 3f;
        addAnimation("Walk", "Characters/imp_axe_demon/imp_axe_demon/redImpWalk.gif", 6, true, 0.25f);

        weakEnemyEntities.add(this);
        //sounds
        sounds.put("EnemyGrowl", Gdx.audio.newSound(Gdx.files.internal("Characters/imp_axe_demon/imp_axe_demon/imp_red/sounds/enemygrowl.mp3")));
    }

    public static Vector2 getRegularSize() {
        return regularSize;
    }

    static public void operate() {
        for (WeakEnemyEntity enemy : weakEnemyEntities) {
            if(enemy.hitWall()) {
                enemy.wanderDirection *= -1;
            }
            enemy.body.setLinearVelocity(enemy.speed * enemy.wanderDirection, enemy.body.getLinearVelocity().y);

            // animate
            enemy.currentAnimation = "Walk";
            enemy.horizontalFaceDirection = enemy.wanderDirection;
            enemy.updateReflection(Player.player.portals);

        }
    }
}