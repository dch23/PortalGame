package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;

public class AnimationManager {
    static HashMap<Animation, Float> animationElapseTimes;

    static public void playAnimation(SpriteBatch spriteBatch, Animation animation, Vector2 position) {
        if (!animationElapseTimes.containsKey(animation)) animationElapseTimes.put(animation, 0f);

        Texture texture = (Texture) animation.getKeyFrame(animationElapseTimes.get(animation));
        spriteBatch.draw(texture, position.x, position.y);

        animationElapseTimes.put(animation, animationElapseTimes.get(animation) + Gdx.graphics.getDeltaTime());
    }

    static private void resetAnimation(Animation animation) {

    }

    static public boolean isAnimationEnd(Animation animation) {
        return false;
    }
}
