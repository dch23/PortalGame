package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;

public class AnimationManager {
    static HashMap<Animation, Float> animationElapseTimes = new HashMap<>();

    static public void playAnimation(Entity entity, SpriteBatch spriteBatch, Animation animation, float textureScale, int horizontalFaceDirection) {
        if (animation == null) return;

        if (!animationElapseTimes.containsKey(animation)) animationElapseTimes.put(animation, 0f);

        TextureRegion keyFrame = (TextureRegion) animation.getKeyFrame(animationElapseTimes.get(animation),true);
        Vector2 size = new Vector2(keyFrame.getRegionWidth() * MyGdxGame.GAME_SCALE * textureScale,
                keyFrame.getRegionHeight() * MyGdxGame.GAME_SCALE * textureScale);
        Vector2 pos = new Vector2(entity.getPosition());
        pos.x -= size.x/2f;
        pos.y -= size.y/2f;
        pos.y = pos.y - entity.size.y/2f + size.y/2f;


        if (horizontalFaceDirection == 1) {
            if (keyFrame.isFlipX()) {
                keyFrame.flip(true, false);
            }
        }
        else {
            if (!keyFrame.isFlipX()) {
                keyFrame.flip(true, false);
            }
        }

        // draw onto screen
        spriteBatch.draw(keyFrame, pos.x, pos.y, size.x, size.y);

        // increment elapse time
        animationElapseTimes.put(animation, animationElapseTimes.get(animation) + Gdx.graphics.getDeltaTime());
    }

    static public void playAnimation(Entity entity, SpriteBatch spriteBatch, String animationName, float textureScale, int horizontalFlipDirection) {
        Animation animation = entity.getAnimation(animationName);
        if (animation == null) return;
        playAnimation(entity, spriteBatch, animation, textureScale, horizontalFlipDirection);
    }

    static private void resetAnimation(Animation animation) {

    }

    static public boolean isAnimationEnd(Animation animation) {
        return false;
    }
}
