package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

import java.util.ArrayList;

public class FireTrail extends Entity {
    static ArrayList<FireTrail> fireTrails = new ArrayList<>();

    float alpha = 1;
    float alphaSpeed = 0.01f;

    public FireTrail(Vector2 position) {
        super("fireTrail", position, new Vector2(0.5f,0.2f), BodyDef.BodyType.DynamicBody, null, 100, 0.1f, false, null);

        // configure
        getBody().getFixtureList().first().setSensor(true);

        //animation
        addAnimation("flame", "Characters/Evil Wizard/Sprites/fire.gif", 5, true, 0.4f);
        currentAnimation = "flame";

        animationTextureSizeScale = 3;

        fireTrails.add(this);

    }


    public static void operate() {
        ArrayList<FireTrail> aliveFireTrails = new ArrayList<>();
        for (int i=0; i<fireTrails.size(); i++) {
            FireTrail ft = fireTrails.get(i);
            if (ft.alive) {
                ft.operateTrail();
                aliveFireTrails.add(ft);
            }
            else {
                ft.dispose();
            }
        }
        fireTrails = aliveFireTrails;
    }

    private void operateTrail() {
        if (alpha <= 0) {
            alpha = 0;
            alive = false;
        }
        else {
            alpha -= alphaSpeed;
        }
    }

    public static void disposeAll() {
        fireTrails = new ArrayList<>();
    }
}
