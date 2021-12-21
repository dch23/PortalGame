package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

import javax.sound.sampled.Port;
import java.util.ArrayList;

public class Player extends Entity {
    Renderer debugRenderer;

    private float speed = 3f;
    private float jumpHeight = 3f;
    private float frictionMagnitude = 0.6f;
    private Vector2 inputHoriz = Vector2.Zero;

    private float groundDistance = 0f;
    private float groundDistanceJumpThreshold = 0.2f;

    private Portals portals;
    private float maxShootPortalDistance = 100f;
    private ArrayList<RayHitInfo> raysHitInfo = new ArrayList<>();
    private RayHitInfo closestRayHitInfo;
    private Vector2 mousePos;

    public Player(World world, OrthographicCamera camera, String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(world, name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);
        this.body.setFixedRotation(true);
        this.portals = new Portals();
        this.debugRenderer = new Renderer(camera);
    }

    private boolean onGround() {
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                groundDistance = body.getPosition().y - point.y;
                return 0;
            }
        };
        Vector2 bottom = body.getPosition();
//        Vector2 bottom = new Vector2(body.getPosition().x, body.getPosition().y - size.y/2f);
        Vector2 endRay = new Vector2(bottom);
        endRay.add(0,-100f);

        world.rayCast(callback, bottom, endRay);

        return (groundDistance < groundDistanceJumpThreshold);
    }

    private void control() {
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean keyDown(int keyCode) {
                switch (keyCode) {
                    case Inputs.Keys.KEY_RIGHT:
                    case Inputs.Keys.ARROW_RIGHT:
                        inputHoriz.x = 1f;
                        break;
                    case Inputs.Keys.KEY_LEFT:
                    case Inputs.Keys.ARROW_LEFT:
                        inputHoriz.y = 1f;
                        break;
                    case Inputs.Keys.KEY_UP:
                    case Inputs.Keys.ARROW_UP:
                        if (onGround()) {
                            body.setLinearVelocity(body.getLinearVelocity().x, jumpHeight);
                        }
                        break;
                    case Inputs.Keys.ARROW_DOWN:
                        break;
                }
                return true;
            }
            public boolean keyUp(int keyCode) {
                switch (keyCode) {
                    case Inputs.Keys.KEY_RIGHT:
                    case Inputs.Keys.ARROW_RIGHT:
                        inputHoriz.x = 0f;
//                        body.setLinearVelocity(slowDownSpeed, body.getLinearVelocity().y);
                        break;
                    case Inputs.Keys.KEY_LEFT:
                    case Inputs.Keys.ARROW_LEFT:
                        inputHoriz.y = 0f;
//                        body.setLinearVelocity(-slowDownSpeed, body.getLinearVelocity().y);
                        break;
                    case Inputs.Keys.KEY_UP:
                    case Inputs.Keys.ARROW_UP:
                        break;
                    case Inputs.Keys.ARROW_DOWN:
                        break;
                }
                return true;
            }

            // portal control
            @Override
            public boolean touchDown(int x, int y, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
//                    mousePos = new Vector2(x * MyGdxGame.GAME_SCALE,(MyGdxGame.SCENE_HEIGHT-y) * MyGdxGame.GAME_SCALE);
                    shootPortal(0);
                }
                else {
                    shootPortal(1);
                }

                return true;
            }
            public boolean mouseMoved(int x, int y) {
//                mousePos = new Vector2(x*MyGdxGame.GAME_SCALE, (MyGdxGame.SCENE_HEIGHT-y) * MyGdxGame.GAME_SCALE);
                return true;
            }
        });
        if (inputHoriz.x - inputHoriz.y != 0) {
            body.setLinearVelocity(Math.max(speed, Math.abs(body.getLinearVelocity().x)) * (inputHoriz.x - inputHoriz.y), body.getLinearVelocity().y);
        }

    }


    private void shootPortal(final int portalNumber) {
        raysHitInfo = new ArrayList<>();
        closestRayHitInfo = null;

        // Set portal
        Vector2 mousePosition = new Vector2(Gdx.input.getX() * MyGdxGame.GAME_SCALE, (MyGdxGame.SCENE_HEIGHT - Gdx.input.getY()) * MyGdxGame.GAME_SCALE);

        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture == null || point == null || normal == null) return 0;

