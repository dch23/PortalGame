package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.holidaystudios.tools.GifDecoder;
import sun.awt.image.GifImageDecoder;

import java.text.Bidi;
import java.util.*;


public class Entity {
    // statics
    static World world;
    static ArrayList<Entity> allEntities = new ArrayList<>();
    static HashMap<Body, Entity> entityFromBodyMap = new HashMap<>();
    //    static HashMap<String, Entity> entityFromNameMap = new HashMap<>();
    static float frameRate = 1f/30f;
    public float renderAngle = 0;
    public boolean canPortalOn = true;

    // properties
    protected String name;
    protected Map<String,Boolean> tags;
    protected Body body;
    protected Vector2 gravity;
    protected Vector2 size;

    public boolean alive = true;
    float closeEnoughToGround = 0.05f;
    float maxGroundRayDistance = 3f;
    private Color color;
    Sprite sprite;


    // animation
    HashMap<String, Animation> animations = new HashMap<>();
    String currentAnimation = null;
    float animationTextureSizeScale = 1.5f;
    int horizontalFaceDirection = 1;

    // render
    RenderEntity renderEntity;


    // portals
    public boolean inPortal = false;
    public Portal portalEntering;
    public Portal portalExiting;
    private float reflectionExtrudeOffset = 0.02f;
    public Entity reflectEntity;

    // sounds
    public HashMap<String, Sound> sounds = new HashMap<>();
    private Object[] array;


