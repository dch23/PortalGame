package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import javax.sound.sampled.Port;
import java.util.ArrayList;

public class Portals {
    private Renderer entityRenderer = MyGdxGame.entityRenderer;;
    private World world;
    private float pullMagnitude = 2f;
    private float suckStrength = 30f;
    private float shrinkWallAmount = 0.1f;

    Portal[] portals;

    public Portals(World world) {
        this.world = world;

        portals = new Portal[2];
        portals[0] = new Portal(world, Color.BLUE);
        portals[1] = new Portal(world, Color.PURPLE);

        portals[0].setOtherPortal(portals[1]);
        portals[1].setOtherPortal(portals[0]);

        portals[0].setSprite(new Sprite(new Texture("sprites/portal1.png")));
        portals[1].setSprite(new Sprite(new Texture("sprites/portal2.png")));
    }

    public void renderPortals(SpriteBatch spriteBatch) {
        spriteBatch.begin();
        for (Portal p : portals) {
            if (p.getSurface() == null) continue;
            p.getSprite().draw(spriteBatch);
//            Vector2 offset = PMath.divideVector2(this.entity.size, 2f);
//            this.entity.sprite.setSize(this.entity.size.x, this.entity.size.y);
//            this.entity.sprite.setPosition(this.entity.getPosition().x - offset.x, this.entity.getPosition().y - offset.y);
//            this.entity.sprite.setOriginCenter();
//            this.entity.sprite.setRotation(this.entity.getBody().getAngle());
//            this.entity.sprite.draw(this.spriteBatch);
        }
        spriteBatch.end();
    }