//                mousePos = point;
//                System.out.println("HIT");
                // Multiple hits
                raysHitInfo.add(new RayHitInfo(fixture, new Vector2(point), normal, fraction));
                return 1;
            }
        };

//        mousePos = PMath.addVector2(this.body.getPosition(),
//                PMath.multVector2(PMath.normalizeVector2(PMath.subVector2(mousePosition,this.body.getPosition())), maxShootPortalDistance));

        // call raycast in world from player position to the max distance away from it based off the maxShootPortalDistance
        world.rayCast(callback, this.body.getPosition(), PMath.addVector2(this.body.getPosition(),
                PMath.multVector2(PMath.normalizeVector2(PMath.subVector2(mousePosition,this.body.getPosition())), maxShootPortalDistance)));

        // Finding closest ray hit
        if (raysHitInfo!=null) {
            if (raysHitInfo.size() == 0) return;
            closestRayHitInfo = raysHitInfo.get(0);
            for (RayHitInfo rayHitInfo : raysHitInfo) {
                float distance1 = PMath.magnitude(PMath.subVector2(closestRayHitInfo.point, this.body.getPosition()));
                float distance2 = PMath.magnitude(PMath.subVector2(rayHitInfo.point, this.body.getPosition()));
                if (distance2 < distance1) {
                    closestRayHitInfo = rayHitInfo;
                }
            }
        }

        // portal to the closest
        if (closestRayHitInfo != null) {
            if (closestRayHitInfo.fixture.getBody().getType() == BodyDef.BodyType.StaticBody) {
                if (properPortalNormal(closestRayHitInfo.normal)) {
//                    System.out.println(closestRayHitInfo.fixture.getBody());
                    portals.setPortal(world, portalNumber, closestRayHitInfo.point, closestRayHitInfo.normal, true, closestRayHitInfo.fixture);
                }
            }
        }
    }

    private boolean properPortalNormal(Vector2 normal) {
//        System.out.println(normal);
        return normal.equals(new Vector2(1,0)) || normal.equals(new Vector2(-1,0)) || normal.equals(new Vector2(0,1)) || normal.equals(new Vector2(0,-1));
    }

    private void airResistance() {  // Doesnt work
//        Vector2 position = new Vector2(body.getPosition());
//        Vector2 velocity = new Vector2(body.getLinearVelocity());
//        Vector2 direction = new Vector2(velocity.nor());
//
//
//        direction = new Vector2(-direction.x, -direction.y);
//        Vector2 airResistanceVector = new Vector2(direction.x * airResistanceMagnitude, direction.y * airResistanceMagnitude);
//        if (PMath.magnitude(velocity) - airResistanceMagnitude > 0f) {
//            body.setLinearVelocity(new Vector2(velocity.x - airResistanceVector.x, velocity.y - airResistanceVector.y));
//        }

//        float magnitude = PMath.magnitude(velocity);
//
//        if (magnitude - airResistanceMagnitude > 0f) {
////            velocity = velocity.add(airResistanceVector);
//        }
//        body.setLinearVelocity(velocity);

//        direction = direction.nor();
//        direction = Vector2.Zero.mulAdd(direction, -airResistanceMagnitude);
//
//        Vector2 newPosition = new Vector2(body.getPosition().add(body.getLinearVelocity()).add(direction));
//        Vector2 newDirection = new Vector2(Vector2.Zero.mulAdd(body.getPosition(), -1f));
//        if (direction == newDirection) System.out.println("PROLLY WORK");
    }

    private void friction () {
        float xVelocity = body.getLinearVelocity().x;
        float direction = xVelocity / Math.abs(xVelocity);
        float newXVelocity = (Math.abs(xVelocity) - frictionMagnitude) * direction;
        float newDirection = newXVelocity / Math.abs(newXVelocity);
        if (direction == newDirection) {
            body.setLinearVelocity(newXVelocity, body.getLinearVelocity().y);
        }
        else {
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }
    }

    public void operate() {
//        this.body.setLinearVelocity(new Vector2(-0.1f,this.body.getLinearVelocity().y));
        control();
        friction();


//        mousePos = new Vector2(Gdx.input.getX() * MyGdxGame.GAME_SCALE, Gdx.input.getY() * MyGdxGame.GAME_SCALE);
//        if (mousePos != null) {
//            System.out.p
//            this.debugRenderer.debugLine(this.body.getPosition(), mousePos, Color.WHITE);
//        }
//        airResistance();
    }
}
