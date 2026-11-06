package dev.gamblefx;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayDeque;
import java.util.Deque;

public final class AnimationManager {
    private static final Deque<Anim> queue = new ArrayDeque<>();
    private static Anim current;

    private AnimationManager() {}

    /** Echtes Spiel-Ereignis: beachtet Master- und Kategorie-Schalter. */
    public static void trigger(Drop d) {
        if (!Config.isEnabled() || !Config.isCategoryEnabled(d.category())) return;
        force(d);
    }

    /** Ignoriert alle Schalter (Test/Vorschau). */
    public static void force(Drop d) {
        if (queue.size() >= 3) return;
        Style s = switch (Config.getStyleMode()) {
            case "CRATE" -> Style.CRATE;
            case "STRIP" -> Style.STRIP;
            case "SLOTS" -> Style.SLOTS;
            default -> d.category().defaultStyle;
        };
        queue.add(switch (s) {
            case CRATE -> new CrateAnimation(d);
            case STRIP -> new CaseAnimation(d);
            case SLOTS -> new SlotAnimation(d);
        });
    }

    public static void clear() {
        queue.clear();
        current = null;
    }

    public static void render(GuiGraphicsExtractor g) {
        if (current == null) current = queue.poll();
        if (current == null) return;
        if (!current.render(g, g.guiWidth(), g.guiHeight())) current = null;
    }
}
