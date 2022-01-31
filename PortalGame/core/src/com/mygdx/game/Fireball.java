package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

import java.util.ArrayList;

public class Fireball extends Entity {
    //static
    private static ArrayList<Fireball> fireballs = new ArrayList<>();

    // private
    private float speed = 1.3f;


    float animationTextureSizeScaleOrangeMultiplier = 30;
    float getAnimationTextureSizeScaleBlueMultiplier = 11;

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
        int ac=0;
        ArrayList<Fireball> aliveFireballs = new ArrayList<>();
        for (int i=0; i<fireballs.size(); i++) {
            Fireball fb = fireballs.get(i);
            if (fb.alive) {
                fb.operateBall();
                aliveFireballs.add(fb);
                ac += fb.animations.size();
            }
            else {
                fb.dispose();
            }
        }
        fireballs = aliveFireballs;
//        System.out.println(AnimationManager.animationElapseTimes.size());
    }

    private void operateBall() {
        if (!PMath.inBounds(getPosition(), getSize(), new Vector2(0,0), MyGdxGame.gameBounds)) {
            alive = false;
        }
        updateReflection(Player.player.portals);

    }

    public static void disposeAll() {
        fireballs = new ArrayList<>();
    }
}
