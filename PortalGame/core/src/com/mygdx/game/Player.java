package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

public class Player extends Entity {
    private float speed = 1f;
    private float slowDownSpeed = 0.1f;
    private float jumpHeight = 3f;
    private float airResistanceMagnitude = 0.001f;
    private Vector2 inputHoriz = Vector2.Zero;

    public Player(World world, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(world, position, size, bodyType, color, density, friction, gravityEnabled, sprite);
        this.body.setFixedRotation(true);
    }

    private boolean onGround() {
        Vector2 bottom = new Vector2(body.getPosition().x, body.getPosition().y - size.y/2f);
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                return 0;
            }
        };
        world.rayCast(callback, bottom, bottom.add(0f, -100));

        return false;
    }

    private void control() {
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean keyDown(int keyCode) {
                switch (keyCode) {
                    case Inputs.Keys.RIGHT:
                        inputHoriz.x = 1f;
                        break;
                    case Inputs.Keys.LEFT:
                        inputHoriz.y = 1f;
                        break;
                    case Inputs.Keys.UP:
                        if (onGround()) {
                            body.setLinearVelocity(body.getLinearVelocity().x, jumpHeight);
                        }
                        break;
                    case Inputs.Keys.DOWN:
                        break;
                }
                return true;
            }
            public boolean keyUp(int keyCode) {
                switch (keyCode) {
                    case Inputs.Keys.RIGHT:
                        inputHoriz.x = 0f;
//                        body.setLinearVelocity(slowDownSpeed, body.getLinearVelocity().y);
                        break;
                    case Inputs.Keys.LEFT:
                        inputHoriz.y = 0f;
//                        body.setLinearVelocity(-slowDownSpeed, body.getLinearVelocity().y);
                        break;
                    case Inputs.Keys.UP:
                        break;
                    case Inputs.Keys.DOWN:
                        break;
                }
                return true;
            }
        });
        if (inputHoriz.x - inputHoriz.y != 0) {
            body.setLinearVelocity(speed * (inputHoriz.x - inputHoriz.y), body.getLinearVelocity().y);
        }
    }

    private void airResistance() {  // Doesnt work
        Vector2 direction = body.getLinearVelocity();
        direction = direction.nor();
        System.out.println(direction);
        direction = Vector2.Zero.mulAdd(direction, -airResistanceMagnitude);
        body.applyForceToCenter(direction, false);
    }

    public void operate() {
//        this.body.setLinearVelocity(new Vector2(-0.1f,this.body.getLinearVelocity().y));
        control();
    }
}