    public Entity(String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {

        // Initialize Variables
        this.name = name;
        this.gravity = Entity.world.getGravity();
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
//        entityFromNameMap.put(this.name, this);
        allEntities.add(this);

        // Free memory
        shape.dispose();

        // Creating an Floating Dynamic Object
        if (!gravityEnabled) this.body.setGravityScale(0f);

        // add to render layer
        int renderLayerIndex = 1;
        renderEntity = new RenderEntity(MyGdxGame.entityRenderer.spriteBatch, this);
//        if (!getName().equals("Boss") && !getName().equals("fireball")) {
//            MyGdxGame.entityRenderer.addToRenderLayer(renderLayerIndex, this);
//        }
    }

    public static void setWorld(World world) {
        Entity.world = world;
    }

    public static void operation() {
        for (Entity e : allEntities) {
            if (e.getName().equals("portal collider")) continue;
            if (e.getBody().getType() == BodyDef.BodyType.StaticBody) continue;
            if (e.alive && e.getBody() != null) {
                e.setAngle(e.renderAngle, true);
            }
        }
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
                reflectEntity = new ReflectEntity(world, "reflect " + getName(), new Vector2(0, 0), this.size,
                        BodyDef.BodyType.StaticBody, color, getBody().getFixtureList().first().getDensity(),
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

            // angle for only fireballs
            if (getName().equals("fireball")) {
                float angle = PMath.dir2Deg(portalExiting.getNormal());
                reflectEntity.renderAngle = angle;
            }
            else {
                reflectEntity.renderAngle = renderAngle;
            }


            if (intrudingWidth >= sizeAxis + reflectionExtrudeOffset) {
//                System.out.println(intrudingWidth);
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
//        System.out.println(ePositionAxis + " >= " + botBoundPortal + " && " + ePositionAxis + " <= " + topBoundPortal);
        return ePositionAxis >= botBoundPortal && ePositionAxis <= topBoundPortal;
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
//        if (this.body == null) return null;
        return this.body.getPosition();
    }
    public Vector2 getSize() {
        return this.size;
    }

    public void setPosition(Vector2 pos) {
        getBody().setTransform(pos, getBody().getAngle());
    }

    public void setAngle(float angle, boolean centerOrigin) {
        Vector2 topLeft = PMath.addVector2(getPosition(), new Vector2(-size.x/2, size.y/2));
//        System.out.println("set to angle: " + angle);
        getBody().setTransform(getPosition(), (float) Math.toRadians(angle));
        if (!centerOrigin) {
            setPosition(topLeft);

//            Vector2 offset = new Vector2(-this.size.x/2f,0);
//            Vector2 angleDirection = new Vector2((float) Math.cos(Math.toRadians(angle)), (float) Math.sin(Math.toRadians(angle)));
//            offset = PMath.addVector2(offset, PMath.multVector2(angleDirection, this.size.x/2f));
//
            float angle1 = angle, angle2 = angle - 90;
            Vector2 right = PMath.deg2dir(angle1), down = PMath.deg2dir(angle2);
            Vector2 goal = PMath.addVector2(topLeft, PMath.multVector2(right, size.x/2));
            goal = PMath.addVector2(goal, PMath.multVector2(down, size.y/2));
            setPosition(goal);
//
//
//            System.out.println("right: " + right);
//            System.out.println("down: " + down);
//            System.out.println("top left: " + topLeft);
//            System.out.println("goal: " + goal);
//            System.out.println();
        }
    }

    public String getName() {
        return this.name;
    }

    public Sprite getSprite() {
        return this.sprite;
    }

    static Entity entityFromBody(Body body) {
        return entityFromBodyMap.get(body);
    }

//    static Entity entityFromName(String name) {
//        return entityFromNameMap.get(name);
//    }


    private Animation animationFromSpriteSheet(String animationDirectory, int numberOfFrames, Animation.PlayMode playMode, float frameRate) {
        Texture animationSheet = new Texture(animationDirectory);
        Vector2 sheetSize = new Vector2(animationSheet.getWidth(), animationSheet.getHeight());

        float frameWidth = sheetSize.x / (float) numberOfFrames;
        TextureRegion[][] framesChart = TextureRegion.split(animationSheet, (int) frameWidth, (int) sheetSize.y);
        TextureRegion[] frames = framesChart[0];

        Animation animation = new Animation(frameRate, frames);
        animation.setPlayMode(playMode);
        return animation;
    }


    public void addAnimation(String name, String animationDirectory, int numberOfFrames, boolean loop, float speedScale) {
        Animation.PlayMode playMode = Animation.PlayMode.NORMAL;
        if (loop) playMode = Animation.PlayMode.LOOP;
        Animation animation =
                animationDirectory.endsWith("gif") ? GifDecoder.loadGIFAnimation(playMode, Gdx.files.internal(animationDirectory).read(), frameRate / speedScale)
                        : animationFromSpriteSheet(animationDirectory, numberOfFrames, playMode, frameRate / speedScale);
        animations.put(name, animation);
    }

    public Animation getAnimation(String animationName) {
        return animations.get(animationName);
    }

    // Free up memory when Game is closed, MUST LOOK AT CAREFULLY!
    static void disposeAll() {
        // dispose entities
        for (int i=allEntities.size()-1; i>=0; i--) {
//            System.out.println(i);
            Entity e = allEntities.get(i);
            e.dispose();
        }
        allEntities = new ArrayList<>();
        entityFromBodyMap = new HashMap<>();

        Player.player = null;
        WeakEnemyEntity.weakEnemyEntities = new ArrayList<>();
        MidEnemyEntity.midEnemyEntities = new ArrayList<>();
        // MUST DISPOSE ALL SHAPE RENDERERS
//        shapeRenderer.dispose();
        // MUST DISPOSE ALL ENTITIES;
    }

    public void dispose() {

        // remove from maps
        entityFromBodyMap.remove(getBody());
        allEntities.remove(this);

        // delete animations
        if (reflectEntity != null) reflectEntity.dispose();

        Collection<Animation> col = animations.values();
        Object[] arr = col.toArray();
        for (Object ob : arr) {
            Animation a = (Animation) ob;

            Object[] frames = a.getKeyFrames();
            for (Object frame : frames) {
                TextureRegion tr = (TextureRegion) frame;
                Texture t = tr.getTexture();
                t.getTextureData().disposePixmap();
                t.dispose();
            }
            AnimationManager.animationElapseTimes.remove(a);
        }

        animations.clear();

        // delete texture regions!!!


        // delete sounds
        if (sounds != null) {
            Set<Map.Entry<String, Sound>> soundSet = sounds.entrySet();
            Object[] array = soundSet.toArray();
            for (Object object : array) {
                Map.Entry<String, Sound> e = (Map.Entry<String, Sound>) object;
                Sound sound = e.getValue();
                sound.dispose();
            }
        }
        sounds = null;

        // delete body
        if(this.body == null) return;
        world.destroyBody(this.body);
        this.body = null;
    }


    public void setName(String name) {
        this.name = name;
    }
}
