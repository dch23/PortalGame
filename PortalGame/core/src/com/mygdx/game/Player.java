package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class Player extends Entity {
    private float speed = 0.7f;
    private float slowDownSpeed = 0.1f;
    private float jumpHeight = 3f;
    private float inputHoriz = 0f;

    public Player(World world, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(world, position, size, bodyType, color, density, friction, gravityEnabled, sprite);
        this.body.setFixedRotation(true);
    }

    private void control() {
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean keyDown(int keyCode) {
                switch (keyCode) {
                    case Inputs.Keys.RIGHT:
                        inputHoriz = 1f;
                        break;
                    case Inputs.Keys.LEFT:
                        inputHoriz = -1f;
                        break;
                    case Inputs.Keys.UP:
                        body.setLinearVelocity(body.getLinearVelocity().x, jumpHeight);
                        break;
                    case Inputs.Keys.DOWN:
                        break;
                }
                return true;
            }
            public boolean keyUp(int keyCode) {
                switch (keyCode) {
                    case Inputs.Keys.RIGHT:
                        inputHoriz = 0f;
                        body.setLinearVelocity(slowDownSpeed, body.getLinearVelocity().y);
                        break;
                    case Inputs.Keys.LEFT:
                        inputHoriz = 0f;
                        body.setLinearVelocity(-slowDownSpeed, body.getLinearVelocity().y);
                        break;
                    case Inputs.Keys.UP:
                        break;
                    case Inputs.Keys.DOWN:
                        break;
                }
                return true;
            }
        });
        if (inputHoriz != 0) {
            body.setLinearVelocity(speed*inputHoriz, body.getLinearVelocity().y);
        }
    }

    public void operate() {
//        this.body.setLinearVelocity(new Vector2(-0.1f,this.body.getLinearVelocity().y));
        control();
    }
}
