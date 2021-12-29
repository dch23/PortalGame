package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class ReflectEntity extends Entity {
    public Portal exitPortal;

    public ReflectEntity(World world, String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(world, name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);
    }


//    public ReflectEntity(World world, String name, Vector2 position, Vector2 size, Color color, float density, float friction, Sprite sprite) {
//        super(world, name, position, size, BodyDef.BodyType.DynamicBody, color, density, friction, true, sprite);
//    }

    public void updatePosition() {
//        body.getPosition().x = 1;
//        body.getPosition().y = 1;
    }

    public void render(Renderer renderer, Sprite sprite) {
//        Vector2 position;
//        renderer.renderSprite(this.sprite, position, this.size, new Vector2(this.size.x/2f, this.size.y/2f), this.angle);
    }
}
