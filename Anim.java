package dev.gamblefx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

import java.util.Random;

public abstract class Anim {
    protected static final long HOLD = 3600L, FADE_IN = 300L, FADE_OUT = 700L;

    protected final Drop drop;
    protected final Rarity rarity;
    protected final float speed;
    protected final Random rnd = new Random();
    protected final long start = System.currentTimeMillis();

    protected Anim(Drop drop) {
        this.drop = drop;
        this.rarity = drop.rarity();
        this.speed = Config.getSpeed();
    }

    /** Skaliert eine Dauer in ms mit dem Tempo-Regler. */
    protected long k(long ms) { return (long) (ms / speed); }

    /** @return false, wenn die Animation beendet ist. */
    public abstract boolean render(GuiGraphicsExtractor g, int sw, int sh);

    protected void sound(SoundEvent e, float vol, float pitch) {
        if (!Config.isSounds()) return;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(e, pitch, vol));
    }
}
