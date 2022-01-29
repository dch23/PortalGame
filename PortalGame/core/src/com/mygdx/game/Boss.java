package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

public class Boss extends EnemyEntity {
    // static
    static Boss boss;
    private static Vector2 regularSize = new Vector2(0.2f,0.55f);

    // Boss-Player interaction
    public boolean touchedPlayer = false;
    private float pushMagnitude = 10f;



    public Boss(String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);

        addAnimation("Idle", "Characters/Evil Wizard/Sprites/Idle.png", 8, true, 0.3f);
        currentAnimation = "Idle";
        animationTextureSizeScale = 3;
        horizontalFaceDirection = -1;

        getBody().getFixtureList().first().setSensor(true);

        boss = this;
    }

    public static void operate() {
        if (boss == null) return;
       if (boss.touchedPlayer) {
           boss.pushPlayer();
           boss.touchedPlayer = false;
           System.out.println("hit");
       }
    }

    public static Vector2 getRegularSize() {
        return regularSize;
    }

    public void pushPlayer() {
        Vector2 bossPos = getPosition(), playerPos = Player.player.getPosition();
        Vector2 direction = PMath.normalizeVector2(PMath.subVector2(playerPos, bossPos));

        Vector2 forceVector = PMath.multVector2(direction, pushMagnitude);
        Player.player.getBody().setLinearVelocity(forceVector);
    }
}
