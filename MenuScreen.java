package dev.gamblefx;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MenuScreen extends Screen {
    private static final float[] SPEEDS = {0.5f, 1f, 1.5f, 2f, 3f};
    private static final String[] MODES = {"CRATE", "STRIP", "SLOTS", "MIXED"};
    private static final String[] MODE_NAMES = {"Crate-Reveal", "Case Opening", "Slot Machine", "Gemischt"};

    private final Random rnd = new Random();
    private final Map<Category, Button> catButtons = new HashMap<>();
    private Button bMaster, bSounds, bConfetti, bSpeed, bStyle;
    private int top;

    public MenuScreen() {
        super(Component.literal("GambleFX"));
    }

    private static String onOff(boolean b) { return b ? "\u00a7aAN" : "\u00a7cAUS"; }

    private int modeIndex() {
        for (int i = 0; i < MODES.length; i++) if (MODES[i].equals(Config.getStyleMode())) return i;
        return 0;
    }

    @Override
    protected void init() {
        catButtons.clear();
        top = Math.max(16, this.height / 2 - 108);
        int lx = this.width / 2 - 155, rx = this.width / 2 + 5, w = 150, bh = 16;

        bMaster = addRenderableWidget(Button.builder(Component.empty(), b -> {
            Config.setEnabled(!Config.isEnabled()); refresh();
        }).bounds(lx, top, w, bh).build());
        bSounds = addRenderableWidget(Button.builder(Component.empty(), b -> {
            Config.setSounds(!Config.isSounds()); refresh();
        }).bounds(rx, top, w, bh).build());
        bConfetti = addRenderableWidget(Button.builder(Component.empty(), b -> {
            Config.setConfetti(!Config.isConfetti()); refresh();
        }).bounds(lx, top + 18, w, bh).build());
        bSpeed = addRenderableWidget(Button.builder(Component.empty(), b -> {
            int best = 0;
            for (int i = 0; i < SPEEDS.length; i++) if (Math.abs(SPEEDS[i] - Config.getSpeed()) < 0.01f) best = i;
            Config.setSpeed(SPEEDS[(best + 1) % SPEEDS.length]); refresh();
        }).bounds(rx, top + 18, w, bh).build());
        bStyle = addRenderableWidget(Button.builder(Component.empty(), b -> {
            Config.setStyleMode(MODES[(modeIndex() + 1) % MODES.length]); refresh();
        }).bounds(lx, top + 36, w, bh).build());
        addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> this.onClose())
                .bounds(rx, top + 36, w, bh).build());

        // Grosser Test-Knopf: spielt das Diana-Beispiel (Chimera I) ab
        addRenderableWidget(Button.builder(Component.literal("\u00a76\u25B6 Drop-Animation testen (Diana: Chimera I)"), b -> {
            Config.save();
            this.onClose();
            AnimationManager.force(Drop.diana());
        }).bounds(lx, top + 54, 310, bh).build());

        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            final Category c = cats[i];
            int x = (i % 2 == 0) ? lx : rx;
            int y = top + 76 + (i / 2) * 18;
            Button toggle = addRenderableWidget(Button.builder(Component.empty(), b -> {
                Config.setCategoryEnabled(c, !Config.isCategoryEnabled(c)); refresh();
            }).bounds(x, y, 120, bh).build());
            catButtons.put(c, toggle);
            addRenderableWidget(Button.builder(Component.literal("Test"), b -> {
                Config.save();
                this.onClose();
                Rarity r = Rarity.values()[Rarity.RARE.ordinal() + rnd.nextInt(4)]; // RARE..MYTHIC
                AnimationManager.force(Drop.sample(c, r));
            }).bounds(x + 122, y, 28, bh).build());
        }
        refresh();
    }

    private void refresh() {
        bMaster.setMessage(Component.literal("Alle Animationen: " + onOff(Config.isEnabled())));
        bSounds.setMessage(Component.literal("Sounds: " + onOff(Config.isSounds())));
        bConfetti.setMessage(Component.literal("Konfetti: " + onOff(Config.isConfetti())));
        bSpeed.setMessage(Component.literal("Tempo: \u00a7e" + Config.getSpeed() + "x"));
        bStyle.setMessage(Component.literal("Stil: \u00a7e" + MODE_NAMES[modeIndex()]));
        for (Map.Entry<Category, Button> e : catButtons.entrySet()) {
            e.getValue().setMessage(Component.literal(e.getKey().title + ": " + onOff(Config.isCategoryEnabled(e.getKey()))));
        }
        Config.save();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(this.font, Component.literal("\u00a76\u00a7lGambleFX \u00a7r\u00a77- Einstellungen"),
                this.width / 2, top - 12, 0xFFFFFFFF);
        g.centeredText(this.font, Component.literal("\u00a77Test = Vorschau der Animation dieser Kategorie"),
                this.width / 2, top + 76 + 7 * 18 + 2, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        Config.save();
        super.onClose();
    }
}
