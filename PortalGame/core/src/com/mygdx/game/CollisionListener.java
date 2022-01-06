package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class CollisionListener implements ContactListener {

    public CollisionListener() {

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

    @Override
    public void beginContact(Contact contact) {
        Fixture f1 = contact.getFixtureA();
        Fixture f2 = contact.getFixtureB();
        if (f1.isSensor() || f2.isSensor()) {
            if (f1.isSensor() && f2.isSensor()) return;

            Fixture solidFixture = f1.isSensor() ? f2 : f1;
            Fixture wallFixture = f1.isSensor() ? f1 : f2;

            Entity e = Entity.entityFromBody(solidFixture.getBody());
            if (e.inPortal) return;

            Portals portals = ((Player) Entity.entityFromName("Player")).portals;
            Integer portalNumber = null;
            if (portals.portals[0].getSurface() == portals.portals[1].getSurface()) {
                float topBoundPortal1 = portals.portals[0].getPosition().y + Portal.portalLength / 2f - e.size.y / 2f;
                float botBoundPortal1 = portals.portals[0].getPosition().y - Portal.portalLength / 2f + e.size.y / 2f;

                float topBoundPortal2 = portals.portals[1].getPosition().y + Portal.portalLength / 2f - e.size.y / 2f;
                float botBoundPortal2 = portals.portals[1].getPosition().y - Portal.portalLength / 2f + e.size.y / 2f;

                if (e.getPosition().y < topBoundPortal1 && e.getPosition().y > botBoundPortal1) portalNumber = 0;
                else if (e.getPosition().y < topBoundPortal2 && e.getPosition().y > botBoundPortal2) portalNumber = 1;
            }
            else {
                portalNumber = wallFixture == portals.portals[0].getSurface() ? 0 : 1;
            }

            if (portalNumber != null) {
                Portal portalEntering = portals.portals[portalNumber];

                boolean goingIntoPortal = portals.isGoingIntoPortal(e, portalEntering);
                if (goingIntoPortal) {
                    portals.linkPortal(solidFixture, portalNumber);
                }
            }
        }
    }
};