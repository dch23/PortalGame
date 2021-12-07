package com.mygdx.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.text.Bidi;
import java.util.ArrayList;
import java.util.Map;

public class Entity {
    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    private Map<String,Boolean> tags;

    private Vector2 size;
    private Body body;
    private Color color;



    public Entity(World world, Vector2 position, Vector2 size, Color color, float density) {
//        this.shape = new Rectangle(position.x, position.y, size.x, size.y);
//        this.shape = shape;
//        this.color = color;
//
//        this.shape.setAsBox(size.x, size.y);



        // Create a BodyDef and apply to private body variable
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.x = position.x;
        bodyDef.position.y = position.y;
        bodyDef.fixedRotation = true;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        this.body = world.createBody(bodyDef);



        // set the polygon box shape
        PolygonShape shape = new PolygonShape();
//        Vector2[] vertices = new Vector2[]{
//                position,
//                new Vector2(position.x + size.x, position.y),
//                new Vector2(position.x, position.y + size.y),
//                new Vector2(position.x + size.x, position.y + size.y),
//        };
//        shape.set(vertices);

        shape.setAsBox(size.x,size.y);

        // Set size for drawing later on
        this.size = size;

        // Shape and collision of the physics body
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
//        fixtureDef.density = density;
        this.body.createFixture(fixtureDef);

        shape.dispose();


        body.setAwake(true);
        // color of render
        this.color = color;
    }

//    public Entity(ArrayList<String> tags) {
//        this.tags = tags;
//    }

    public void initialize() {
//
    }

    public void render(Camera camera) {
//        System.out.println(this.body.getPosition());
        this.shapeRenderer.setProjectionMatrix(camera.combined);
        this.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        this.shapeRenderer.setColor(color);
        this.shapeRenderer.rect(this.body.getPosition().x, this.body.getPosition().y, size.x, size.y);
        this.shapeRenderer.end();

    }

    public void addTag(String tag) {
        this.tags.put(tag, true);
    }

    public void add2Position(Vector2 vector2) {
//        this.shape.setPosition(this.shape.x + vector2.x, this.shape.y + vector2.y);
    }

    public void move(Vector2 direction, float magnitude) {
//        Vector2 moveVector = new Vector2().mulAdd(direction, magnitude);
//        this.shape.setPosition(this.shape.x + moveVector.x, this.shape.y + moveVector.y);

    }

    public void applyForce(Vector2 direction, float magnitude) {
        Vector2 forceVector = new Vector2().mulAdd(direction, magnitude);
//        System.out.println("Apply Force: " + forceVector);
        this.body.applyForceToCenter(forceVector, true);
    }








//    public Vector2 getPosition() {
//        return this.shape.getPosition(Vector2.Zero);
//    }
//    public Vector2 getSize() {
//        return this.shape.getSize(Vector2.Zero);
//    }
    static void dispose() {
        // MUST DISPOSE ALL SHAPE RENDERERS
//        shapeRenderer.dispose();
        // MUST DISPOSE ALL ENTITIES;
    }

}