    public void setPortal(World world, int portalNumber, Vector2 position, Vector2 normal, boolean enabled, Fixture fixtureHit) {
//        System.out.println(portalNumber);

        //        System.out.println(Entity.entityFromBody(fixture.getBody()));

//        System.out.println(normal);
//        if (fixture==null) return;
        // set for certain portal number

        // set constant portal data
        portals[portalNumber].setNormal(normal);
        portals[portalNumber].setPosition(position);
        portals[portalNumber].setEnabled(enabled);

        // if the portal doesn't have a surface then:
        if (portals[portalNumber].getSurface() == null) {
            // set the new surface
            portals[portalNumber].setSurface(fixtureHit.getBody().getFixtureList().first());
            portals[portalNumber].getSurface().setSensor(true);

            // if the portal is being shot on a surface that has the other portal then reset the surface
            if (portals[portalNumber].getOtherPortal().getSurface() == fixtureHit.getBody().getFixtureList().first()) {
                portals[portalNumber].reset(world, fixtureHit);
            }
        }
        else {
            // if the portal already has a surface, must reset that surface before attaching to another surface
            portals[portalNumber].reset(world, fixtureHit);
        }

        Entity surfaceEntity = Entity.entityFromBody(portals[portalNumber].getSurface().getBody());
        Vector2 surfacePosition = new Vector2(surfaceEntity.getPosition());
        Vector2 surfaceSize = new Vector2(surfaceEntity.size);

//        float thickness;
//        float length;

        float positionOffset;
        float firstLength;
        float midLength;
        float lastLength;

        PolygonShape firstShape = new PolygonShape();
        PolygonShape midShape = null;
        PolygonShape lastShape = new PolygonShape();

        float axisPortalPosition;
        float axisSurfacePosition;
        float axisSurfaceThickness;
        float axisSurfaceLength;

//        Vector2 axis = PMath.absoluteVector2(normal);

        // if the portal is horizontal then:
        if (normal.y == 0f) {
            axisPortalPosition = position.y;
            axisSurfacePosition = surfacePosition.y;

            axisSurfaceThickness = surfaceSize.x;
            axisSurfaceLength = surfaceSize.y;

//            Portal.portalLength / 2f;
//            if (normal.x == 1) {
//                // put portal according to normal
//            }
//            else {
//                // put portal according to normal
//            }
        }
        else {
            axisPortalPosition = position.x;
            axisSurfacePosition = surfacePosition.x;

            axisSurfaceThickness = surfaceSize.y;
            axisSurfaceLength = surfaceSize.x;
        }

        // shrink surface thickness
//        axisSurfaceThickness

        positionOffset = axisSurfacePosition - axisSurfaceLength/2f;
//        System.out.println(positionOffset);

        // if shooting both portals on same surface
        if (portals[portalNumber].getSurface() == portals[portalNumber].getOtherPortal().getSurface()) {
            midShape = new PolygonShape();

            // getting the lower and higher portal (changed to closer and farther because of other axis)

            float portalPosition, otherPortalPosition;
            if (normal.y == 0f) {
                portalPosition = portals[portalNumber].getPosition().y;
                otherPortalPosition = portals[portalNumber].getOtherPortal().getPosition().y;
            }
            else {
                portalPosition = portals[portalNumber].getPosition().x;
                otherPortalPosition = portals[portalNumber].getOtherPortal().getPosition().x;
            }

            Portal closerPortal, fartherPortal;
            if (portalPosition < otherPortalPosition) {
                closerPortal = portals[portalNumber];
                fartherPortal = portals[portalNumber].getOtherPortal();
            }
            else {
                closerPortal = portals[portalNumber].getOtherPortal();
                fartherPortal = portals[portalNumber];
            }


            float axisCloserPortalPosition, axisFartherPortalPosition;
            if (normal.y == 0f) {
                axisCloserPortalPosition = closerPortal.getPosition().y;
                axisFartherPortalPosition = fartherPortal.getPosition().y;
            }
            else {
                axisCloserPortalPosition = closerPortal.getPosition().x;
                axisFartherPortalPosition = fartherPortal.getPosition().x;
            }


            firstLength = axisCloserPortalPosition - Portal.portalLength/2f - positionOffset;
            midLength = axisFartherPortalPosition - Portal.portalLength/2f - (axisCloserPortalPosition + Portal.portalLength/2f);
            lastLength = axisSurfaceLength - midLength - firstLength - 2 * Portal.portalLength;

//            System.out.println(firstLength + " vs " + midLength);

            float firstWidth, midWidth, lastWidth, firstHeight, midHeight, lastHeight;
            float firstOriginOffset = -(axisSurfacePosition - axisCloserPortalPosition) - Portal.portalLength/2f - firstLength/2f;
            float midOriginOffset = -(axisSurfacePosition - axisCloserPortalPosition) + Portal.portalLength/2f + midLength/2f;
            float lastOriginOffset = -(axisSurfacePosition - axisFartherPortalPosition) + Portal.portalLength/2f + lastLength/2f;
            Vector2 firstOriginOffsetVector, midOriginOffsetVector, lastOriginOffsetVector;
            if (normal.y == 0f) {
                firstWidth = axisSurfaceThickness/2f;
                midWidth = axisSurfaceThickness/2f;
                lastWidth = axisSurfaceThickness/2f;

                firstHeight = firstLength/2f;
                midHeight = midLength/2f;
                lastHeight = lastLength/2f;

                firstOriginOffsetVector = new Vector2(0, firstOriginOffset);
                midOriginOffsetVector = new Vector2(0, midOriginOffset);
                lastOriginOffsetVector = new Vector2(0, lastOriginOffset);
            }
            else {
                firstHeight = axisSurfaceThickness/2f;
                midHeight = axisSurfaceThickness/2f;
                lastHeight = axisSurfaceThickness/2f;

                firstWidth = firstLength/2f;
                midWidth = midLength/2f;
                lastWidth = lastLength/2f;

                firstOriginOffsetVector = new Vector2(firstOriginOffset, 0);
                midOriginOffsetVector = new Vector2(midOriginOffset, 0);
                lastOriginOffsetVector = new Vector2(lastOriginOffset, 0);
            }

            firstShape.setAsBox(firstWidth, firstHeight, firstOriginOffsetVector, 0);
            midShape.setAsBox(midWidth, midHeight, midOriginOffsetVector, 0);
            lastShape.setAsBox(lastWidth, lastHeight, lastOriginOffsetVector, 0);
        }
        else {
            firstLength = axisPortalPosition - Portal.portalLength / 2f - positionOffset;
            lastLength = axisSurfacePosition + axisSurfaceLength / 2f - positionOffset - firstLength - Portal.portalLength;

            float firstWidth, lastWidth, firstHeight, lastHeight;
            float firstOriginOffset = -(axisSurfacePosition - axisPortalPosition) - Portal.portalLength / 2f - firstLength / 2f;
            float lastOriginOffset = axisPortalPosition - axisSurfacePosition + Portal.portalLength / 2f + lastLength / 2f;
            Vector2 firstOriginOffsetVector, lastOriginOffsetVector;

            if (normal.y == 0) {
                firstWidth = axisSurfaceThickness / 2f;
                lastWidth = axisSurfaceThickness / 2f;

                firstHeight = firstLength / 2f;
                lastHeight = lastLength / 2f;

                firstOriginOffsetVector = new Vector2(0, firstOriginOffset);
                lastOriginOffsetVector = new Vector2(0, lastOriginOffset);
            }
            else {
                firstWidth = firstLength / 2f;
                lastWidth = lastLength / 2f;

                firstHeight = axisSurfaceThickness / 2f;
                lastHeight = axisSurfaceThickness / 2f;

                firstOriginOffsetVector = new Vector2(firstOriginOffset, 0);
                lastOriginOffsetVector = new Vector2(lastOriginOffset, 0);
            }

            firstShape.setAsBox(firstWidth, firstHeight, firstOriginOffsetVector, 0f);
            lastShape.setAsBox(lastWidth, lastHeight, lastOriginOffsetVector, 0f);
        }



        FixtureDef botFixtureDef = new FixtureDef();
        botFixtureDef.shape = firstShape;
        botFixtureDef.density = portals[portalNumber].getSurface().getDensity();
        botFixtureDef.friction = portals[portalNumber].getSurface().getFriction();
        botFixtureDef.restitution = portals[portalNumber].getSurface().getRestitution();

        FixtureDef topFixtureDef = new FixtureDef();
        topFixtureDef.shape = lastShape;
        topFixtureDef.density = portals[portalNumber].getSurface().getDensity();
        topFixtureDef.friction = portals[portalNumber].getSurface().getFriction();
        topFixtureDef.restitution = portals[portalNumber].getSurface().getRestitution();

        surfaceEntity.getBody().createFixture(botFixtureDef);

        if (midShape != null) {
            FixtureDef midFixtureDef = new FixtureDef();
            midFixtureDef.shape = midShape;
            midFixtureDef.density = portals[portalNumber].getSurface().getDensity();
            midFixtureDef.friction = portals[portalNumber].getSurface().getFriction();
            midFixtureDef.restitution = portals[portalNumber].getSurface().getRestitution();

            surfaceEntity.getBody().createFixture(midFixtureDef);
        }

        surfaceEntity.getBody().createFixture(topFixtureDef);
    }

