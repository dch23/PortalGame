package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

import javax.print.attribute.HashAttributeSet;
import java.util.ArrayList;
import java.util.HashMap;

public class Fireball extends Entity {
    //static
    private static ArrayList<Fireball> fireballs = new ArrayList<>();
    private static ArrayList<Fireball> aliveFireballs = new ArrayList<>();

    // private
    private float speed = 1.3f;


    float animationTextureSizeScaleOrangeMultiplier = 30*2;
    float getAnimationTextureSizeScaleBlueMultiplier = 11*2;

    public Fireball(Vector2 position, Vector2 size, Vector2 direction) {
        super("fireball", position, size, BodyDef.BodyType.DynamicBody, null, 100, 0.1f, false, null);

        // configure
        renderAngle = PMath.dir2Deg(direction);
        getBody().getFixtureList().first().setSensor(true);
        animationTextureSizeScale = size.x;

        // animation
        addAnimation("orange", "Characters/Evil Wizard/Sprites/smallFb.gif", 15,true, 1);
        addAnimation("blue", "Characters/Wizard Pack/blueFire.gif", 15,true, 1);
        currentAnimation = "orange";

        getBody().setLinearVelocity(PMath.multVector2(direction, speed));
        fireballs.add(this);
    }


    public static void operate() {
//        int ac=0;
        aliveFireballs.clear();
        for (int i=0; i<fireballs.size(); i++) {
            Fireball fb = fireballs.get(i);
            if (fb.alive) {
                fb.operateBall();
                aliveFireballs.add(fb);
//                ac += fb.animations.size();
            }
            else {
                fb.dispose();
            }
        }
        fireballs.clear();
        for (Fireball fb : aliveFireballs) fireballs.add(fb);
//        System.out.println(AnimationManager.animationElapseTimes.size());
    }

    public static void disposeF() {
        for (Fireball fb : fireballs) {
            fb.alive = false;
        }
    }

    private void operateBall() {
        if (!PMath.inBounds(getPosition(), getSize(), new Vector2(0,0), MyGdxGame.gameBounds)) {
            alive = false;
        }

        Vector2 dir = PMath.normalizeVector2(getBody().getLinearVelocity());
        getBody().setLinearVelocity(PMath.multVector2(dir, speed));

        updateReflection(Player.player.portals);

    }

    public static void disposeAll() {
        fireballs = new ArrayList<>();
    }
}
