package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import sun.security.action.GetBooleanAction;

import java.util.ArrayList;

public class Boss extends EnemyEntity {
    // static
    static Boss boss;
    private static Vector2 regularSize = new Vector2(0.2f,0.55f);

    // Boss-Player interaction
    public boolean touchedPlayer = false;
    public boolean isHurt = false;
    private float pushMagnitude = 10f;

    // Fireballs
    private float createFireballDis = 0.2f;
    private float fireBallSize = 0.05f;

    // mine
    private float speed = 2f;
    private float spinSpeed = 1f;

    //death
    boolean triggerDeath = true;

    //state
    enum State {
        FOLLOW_PLAYER,
        ATTACK_CENTRE, ATTACK_TRAIL,
    }
    State state;

    class AttackCentre {
        public boolean attack = false;
        public float spawnFireballsDistance = 0.3f;

        private Vector2 offset = new Vector2(0, 1);
        Vector2 position = PMath.addVector2(PMath.divideVector2(MyGdxGame.gameBounds, 2), offset);

        public boolean attackStart = false;
        public float attackStartTime;
        public float attackDuration = 4f;

        public int iterations = 0;
        public int maxIterations = 100;

        public boolean firedIteration = false;
        public float iterationStartTime;
        public float iterationDuration = 0.2f;   // 1 second

        public int numFireballs = 2;
        Vector2 fireballSize = new Vector2(0.05f,0.05f);

        public void reset() {
            attackStart = false;
            iterations = 0;
            firedIteration = false;
        }
    }

    class AttackTrail {
        public ArrayList<Vector2> points = new ArrayList<>();

        public boolean spawnTrail = false;
        private int numTrails = 3;
        public int currentPoint = 0;

        public float speed = 5f;
        private float changeHeight = MyGdxGame.gameBounds.y / (float) (numTrails+1);
        public float trailOffsetMag = 0.2f;

        // cool down
        public boolean startCoolDown = false;
        public float spawnedTrailStartTime;
        public float spawnTrailCoolDown = 0.001f;



        public AttackTrail() {
            // generate points
            for (int i=1; i<=numTrails; i++) {
                float height = MyGdxGame.gameBounds.y - changeHeight * i;
                if (i % 2 == 0) {
                    points.add(new Vector2(MyGdxGame.gameBounds.x,height));
                    points.add(new Vector2(0,height));
                }
                else {
                    points.add(new Vector2(0, height));
                    points.add(new Vector2(MyGdxGame.gameBounds.x, height));
                }
            }
        }

        public Vector2 getCurrentPoint() {
            return points.get(currentPoint);
        }

        public void reset() {
            currentPoint = 0;
            spawnTrail = false;
        }
    }

    AttackCentre attackCentre = new AttackCentre();
    AttackTrail attackTrail = new AttackTrail();

    public Boss(String name, Vector2 position, Vector2 size, BodyDef.BodyType bodyType, Color color, float density, float friction, boolean gravityEnabled, Sprite sprite) {
        super(name, position, size, bodyType, color, density, friction, gravityEnabled, sprite);

        // configure
        boss = this;
        state = State.ATTACK_CENTRE;

        // animations
        addAnimation("Idle", "Characters/Evil Wizard/Sprites/Idle.png", 8, true, 0.3f);
        addAnimation("Death", "Characters/Evil Wizard/Sprites/Death.png", 5, false, 0.3f);
        currentAnimation = "Idle";
        animationTextureSizeScale = 3;
        horizontalFaceDirection = -1;

        // custom
        getBody().getFixtureList().first().setSensor(true);
        boss.fadeSpeed = 0.006f;

        // sounds
        sounds.put("crush", Gdx.audio.newSound(Gdx.files.internal("Characters/Evil Wizard/sounds/Ill crush you.mp3")));
        sounds.put("Fire", Gdx.audio.newSound(Gdx.files.internal("Characters/Evil Wizard/sounds/bossfire.mp3")));
        sounds.put("Hurt", Gdx.audio.newSound(Gdx.files.internal("Characters/Evil Wizard/sounds/Grunting-from-Being-Hit-A4-www.fesliyanstudios.com.mp3")));
        sounds.put("Laugh", Gdx.audio.newSound(Gdx.files.internal("Characters/Evil Wizard/sounds/bosslaugh.mp3")));

        // idle sounds
        idleSounds = new String[] {"crush", "Fire", "Laugh"};

        //set health
        BossHealth.resetHealth();

    }

    public static Vector2 getRegularSize() {
        return regularSize;
    }

