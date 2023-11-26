package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;

import java.util.ArrayList;
import java.util.HashMap;

public class Player extends Entity {

    static final HashMap<String, Boolean> ignoreMapForRays = new HashMap<String, Boolean>() {{
        put("block enemy", true);
        put("portal collider", true);
        put("fireTrail", true);
    }};

    static protected Player player;
    static public Vector2 regularSize = new Vector2(0.15f,0.4f);

    // used for drawing a debug line for the mouse (line from player to mouse)
    Renderer debugRenderer;

    // variables that control the player values
    private float speed = 2f;
    private float jumpHeight = 2.5f;
    private float groundFrictionMagnitude = 0.6f;
    private float airFrictionMagnitude = 0.2f;

    // Vector 2's have two values, x and y. y in this case will be = 1 if the left key is pressed and x in this case will be = 1 if the right key is pressed.
    private Vector2 inputHoriz = new Vector2(0,0);

    // the portal variables
    public Portals portals;
    private float maxShootPortalDistance = 100f;                    // the farthest you can shoot a portal
    private ArrayList<RayHitInfo> raysHitInfo = new ArrayList<>();  // finding a portal surface when shooting a portal is done by constantly adding ray information to this array every time a ray is created when mouse clicked
    private RayHitInfo closestRayHitInfo;                           // used to set the closest plausible surface to put a portal on
    private Vector2 mousePos; // used to keep track of the position of the mouse


    // return to menu after death delay
    private float deathTime;
    private float restartDelay = 3f;


