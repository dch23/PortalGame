package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;

public class AudioManager {

    static public void playSound(Sound sound, float volume, boolean loop, boolean randomPitch) {
        long soundId;
        if (loop) {
            soundId = sound.loop(volume);
        }
        else {
            soundId = sound.play(volume);
        }
        if (randomPitch) {
            float pitch = PMath.getRandomRangeFloat(0.95f, 1.05f);
            sound.setPitch(soundId, pitch);
        }
    }
}
