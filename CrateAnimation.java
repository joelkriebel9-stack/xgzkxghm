package dev.gamblefx;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Nachbau der Beispiel-Animation:
 * 1) Kiste faellt auf Podest  2) Kiste wackelt, Spannung steigt  3) Deckel springt auf, Lichtstrahl
 * 4) Item steigt auf, Strahlenkranz, Partikel  5) Text: "RARE DROP!" / Name / Untertitel
 */
public class CrateAnimation extends Anim {
    private final ItemStack stack;
    private final List<float[]> particles = new ArrayList<>(); // x,y,vx,vy,life,size,rgb
    private long last = start;
    private float spawnAcc = 0;
    private long nextTick = 0;
    private boolean openPlayed = false, revealPlayed = false;
    private final int partRgb;

    public CrateAnimation(Drop d) {
        super(d);
        this.stack = ItemResolver.resolve(d);
        int r = (rarity.rgb >> 16 & 0xFF), gg = (rarity.rgb >> 8 & 0xFF), b = (rarity.rgb & 0xFF);
        // aufgehellte Seltenheitsfarbe fuer Partikel (rosa bei Mythic)
        this.partRgb = ((r + 255) / 2 << 16) | ((gg + 255) / 2 << 8) | ((b + 255) / 2);
    }

    private static float bounce(float x) {
        float n1 = 7.5625f, d1 = 2.75f;
        if (x < 1 / d1) return n1 * x * x;
        if (x < 2 / d1) { x -= 1.5f / d1; return n1 * x * x + 0.75f; }
        if (x < 2.5 / d1) { x -= 2.25f / d1; return n1 * x * x + 0.9375f; }
        x -= 2.625f / d1;
        return n1 * x * x + 0.984375f;
    }

    private static float easeOutBack(float x) {
        float c1 = 1.70158f, c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(x - 1, 3) + c1 * (float) Math.pow(x - 1, 2);
    }

    private static float easeOutCubic(float x) { return 1f - (float) Math.pow(1f - x, 3); }

    @Override
    public boolean render(GuiGraphicsExtractor g, int sw, int sh) {
        long now = System.currentTimeMillis();
        long t = now - start;
        float dt = Math.min(0.1f, (now - last) / 1000f);
        last = now;

        long d1 = k(700), d2 = k(2100), d3 = k(2800), d4 = k(3600);
        long total = d4 + HOLD + FADE_OUT;
        if (t >= total) return false;

        float a = 1f;
        if (t < FADE_IN) a = t / (float) FADE_IN;
        else if (t > d4 + HOLD) a = 1f - (t - d4 - HOLD) / (float) FADE_OUT;
        a = Gfx.clamp01(a);

        float cx = sw / 2f, cy = sh / 2f - 6f;
        float sc = Math.max(1f, Math.min(1.8f, sh / 220f));

        // --- Hintergrund abdunkeln ---
        Gfx.rect(g, 0, 0, sw, sh, 0x050309, 0.62f * a);

        // --- Glow, wird mit der Spannung staerker ---
        float gi;
        if (t < d1) gi = 0.12f;
        else if (t < d2) gi = 0.12f + 0.25f * (t - d1) / (float) (d2 - d1);
        else if (t < d3) gi = 0.37f + 0.35f * (t - d2) / (float) (d3 - d2);
        else gi = 0.72f + 0.08f * (float) Math.sin(t / 140.0);
        Gfx.glow(g, cx, cy, 130f * sc, rarity.rgb, gi * 0.55f * a);

        // --- Partikel (rosa Quadrate) ---
        updateParticles(g, dt, t, d1, d3, cx, cy, sc, a);

        // --- Kiste ---
        long fadeCrate = k(350);
        float ca = t < d3 ? 1f : Gfx.clamp01(1f - (t - d3) / (float) fadeCrate);
        if (ca > 0.02f) drawCrate(g, t, d1, d2, d3, cx, cy, sc, ca * a);

        // --- Item + Strahlen ---
        if (t >= d3) {
            float p = Gfx.clamp01((t - d3) / (float) k(800));
            float ra = Gfx.clamp01((t - d3) / (float) k(500)) * a;
            float itemY = cy - 22f + (1f - easeOutCubic(p)) * 26f + (float) Math.sin(t / 260.0) * 2.5f;

            // Rotierende Strahlen
            for (int i = 0; i < 20; i++) {
                g.pose().pushMatrix();
                g.pose().translate(cx, itemY);
                g.pose().rotate((float) (i * Math.PI * 2 / 20 + t * 0.0004));
                float L = sc;
                Gfx.rect(g, 14 * L, -2 * L, 50 * L, 2 * L, rarity.rgb, 0.34f * ra);
                Gfx.rect(g, 50 * L, -1.5f * L, 82 * L, 1.5f * L, rarity.rgb, 0.20f * ra);
                Gfx.rect(g, 82 * L, -1 * L, 118 * L, 1 * L, rarity.rgb, 0.10f * ra);
                g.pose().popMatrix();
            }
            Gfx.glow(g, cx, itemY, 70f * sc, 0xFFFFFF, 0.25f * ra);

            // Item
            float is = Math.max(0.01f, easeOutBack(p)) * 4.2f * sc;
            g.pose().pushMatrix();
            g.pose().translate(cx, itemY);
            g.pose().scale(is, is);
            g.item(stack, -8, -8);
            g.pose().popMatrix();

            // Texte
            float ta = Gfx.clamp01((t - d3 - k(350)) / (float) k(400)) * a;
            float ty = itemY + 8f * 4.2f * sc + 12f;
            Gfx.text(g, "\u00a7l" + drop.header(), cx, ty, 1.25f, 0xFFAA00, ta);
            Gfx.text(g, drop.name(), cx, ty + 17f, 1.7f, rarity.rgb, ta);
            if (!drop.sub().isEmpty()) Gfx.text(g, drop.sub(), cx, ty + 42f, 0.9f, 0xAAAAAA, ta);

            if (!revealPlayed && p > 0.3f) {
                revealPlayed = true;
                onReveal(cx, itemY, sc);
            }
        }

        return true;
    }

