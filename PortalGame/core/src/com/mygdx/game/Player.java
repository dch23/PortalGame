package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class Player extends Entity {

    public Player(World world, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(world, position, size, bodyType, color, density, friction, gravityEnabled, sprite);
        this.body.setFixedRotation(true);
    }

    public void operate() {
//        this.body.setLinearVelocity(new Vector2(-0.1f,this.body.getLinearVelocity().y));
    }
}
