package com.mygdx.game;

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
            System.out.println("out portal");
            Fixture solidFixture = f1.isSensor() ? f2 : f1;
            Fixture wallFixture = f1.isSensor() ? f1 : f2;
            this.portals.unlinkPortal(solidFixture);
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
            System.out.println("in portal");
            Fixture solidFixture = f1.isSensor() ? f2 : f1;
            Fixture wallFixture = f1.isSensor() ? f1 : f2;
            this.portals.linkPortal(solidFixture, wallFixture);
        }
    }
};