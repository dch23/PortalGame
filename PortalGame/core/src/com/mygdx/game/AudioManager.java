package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;

public class AudioManager {
    static public void playSound(Sound sound, float volume, boolean loop) {
        long soundId = sound.play(volume);
        float pitch = PMath.getRandomRangeFloat(0.95f, 1.05f);
        sound.setPitch(soundId, pitch);
    }
}
