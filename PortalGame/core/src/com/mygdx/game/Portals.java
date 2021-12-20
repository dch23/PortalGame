package com.mygdx.game;

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
    }

    public void setPortal(int portalNumber, Vector2 position, Vector2 normal, boolean enabled, Fixture fixture) {
//        System.out.println(Entity.entityFromBody(fixture.getBody()));

        if (portals[portalNumber].getFixture() != null) {
            portals[portalNumber].reset();
            return;
        }
//        if (fixture==null) return;
        // set for certain portal number
        portals[portalNumber].setPosition(position);
        portals[portalNumber].setNormal(normal);
        portals[portalNumber].setEnabled(enabled);


        portals[portalNumber].setFixture(fixture);
        portals[portalNumber].getFixture().setSensor(true);

        if (normal.y == 0) {
            Entity entity = Entity.entityFromBody(fixture.getBody());
            Vector2 bodyPosition = new Vector2(entity.getPosition());
            Vector2 bodySize = new Vector2(entity.size);

            float width = bodySize.x-0.09f;
            float botBodyHeight = bodyPosition.y - bodySize.y/2f;

            float botHeight = position.y - Portal.portalLength/2f - botBodyHeight;
            float topHeight = bodyPosition.y + bodySize.y / 2f - botBodyHeight - botHeight - Portal.portalLength;

            PolygonShape botShape = new PolygonShape();
            botShape.setAsBox(width / 2f, botHeight / 2f, new Vector2(0,-(bodyPosition.y - position.y)- Portal.portalLength/2f - botHeight/2f), 0f);
            PolygonShape topShape = new PolygonShape();
            topShape.setAsBox(width / 2f, topHeight / 2f, new Vector2(0, position.y - bodyPosition.y + Portal.portalLength/2f + topHeight/2f), 0f);

            FixtureDef botFixtureDef = new FixtureDef();
            botFixtureDef.shape = botShape;
            botFixtureDef.density = fixture.getDensity();
            botFixtureDef.friction = fixture.getFriction();
            botFixtureDef.restitution = fixture.getRestitution();

            FixtureDef topFixtureDef = new FixtureDef();
            topFixtureDef.shape = topShape;
            topFixtureDef.density = fixture.getDensity();
            topFixtureDef.friction = fixture.getFriction();
            topFixtureDef.restitution = fixture.getRestitution();

            entity.body.createFixture(topFixtureDef);
            entity.body.createFixture(botFixtureDef);

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

    public void reset() {
        Body body = this.fixture.getBody();
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = body.getType();
        bodyDef.position.set(body.getPosition());
        bodyDef.fixedRotation = body.isFixedRotation();
        bodyDef.angle = body.getAngle();
        bodyDef.active = body.isActive();
        bodyDef.gravityScale = body.getGravityScale();

        World world = body.getWorld();
        world.destroyBody(body);
        Body newBody = world.createBody(bodyDef);
//        newBody.createFixture(this.fixture);



//        Body body = this.fixture.getBody();
//        body.getFixtureList().pop();
//        body.getFixtureList().pop();
//        Array<Fixture> fixtures = body.getFixtureList();
//        FixtureDef originalFixtureDef = new FixtureDef();
//        originalFixtureDef.shape = fixtures.get(0).getShape();
//        originalFixtureDef.density = fixtures.get(0).getDensity();
//        originalFixtureDef.friction = fixtures.get(0).getFriction();
//
//        body.getFixtureList().clear();
//        System.out.println(body.getFixtureList().size);
//        body.createFixture(originalFixtureDef);

        setFixture(null);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Fixture getFixture() {
        return fixture;
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
