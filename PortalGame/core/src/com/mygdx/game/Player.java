package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
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
    // used for drawing a debug line for the mouse (line from player to mouse)
    Renderer debugRenderer;

    // variables that control the player values
    private float speed = 2f;
    private float jumpHeight = 4f;
    private float frictionMagnitude = 0.6f;

    // Vector 2's have two values, x and y. y in this case will be = 1 if the left key is pressed and x in this case will be = 1 if the right key is pressed.
    private Vector2 inputHoriz = Vector2.Zero;

    // not used
    private float groundDistance = 0f;
    private float groundDistanceJumpThreshold = 0.4f;

    // the portal variables
    public Portals portals;
    private float maxShootPortalDistance = 100f;                    // the farthest you can shoot a portal
    private ArrayList<RayHitInfo> raysHitInfo = new ArrayList<>();  // finding a portal surface when shooting a portal is done by constantly adding ray information to this array every time a ray is created when mouse clicked
    private RayHitInfo closestRayHitInfo;                           // used to set the closest plausible surface to put a portal on
    private Vector2 mousePos; // used to keep track of the position of the mouse

    // Player Properties

    //animations



    public Player(World world, OrthographicCamera camera, String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        // constructor similarity to the entity is set with super
        super(world, name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);

        // lock the rotation of the player
        this.body.setFixedRotation(true);
        this.portals = new Portals(this.world);     // create the portals instance
        this.debugRenderer = new Renderer(camera);  // set a debug renderer to draw lines

        addAnimation("Idle", "Characters/Wizard Pack/Idle.png", 6, true, 0.3f);
        addAnimation("Run", "Characters/Wizard Pack/Run.png", 8, true, 0.3f);
        addAnimation("Jump", "Characters/Wizard Pack/Jump.png", 2, true, 0.3f);
        addAnimation("Fall", "Characters/Wizard Pack/Fall.png", 2, true, 0.3f);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keyCode) {           // if a key is pressed down (holding the key only fires this function once still)
                switch (keyCode) {                          // switch statement (look up if your not sure how it works)
                    case Inputs.Keys.KEY_RIGHT:
                    case Inputs.Keys.ARROW_RIGHT:
                        inputHoriz.x = 1f;                  // if the right arrow key or the right button key is pressed then set the inputHoriz x to 1
                        break;
                    case Inputs.Keys.KEY_LEFT:
                    case Inputs.Keys.ARROW_LEFT:
                        inputHoriz.y = 1f;                  // if the left arrow or left button key is pressed then set input horiz y to 1
                        break;
                    case Inputs.Keys.KEY_UP:
                    case Inputs.Keys.ARROW_UP:
                        jump();
                        break;
                    case Inputs.Keys.ARROW_DOWN:
                        break;
                }
                return true;        // must return a value because it is a boolean function
            }
            public boolean keyUp(int keyCode) {
                switch (keyCode) {
                    case Inputs.Keys.KEY_RIGHT:
                    case Inputs.Keys.ARROW_RIGHT:           // if the right arrow or right button key is released then reset the inputHoriz x to 0
                        inputHoriz.x = 0f;
//                        body.setLinearVelocity(slowDownSpeed, body.getLinearVelocity().y);
                        break;
                    case Inputs.Keys.KEY_LEFT:
                    case Inputs.Keys.ARROW_LEFT:
                        inputHoriz.y = 0f;                  // if the left arrow or left button key is released the reset the inputHoriz y to 0
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
                mousePos = new Vector2(x*MyGdxGame.GAME_SCALE, (MyGdxGame.SCENE_HEIGHT-y) * MyGdxGame.GAME_SCALE);
                return true;
            }
        });
    }


    // right now the on ground function just returns true, havent found a good way to check if on ground
    private boolean onGround() {
        final ArrayList<RayHitInfo> myRaysHitInfo = new ArrayList<>();            // refresh the rays information list
        RayHitInfo myClosestRayHitInfo = null;                   // reset the closest ray to nothing

        // shooting a ray is done by ray callbacks, read about rays on libgdx docs, learn about Vector2 normal, most likely dont need to know about fraction variable
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture == null || point == null || normal == null) return 0;
                // Multiple hits
                myRaysHitInfo.add(new RayHitInfo(fixture, new Vector2(point), new Vector2(normal), fraction));
                return 1;
            }
        };

        // look at the world.rayCast function on the libgdx docs and see what parameters you must provide
        Vector2 endOfRay = PMath.addVector2(getPosition(), new Vector2(0, -maxGroundRayDistance));
        world.rayCast(callback, getPosition(), endOfRay);

        // Finding the closest ray hit through a searching algorithm
        if (myRaysHitInfo != null) {
            if (myRaysHitInfo.size() == 0) return false;
            for (RayHitInfo rayHitInfo : myRaysHitInfo) {
                if (!rayHitInfo.fixture.isSensor()) if (myClosestRayHitInfo == null) myClosestRayHitInfo = rayHitInfo;
                if (myClosestRayHitInfo == null) continue;

                float distance1 = PMath.magnitude(PMath.subVector2(myClosestRayHitInfo.point, this.body.getPosition()));
                float distance2 = PMath.magnitude(PMath.subVector2(rayHitInfo.point, this.body.getPosition()));
                if (distance2 < distance1 && !rayHitInfo.fixture.isSensor()) {
                    myClosestRayHitInfo = rayHitInfo;
                }
            }
        }
        if (myClosestRayHitInfo == null) return false;

        Vector2 bottom = PMath.addVector2(getPosition(), new Vector2(0, -size.y/2f));
        float distanceFromGround = PMath.magnitude(PMath.subVector2(myClosestRayHitInfo.point, bottom));
        return distanceFromGround < closeEnoughToGround;
    }

    // the control function allows for input reactions
    private void control() {
        // process the input

        if (inputHoriz.x - inputHoriz.y != 0) { // if the right and left input values have a difference other than 0, then set the player velocity to a value,
                                                // the only case that they would have a difference of 0 is when both of them are pressed,
                                                // so you wouldn't want to change the velocity if right and left are pressed down.
            float direction = (inputHoriz.x - inputHoriz.y);

            body.setLinearVelocity(Math.max(speed, Math.abs(body.getLinearVelocity().x)) * direction, body.getLinearVelocity().y);
            horizontalFaceDirection = (int) direction;

            if (this.alive && onGround()) {
                this.currentAnimation = "Run";
            }
        }
        else {
            if (this.alive) {
                if (onGround()) {
                    this.currentAnimation = "Idle";
                }
            }
        }
        if (!onGround() && alive) {
            if (getBody().getLinearVelocity().y > 0) {
                this.currentAnimation = "Jump";
            }
            else if (getBody().getLinearVelocity().y < 0) {
                this.currentAnimation = "Fall";
            }
        }



    }

    private void jump() {
        if (onGround() && alive) {
            body.setLinearVelocity(body.getLinearVelocity().x, jumpHeight);     // if on the ground, then set the y speed to the jump height, this simulates a sort of impact force upwards
        }
    }


    private void shootPortal(final int portalNumber) {
        raysHitInfo = new ArrayList<>();            // refresh the rays information list
        closestRayHitInfo = null;                   // reset the closest ray to nothing

        // Set portal
        Vector2 mousePosition = new Vector2(Gdx.input.getX() * MyGdxGame.GAME_SCALE, (MyGdxGame.SCENE_HEIGHT - Gdx.input.getY()) * MyGdxGame.GAME_SCALE); // getting the mouse position (MUST USE GAME SCALE)

        // shooting a ray is done by ray callbacks, read about rays on libgdx docs, learn about Vector2 normal, most likely dont need to know about fraction variable
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture == null || point == null || normal == null) return 0;