    private void drawCrate(GuiGraphicsExtractor g, long t, long d1, long d2, long d3, float cx, float cy, float sc, float a) {
        float dropY = 0f, angle = 0f, shakeX = 0f, lift = 0f, tilt = 0f, beam = 0f;

        if (t < d1) {
            dropY = -(1f - bounce(t / (float) d1)) * (cy + 140f);
        } else if (t < d2) {
            float p2 = (t - d1) / (float) (d2 - d1);
            angle = (float) Math.sin(t * 0.045) * (0.02f + 0.10f * p2);
            shakeX = (float) Math.sin(t * 0.09) * 1.5f * p2;
            if (t >= nextTick) {
                sound(SoundEvents.ITEM_PICKUP, 0.5f, 0.8f + 0.9f * p2);
                nextTick = t + (long) ((260 - 170 * p2) / speed);
            }
        } else {
            float p3 = Gfx.clamp01((t - d2) / (float) (d3 - d2));
            lift = easeOutCubic(p3) * 34f;
            tilt = p3 * 0.5f;
            beam = p3;
            angle = (float) Math.sin(t * 0.09) * 0.03f * (1f - p3);
            if (!openPlayed) {
                openPlayed = true;
                sound(SoundEvents.CHEST_OPEN, 1f, 0.9f);
                sound(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, 0.8f, 1.1f);
            }
            // weisser Blitz beim Aufspringen
            float fl = 1f - Gfx.clamp01((t - d2) / (float) k(260));
            Gfx.rect(g, 0, 0, g.guiWidth(), g.guiHeight(), 0xFFFFFF, 0.45f * fl * a);
        }

        g.pose().pushMatrix();
        g.pose().translate(cx + shakeX * sc, cy + dropY);
        g.pose().scale(sc, sc);
        g.pose().rotate(angle);
        g.pose().translate(0, -30);

        g.pose().pushMatrix();
        g.pose().translate(0, Crate.H);
        Crate.pedestal(g, a);
        g.pose().popMatrix();

        Crate.body(g, a, lift > 0f);

        if (beam > 0f) {
            for (int i = 0; i < 16; i++) {
                float w = (10 + i * 3.2f) * (0.6f + 0.4f * beam);
                float y = Crate.LID_ROWS * Crate.CH - i * 9;
                Gfx.rect(g, -w / 2, y - 9, w / 2, y, partRgb, 0.40f * beam * (1f - i / 16f) * a);
            }
        }

        g.pose().pushMatrix();
        g.pose().translate(lift * 0.35f, -lift);
        g.pose().rotate(tilt * 0.35f);
        Crate.lid(g, a);
        g.pose().popMatrix();

        g.pose().popMatrix();
    }

    private void onReveal(float cx, float cy, float sc) {
        if (rarity.atLeast(Rarity.LEGENDARY)) sound(SoundEvents.PLAYER_LEVELUP, 1f, 1f);
        else sound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1f, 1f);
        if (rarity.atLeast(Rarity.EPIC)) sound(SoundEvents.AMETHYST_BLOCK_CHIME, 1f, 1.2f);
        if (Config.isConfetti()) {
            int n = 40 + rarity.ordinal() * 14;
            for (int i = 0; i < n; i++) {
                double ang = rnd.nextDouble() * Math.PI * 2;
                float sp = 40f + rnd.nextFloat() * 160f;
                particles.add(new float[]{cx, cy, (float) Math.cos(ang) * sp, (float) Math.sin(ang) * sp - 40f,
                        2.2f + rnd.nextFloat() * 1.5f, 3f + rnd.nextInt(2), partRgb});
            }
        }
    }

    private void updateParticles(GuiGraphicsExtractor g, float dt, long t, long d1, long d3,
                                 float cx, float cy, float sc, float a) {
        if (Config.isConfetti() && t > d1) {
            spawnAcc += dt * (t < d3 ? 18f : 38f);
            while (spawnAcc >= 1f) {
                spawnAcc -= 1f;
                particles.add(new float[]{
                        cx + (rnd.nextFloat() - 0.5f) * 300f * sc,
                        cy + (rnd.nextFloat() - 0.3f) * 200f * sc,
                        (rnd.nextFloat() - 0.5f) * 14f, -6f - rnd.nextFloat() * 14f,
                        2.5f + rnd.nextFloat() * 1.5f, 3f + rnd.nextInt(2), partRgb});
            }
        }
        Iterator<float[]> it = particles.iterator();
        while (it.hasNext()) {
            float[] p = it.next();
            p[4] -= dt;
            if (p[4] <= 0f) { it.remove(); continue; }
            p[0] += p[2] * dt;
            p[1] += p[3] * dt;
            p[2] *= 0.985f;
            p[3] = p[3] * 0.985f + 5f * dt; // leichte Schwerkraft
            float al = Math.min(1f, p[4] / 0.8f) * 0.85f * a;
            Gfx.rect(g, p[0], p[1], p[0] + p[5], p[1] + p[5], (int) p[6], al);
        }
    }
}
