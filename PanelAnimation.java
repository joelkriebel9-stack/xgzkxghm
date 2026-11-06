package dev.gamblefx;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Gemeinsame Basis fuer panelbasierte Animationen (Case Opening, Slot Machine). */
public abstract class PanelAnimation extends Anim {
    private long last = start;
    private boolean revealed = false;
    private final List<float[]> confetti = new ArrayList<>(); // x,y,vx,vy,rgb,life

    protected PanelAnimation(Drop d) { super(d); }

    protected abstract long spinMillis();
    protected abstract int panelHeight();
    protected abstract void drawContent(GuiGraphicsExtractor g, long t, int x, int y, int w, int h, float a);

    @Override
    public boolean render(GuiGraphicsExtractor g, int sw, int sh) {
        long now = System.currentTimeMillis();
        long t = now - start;
        float dt = Math.min(0.1f, (now - last) / 1000f);
        last = now;

        long spin = spinMillis();
        long total = spin + HOLD + FADE_OUT;
        if (t >= total) return false;

        float a = 1f;
        if (t < FADE_IN) a = t / (float) FADE_IN;
        else if (t > spin + HOLD) a = 1f - (t - spin - HOLD) / (float) FADE_OUT;
        a = Gfx.clamp01(a);

        int pw = Math.min(sw - 24, 340);
        int ph = panelHeight();
        int px = (sw - pw) / 2;
        int py = 40;
        boolean done = t >= spin;
        int accent = done ? rarity.rgb : 0xFFD24A;
        float pulse = done ? 0.65f + 0.35f * (float) Math.sin(t / 80.0) : 0.8f;

        Gfx.rect(g, px - 10, py - 24, px + pw + 10, py + ph + 40, 0x0B0B14, 0.85f * a);
        Gfx.frame(g, px - 10, py - 24, px + pw + 10, py + ph + 40, accent, pulse * a);
        Gfx.text(g, "\u00a7l" + drop.category().title + (done ? "" : " ..."), sw / 2f, py - 16, 1f, 0xFFAA00, a);

        drawContent(g, t, px, py, pw, ph, a);

        if (done) {
            if (!revealed) {
                revealed = true;
                reveal(px, py, pw, ph);
            }
            Gfx.text(g, "\u00a7l" + rarity.label, sw / 2f, py + ph + 8, 1f, rarity.rgb, a);
            Gfx.text(g, Gfx.trim(drop.name(), pw), sw / 2f, py + ph + 20, 1f, 0xFFFFFF, a);
        }
        drawConfetti(g, dt, a);
        return true;
    }

    private void reveal(int px, int py, int pw, int ph) {
        if (rarity.atLeast(Rarity.MYTHIC)) {
            sound(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, 1f, 1f);
            sound(SoundEvents.PLAYER_LEVELUP, 1f, 1.3f);
        } else if (rarity.atLeast(Rarity.LEGENDARY)) {
            sound(SoundEvents.PLAYER_LEVELUP, 1f, 1f);
        } else {
            sound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
        if (Config.isConfetti() && rarity.atLeast(Rarity.EPIC)) {
            int n = 30 + rarity.ordinal() * 15;
            int[] cols = {rarity.rgb, 0xFFD24A, 0xFFFFFF};
            for (int i = 0; i < n; i++) {
                confetti.add(new float[]{
                        px + pw / 2f + (rnd.nextFloat() - 0.5f) * 80f, py + ph / 2f,
                        (rnd.nextFloat() - 0.5f) * 320f, -60f - rnd.nextFloat() * 220f,
                        cols[rnd.nextInt(cols.length)], 1.4f + rnd.nextFloat()});
            }
        }
    }

    private void drawConfetti(GuiGraphicsExtractor g, float dt, float a) {
        Iterator<float[]> it = confetti.iterator();
        while (it.hasNext()) {
            float[] c = it.next();
            c[3] += 380f * dt;
            c[0] += c[2] * dt;
            c[1] += c[3] * dt;
            c[5] -= dt;
            if (c[5] <= 0) { it.remove(); continue; }
            Gfx.rect(g, c[0], c[1], c[0] + 3, c[1] + 2, (int) c[4], Math.min(1f, c[5]) * a);
        }
    }
}
