package com.mygdx.game;

import com.badlogic.gdx.physics.box2d.*;

public class WeakEnemyCollisionListener implements ContactListener {
    private String enemyName = "Enemy1";
    @Override
    public void beginContact(Contact contact) {
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();

        Entity entityA = Entity.entityFromBody(bodyA);
        Entity entityB = Entity.entityFromBody(bodyB);
        if (entityA.getName() == enemyName || entityB.getName() == enemyName) {
            if (entityA.getName() != entityB.getName()) {
                Entity enemyEntity = entityA.getName() == enemyName ? entityA : entityB; // ternary operator, google if your not sure
                Entity otherEntity = entityA.getName() != enemyName ? entityA : entityB;
                if(otherEntity.getName() == "Player"){
                    Player player = (Player)otherEntity;
                    player.alive = false;
                }

            }
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
}
