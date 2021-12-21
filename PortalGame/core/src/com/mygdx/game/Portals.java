package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import javax.sound.sampled.Port;

public class Portals {
    Portal[] portals;
    public Portals() {
        portals = new Portal[2];
        portals[0] = new Portal();
        portals[1] = new Portal();

        portals[0].setOtherPortal(portals[1]);
        portals[1].setOtherPortal(portals[0]);
    }

    public void setPortal(World world, int portalNumber, Vector2 position, Vector2 normal, boolean enabled, Fixture fixtureHit) {
//        System.out.println(Entity.entityFromBody(fixture.getBody()));


//        if (fixture==null) return;
        // set for certain portal number
        portals[portalNumber].setPosition(position);
        portals[portalNumber].setNormal(normal);
        portals[portalNumber].setEnabled(enabled);

        if (portals[portalNumber].getFixture() == null) {
            portals[portalNumber].setFixture(fixtureHit.getBody().getFixtureList().first());
            portals[portalNumber].getFixture().setSensor(true);

            if (portals[portalNumber].getOtherPortal().getFixture() == fixtureHit.getBody().getFixtureList().first()) {
                portals[portalNumber].reset(world, fixtureHit);
            }
        }
        else {
            portals[portalNumber].reset(world, fixtureHit);
        }


        if (normal.y == 0) {
            Entity entity = Entity.entityFromBody(portals[portalNumber].getFixture().getBody());
            Vector2 bodyPosition = new Vector2(entity.getPosition());
            Vector2 bodySize = new Vector2(entity.size);

            float width = bodySize.x;

            float liftHeight = bodyPosition.y - bodySize.y/2f;
            float botHeight;
            float midHeight;
            float topHeight;

            PolygonShape botShape = new PolygonShape();
            PolygonShape midShape = null;
            PolygonShape topShape = new PolygonShape();

            // if shooting both portals on same surface
            if (portals[portalNumber].getFixture() == portals[portalNumber].getOtherPortal().getFixture()) {
                midShape = new PolygonShape();

                // getting the lower and higher portal
                Portal lowerPortal, higherPortal;
                if (portals[portalNumber].getPosition().y < portals[portalNumber].getOtherPortal().getPosition().y) {
                    lowerPortal = portals[portalNumber];
                    higherPortal = portals[portalNumber].getOtherPortal();
                }
                else {
                    lowerPortal = portals[portalNumber].getOtherPortal();
                    higherPortal = portals[portalNumber];
                }

                botHeight = lowerPortal.getPosition().y - Portal.portalLength/2f - liftHeight;
                midHeight = higherPortal.getPosition().y - Portal.portalLength/2f - (lowerPortal.getPosition().y + Portal.portalLength/2f);
                topHeight = bodySize.y - midHeight - botHeight - 2 * Portal.portalLength;

                System.out.println(botHeight + " vs " + midHeight);

                botShape.setAsBox(width/2f, botHeight/2f, new Vector2(0, -(bodyPosition.y - lowerPortal.getPosition().y) - Portal.portalLength/2f - botHeight/2f), 0f);
                midShape.setAsBox(width/2f, midHeight/2f, new Vector2(0,-(bodyPosition.y - lowerPortal.getPosition().y) + Portal.portalLength/2f + midHeight/2f),0f);
                topShape.setAsBox(width/2f, topHeight/2f, new Vector2(0,-(bodyPosition.y - higherPortal.getPosition().y) + Portal.portalLength/2f + topHeight/2f),0f);
            }
            else {
                botHeight = position.y - Portal.portalLength / 2f - liftHeight;
                topHeight = bodyPosition.y + bodySize.y / 2f - liftHeight - botHeight - Portal.portalLength;

                botShape.setAsBox(width / 2f, botHeight / 2f, new Vector2(0, -(bodyPosition.y - position.y) - Portal.portalLength / 2f - botHeight / 2f), 0f);
                topShape.setAsBox(width / 2f, topHeight / 2f, new Vector2(0, position.y - bodyPosition.y + Portal.portalLength / 2f + topHeight / 2f), 0f);
            }

            FixtureDef botFixtureDef = new FixtureDef();
            botFixtureDef.shape = botShape;
            botFixtureDef.density = portals[portalNumber].getFixture().getDensity();
            botFixtureDef.friction = portals[portalNumber].getFixture().getFriction();
            botFixtureDef.restitution = portals[portalNumber].getFixture().getRestitution();

            FixtureDef topFixtureDef = new FixtureDef();
            topFixtureDef.shape = topShape;
            topFixtureDef.density = portals[portalNumber].getFixture().getDensity();
            topFixtureDef.friction = portals[portalNumber].getFixture().getFriction();
            topFixtureDef.restitution = portals[portalNumber].getFixture().getRestitution();

            entity.getBody().createFixture(botFixtureDef);

            if (midShape != null) {
                FixtureDef midFixtureDef = new FixtureDef();
                midFixtureDef.shape = midShape;
                midFixtureDef.density = portals[portalNumber].getFixture().getDensity();
                midFixtureDef.friction = portals[portalNumber].getFixture().getFriction();
                midFixtureDef.restitution = portals[portalNumber].getFixture().getRestitution();

                entity.getBody().createFixture(midFixtureDef);
            }

            entity.getBody().createFixture(topFixtureDef);

            System.out.print("Created: ");
            for (int i=1; i<entity.getBody().getFixtureList().size; i++) {
                System.out.print( entity.getBody().getFixtureList().get(i)+ " ");
            }
            System.out.println();

//            Portal.portalLength / 2f;
            if (normal.x == 1) {

            }
            else {

            }
        }

    }
}

