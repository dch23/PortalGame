package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodeKeyframe;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;

public class AnimationManager {
    static HashMap<Animation, Float> animationElapseTimes = new HashMap<>();


    static public void playAnimation(Entity entity, SpriteBatch spriteBatch, Animation animation, float textureScale, int horizontalFaceDirection, float angle) {
        if (animation == null) return;
        if (!animationElapseTimes.containsKey(animation)) animationElapseTimes.put(animation, 0f);

        // data
        TextureRegion keyFrame = (TextureRegion) animation.getKeyFrame(animationElapseTimes.get(animation),true);
        Vector2 size = new Vector2(keyFrame.getRegionWidth() * MyGdxGame.GAME_SCALE * textureScale,
                keyFrame.getRegionHeight() * MyGdxGame.GAME_SCALE * textureScale);
        Vector2 pos = new Vector2(entity.getPosition());
        pos.x -= size.x/2f;
        pos.y -= size.y/2f;
        if (!entity.getName().equals("fireball") && !entity.getName().equals("Boss")) pos.y = pos.y - entity.size.y/2f + size.y/2f;

        // flip
        if (horizontalFaceDirection == 1) {
            if (keyFrame.isFlipX()) keyFrame.flip(true, false);
        }
        else {
            if (!keyFrame.isFlipX()) keyFrame.flip(true, false);
        }

        // draw onto screen (new with rotation!)
        Vector2 origin = PMath.divideVector2(size, 2);
        spriteBatch.draw(keyFrame, pos.x, pos.y, origin.x, origin.y, size.x, size.y, 1, 1, angle);

        // increment elapse
        float newElapsedTime = animationElapseTimes.get(animation);
        if (animation.getPlayMode() != Animation.PlayMode.LOOP) {
            Object[] frames = animation.getKeyFrames();
            TextureRegion lastFrame = (TextureRegion) frames[frames.length-1];
            if (!keyFrame.equals(lastFrame)) {
                newElapsedTime += Gdx.graphics.getDeltaTime();
            }
        }
        else newElapsedTime += Gdx.graphics.getDeltaTime();

        animationElapseTimes.put(animation, newElapsedTime);
    }


    static public void playAnimation(Entity entity, SpriteBatch spriteBatch, String animationName, float textureScale, int horizontalFlipDirection, float angle) {
        Animation animation = entity.getAnimation(animationName);
        if (animation == null) return;
        if (entity.getName().equals("fireball")) {
            Fireball fb = (Fireball) entity;
            float multiplier = (fb.currentAnimation.equals("orange") ? fb.animationTextureSizeScaleOrangeMultiplier : fb.getAnimationTextureSizeScaleBlueMultiplier);
            textureScale *= multiplier;
        }
        playAnimation(entity, spriteBatch, animation, textureScale, horizontalFlipDirection, angle);
    }

    static private void resetAnimation(Animation animation) {

    }

    static public boolean isAnimationEnd(Animation animation) {
        return false;
    }
}
