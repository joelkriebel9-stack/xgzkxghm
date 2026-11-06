package dev.gamblefx;

import java.util.Random;

public enum Rarity {
    COMMON("COMMON", 0xFFFFFF, 50),
    UNCOMMON("UNCOMMON", 0x55FF55, 26),
    RARE("RARE", 0x5555FF, 13),
    EPIC("EPIC", 0xAA00AA, 7),
    LEGENDARY("LEGENDARY", 0xFFAA00, 3.5),
    MYTHIC("MYTHIC", 0xFF55FF, 1.2),
    DIVINE("DIVINE", 0x55FFFF, 0.3),
    SPECIAL("SPECIAL", 0xFF5555, 0);

    public final String label;
    public final int rgb;
    public final double weight;

    Rarity(String label, int rgb, double weight) {
        this.label = label;
        this.rgb = rgb;
        this.weight = weight;
    }

    public boolean atLeast(Rarity o) { return ordinal() >= o.ordinal(); }

    public static Rarity max(Rarity a, Rarity b) { return a.ordinal() >= b.ordinal() ? a : b; }

    /** Findet die Seltenheit zu einer Chat-Textfarbe (z.B. 0xFF55FF = Mythic). */
    public static Rarity fromRgb(int rgb) {
        for (Rarity r : values()) if (r.rgb == (rgb & 0xFFFFFF)) return r;
        return null;
    }

    public static Rarity randomFiller(Random rnd) {
        double sum = 0;
        for (Rarity r : values()) sum += r.weight;
        double x = rnd.nextDouble() * sum;
        for (Rarity r : values()) {
            x -= r.weight;
            if (x <= 0 && r.weight > 0) return r;
        }
        return COMMON;
    }

    public static Rarity fromCoins(double coins) {
        if (coins < 100_000) return COMMON;
        if (coins < 500_000) return UNCOMMON;
        if (coins < 2_000_000) return RARE;
        if (coins < 10_000_000) return EPIC;
        if (coins < 50_000_000) return LEGENDARY;
        if (coins < 200_000_000) return MYTHIC;
        return DIVINE;
    }
}