class Portal {
    static final float portalLength = 0.4f;

    private Portal otherPortal;

    private Vector2 position;
    private Vector2 normal;
    private Fixture fixture;
    private boolean enabled = false;

    public Portal() {};

    public Portal(Vector2 position, Vector2 normal, Fixture fixture) {
        this.position = position;
        this.normal = normal;
        this.fixture = fixture;
    }

    public void reset(World world, Fixture fixtureHit) {
        System.out.println("RESET");
        if (getFixture() != getOtherPortal().getFixture()) {
            if (fixtureHit.getBody() == getFixture().getBody()) {
                for (int i = fixtureHit.getBody().getFixtureList().size-1; i > 0; i--) {
                    fixtureHit.getBody().destroyFixture(fixtureHit.getBody().getFixtureList().get(i));
                }
            } else {
                for (int i = getFixture().getBody().getFixtureList().size-1; i > 0; i--) {
                    getFixture().getBody().destroyFixture(getFixture().getBody().getFixtureList().get(i));
                }
                for (int i = getOtherPortal().getFixture().getBody().getFixtureList().size-1; i > 0; i--) {
                    getOtherPortal().getFixture().getBody().destroyFixture(getOtherPortal().getFixture().getBody().getFixtureList().get(i));
                }
                getFixture().setSensor(false);
                setFixture(fixtureHit.getBody().getFixtureList().first());
                getFixture().setSensor(true);
            }
        }
        else {
            // if what i am hitting has the other portal on it then ->
            if (fixtureHit.getBody() == getOtherPortal().getFixture().getBody()) {
                System.out.print("Removing: ");
                for (int i = fixtureHit.getBody().getFixtureList().size - 1; i>=1; i--) {
                    System.out.print(fixtureHit.getBody().getFixtureList().get(i));
                    fixtureHit.getBody().destroyFixture(fixtureHit.getBody().getFixtureList().get(i));
                    setFixture(fixtureHit.getBody().getFixtureList().first());
                }
                System.out.println();
            }
            else {
                setFixture(fixtureHit.getBody().getFixtureList().first());
                getFixture().setSensor(true);

                // reset other portal fixture
                // remove fixtures
                Fixture otherPortalFixture = getOtherPortal().getFixture();
                for (int i = otherPortalFixture.getBody().getFixtureList().size-1; i > 0; i--) {
                    otherPortalFixture.getBody().destroyFixture(otherPortalFixture.getBody().getFixtureList().get(i));
                }

                //add new fixtures
                Entity otherEntity = Entity.entityFromBody(otherPortalFixture.getBody());
                Vector2 otherPortalPosition = getOtherPortal().getPosition();
                Vector2 bodyPosition = new Vector2(otherEntity.getPosition());
                Vector2 bodySize = new Vector2(otherEntity.size);

                float width = bodySize.x;

                float liftHeight = bodyPosition.y - bodySize.y/2f;
                float botHeight = otherPortalPosition.y - Portal.portalLength / 2f - liftHeight;
                float topHeight = bodyPosition.y + bodySize.y / 2f - liftHeight - botHeight - Portal.portalLength;

                PolygonShape botShape = new PolygonShape();
                PolygonShape topShape = new PolygonShape();
                botShape.setAsBox(width / 2f, botHeight / 2f, new Vector2(0, -(bodyPosition.y - otherPortalPosition.y) - Portal.portalLength / 2f - botHeight / 2f), 0f);
                topShape.setAsBox(width / 2f, topHeight / 2f, new Vector2(0, otherPortalPosition.y - bodyPosition.y + Portal.portalLength / 2f + topHeight / 2f), 0f);


                FixtureDef botFixtureDef = new FixtureDef();
                botFixtureDef.shape = botShape;
                botFixtureDef.density = otherPortalFixture.getDensity();
                botFixtureDef.friction = otherPortalFixture.getFriction();
                botFixtureDef.restitution = otherPortalFixture.getRestitution();

                FixtureDef topFixtureDef = new FixtureDef();
                topFixtureDef.shape = topShape;
                topFixtureDef.density = otherPortalFixture.getDensity();
                topFixtureDef.friction = otherPortalFixture.getFriction();
                topFixtureDef.restitution = otherPortalFixture.getRestitution();

                otherEntity.getBody().createFixture(botFixtureDef);
                otherEntity.getBody().createFixture(topFixtureDef);

//                System.out.println(getOtherPortal().getFixture().getBody().getFixtureList().size);
//                boolean isHigher = getPosition().y > getOtherPortal().getPosition().y;
//                if (isHigher) {
//                    // Destroy top
//                    Fixture topFixture = getOtherPortal().getFixture().getBody().getFixtureList().get(3);
//                    getOtherPortal().getFixture().getBody().destroyFixture(topFixture);
//
//                    // Stretch mid
//                    Fixture midFixture = getOtherPortal().getFixture().getBody().getFixtureList().get(2);
//                    PolygonShape newMidShape = new PolygonShape();
//
//                    float width =
//                    newMidShape.setAsBox(width, newMidHeight);
//                }

//                reset(world, getOtherPortal().getFixture());

            }
        }
//        Entity pastEntity = Entity.entityFromBody(getFixture().getBody());
//        Entity newEntity = new Entity(pastEntity.world, pastEntity.name, pastEntity.getBody().getPosition(), pastEntity.size,
//                pastEntity.body.getType(), null,
//                pastEntity.getBody().getFixtureList().first().getDensity(),
//                pastEntity.getBody().getFixtureList().first().getFriction(),
//                false, null);
//
////        setFixture(fixtureHit.getBody().getFixtureList().first());
//        if (fixtureHit.getBody() != newEntity.getBody()) {
//            setFixture(fixtureHit.getBody().getFixtureList().first());
//            getFixture().setSensor(true);
//        }
//        else {
////            setFixture(newEntity.getBody().getFixtureList().first());
//
//        }
//        pastEntity.dispose();



//        Body body = getFixture().getBody();
//        BodyDef bodyDef = new BodyDef();
//        bodyDef.type = body.getType();
//        bodyDef.position.set(body.getPosition());
//        bodyDef.fixedRotation = body.isFixedRotation();
//        bodyDef.angle = body.getAngle();
//        bodyDef.active = body.isActive();
//        bodyDef.gravityScale = body.getGravityScale();

//        World world = body.getWorld();
//        Body newBody = world.createBody(bodyDef);
//
//        FixtureDef firstFixtureDef = new FixtureDef();
//        firstFixtureDef.friction = getFixture().getBody().getFixtureList().first().getFriction();
//        firstFixtureDef.density = getFixture().getBody().getFixtureList().first().getDensity();
//        firstFixtureDef.shape = getFixture().getBody().getFixtureList().first().getShape();
//        firstFixtureDef.restitution = getFixture().getBody().getFixtureList().first().getRestitution();
//
//        Fixture fixture = newBody.createFixture(firstFixtureDef);




//        newBody.createFixture(this.fixture);



//        Body pastBody = getFixture().getBody();
//        if (pastBody.getFixtureList().size >= 3) {
//            pastBody.getFixtureList().pop();
//            pastBody.getFixtureList().pop();
//        }
//        setFixture(fixtureHit.getBody().getFixtureList().first());
//        if (pastBody != fixtureHit.getBody()) {
//            pastBody.getFixtureList().first().setSensor(false);
////            System.out.println("YESSIR");
//        }
//        getFixture().setSensor(true);

//        Array<Fixture> fixtures = body.getFixtureList();
//        FixtureDef originalFixtureDef = new FixtureDef();
//        originalFixtureDef.shape = fixtures.get(0).getShape();
//        originalFixtureDef.density = fixtures.get(0).getDensity();
//        originalFixtureDef.friction = fixtures.get(0).getFriction();
//
//        body.getFixtureList().clear();
//        System.out.println(body.getFixtureList().size);
//        body.createFixture(originalFixtureDef);

//        setFixture(newBody.getFixtureList().first());






//        getFixture().setSensor(true);

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

    public Fixture getFixture() {
        return this.fixture;
    }

    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
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
