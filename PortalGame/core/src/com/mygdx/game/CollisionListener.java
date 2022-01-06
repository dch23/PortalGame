package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class CollisionListener implements ContactListener {
    private Portals portals;

    public CollisionListener(Portals portals) {
        this.portals = portals;
    }

    @Override
    public void endContact(Contact contact) {
        Fixture f1 = contact.getFixtureA();
        Fixture f2 = contact.getFixtureB();
//        Entity e1 = Entity.entityFromBody(f1.getBody());
//        Entity e2 = Entity.entityFromBody(f2.getBody());
        if (f1.isSensor() || f2.isSensor()) {
            if (f1.isSensor() && f2.isSensor()) return;
//            System.out.println("out portal");
            Fixture solidFixture = f1.isSensor() ? f2 : f1;
            Fixture wallFixture = f1.isSensor() ? f1 : f2;
        }
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
//        Entity e1 = Entity.entityFromBody(f1.getBody());
//        Entity e2 = Entity.entityFromBody(f2.getBody());
        if (f1.isSensor() || f2.isSensor()) {
            if (f1.isSensor() && f2.isSensor()) return;

            Fixture solidFixture = f1.isSensor() ? f2 : f1;
            Fixture wallFixture = f1.isSensor() ? f1 : f2;

//            System.out.println(solidFixture.getBody().getLinearVelocity());
            Entity e = Entity.entityFromBody(solidFixture.getBody());
            if (e.inPortal) return;
//            System.out.println("in portal");
//            System.out.println("entering with an x vel of: " + e.getBody().getLinearVelocity().x);


//            if (portals.isGoingIntoPortal(e, portals.portals[0]))
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
//                int properDirection = (int) (Math.abs(portalEntering.getPosition().x - e.getPosition().x) / (portalEntering.getPosition().x - e.getPosition().x));
//                int realDirection = (int) (Math.abs(e.getBody().getLinearVelocity().x) / e.getBody().getLinearVelocity().x);
//                boolean goingIntoPortal = properDirection == realDirection;
//                System.out.println(properDirection + " vs. " + realDirection);

                boolean goingIntoPortal = portals.isGoingIntoPortal(e, portalEntering);
//                System.out.println(goingIntoPortal ? "Going in to portal" : "Not going into portal");
                if (goingIntoPortal) {
                    this.portals.linkPortal(solidFixture, portalNumber);
                }
            }
        }
    }
};