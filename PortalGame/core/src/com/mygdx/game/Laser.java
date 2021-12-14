package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

public class Laser {
    static ShapeRenderer shapeRenderer = new ShapeRenderer();

    private World world;
    private Vector2 position;
    private Vector2 endPosition;
    private Vector2 maxPosition;
    private Color color;
    private float thickness;
    private float maxLength;

    static public void setProjectionMatrix(Matrix4 matrix4) {
        shapeRenderer.setProjectionMatrix(matrix4);
    }

    public Laser(World world, Vector2 position, Color color, float angle, float thickness, float maxLength) {
        this.world = world;
        this.position = position;
        this.maxPosition = position.cpy().add(new Vector2((float) Math.cos(angle), (float) Math.sin(angle)).scl(maxLength));
        this.endPosition = this.maxPosition;
        this.color = color;
        this.thickness = thickness;
        this.maxLength = maxLength;
    }

    static public void beginRender() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }
    static public void endRender() {
        shapeRenderer.end();
    }

    public void render() {
        shapeRenderer.setColor(this.color);
        shapeRenderer.rectLine(this.position, getLaserEnd(), this.thickness);
    }

    private Vector2 getLaserEnd() {
        // Made final array because the @Override only accepts this
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                endPosition = point;
                return 0;
            }
        };
        world.rayCast(callback, this.position, maxPosition);
        return endPosition;
    }

    static public void dispose() {
        shapeRenderer.dispose();
    }
}