    public void linkPortal(Fixture solid, int portalNumber) {
//        System.out.println("link portal");
        Entity entity = Entity.entityFromBody(solid.getBody());
        Portal portalEntering = portals[portalNumber];
        Portal portalExiting = portalEntering.getOtherPortal();

        entity.portalEntering = portalEntering;
        entity.portalExiting = portalExiting;

        entity.inPortal = true;
    }

    public void unlinkPortal(Fixture solid) {
//        System.out.println("unlink portal");

        Entity entity = Entity.entityFromBody(solid.getBody());

        entity.inPortal = false;

        if (entity.reflectEntity != null) {
            entity.setPosition(entity.reflectEntity.getPosition());
            if (entity.portalExiting.getNormal().y == 0) {
                //            System.out.println(entity.getBody().getLinearVelocity());
                entity.getBody().setLinearVelocity(Math.abs(entity.getBody().getLinearVelocity().x) *
                        entity.portalExiting.getNormal().x, entity.getBody().getLinearVelocity().y);
            } else {
                entity.getBody().setLinearVelocity(entity.getBody().getLinearVelocity().x,
                        Math.abs(entity.getBody().getLinearVelocity().y) * entity.portalExiting.getNormal().y);
            }
        }
//        System.out.println("EXITING with an x vel of: " + entity.getBody().getLinearVelocity().x);

        entity.portalEntering = null;
        entity.portalExiting = null;

        portals[0].suckDirection = null;
        portals[1].suckDirection = null;



    }