    public Player(OrthographicCamera camera, String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        // constructor similarity to the entity is set with super
        super(name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);

        // configure
        animationTextureSizeScale = 1.4f;

        // lock the rotation of the player
        this.body.setFixedRotation(true);
        this.portals = new Portals(this.world);     // create the portals instance
        this.debugRenderer = new Renderer(new SpriteBatch(), camera);  // set a debug renderer to draw lines

        // animations
        addAnimation("Idle", "Characters/Wizard Pack/Idle.png", 6, true, 0.3f);
        addAnimation("Run", "Characters/Wizard Pack/Run.png", 8, true, 0.3f);
        addAnimation("Jump", "Characters/Wizard Pack/Jump.png", 2, true, 0.3f);
        addAnimation("Fall", "Characters/Wizard Pack/Fall.png", 2, true, 0.3f);
        addAnimation("Death", "Characters/Wizard Pack/Death.png", 7, false, 0.3f);

        // sounds
        sounds.put("Jump1", Gdx.audio.newSound(Gdx.files.internal("Characters/Wizard Pack/Sound/WizardJump1.mp3")));
        sounds.put("Jump2", Gdx.audio.newSound(Gdx.files.internal("Characters/Wizard Pack/Sound/WizardJump2.mp3")));
        sounds.put("PortalShoot1", Gdx.audio.newSound(Gdx.files.internal("Characters/Wizard Pack/Sound/PortalShoot1.mp3")));
        sounds.put("PortalShoot2", Gdx.audio.newSound(Gdx.files.internal("Characters/Wizard Pack/Sound/PortalShoot2.mp3")));
        sounds.put("EnteringPortal", Gdx.audio.newSound(Gdx.files.internal("music/portal jumping (1).mp3")));
        sounds.put("Death", Gdx.audio.newSound(Gdx.files.internal("Characters/Wizard Pack/Sound/losing sound effect (1) (1).mp3")));
        sounds.put("Walking", Gdx.audio.newSound(Gdx.files.internal("Characters/Wizard Pack/Sound/walking.mp3")));

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
                int portalNumber = 0;
                if (button == Input.Buttons.LEFT) {
//                    mousePos = new Vector2(x * MyGdxGame.GAME_SCALE,(MyGdxGame.SCENE_HEIGHT-y) * MyGdxGame.GAME_SCALE);
                    shootPortal(0);
                }
                else {
                    shootPortal(1);
                    portalNumber = 1;
                }

                return true;
            }
            public boolean mouseMoved(int x, int y) {
                mousePos = new Vector2(x*MyGdxGame.GAME_SCALE, (MyGdxGame.SCENE_HEIGHT-y) * MyGdxGame.GAME_SCALE);
                return true;
            }
        });
        player = this;
    }


    // right now the on ground function just returns true, havent found a good way to check if on ground
    private boolean onGround() {
        boolean grounded = false;
        ArrayList<RayHitInfo> rays = new ArrayList<>();

        rays.add(PMath.getClosestRayHitInfo(world, getPosition(), new Vector2(0,-1), maxGroundRayDistance, false, ignoreMapForRays));
        rays.add(PMath.getClosestRayHitInfo(world, PMath.addVector2(getPosition(), new Vector2(-size.x/2f, 0)), new Vector2(0,-1), maxGroundRayDistance, false, ignoreMapForRays));
        rays.add(PMath.getClosestRayHitInfo(world, PMath.addVector2(getPosition(), new Vector2(size.x/2f, 0)), new Vector2(0,-1), maxGroundRayDistance, false, ignoreMapForRays));

        Vector2 bottom = PMath.addVector2(getPosition(), new Vector2(0,-size.y/2f));
        for (RayHitInfo ray : rays) {
            if (ray == null) continue;
            float distanceFromGround = Math.abs(ray.point.y - bottom.y);
            if (distanceFromGround <= closeEnoughToGround) {
                grounded = true;
                break;
            }
        }
        return grounded;

//        if (groundRayHitInfo != null) {
//            Vector2 bottom = PMath.addVector2(getPosition(), new Vector2(0,-size.y/2f));
//            float distanceFromGround = PMath.magnitude(PMath.subVector2(groundRayHitInfo.point, bottom));
//            grounded = distanceFromGround <= closeEnoughToGround;
//        }
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

            int jumpNumber = PMath.getRandomRangeInt(1,3);
            Sound jumpSound = (jumpNumber==1 ? sounds.get("Jump1") : sounds.get("Jump2"));
            AudioManager.playSound(jumpSound, 0.5f, false, false);
        }
    }

    private void shootPortal(final int portalNumber) {
        // alive?
        if (!alive) return;

        //data
        Vector2 mousePosition = new Vector2(Gdx.input.getX() * MyGdxGame.GAME_SCALE, (MyGdxGame.SCENE_HEIGHT - Gdx.input.getY()) * MyGdxGame.GAME_SCALE); // getting the mouse position (MUST USE GAME SCALE)
        Vector2 shootDirection = PMath.normalizeVector2(PMath.subVector2(mousePosition,getPosition()));
        RayHitInfo closestRayHitInfo = PMath.getClosestRayHitInfo(world, getPosition(), shootDirection, maxShootPortalDistance, true, ignoreMapForRays);

        // trail
        Vector2 trailEndPoint = PMath.addVector2(getPosition(), PMath.multVector2(shootDirection, 100));
        if (closestRayHitInfo != null) trailEndPoint = closestRayHitInfo.point;
        PortalTrails.addTrail(getPosition(), trailEndPoint, portals.portals[portalNumber].trailColor);

        //sound
        Sound shootSound = (portalNumber == 1) ? sounds.get("PortalShoot1") : sounds.get("PortalShoot2");
        AudioManager.playSound(shootSound, 0.3f, false, false);


        if (closestRayHitInfo == null) return;

        // see if it is a valid place to place a portal
        Entity surfaceEntity = Entity.entityFromBody(closestRayHitInfo.fixture.getBody());
        if (!surfaceEntity.canPortalOn || surfaceEntity.getName().equals("die")) return;

        if (closestRayHitInfo.fixture.getBody().getType() == BodyDef.BodyType.StaticBody) {
            if (properPortalNormal(closestRayHitInfo.normal)) {
                portals.setPortal(world, portalNumber, closestRayHitInfo.point, closestRayHitInfo.normal, true, closestRayHitInfo.fixture);
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
        float frictionMagnitude = (onGround() ? groundFrictionMagnitude : airFrictionMagnitude);
        float newXVelocity = (Math.abs(xVelocity) - frictionMagnitude) * direction;
        float newDirection = newXVelocity / Math.abs(newXVelocity);                 // getting the new direction that the nex x velocity is pointing to
        if (direction == newDirection) {                                            // if the old direction is the same as the new direction then
            body.setLinearVelocity(newXVelocity, body.getLinearVelocity().y);       // set the velocity accordingly
        }
        else {
            body.setLinearVelocity(0, body.getLinearVelocity().y);              // if the direction changes to the other direction (as in the friction makes it not only reach 0, but goes past 0 is some cases) then automatically set it to 0
        }
    }

    static public void operate() {
        if (player == null) return;
//        player.alive = true;

        // mousePos = new Vector2(Gdx.input.getX() * MyGdxGame.GAME_SCALE, Gdx.input.getY() * MyGdxGame.GAME_SCALE);

        player.friction();

//        if (player.mousePos != null) player.debugRenderer.debugLine(player.body.getPosition(), player.mousePos, Color.WHITE);
        player.updateReflection(player.portals);


        if (player.alive) {
            player.control();
        }
        else {
            player.die();
        }

    }

    static public void renderPortals() {
        if (player == null) return;
        player.portals.renderPortals(MyGdxGame.entityRenderer.getBatch());
    }

    private void die() {
//        getBody().getFixtureList().first().setSensor(true);

        if (currentAnimation != "Death") {
            // trigger
            AudioManager.playSound(Player.player.sounds.get("Death"), 0.5f, false, true);
            deathTime = MyGdxGame.gameElapsedTime;
        }
        currentAnimation = "Death";

        // return to menu
        float timeElapsed = MyGdxGame.gameElapsedTime - deathTime;
        if (timeElapsed >= restartDelay) {
            MyGdxGame.changeLevel(0);
        }
    }
}
