package dev.gamblefx;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

/** CS:GO-artiges Case-Opening: ein Streifen laeuft aus und bleibt auf dem Gewinn stehen. */
public class CaseAnimation extends PanelAnimation {
    private static final int CARD_W = 64, CARD_H = 48, GAP = 4, STEP = CARD_W + GAP;
    private static final int COUNT = 48, WIN = 40;
    private static final String[] FILLER = {
            "Enchanted Book", "Rotten Flesh", "Diamond", "Coal", "Gold Ingot", "Wheat", "Ender Pearl",
            "Jasper Gemstone", "Spider Eye", "Bone", "Cobblestone", "Hyperion", "Necron's Handle",
            "Dragon Claw", "Titanium", "Summoning Eye", "Booster Cookie", "Magical Soup", "Nichts", "Sand"};

    private final String[] names = new String[COUNT];
    private final Rarity[] rars = new Rarity[COUNT];
    private final float jitter;
    private int lastTick = -1;

    public CaseAnimation(Drop d) {
        super(d);
        for (int i = 0; i < COUNT; i++) {
            names[i] = FILLER[rnd.nextInt(FILLER.length)];
            rars[i] = Rarity.randomFiller(rnd);
        }
        names[WIN] = d.name();
        rars[WIN] = d.rarity();
        jitter = (rnd.nextFloat() - 0.5f) * (CARD_W * 0.6f);
    }

    @Override protected long spinMillis() { return (long) (5200 / speed); }
    @Override protected int panelHeight() { return CARD_H + 8; }

    @Override
    protected void drawContent(GuiGraphicsExtractor g, long t, int x, int y, int w, int h, float a) {
        long spin = spinMillis();
        float p = Math.min(1f, t / (float) spin);
        float e = 1f - (float) Math.pow(1f - p, 4);
        float scroll = (WIN * STEP + CARD_W / 2f + jitter) * e;
        int cx = x + w / 2;
        boolean done = t >= spin;

        int idx = (int) Math.floor(scroll / STEP);
        if (idx != lastTick) {
            lastTick = idx;
            if (!done) sound(SoundEvents.ITEM_PICKUP, 0.35f, 0.8f + 0.8f * p);
        }

        Gfx.rect(g, x, y, x + w, y + h, 0x000000, 0.45f * a);
        g.enableScissor(x, y, x + w, y + h);
        g.pose().pushMatrix();
        g.pose().translate(cx - scroll, 0f);
        for (int i = 0; i < COUNT; i++) {
            float screenLeft = cx - scroll + i * STEP;
            if (screenLeft + CARD_W < x || screenLeft > x + w) continue;
            float ca = (done && i != WIN) ? 0.4f * a : a;
            int left = i * STEP;
            int top = y + 4;
            Rarity r = rars[i];
            Gfx.rect(g, left, top, left + CARD_W, top + CARD_H, 0x1A1A26, ca);
            Gfx.rect(g, left, top, left + CARD_W, top + CARD_H, r.rgb, 0.18f * ca);
            Gfx.rect(g, left, top + CARD_H - 4, left + CARD_W, top + CARD_H, r.rgb, ca);
            List<String> lines = Gfx.wrap(names[i], CARD_W - 6);
            int n = Math.min(3, lines.size());
            float ty = top + (CARD_H - 4 - n * 9) / 2f;
            for (int k = 0; k < n; k++) Gfx.text(g, lines.get(k), left + CARD_W / 2f, ty + k * 9, 1f, r.rgb, ca);
            if (done && i == WIN) Gfx.frame(g, left - 1, top - 1, left + CARD_W + 1, top + CARD_H + 1, r.rgb, a);
        }
        g.pose().popMatrix();

        for (int k = 0; k < 14; k++) {
            float va = (1f - k / 14f) * 0.9f * a;
            Gfx.rect(g, x + k, y, x + k + 1, y + h, 0x0B0B14, va);
            Gfx.rect(g, x + w - k - 1, y, x + w - k, y + h, 0x0B0B14, va);
        }
        g.disableScissor();

        Gfx.rect(g, cx - 1, y - 6, cx + 1, y + h + 6, 0xFFD24A, a);
    }
}
