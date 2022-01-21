package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

import java.awt.*;
import java.util.ArrayList;

public class Renderer {
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private SpriteBatch spriteBatch;
    private ArrayList<ArrayList<RenderEntity>> renderLayers = new ArrayList<>(3);

    public Renderer(OrthographicCamera camera) {
        shapeRenderer.setProjectionMatrix(camera.combined);
    }
    public Renderer(SpriteBatch batch) {
        this.spriteBatch = batch;

        // initialize renderLayers
        for (int i = 0; i < 3; ++i) renderLayers.add(new ArrayList<RenderEntity>());
    }

    public void addToRenderLayer(int index, Entity entity) {
//        System.out.println(renderLayers.size());
        if (index < 0 || index >= renderLayers.size()) return;

        RenderEntity renderEntity = new RenderEntity(this.spriteBatch, entity);
        renderLayers.get(index).add(renderEntity);
//        System.out.println(renderLayers.get(index).size());
    }

    public void beginRender() {
        this.spriteBatch.begin();
    }
    public void endRender() {
        this.spriteBatch.end();
    }

    public void render() {
        for (ArrayList<RenderEntity> layer : renderLayers) {
            for (RenderEntity renderEntity : layer) {
                renderEntity.render();
            }
        }
    }


    public SpriteBatch getBatch() {
        return spriteBatch;
    }

    public void renderSprite (Sprite sprite, Vector2 position, Vector2 size, Vector2 offset, float degrees) {
        //check
        if (sprite == null || position == null) return;

        // Set sprite ready to draw
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

class RenderEntity {
    private SpriteBatch spriteBatch;
    public Entity entity;

    public RenderEntity(SpriteBatch spriteBatch, Entity entity) {
        this.spriteBatch = spriteBatch;
        this.entity = entity;
    }

    public void render() {



        if (this.spriteBatch == null) {
//            System.out.println("Cannot render item");
            return;
        }
        if (entity.getSprite() != null) {
            // render entity
            Vector2 offset = PMath.divideVector2(this.entity.size, 2f);
            this.entity.sprite.setSize(this.entity.size.x, this.entity.size.y);
            this.entity.sprite.setPosition(this.entity.getPosition().x - offset.x, this.entity.getPosition().y - offset.y);
            this.entity.sprite.setOriginCenter();
            this.entity.sprite.setRotation(this.entity.getBody().getAngle());
            this.entity.sprite.draw(this.spriteBatch);

            if (this.entity.reflectEntity == null) return;
            if (this.entity.reflectEntity.sprite == null) return;

            // render reflect entity
            offset = PMath.divideVector2(this.entity.reflectEntity.size, 2f);
            this.entity.reflectEntity.sprite.setSize(this.entity.reflectEntity.size.x, this.entity.reflectEntity.size.y);
            this.entity.reflectEntity.sprite.setPosition(this.entity.reflectEntity.getPosition().x - offset.x,
                    this.entity.reflectEntity.getPosition().y - offset.y);
            this.entity.reflectEntity.sprite.setOriginCenter();
            this.entity.reflectEntity.sprite.setRotation(this.entity.reflectEntity.getBody().getAngle());
            this.entity.reflectEntity.sprite.draw(this.spriteBatch);
        }

        // animation
        if (this.entity.currentAnimation != null) {
            AnimationManager.playAnimation(this.entity, spriteBatch, this.entity.currentAnimation, entity.animationTextureSizeScale);
            if (this.entity.reflectEntity != null) {
                AnimationManager.playAnimation(this.entity.reflectEntity, spriteBatch,
                        this.entity.getAnimation(this.entity.currentAnimation), entity.animationTextureSizeScale);
            }
        }
    }
}
