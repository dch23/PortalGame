package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Renderer {
    private SpriteBatch spriteBatch;

    public Renderer(SpriteBatch batch) {
        this.spriteBatch = batch;
    }

    public SpriteBatch getBatch() {
        return spriteBatch;
    }

    public void renderSprite (Sprite sprite, Vector2 position, Vector2 size, Vector2 offset, float degrees) {
        // Set sprite ready to draw
//        sprite.setOriginCenter();
        sprite.setSize(size.x,size.y);

        sprite.setPosition(position.x-offset.x, position.y-offset.y);
        sprite.setOrigin(0,0);
        sprite.setRotation(degrees);



        sprite.draw(this.spriteBatch);


        // Reset sprite for conformity
//        sprite.setPosition(0,0);
//        sprite.setRotation(0);
    }
}