    // renderer.renderSprite(this.sprite, this.body.getPosition(), this.size, new Vector2(this.size.x/2f, this.size.y/2f), (float) Math.toDegrees(this.body.getAngle()));

    public boolean isGoingIntoPortal(Entity e, Portal p) {
        Float eVelocity = null;
        Integer normal = null;
        if (p.getNormal().y == 0) {
            eVelocity = e.getBody().getLinearVelocity().x;
            normal = (int) p.getNormal().x;
        }
        else if (p.getNormal().x == 0) {
            eVelocity = e.getBody().getLinearVelocity().y;
            normal = (int) p.getNormal().y;
        }

        if (eVelocity != null && normal != null && eVelocity != 0) {
            int eDirection = (int) (eVelocity / Math.abs(eVelocity));
            return eDirection == -normal;
        }
        return false;
    }

    public boolean isLeavingPortal(Entity e, Portal p) {
        Float eVelocity = null;
        Integer normal = null;
        if (p.getNormal().y == 0) {
            eVelocity = e.getBody().getLinearVelocity().x;
            normal = (int) p.getNormal().x;
        }
        else if (p.getNormal().x == 0) {
            eVelocity = e.getBody().getLinearVelocity().y;
            normal = (int) p.getNormal().y;
        }

        if (eVelocity != null && normal != null && eVelocity != 0) {
            int eDirection = (int) (eVelocity / Math.abs(eVelocity));
            return eDirection == normal;
        }
        return false;
    }

    public void suckEntity(Portal p, Entity e) {
        if (p.suckDirection == null) {
            p.suckDirection = PMath.normalizeVector2(PMath.subVector2(p.getPosition(), e.getPosition()));
        }
        if (p.getNormal().y == 0) {
            p.suckDirection.y = 0;
        }
        if (p.getNormal().x == 0) {
            p.suckDirection.x = 0;
        }
        e.applyForce(p.suckDirection, this.suckStrength);
    }

    public boolean properPositionToPortal(Portal portalEntering, Entity entity) {
        Float ePositionAxis;
        Float topBoundPortal;
        Float botBoundPortal;
        if (portalEntering.getNormal().y == 0) {
            topBoundPortal = portalEntering.getPosition().y + Portal.portalLength / 2f - entity.size.y / 2f;
            botBoundPortal = portalEntering.getPosition().y - Portal.portalLength / 2f + entity.size.y / 2f;
            ePositionAxis = entity.getPosition().y;
        }
        else {
            topBoundPortal = portalEntering.getPosition().x + Portal.portalLength / 2f - entity.size.x / 2f;
            botBoundPortal = portalEntering.getPosition().x - Portal.portalLength / 2f + entity.size.x / 2f;
            ePositionAxis = entity.getPosition().x;
        }
//        System.out.println(ePositionAxis + " >= " + botBoundPortal + " && " + ePositionAxis + " <= " + topBoundPortal);
        return ePositionAxis >= botBoundPortal && ePositionAxis <= topBoundPortal;
    }

}

class Portal {
    static final float portalLength = 0.7f;

    private Portal otherPortal;

    protected Sprite sprite;

    private Vector2 position;
    private Vector2 normal;
    private Fixture surface;
    private boolean enabled = false;

    protected World world;
    protected Vector2 suckDirection = null;
    protected ContactListener contactListener;

    public Color trailColor;

    public Portal(final World world, Color trailColor) {
        this.world = world;
        this.trailColor = trailColor;
    };


    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
        Vector2 scaledSize = PMath.multVector2(new Vector2(this.sprite.getWidth(), this.sprite.getHeight()), MyGdxGame.GAME_SCALE);

        float portalLengthScale = Portal.portalLength / scaledSize.y;
        scaledSize = PMath.multVector2(scaledSize, portalLengthScale);