    public static void operate() {
        if (boss == null) return;


        if (boss.state != State.ATTACK_CENTRE) boss.spinTo(0);

        if (boss.alive) {
            // idle sounds
            boss.playRandomIdleSound();

            // hit player
            if (boss.touchedPlayer) {
                boss.pushPlayer();
                boss.touchedPlayer = false;
                //            System.out.println("hit");
            }

            // states
            switch (boss.state) {
                case FOLLOW_PLAYER:
//                    boss.spinTo(0);
                    boss.getBody().setLinearVelocity(new Vector2(0, 0));
//                boss.state = State.ATTACK_CENTRE;
                    break;
                case ATTACK_CENTRE:
                    if (!boss.attackCentre.attackStart) {
                        boss.attackCentre.attackStartTime = MyGdxGame.gameElapsedTime;
                        boss.attackCentre.attackStart = true;
                    } else {
                        float timeElapsed = MyGdxGame.gameElapsedTime - boss.attackCentre.attackStartTime;
                        if (timeElapsed >= boss.attackCentre.attackDuration) {
                            // next state!
                            boss.state = State.ATTACK_TRAIL;
                            boss.attackCentre.reset();
                            break;
                        }
                    }

                    // should only be able to do this if not in portal
                    boss.goTo(boss.attackCentre.position, boss.speed);
                    if (boss.atPos(boss.attackCentre.position)) boss.attackCentre.attack = true;

                    if (boss.attackCentre.attack) {
                        spin();
                        if (boss.attackCentre.iterations < boss.attackCentre.maxIterations) {
                            if (!boss.attackCentre.firedIteration) {
                                spawnFireballsInCircle(boss.attackCentre.numFireballs, boss.attackCentre.spawnFireballsDistance);
                                boss.attackCentre.firedIteration = true;
                                boss.attackCentre.iterations++;

                                // attacking delay
                                boss.attackCentre.iterationStartTime = MyGdxGame.gameElapsedTime;
                            } else {
                                float timeElapsed = MyGdxGame.gameElapsedTime - boss.attackCentre.iterationStartTime;
                                if (timeElapsed >= boss.attackCentre.iterationDuration) {
                                    boss.attackCentre.firedIteration = false;
                                }
                            }
                        }
                    }
                    break;
                case ATTACK_TRAIL:
//                    boss.spinTo(0);
                    boss.goTo(boss.attackTrail.getCurrentPoint(), boss.attackTrail.speed);
                    if (boss.attackTrail.spawnTrail) {
                        if (!boss.attackTrail.startCoolDown) {
                            boss.attackTrail.startCoolDown = true;
                            boss.attackTrail.spawnedTrailStartTime = MyGdxGame.gameElapsedTime;
                        }
                        float elapsedTime = MyGdxGame.gameElapsedTime - boss.attackTrail.spawnedTrailStartTime;
                        if (elapsedTime >= boss.attackTrail.spawnTrailCoolDown) {
                            spawnFireTrail();
                            boss.attackTrail.startCoolDown = false;
                        }
                    }

                    int xDir = (boss.getBody().getLinearVelocity().x == 0 ? 0 : (boss.getBody().getLinearVelocity().x > 0 ? 1 : -1));
                    if (xDir != 0) {
                        boss.horizontalFaceDirection = xDir;
                    }

                    if (boss.atPos(boss.attackTrail.getCurrentPoint())) {
                        // can the boss spawn trails?
                        if ((boss.attackTrail.currentPoint + 1) % 2 == 0) {
                            boss.attackTrail.spawnTrail = false;
                        } else boss.attackTrail.spawnTrail = true;

                        if (boss.attackTrail.currentPoint < boss.attackTrail.points.size() - 1)
                            boss.attackTrail.currentPoint++;
                        else {
                            // next state!
                            boss.state = State.ATTACK_CENTRE;
                            boss.attackTrail.reset();
                        }
                    }

                    break;
            }


            // hurt
            if (boss.isHurt) {
//            boss.currentAnimation = "Hurt";
                AudioManager.playSound(boss.sounds.get("Hurt"), 1, false, true);
                boss.isHurt = false;
            }
        }
        else {
            if (boss.triggerDeath) {
                boss.currentAnimation = "Death";
                boss.getBody().setLinearVelocity(new Vector2(0,0));
                boss.triggerDeath = false;

                // sound
                AudioManager.playSound(boss.sounds.get("Hurt"), 1, false, true);
                boss.isHurt = false;
            }
            boss.die();
        }
    }

    private static void spawnFireTrail() {
        Vector2 travelDirection = PMath.normalizeVector2(boss.getBody().getLinearVelocity());
        Vector2 behindDirection = PMath.multVector2(travelDirection, -1);
        Vector2 trailPosition = PMath.addVector2(boss.getPosition(), PMath.multVector2(behindDirection, boss.attackTrail.trailOffsetMag));
        new FireTrail(trailPosition);
    }

    private static void spawnFireballsInCircle(int numberOfFireballs, float spawnFireBallsDistance) {
        float angleChange = 360 / (float)numberOfFireballs;
        for (int i=0; i<numberOfFireballs; i++) {
            float angle = angleChange * i + boss.renderAngle;
            Vector2 direction = PMath.deg2dir(angle);
            Vector2 position = PMath.addVector2(boss.getPosition(), PMath.multVector2(direction, spawnFireBallsDistance));
            new Fireball(position, boss.attackCentre.fireballSize, direction);
        }
    }

    private void spinTo(float angle) {
        if (boss.renderAngle > angle) {
            spin();
        }
        else {
            boss.renderAngle = 0;
        }
    }

    private static void spin() {
        boss.renderAngle += boss.spinSpeed;
        boss.renderAngle = boss.renderAngle % 360;
    }

    private boolean atPos(Vector2 pos) {
        float dis = PMath.magnitude(PMath.subVector2(pos, boss.getPosition()));
//        System.out.println(dis);
        return dis < closeEnoughToGround;
    }

    private void goTo(Vector2 position, float speed) {
        if (!boss.atPos(position)) {
            Vector2 direction = PMath.subVector2(position, boss.getPosition());
            direction = PMath.normalizeVector2(direction);

            Vector2 velocity = PMath.multVector2(direction, speed);
            boss.getBody().setLinearVelocity(velocity);
        }
        else {
            boss.setPosition(position);
        }
    }

    public void pushPlayer() {
        Vector2 bossPos = getPosition(), playerPos = Player.player.getPosition();
        Vector2 direction = PMath.normalizeVector2(PMath.subVector2(playerPos, bossPos));

        Vector2 forceVector = PMath.multVector2(direction, pushMagnitude);
        Player.player.getBody().setLinearVelocity(forceVector);
    }

    public void destroyWeapons() {
        Fireball.disposeF();
        FireTrail.disposeF();
    }
}
