package com.mygdx.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.text.Bidi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Entity {
    static HashMap<Body, Entity> entityFromBodyMap = new HashMap<>();
    static HashMap<String, Entity> entityFromNameMap = new HashMap<>();

    protected World world;
    protected String name;
    protected Map<String,Boolean> tags;
    protected Body body;
    protected Vector2 gravity;
    protected Vector2 size;

    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private PolygonSpriteBatch polygonSpriteBatch = new PolygonSpriteBatch();

    PolygonRegion polygonRegion;
    TextureRegion textureRegion;
    PolygonSprite polygonSprite;
    Sprite sprite;

    private Color color;

    public boolean inPortal = false;
    public Portal portalEntering;
    public Portal portalExiting;
    private float reflectionExtrudeOffset = 0.02f;
//
    public Entity reflectEntity;

    public Entity(World world, String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {

        // Initialize Variables
        this.world = world;
        this.name = name;
        this.gravity = world.getGravity();
        this.color = color;
        this.size = size;

        if (sprite != null) {
            sprite = new Sprite(sprite);    //copy
            this.sprite = sprite;
            this.sprite.setColor(this.color);
        }

        // Create a BodyDef and apply to Body Object
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.x = position.x;
        bodyDef.position.y = position.y;
//        bodyDef.fixedRotation = true;
        bodyDef.type = bodyType;
        this.body = world.createBody(bodyDef);

        // Set the Polygon Box shape
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(this.size.x/2f,this.size.y/2f);

        // Fixture is used as a collision mesh for the Physics Body Object
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        this.body.createFixture(fixtureDef);

        // add entity to entity map
        entityFromBodyMap.put(this.body, this);
        entityFromNameMap.put(this.name, this);

        // Free memory
        shape.dispose();

        // Creating an Floating Dynamic Object
        if (!gravityEnabled) this.body.setGravityScale(0f);
    }

    // Attempt to Gather the vertices that make up the FIRST Fixture of the Physics Body Object
    private float[] getVertices() {
        PolygonShape shape = (PolygonShape) this.body.getFixtureList().first().getShape();
        float[] vertices = new float[shape.getVertexCount()*2];
        for (int i=0; i<shape.getVertexCount(); i++) {
            Vector2 vertex = Vector2.Zero;
            shape.getVertex(i, vertex);
            vertices[i * 2] = vertex.x; vertices[i * 2 + 1] = vertex.y;
        }
        for (float f : vertices) System.out.print(f+ " ");
        System.out.println();
        Polygon polygon = new Polygon(vertices);
        polygon.setPosition(this.body.getPosition().x, this.body.getPosition().y);
        polygon.rotate(this.body.getAngle());
        return polygon.getTransformedVertices();
    }

    public void updateReflection(Portals portals) {
        if (portals.portals[0].getSurface() == null || portals.portals[1].getSurface() == null) return;


        if (portalEntering == null || portalExiting == null) {
            if (reflectEntity != null) {
//                setPosition(reflectEntity.getPosition());
                reflectEntity.dispose();
            }
            reflectEntity = null;

        }
        else {

            if (!properPositionToReflect()) {
                if (inPortal) {
//                    portals.unlinkPortal(getBody().getFixtureList().first());
//                    inPortal = false;
                }
                return;
            };

            //suck entity in portal
            portals.suckEntity(portalEntering, this);

            if (reflectEntity == null) {
                reflectEntity = new ReflectEntity(this.world, getName(), new Vector2(0, 0), this.size,
                        BodyDef.BodyType.StaticBody, getSprite().getColor(), getBody().getFixtureList().first().getDensity(),
                        getBody().getFixtureList().first().getFriction(), false, getSprite());
            }

            Vector2 portalEnteringSurfacePosition = portalEntering.getSurface().getBody().getPosition();
            Vector2 portalExitingSurfacePosition = portalExiting.getSurface().getBody().getPosition();

            Float portalEnteringSurfaceThickness = null;
            Float entityThickness = null;
            Float distanceFromPortalEnteringSurface = null;



            if (portalEntering.getNormal().y == 0) {
                portalEnteringSurfaceThickness = Entity.entityFromBody(portalEntering.getSurface().getBody()).size.x;
                entityThickness = size.x;
                distanceFromPortalEnteringSurface = (portalEnteringSurfacePosition.x - getPosition().x) * -portalEntering.getNormal().x;
            }
            else {
                portalEnteringSurfaceThickness = Entity.entityFromBody(portalEntering.getSurface().getBody()).size.y;
                entityThickness = size.y;
                distanceFromPortalEnteringSurface = (portalEnteringSurfacePosition.y - getPosition().y) * -portalEntering.getNormal().y;
            }
            float halfEntityAndHalfWall = entityThickness / 2f + portalEnteringSurfaceThickness / 2f;
            float intrudingWidth = Math.abs(halfEntityAndHalfWall - distanceFromPortalEnteringSurface);


            Float portalExitingSurfaceThickness = null;
            Float reflectEntityAxis = null;

            Float sizeAxis = null;

            if (portalExiting.getNormal().y == 0) {
                portalExitingSurfaceThickness = Entity.entityFromBody(portalExiting.getSurface().getBody()).size.x;
                reflectEntityAxis = portalExitingSurfacePosition.x + portalExitingSurfaceThickness / 2f * portalExiting.getNormal().x
                        - entityThickness / 2f * portalExiting.getNormal().x + intrudingWidth * portalExiting.getNormal().x;

                reflectEntity.setPosition(new Vector2(reflectEntityAxis, portalExiting.getPosition().y));

                sizeAxis = size.x;
            }
            else {
                portalExitingSurfaceThickness = Entity.entityFromBody(portalExiting.getSurface().getBody()).size.y;
                reflectEntityAxis = portalExitingSurfacePosition.y + portalExitingSurfaceThickness / 2f * portalExiting.getNormal().y
                        - entityThickness / 2f * portalExiting.getNormal().y + intrudingWidth * portalExiting.getNormal().y;
                reflectEntity.setPosition(new Vector2(portalExiting.getPosition().x, reflectEntityAxis));

                sizeAxis = size.y;
            }

            //            System.out.println("entering with an x vel of: " + getBody().getLinearVelocity().x);


            if (intrudingWidth >= sizeAxis + reflectionExtrudeOffset) {
                System.out.println(intrudingWidth);
                if (portals.isGoingIntoPortal(this, portalEntering)) {
                    portals.unlinkPortal(getBody().getFixtureList().first());
                }
            }
//            if (intrudingWidth <= 0.03) {
//                if (portals.isLeavingPortal(this, portalEntering)) {
//                    portals.unlinkPortal(getBody().getFixtureList().first());
//                }
//            }
        }
    }

    private boolean properPositionToReflect() {
        Float ePositionAxis;
        Float topBoundPortal;
        Float botBoundPortal;
        if (portalEntering.getNormal().y == 0) {
            topBoundPortal = portalEntering.getPosition().y + Portal.portalLength / 2f - size.y / 2f;
            botBoundPortal = portalEntering.getPosition().y - Portal.portalLength / 2f + size.y / 2f;
            ePositionAxis = getPosition().y;
        }
        else {
            topBoundPortal = portalEntering.getPosition().x + Portal.portalLength / 2f - size.x / 2f;
            botBoundPortal = portalEntering.getPosition().x - Portal.portalLength / 2f + size.x / 2f;
            ePositionAxis = getPosition().x;
        }
        System.out.println(ePositionAxis + " >= " + botBoundPortal + " && " + ePositionAxis + " <= " + topBoundPortal);
        return ePositionAxis >= botBoundPortal && ePositionAxis <= topBoundPortal;
    }


    // Render: NEEDS WORK
    public void render(Renderer renderer, Camera camera) {
        renderer.renderSprite(this.sprite, this.body.getPosition(), this.size,
                new Vector2(this.size.x/2f, this.size.y/2f),
                (float) Math.toDegrees(this.body.getAngle()));

//        System.out.println(this.body.getPosition());
//        PolygonShape polygonShape = (PolygonShape) this.body.getFixtureList().first().getShape();


//        this.shapeRenderer.setProjectionMatrix(camera.combined);
//        this.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        this.shapeRenderer.setColor(color);

//        float[] vertices = getVertices();
//        polygonRegion = new PolygonRegion(new TextureRegion(), vertices, new short[]{});
//        polygonSprite = new PolygonSprite(polyReg);
//        this.shapeRenderer.polygon(vertices);

//        this.shapeRenderer.rect(this.body.getPosition().x-size.x/2f, this.body.getPosition().y-size.y/2f, size.x, size.y);
//        this.shapeRenderer.end();

    }

    // Tag System
    public void addTag(String tag) {
        this.tags.put(tag, true);
    }

    // Adding a Force to the Physics Body Object
    public void applyForce(Vector2 direction, float magnitude) {
        Vector2 forceVector = new Vector2().mulAdd(direction, magnitude);
//        System.out.println("Apply Force: " + forceVector);
        this.body.applyForceToCenter(forceVector, true);
    }
    public void applyForce(Vector2 forceVector) {
        this.body.applyForceToCenter(forceVector, true);
    }





    // Accessor Methods
    public Body getBody() {
        return this.body;
    }


    public void setBody(Body body) {
        entityFromBodyMap.remove(this.body);
        this.body = body;
        entityFromBodyMap.put(this.body, this);
    }

    public Vector2 getPosition() {
        return this.body.getPosition();
    }
//    public Vector2 getSize() {
//        return this.body.getFixtureList().first().getShape().;
//    }

    public void setPosition(Vector2 pos) {
        getBody().setTransform(pos, getBody().getAngle());
    }

    public void setAngle(float angle, boolean centerOrigin) {
        getBody().setTransform(getPosition(), (float) Math.toRadians(angle));
        if (!centerOrigin) {
            Vector2 offset = new Vector2(-this.size.x/2f,0);
            Vector2 angleDirection = new Vector2((float) Math.cos(Math.toRadians(angle)), (float) Math.sin(Math.toRadians(angle)));
            offset = PMath.addVector2(offset, PMath.multVector2(angleDirection, this.size.x/2f));

            getBody().setTransform(PMath.addVector2(getPosition(),offset), getBody().getAngle());
        }
    }

    public String getName() {
        return this.name;
    }

    public Sprite getSprite() {
        return this.sprite;
    }
    // Free up memory when Game is closed, MUST LOOK AT CAREFULLY!
    static void disposeAll() {
        // MUST DISPOSE ALL SHAPE RENDERERS
//        shapeRenderer.dispose();
        // MUST DISPOSE ALL ENTITIES;
    }

    public void dispose() {
        if(this.body == null) return;
        this.world.destroyBody(this.body);
        this.body = null;
    }

    static Entity entityFromBody(Body body) {
        return entityFromBodyMap.get(body);
    }

    static Entity entityFromName(String name) {
        return entityFromNameMap.get(name);
    }

}
