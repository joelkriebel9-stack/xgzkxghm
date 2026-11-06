package dev.gamblefx;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.sounds.SoundEvents;

/** Einarmiger Bandit: 3 Walzen stoppen nacheinander. Hohe Seltenheit = Jackpot (3 gleiche). */
public class SlotAnimation extends PanelAnimation {
    private static final int WIN_W = 70, WIN_H = 90, CELL = 30, GAP = 8;
    private static final String[] GLYPH = {"7", "$", "\u2605", "\u2666", "\u2663", "\u2665"};
    private static final int[] COLOR = {0xFF5555, 0xFFFF55, 0xFFAA00, 0x55FFFF, 0x55FF55, 0xFF55FF};

    private final int[][] seq = new int[3][];
    private final int[] dist = new int[3];
    private final long[] stop = new long[3];
    private final int[] lastBase = {-1, -1, -1};
    private final boolean[] stopped = new boolean[3];

    public SlotAnimation(Drop d) {
        super(d);
        int[] fin = finalSymbols();
        for (int i = 0; i < 3; i++) {
            dist[i] = 26 + i * 9;
            seq[i] = new int[dist[i] + 6];
            for (int k = 0; k < seq[i].length; k++) seq[i][k] = rnd.nextInt(GLYPH.length);
            seq[i][dist[i]] = fin[i];
            stop[i] = (long) ((2000 + i * 900) / speed);
        }
    }

    private int[] finalSymbols() {
        int[] f = new int[3];
        if (rarity.atLeast(Rarity.EPIC)) {
            int s = rarity.atLeast(Rarity.MYTHIC) ? 0 : rarity.atLeast(Rarity.LEGENDARY) ? 2 : 3;
            f[0] = f[1] = f[2] = s;
        } else if (rarity == Rarity.RARE) {
            int s = rnd.nextInt(GLYPH.length);
            f[0] = f[1] = s;
            f[2] = (s + 1 + rnd.nextInt(GLYPH.length - 1)) % GLYPH.length;
        } else {
            f[0] = rnd.nextInt(GLYPH.length);
            f[1] = (f[0] + 1 + rnd.nextInt(GLYPH.length - 1)) % GLYPH.length;
            do { f[2] = rnd.nextInt(GLYPH.length); } while (f[2] == f[0] || f[2] == f[1]);
        }
        return f;
    }

    @Override protected long spinMillis() { return stop[2] + 250; }
    @Override protected int panelHeight() { return WIN_H; }

    @Override
    protected void drawContent(GuiGraphicsExtractor g, long t, int x, int y, int w, int h, float a) {
        int totalW = 3 * WIN_W + 2 * GAP;
        int x0 = x + (w - totalW) / 2;
        boolean done = t >= spinMillis();
        boolean tick = false;

        for (int r = 0; r < 3; r++) {
            int wx = x0 + r * (WIN_W + GAP);
            Gfx.rect(g, wx, y, wx + WIN_W, y + h, 0x14141E, a);

            float tr = Math.min(1f, t / (float) stop[r]);
            float e = 1f - (float) Math.pow(1f - tr, 3);
            float pos = dist[r] * e;
            int base = (int) Math.floor(pos);

            if (base != lastBase[r]) {
                lastBase[r] = base;
                if (tr < 1f) tick = true;
            }
            if (tr >= 1f && !stopped[r]) {
                stopped[r] = true;
                sound(SoundEvents.ANVIL_LAND, 0.5f, 1.6f);
            }

            g.enableScissor(wx, y, wx + WIN_W, y + h);
            g.pose().pushMatrix();
            g.pose().translate(0f, (pos - base) * CELL);
            for (int i = base - 2; i <= base + 2; i++) {
                int len = seq[r].length;
                int s = seq[r][((i % len) + len) % len];
                float yc = y + h / 2f - (i - base) * CELL;
                Gfx.text(g, GLYPH[s], wx + WIN_W / 2f, yc - 4 * 2.2f, 2.2f, COLOR[s], a);
            }
            g.pose().popMatrix();
            for (int k = 0; k < 10; k++) {
                float va = (1f - k / 10f) * 0.85f * a;
                Gfx.rect(g, wx, y + k, wx + WIN_W, y + k + 1, 0x14141E, va);
                Gfx.rect(g, wx, y + h - k - 1, wx + WIN_W, y + h - k, 0x14141E, va);
            }
            g.disableScissor();

            Gfx.frame(g, wx, y, wx + WIN_W, y + h, done ? rarity.rgb : 0x3A3A4A, a);
        }
        if (tick) sound(SoundEvents.ITEM_PICKUP, 0.25f, 1.4f);

        Gfx.rect(g, x0 - 4, y + h / 2f - 1, x0 + totalW + 4, y + h / 2f + 1, 0xFF3030, 0.9f * a);
    }
}
