package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Renderer {
    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    private SpriteBatch spriteBatch;
    public Renderer(OrthographicCamera camera) {
        shapeRenderer.setProjectionMatrix(camera.combined);
    }
    public Renderer(SpriteBatch batch) {
        this.spriteBatch = batch;
    }

    public SpriteBatch getBatch() {
        return spriteBatch;
    }

    public void renderSprite (Sprite sprite, Vector2 position, Vector2 size, Vector2 offset, float degrees) {
        //check
        if (sprite == null || position == null) return;

        // Set sprite ready to draw
//        sprite.setOriginCenter();
        sprite.setSize(size.x,size.y);

        sprite.setPosition(position.x-offset.x, position.y-offset.y);
        sprite.setOriginCenter();
        sprite.setRotation(degrees);



        sprite.draw(this.spriteBatch);


        // Reset sprite for conformity
//        sprite.setPosition(0,0);
//        sprite.setRotation(0);
    }

    public void debugLine(Vector2 start, Vector2 end, Color color) {
        this.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        this.shapeRenderer.setColor(color);
        this.shapeRenderer.rectLine(start, end, 0.01f);
        this.shapeRenderer.end();
    }
}
