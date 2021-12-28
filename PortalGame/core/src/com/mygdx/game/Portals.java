package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import javax.sound.sampled.Port;
import java.util.ArrayList;

public class Portals {
    Portal[] portals;
    public Portals(World world) {
        portals = new Portal[2];
        portals[0] = new Portal(world);
        portals[1] = new Portal(world);

        portals[0].setOtherPortal(portals[1]);
        portals[1].setOtherPortal(portals[0]);
    }

    public void setPortal(World world, int portalNumber, Vector2 position, Vector2 normal, boolean enabled, Fixture fixtureHit) {
        System.out.println(portalNumber);

        //        System.out.println(Entity.entityFromBody(fixture.getBody()));

//        System.out.println(normal);
//        if (fixture==null) return;
        // set for certain portal number

        // set constant portal data
        portals[portalNumber].setPosition(position);
        portals[portalNumber].setNormal(normal);
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

//        System.out.print("Created: ");
//        for (int i=1; i<surfaceEntity.getBody().getFixtureList().size; i++) {
//            System.out.print( surfaceEntity.getBody().getFixtureList().get(i)+ " ");
//        }
//        System.out.println();
    }
}

class Portal {
    static final float portalLength = 0.4f;

    private Portal otherPortal;

    private Vector2 position;
    private Vector2 normal;
    private Fixture surface;
    private boolean enabled = false;

    protected World world;
    protected ContactListener contactListener;



    public Portal(final World world) {
        this.world = world;

        contactListener = new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture fixture1 = contact.getFixtureB();
                Fixture fixture2 = contact.getFixtureB();


                if (fixture1 == null || fixture2 == null) return;
//                System.out.println(fixture1.isSensor() + " vs " + fixture2.isSensor());

                if (fixture1.getBody() != fixture2.getBody()) {
                    Entity entity1 = Entity.entityFromBody(fixture1.getBody());
                    Entity entity2 = Entity.entityFromBody(fixture2.getBody());
//                    System.out.println(entity1.getName() + " vs " + entity2.getName());

                }
                Entity entity1 = Entity.entityFromBody(fixture1.getBody());
                Entity entity2 = Entity.entityFromBody(fixture2.getBody());
//                System.out.println(entity1.getName() + " vs " + entity2.getName());
                if (fixture1!=fixture2) {
                    System.out.println(fixture1 + " vs " + fixture2);
                }
            }


            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        };
        world.setContactListener(contactListener);
    };

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

