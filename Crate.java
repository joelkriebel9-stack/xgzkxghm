package dev.gamblefx;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Prozedural gezeichnete Pixel-Kiste + Podest (isometrisch), angelehnt an das Beispielvideo. */
final class Crate {
    static final int CW = 6, CH = 6;
    static final int W = 42, H = 42, LID_ROWS = 2;          // Kiste
    static final int PW = 66, PH = 12;                      // Podest

    // Flaechen: 0 Deckel-oben, 1 links, 2 rechts, 3 Innenraum, 4 Podest-oben, 5 Podest-links, 6 Podest-rechts
    private static int noise(int x, int y, int seed) {
        int h = x * 374761393 + y * 668265263 + seed * 1442695041;
        h = (h ^ (h >>> 13)) * 1274126177;
        return (h ^ (h >>> 16)) & 0x7fffffff;
    }

    private static int pick(int[] pal, int ci, int r, int seed) {
        return pal[noise(ci, r, seed) % pal.length];
    }

    private static int col(int face, int ci, int r, int seed) {
        switch (face) {
            case 0:
                if ((ci + r) % 5 < 2) return pick(new int[]{0xA8294F, 0x8B1F42, 0xC03A5C}, ci, r, seed);
                return pick(new int[]{0xF4A6C4, 0xE87DA3, 0xF7B8D0, 0xD2496F}, ci, r, seed);
            case 1:
                if (ci == 1 || ci == 2) return pick(new int[]{0xE0809F, 0xF0A0BC, 0xC95F82}, ci, r, seed);
                return pick(new int[]{0x4A1F3D, 0x5C2749, 0x6A2E55}, ci, r, seed);
            case 2:
                if ((ci == 3 || ci == 4) && (r == 2 || r == 3)) return pick(new int[]{0xD73A4A, 0xFF5566, 0xB82A3A}, ci, r, seed);
                return pick(new int[]{0x2E6B70, 0x3A8286, 0x4A9A98, 0x275C61, 0x5FB0AA}, ci, r, seed);
            case 3:
                return pick(new int[]{0xB0202F, 0xD03040, 0x8E1826, 0xE24455}, ci, r, seed);
            case 4:
                return pick(new int[]{0xB07C44, 0xA06E3A, 0xC08A4E, 0x94622F}, ci, r, seed);
            case 5:
                return pick(new int[]{0x6B4220, 0x5E3A1C, 0x754A26}, ci, r, seed);
            default:
                return pick(new int[]{0x7E5028, 0x724624, 0x8A5A30}, ci, r, seed);
        }
    }

    /**
     * Zeichnet eine Isometrie-Box um den Ursprung (Mittelpunkt der Deckelflaeche).
     * rowFrom/rowTo = welche Seitenzeilen, top = Deckelflaeche mitzeichnen.
     */
    static void box(GuiGraphicsExtractor g, int w, int rowFrom, int rowTo, int topFace, int leftFace, int rightFace,
                    float a, int seed, boolean top, int topShift, boolean outline) {
        int cols = 2 * w / CW;
        int half = cols / 2;
        for (int pass = outline ? 0 : 1; pass < 2; pass++) {
            int pad = pass == 0 ? 2 : 0;
            for (int ci = 0; ci < cols; ci++) {
                int x = -w + ci * CW;
                float mid = x + CW / 2f;
                int hh = Math.round((w - Math.abs(mid)) / 2f);
                boolean left = ci < half;
                int sideCi = left ? ci : ci - half;
                if (top) {
                    int r = 0;
                    for (int y = -hh + topShift; y < hh + topShift; y += CH, r++) {
                        int y2 = Math.min(y + CH, hh + topShift);
                        Gfx.rect(g, x - pad, y - pad, x + CW + pad, y2 + pad,
                                pass == 0 ? 0x000000 : col(topFace, ci, r, seed), a);
                    }
                }
                for (int r = rowFrom; r < rowTo; r++) {
                    int y1 = hh + r * CH;
                    Gfx.rect(g, x - pad, y1 - pad, x + CW + pad, y1 + CH + pad,
                            pass == 0 ? 0x000000 : col(left ? leftFace : rightFace, sideCi, r, seed), a);
                }
            }
        }
    }

    static void pedestal(GuiGraphicsExtractor g, float a) {
        box(g, PW, 0, PH / CH, 4, 5, 6, a, 7, true, 0, false);
    }

    static void body(GuiGraphicsExtractor g, float a, boolean open) {
        box(g, W, LID_ROWS, H / CH, 3, 1, 2, a, 3, open, LID_ROWS * CH, true);
    }

    static void lid(GuiGraphicsExtractor g, float a) {
        box(g, W, 0, LID_ROWS, 0, 1, 2, a, 3, true, 0, true);
    }
}
