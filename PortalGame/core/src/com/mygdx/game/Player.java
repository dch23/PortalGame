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
    private float frictionMagnitude = 0.1f;
    private Vector2 inputHoriz = Vector2.Zero;

    private float groundDistance = 0f;
    private float groundDistanceJumpThreshold = 0.2f;

    public Player(World world, String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(world, name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);
        this.body.setFixedRotation(true);
    }

    private boolean onGround() {
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                System.out.println(Entity.entityFromBody(fixture.getBody()).getName());
                groundDistance = body.getPosition().y - point.y;
                return 0;
            }
        };
        Vector2 bottom = body.getPosition();
//        Vector2 bottom = new Vector2(body.getPosition().x, body.getPosition().y - size.y/2f);
        Vector2 endRay = new Vector2(bottom);
        endRay.add(0,-100f);

        world.rayCast(callback, bottom, endRay);

        return (groundDistance < groundDistanceJumpThreshold);
    }

    private void control() {
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean keyDown(int keyCode) {
                switch (keyCode) {
                    case Inputs.Keys.KEY_RIGHT:
                    case Inputs.Keys.ARROW_RIGHT:
                        inputHoriz.x = 1f;
                        break;
                    case Inputs.Keys.KEY_LEFT:
                    case Inputs.Keys.ARROW_LEFT:
                        inputHoriz.y = 1f;
                        break;
                    case Inputs.Keys.KEY_UP:
                    case Inputs.Keys.ARROW_UP:
                        if (onGround()) {
                            body.setLinearVelocity(body.getLinearVelocity().x, jumpHeight);
                        }
                        break;
                    case Inputs.Keys.ARROW_DOWN:
                        break;
                }
                return true;
            }
            public boolean keyUp(int keyCode) {
                switch (keyCode) {
                    case Inputs.Keys.KEY_RIGHT:
                    case Inputs.Keys.ARROW_RIGHT:
                        inputHoriz.x = 0f;
//                        body.setLinearVelocity(slowDownSpeed, body.getLinearVelocity().y);
                        break;
                    case Inputs.Keys.KEY_LEFT:
                    case Inputs.Keys.ARROW_LEFT:
                        inputHoriz.y = 0f;
//                        body.setLinearVelocity(-slowDownSpeed, body.getLinearVelocity().y);
                        break;
                    case Inputs.Keys.KEY_UP:
                    case Inputs.Keys.ARROW_UP:
                        break;
                    case Inputs.Keys.ARROW_DOWN:
                        break;
                }
                return true;
            }
        });
        if (inputHoriz.x - inputHoriz.y != 0) {
            body.setLinearVelocity(Math.max(speed, Math.abs(body.getLinearVelocity().x)) * (inputHoriz.x - inputHoriz.y), body.getLinearVelocity().y);
        }
    }

    private void airResistance() {  // Doesnt work
//        Vector2 position = new Vector2(body.getPosition());
//        Vector2 velocity = new Vector2(body.getLinearVelocity());
//        Vector2 direction = new Vector2(velocity.nor());
//
//
//        direction = new Vector2(-direction.x, -direction.y);
//        Vector2 airResistanceVector = new Vector2(direction.x * airResistanceMagnitude, direction.y * airResistanceMagnitude);
//        if (PMath.magnitude(velocity) - airResistanceMagnitude > 0f) {
//            body.setLinearVelocity(new Vector2(velocity.x - airResistanceVector.x, velocity.y - airResistanceVector.y));
//        }

//        float magnitude = PMath.magnitude(velocity);
//
//        if (magnitude - airResistanceMagnitude > 0f) {
////            velocity = velocity.add(airResistanceVector);
//        }
//        body.setLinearVelocity(velocity);

//        direction = direction.nor();
//        direction = Vector2.Zero.mulAdd(direction, -airResistanceMagnitude);
//
//        Vector2 newPosition = new Vector2(body.getPosition().add(body.getLinearVelocity()).add(direction));
//        Vector2 newDirection = new Vector2(Vector2.Zero.mulAdd(body.getPosition(), -1f));
//        if (direction == newDirection) System.out.println("PROLLY WORK");
    }

    private void friction () {
        float xVelocity = body.getLinearVelocity().x;
        float direction = xVelocity / Math.abs(xVelocity);
        float newXVelocity = (Math.abs(xVelocity) - frictionMagnitude) * direction;
        float newDirection = newXVelocity / Math.abs(newXVelocity);
        if (direction == newDirection) {
            body.setLinearVelocity(newXVelocity, body.getLinearVelocity().y);
        }
        else {
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }
    }

    public void operate() {
//        this.body.setLinearVelocity(new Vector2(-0.1f,this.body.getLinearVelocity().y));
        control();
        friction();
//        airResistance();
    }
}