//        System.out.println("RESET");
//        // if the portal's surface is different from the other portal's surface
//        if (getSurface() != getOtherPortal().getSurface()) {
//            // if the hit surface is the same as the portal's previous surface
//            if (fixtureHit.getBody() == getSurface().getBody()) {
//                // delete all the extra fixtures
//                for (int i = fixtureHit.getBody().getFixtureList().size-1; i > 0; i--) {
//                    fixtureHit.getBody().destroyFixture(fixtureHit.getBody().getFixtureList().get(i));
//                }
//            } else {
//                // if the hit surface is different from the surface the portal was previously on
//                for (int i = getSurface().getBody().getFixtureList().size-1; i > 0; i--) {
//                    getSurface().getBody().destroyFixture(getSurface().getBody().getFixtureList().get(i));
//                }
//                //something weird here
//                if (getOtherPortal().getSurface() != null) {
//                    for (int i = getOtherPortal().getSurface().getBody().getFixtureList().size - 1; i > 0; i--) {
//                        getOtherPortal().getSurface().getBody().destroyFixture(getOtherPortal().getSurface().getBody().getFixtureList().get(i));
//                    }
//                }
//
//                getSurface().setSensor(false);
//                setSurface(fixtureHit.getBody().getFixtureList().first());
//                getSurface().setSensor(true);
//            }
//        }
//        else {
//            System.out.println("Leaving");
//            // if the portal's surface is the same as the other portal's surface
//            // if the hit surface is the same as the other portal's surface
//            if (fixtureHit.getBody() == getOtherPortal().getSurface().getBody()) {
////                System.out.print("Removing: ");
//                // remove the extra fixtures of the hit surface
//                for (int i = fixtureHit.getBody().getFixtureList().size - 1; i>=1; i--) {
////                    System.out.print(fixtureHit.getBody().getFixtureList().get(i));
//                    fixtureHit.getBody().destroyFixture(fixtureHit.getBody().getFixtureList().get(i));
//
//                }
//                // set the surface of the portal to the hit surface
//                setSurface(fixtureHit.getBody().getFixtureList().first());
////                System.out.println();
//            }
//            else {
//                // if the hit surface is different from the other portal's surface then:
//                getSurface().setSensor(false);
//                setSurface(fixtureHit.getBody().getFixtureList().first());
//                getSurface().setSensor(true);
//
//                // reset other portal fixture
//                // remove fixtures
//                Fixture otherPortalFixture = getOtherPortal().getSurface();
//                for (int i = otherPortalFixture.getBody().getFixtureList().size-1; i > 0; i--) {
//                    otherPortalFixture.getBody().destroyFixture(otherPortalFixture.getBody().getFixtureList().get(i));
//                }
//                otherPortalFixture.getBody().getFixtureList().first().setSensor(false);
//
//                //add new fixtures to other portal surface
//                Entity portalSurfaceEntity = Entity.entityFromBody(otherPortalFixture.getBody());
//                Vector2 portalPosition = getOtherPortal().getPosition();
//                Vector2 surfacePosition = new Vector2(portalSurfaceEntity.getPosition());
//                Vector2 surfaceSize = new Vector2(portalSurfaceEntity.size);
//
//                float axisSurfaceThickness;
//                float axisSurfaceLength;
//                float axisSurfacePosition;
//                float axisPortalPosition;
//
//
//                if (getOtherPortal().getNormal().y == 0f) {
//                    axisSurfaceThickness = surfaceSize.x;
//                    axisSurfaceLength = surfaceSize.y;
//                    axisSurfacePosition = surfacePosition.y;
//                    axisPortalPosition = portalPosition.y;
//                }
//                else {
//                    axisSurfaceThickness = surfaceSize.y;
//                    axisSurfaceLength = surfaceSize.x;
//                    axisSurfacePosition = surfacePosition.x;
//                    axisPortalPosition = portalPosition.x;
//                }
//
//                float positionOffset = axisSurfacePosition - axisSurfaceLength/2f;
//                float firstLength = axisPortalPosition - Portal.portalLength / 2f - positionOffset;
//                float lastLength = axisSurfacePosition + axisSurfaceLength / 2f - positionOffset - firstLength - Portal.portalLength;
//
//                PolygonShape firstShape = new PolygonShape();
//                PolygonShape lastShape = new PolygonShape();
//                firstShape.setAsBox(axisSurfaceThickness / 2f, firstLength / 2f, new Vector2(0, -(axisSurfacePosition -
//                        axisPortalPosition) - Portal.portalLength / 2f - firstLength / 2f), 0f);
//                lastShape.setAsBox(axisSurfaceThickness / 2f, lastLength / 2f, new Vector2(0, axisPortalPosition -
//                        axisSurfacePosition+ Portal.portalLength / 2f + lastLength / 2f), 0f);
//
//
//                FixtureDef botFixtureDef = new FixtureDef();
//                botFixtureDef.shape = firstShape;
//                botFixtureDef.density = otherPortalFixture.getDensity();
//                botFixtureDef.friction = otherPortalFixture.getFriction();
//                botFixtureDef.restitution = otherPortalFixture.getRestitution();
//
//                FixtureDef topFixtureDef = new FixtureDef();
//                topFixtureDef.shape = lastShape;
//                topFixtureDef.density = otherPortalFixture.getDensity();
//                topFixtureDef.friction = otherPortalFixture.getFriction();
//                topFixtureDef.restitution = otherPortalFixture.getRestitution();
//
//                portalSurfaceEntity.getBody().createFixture(botFixtureDef);
//                portalSurfaceEntity.getBody().createFixture(topFixtureDef);
//
//            }
//        }
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