//                mousePos = point;
//                System.out.println("HIT");
                // Multiple hits
                raysHitInfo.add(new RayHitInfo(fixture, new Vector2(point), new Vector2(normal), fraction));
                return 1;
            }
        };

//        mousePos = PMath.addVector2(this.body.getPosition(),
//                PMath.multVector2(PMath.normalizeVector2(PMath.subVector2(mousePosition,this.body.getPosition())), maxShootPortalDistance));

        // call raycast in world from player position to the max distance away from it based off the maxShootPortalDistance
        // look at the world.rayCast function on the libgdx docs and see what parameters you must provide
        world.rayCast(callback, this.body.getPosition(), PMath.addVector2(this.body.getPosition(),
                PMath.multVector2(PMath.normalizeVector2(PMath.subVector2(mousePosition,this.body.getPosition())), maxShootPortalDistance)));

        // Finding the closest ray hit through a searching algorithm
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
//        for (RayHitInfo r : raysHitInfo) {
//            float d = PMath.magnitude(PMath.subVector2(r.point, this.body.getPosition()));
//            r.print();
//        }
//        System.out.println();
        // ngl, i have no idea why i added the next line
        closestRayHitInfo = getOriginalFixtureHitInfo(raysHitInfo, closestRayHitInfo);

        // set a portal to the closest plausible surface in the direction you click the mouse
        if (closestRayHitInfo != null) {
            if (closestRayHitInfo.fixture.getBody().getType() == BodyDef.BodyType.StaticBody) {
                if (properPortalNormal(closestRayHitInfo.normal)) {
                    portals.setPortal(world, portalNumber, closestRayHitInfo.point, closestRayHitInfo.normal, true, closestRayHitInfo.fixture);
                }
            }
        }
    }

    private boolean properPortalNormal(Vector2 normal) {
//        System.out.println(normal);
        return normal.equals(new Vector2(1,0)) || normal.equals(new Vector2(-1,0)) || normal.equals(new Vector2(0,1)) || normal.equals(new Vector2(0,-1));
    }
    private RayHitInfo getOriginalFixtureHitInfo(ArrayList<RayHitInfo> hitInfos, RayHitInfo closestRayHitInfo) {
        Fixture originalFixture = closestRayHitInfo.fixture.getBody().getFixtureList().first();
        for (int i=0; i<hitInfos.size(); i++) {
            if (hitInfos.get(i).fixture == originalFixture) return hitInfos.get(i);
        }
        return null;
    }

    private void friction () {
        float xVelocity = body.getLinearVelocity().x;
        float direction = xVelocity / Math.abs(xVelocity);                          // getting the direction the player is traveling in the x axis

        // creating a variable that is the same as the x velocity except it is reduced by the friction magnitude and still pointed towards the direction it needs to go
        float newXVelocity = (Math.abs(xVelocity) - frictionMagnitude) * direction;
        float newDirection = newXVelocity / Math.abs(newXVelocity);                 // getting the new direction that the nex x velocity is pointing to
        if (direction == newDirection) {                                            // if the old direction is the same as the new direction then
            body.setLinearVelocity(newXVelocity, body.getLinearVelocity().y);       // set the velocity accordingly
        }
        else {
            body.setLinearVelocity(0, body.getLinearVelocity().y);              // if the direction changes to the other direction (as in the friction makes it not only reach 0, but goes past 0 is some cases) then automatically set it to 0
        }
    }

    public void operate() {
        // mousePos = new Vector2(Gdx.input.getX() * MyGdxGame.GAME_SCALE, Gdx.input.getY() * MyGdxGame.GAME_SCALE);


        if (alive) {
            control();
        }
        else {
            die();
        }
        friction();

        if (mousePos != null) this.debugRenderer.debugLine(this.body.getPosition(), mousePos, Color.WHITE);
    }

    private void die() {
        getBody().getFixtureList().first().setSensor(true);
    }
}
