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

        Entity e1 = Entity.entityFromBody(f1.getBody());
        Entity e2 = Entity.entityFromBody(f2.getBody());

        // sort entity alphabetically
        Entity tempE = e1;
        Fixture tempF = f1;
        if (e1.getName().compareTo(e2.getName()) > 0) {
            e1 = e2;
            e2 = tempE;
            f1 = f2;
            f2 = tempF;
        }

        String contactString = (f1.isSensor() ? "is sensor " : "not sensor ") + e1.getName() + ", " +
                (f2.isSensor() ? "is sensor " : "not sensor ") + e2.getName();


        switch (contactString) {
            case "not sensor Player, not sensor weakEnemy":
//                e1.alive = false;
                break;
//            case "is sensor map object, not sensor weakEnemy":
//            case "not sensor Player, is sensor map object":
            case "is sensor fireball, is sensor portal collider":
            case "is sensor portal collider, is sensor fireball":
            case "is sensor portal collider, not sensor weakEnemy":
            case "not sensor weakEnemy, is sensor portal collider":
            case "is sensor portal collider, not sensor midEnemy":
            case "not sensor midEnemy, is sensor portal collider":
            case "is sensor portal collider, not sensor Player":
            case "not sensor Player, is sensor portal collider":

//                System.out.println("EEE");
                // portals
//                Fixture solidFixture = f1.isSensor() ? f2 : f1;
//                Fixture wallFixture = f1.isSensor() ? f1 : f2;

                Fixture wallFixture = (e1.getBody().getType().equals(BodyDef.BodyType.StaticBody) ? f1 : f2);
                Fixture solidFixture = (wallFixture == f1 ? f2 : f1);

                Entity e = Entity.entityFromBody(solidFixture.getBody());
                if (e.inPortal) return;


                Portals portals = Player.player.portals;

                Integer portalNumber = null;
//                if (portals.portals[0].getSurface() == portals.portals[1].getSurface()) {
//                    float topBoundPortal1 = portals.portals[0].getPosition().y + Portal.portalLength / 2f - e.size.y / 2f;
//                    float botBoundPortal1 = portals.portals[0].getPosition().y - Portal.portalLength / 2f + e.size.y / 2f;
//
//                    float topBoundPortal2 = portals.portals[1].getPosition().y + Portal.portalLength / 2f - e.size.y / 2f;
//                    float botBoundPortal2 = portals.portals[1].getPosition().y - Portal.portalLength / 2f + e.size.y / 2f;
//
//                    if (e.getPosition().y < topBoundPortal1 && e.getPosition().y > botBoundPortal1) portalNumber = 0;
//                    else if (e.getPosition().y < topBoundPortal2 && e.getPosition().y > botBoundPortal2) portalNumber = 1;
//                }
//                else {
//                    portalNumber = wallFixture == portals.portals[0].getSurface() ? 0 : 1;
//                }

//                if (portals.portals[0].getSurface() == portals.portals[1].getSurface()) {
//                    float topBoundPortal1 = portals.portals[0].getPosition().y + Portal.portalLength / 2f - e.size.y / 2f;
//                    float botBoundPortal1 = portals.portals[0].getPosition().y - Portal.portalLength / 2f + e.size.y / 2f;
//
//                    float topBoundPortal2 = portals.portals[1].getPosition().y + Portal.portalLength / 2f - e.size.y / 2f;
//                    float botBoundPortal2 = portals.portals[1].getPosition().y - Portal.portalLength / 2f + e.size.y / 2f;
//
//                    if (e.getPosition().y < topBoundPortal1 && e.getPosition().y > botBoundPortal1) portalNumber = 0;
//                    else if (e.getPosition().y < topBoundPortal2 && e.getPosition().y > botBoundPortal2) portalNumber = 1;
//                }
//                else {
//                    portalNumber = wallFixture == portals.portals[0].getColliderFixture() ? 0 : 1;
//                }
                portalNumber = (wallFixture == portals.portals[0].getColliderFixture() ? 0 : 1);

                if (portalNumber != null) {
                    if (!portals.properPositionToPortal(portals.portals[portalNumber], e)) break;
//                    System.out.println("trying to go in!");
                    Portal portalEntering = portals.portals[portalNumber];

                    boolean goingIntoPortal = portals.isGoingIntoPortal(e, portalEntering);
//                    System.out.println("is " + e.getName() + " going into " + portalEntering + "? " + goingIntoPortal);
//                    if (e1.getName().equals("fireball") || e2.getName().equals("fireball")) System.out.println("should work");

                    if (goingIntoPortal) {
                        portals.linkPortal(solidFixture, portalNumber);
                    }
                }
                break;
            case "not sensor Player, is sensor exit door":
                if (MyGdxGame.currentLevel+1 == MyGdxGame.maps.size()) break;
                MyGdxGame.currentLevel += 1;
                MyGdxGame.updateLevel = true;
                break;
        }

//        if (f1.isSensor() || f2.isSensor()) {
//            if (f1.isSensor() && f2.isSensor()) return;
//
//
//        }
    }
};