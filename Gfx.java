package dev.gamblefx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

/** Kleine Zeichenhelfer auf Basis von GuiGraphicsExtractor (Minecraft 26.2). */
public final class Gfx {
    private Gfx() {}

    public static Font font() { return Minecraft.getInstance().font; }

    public static int argb(int rgb, float a) {
        int al = (int) (Math.max(0f, Math.min(1f, a)) * 255f);
        return (al << 24) | (rgb & 0xFFFFFF);
    }

    public static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }

    public static void rect(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, int rgb, float a) {
        if (a <= 0.02f) return;
        int ix1 = Math.round(x1), iy1 = Math.round(y1), ix2 = Math.round(x2), iy2 = Math.round(y2);
        if (ix2 <= ix1 || iy2 <= iy1) return;
        g.fill(ix1, iy1, ix2, iy2, argb(rgb, a));
    }

    public static void frame(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int rgb, float a) {
        rect(g, x1, y1, x2, y1 + 2, rgb, a);
        rect(g, x1, y2 - 2, x2, y2, rgb, a);
        rect(g, x1, y1, x1 + 2, y2, rgb, a);
        rect(g, x2 - 2, y1, x2, y2, rgb, a);
    }

    /** Zentrierter, skalierter Text. */
    public static void text(GuiGraphicsExtractor g, String s, float cx, float y, float scale, int rgb, float a) {
        if (a <= 0.05f) return;
        Font f = font();
        g.pose().pushMatrix();
        g.pose().translate(cx, y);
        g.pose().scale(scale, scale);
        g.text(f, s, -f.width(s) / 2, 0, argb(rgb, a), true);
        g.pose().popMatrix();
    }

    /** Gestufte Kreisscheibe (Pixel-Look). */
    public static void disc(GuiGraphicsExtractor g, float cx, float cy, float r, int rgb, float a) {
        int step = 5;
        for (int y = -(int) r; y < r; y += step) {
            float w = (float) Math.sqrt(Math.max(0, r * r - (y + step / 2f) * (y + step / 2f)));
            rect(g, cx - w, cy + y, cx + w, cy + y + step, rgb, a);
        }
    }

    public static void glow(GuiGraphicsExtractor g, float cx, float cy, float radius, int rgb, float a) {
        int rings = 5;
        for (int i = 0; i < rings; i++) {
            float r = radius * (1f - i / (float) rings);
            disc(g, cx, cy, r, rgb, a / rings * 1.4f);
        }
    }

    public static String trim(String s, int maxPx) {
        Font f = font();
        while (s.length() > 1 && f.width(s) > maxPx) s = s.substring(0, s.length() - 1);
        return s;
    }

    public static List<String> wrap(String s, int maxPx) {
        Font f = font();
        List<String> lines = new ArrayList<>();
        String cur = "";
        for (String word : s.split(" ")) {
            String test = cur.isEmpty() ? word : cur + " " + word;
            if (f.width(test) <= maxPx || cur.isEmpty()) cur = test;
            else { lines.add(cur); cur = word; }
        }
        if (!cur.isEmpty()) lines.add(cur);
        return lines;
    }
}