        this.sprite.setSize(scaledSize.x, scaledSize.y);
        this.sprite.setOriginCenter();
    }

    public Sprite getSprite() {
        return this.sprite;
    }

    public Portal(Vector2 position, Vector2 normal, Fixture surface) {
        this.position = position;
        this.normal = normal;
        this.surface = surface;
    }

    public void reset(World world, Fixture fixtureHit) {
        fixtureHit = fixtureHit.getBody().getFixtureList().first();
        if (fixtureHit == getOtherPortal().getSurface()) {
            // reset old surface
            if (getSurface() != null && getSurface() != otherPortal.getSurface()) {
                while (getSurface().getBody().getFixtureList().size > 1) getSurface().getBody().destroyFixture(
                        getSurface().getBody().getFixtureList().get(getSurface().getBody().getFixtureList().size - 1));
                getSurface().setSensor(false);
            }

            // joining the new surface
            while (fixtureHit.getBody().getFixtureList().size > 1) fixtureHit.getBody().destroyFixture(
                    fixtureHit.getBody().getFixtureList().get(fixtureHit.getBody().getFixtureList().size - 1));
            setSurface(fixtureHit);
        }
        else {
            // reset old surface
            while (getSurface().getBody().getFixtureList().size > 1) getSurface().getBody().destroyFixture(
                    getSurface().getBody().getFixtureList().get(getSurface().getBody().getFixtureList().size - 1));
            if (getSurface() == otherPortal.getSurface()) {
                Fixture otherPortalSurface = getOtherPortal().getSurface();

                // add new fixtures to old surface
                Entity portalSurfaceEntity = Entity.entityFromBody(otherPortalSurface.getBody());
                Vector2 portalPosition = getOtherPortal().getPosition();
                Vector2 surfacePosition = new Vector2(portalSurfaceEntity.getPosition());
                Vector2 surfaceSize = new Vector2(portalSurfaceEntity.size);

                float axisSurfaceThickness, axisSurfaceLength, axisSurfacePosition, axisPortalPosition;

                if (getOtherPortal().getNormal().y == 0f) {
                    axisSurfaceThickness = surfaceSize.x;
                    axisSurfaceLength = surfaceSize.y;
                    axisSurfacePosition = surfacePosition.y;
                    axisPortalPosition = portalPosition.y;
                }
                else {
                    axisSurfaceThickness = surfaceSize.y;
                    axisSurfaceLength = surfaceSize.x;
                    axisSurfacePosition = surfacePosition.x;
                    axisPortalPosition = portalPosition.x;
                }

                float positionOffset = axisSurfacePosition - axisSurfaceLength/2f;
                float firstLength = axisPortalPosition - Portal.portalLength / 2f - positionOffset;
                float lastLength = axisSurfacePosition + axisSurfaceLength / 2f - positionOffset - firstLength - Portal.portalLength;

                PolygonShape firstShape = new PolygonShape();
                PolygonShape lastShape = new PolygonShape();

                float firstWidth, midWidth, lastWidth, firstHeight, midHeight, lastHeight;
                float firstOriginOffset = -(axisSurfacePosition - axisPortalPosition) - Portal.portalLength / 2f - firstLength / 2f;
                float lastOriginOffset = axisPortalPosition - axisSurfacePosition+ Portal.portalLength / 2f + lastLength / 2f;
                Vector2 firstOriginOffsetVector, midOriginOffsetVector, lastOriginOffsetVector;
                if (getOtherPortal().getNormal().y == 0f) {
                    firstWidth = axisSurfaceThickness/2f;
                    lastWidth = axisSurfaceThickness/2f;

                    firstHeight = firstLength/2f;
                    lastHeight = lastLength/2f;

                    firstOriginOffsetVector = new Vector2(0, firstOriginOffset);
                    lastOriginOffsetVector = new Vector2(0, lastOriginOffset);
                }
                else {
                    firstHeight = axisSurfaceThickness/2f;
                    midHeight = axisSurfaceThickness/2f;
                    lastHeight = axisSurfaceThickness/2f;

                    firstWidth = firstLength/2f;
                    lastWidth = lastLength/2f;

                    firstOriginOffsetVector = new Vector2(firstOriginOffset, 0);
                    lastOriginOffsetVector = new Vector2(lastOriginOffset, 0);
                }

                firstShape.setAsBox(firstWidth, firstHeight, firstOriginOffsetVector, 0f);
                lastShape.setAsBox(lastWidth, lastHeight, lastOriginOffsetVector, 0f);


                FixtureDef botFixtureDef = new FixtureDef();
                botFixtureDef.shape = firstShape;
                botFixtureDef.density = otherPortalSurface.getDensity();
                botFixtureDef.friction = otherPortalSurface.getFriction();
                botFixtureDef.restitution = otherPortalSurface.getRestitution();

                FixtureDef topFixtureDef = new FixtureDef();
                topFixtureDef.shape = lastShape;
                topFixtureDef.density = otherPortalSurface.getDensity();
                topFixtureDef.friction = otherPortalSurface.getFriction();
                topFixtureDef.restitution = otherPortalSurface.getRestitution();

                portalSurfaceEntity.getBody().createFixture(botFixtureDef);
                portalSurfaceEntity.getBody().createFixture(topFixtureDef);
            }
            else {
                getSurface().setSensor(false);
            }
            setSurface(fixtureHit);
            getSurface().setSensor(true);
        }
    }

    public void setOtherPortal(Portal otherPortal) {
        this.otherPortal = otherPortal;
    }

    public Portal getOtherPortal() {
        return this.otherPortal;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Fixture getSurface() {
        return this.surface;
    }

    public void setSurface(Fixture surface) {
        this.surface = surface;
    }

    public void setPosition(Vector2 position) {
        this.position = position;

        // set sprite position and rotation
        resetSprite();

    }

    private void resetSprite() {
        float positionAxis;
        float offsetAxis;
        float normalAxis;
        if (getNormal().y == 0) {
            offsetAxis = getPosition().y;
            normalAxis = getNormal().x;
            positionAxis = getPosition().x;
            if (normalAxis == 1) positionAxis -= getSprite().getWidth();

            float degrees = normalAxis == 1 ? 0 : 180;
            getSprite().setRotation(degrees);
            getSprite().setPosition(positionAxis, offsetAxis - getSprite().getHeight()/2f);
        }
        else {
            offsetAxis = getPosition().x;
            normalAxis = getNormal().y;
            positionAxis = getPosition().y;
            positionAxis -= getSprite().getHeight()/2f + getSprite().getWidth()/2f * normalAxis;

            float degrees = normalAxis == 1 ? 90 : -90;
            getSprite().setRotation(degrees);
            getSprite().setPosition(offsetAxis - getSprite().getWidth()/2f, positionAxis);
        }

    }



    public Vector2 getNormal() {
        return normal;
    }

    public void setNormal(Vector2 normal) {
        this.normal = normal;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}

class PortalTrails {
    private static ShapeRenderer shapeRenderer = new ShapeRenderer();
    private static float startOpacity = 1;
    private static ArrayList<PortalTrail> trails = new ArrayList<>();

    static public void setProjectionMatrix(Matrix4 matrix4) {
        shapeRenderer.setProjectionMatrix(matrix4);
    }

    public static void addTrail(Vector2 start, Vector2 end, Color color) {
        trails.add(new PortalTrail(start, end, startOpacity, new Color(color)));
    }

    public static void draw() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        ArrayList<PortalTrail> awakeTrails = new ArrayList<>();
        for (PortalTrail trail : trails) {
            trail.fade();
            trail.shrink();
            trail.draw(shapeRenderer);
            if (trail.getColor().a > 0 && trail.getWidth() > 0) awakeTrails.add(trail);
        }


        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        trails = awakeTrails;
    }
}

class PortalTrail {
    private Color color;
    private Vector2 start;
    private Vector2 end;
    private float width = 0.04f;
    private float fadeSpeed = 0.05f;
    private float shrinkSpd = 0.0025f;

    public PortalTrail (Vector2 start, Vector2 end, float opacity, Color color) {
        this.start = start;
        this.end = end;
        this.color = color;
        this.color.a = opacity;
    }

    public void draw(ShapeRenderer renderer) {
        renderer.setColor(color);
//        System.out.println(color.a);
        renderer.rectLine(start, end, width);
    }

    public void fade() {
        if (color.a <= 0.01f) {
            color.a = 0.01f;
            return;
        }
        color.a -= fadeSpeed;
    }

    public void shrink() {
        if (width <= 0.001f) {
            width = 0.001f;
            return;
        }
        width -= shrinkSpd;
    }

    public Color getColor() {
        return color;
    }

    public float getWidth() {
        return width;
    }
}