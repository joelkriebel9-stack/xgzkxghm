package dev.gamblefx;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

public class GambleFXClient implements ClientModInitializer {
    public static final String MODID = "gamblefx";
    public static volatile boolean requestMenu = false;

    private static KeyMapping openMenuKey;
    private static final Random RND = new Random();
    private static final Set<Screen> SEEN = Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public void onInitializeClient() {
        Config.load();

        // --- Taste: Menue oeffnen (Standard: Rechts-Shift) ---
        KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MODID, "main"));
        openMenuKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.gamblefx.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, cat));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean open = requestMenu;
            while (openMenuKey.consumeClick()) open = true;
            if (open) {
                requestMenu = false;
                client.gui.setScreen(new MenuScreen());
            }
        });

        // --- Chat-Ereignisse (Drops, Auktionen, ...) ---
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) Triggers.handleChat(message);
        });

        // --- Rendering: HUD (kein Screen offen) + ueber allen Screens ---
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MODID, "animations"), (graphics, tracker) -> {
            if (Minecraft.getInstance().gui.screen() == null) AnimationManager.render(graphics);
        });
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!SEEN.add(screen)) return; // nur einmal pro Screen-Instanz (AFTER_INIT feuert auch bei Resize)
            ScreenEvents.afterExtract(screen).register((s, graphics, mx, my, dt) -> AnimationManager.render(graphics));
            if (screen instanceof ContainerScreen) Triggers.handleGui(screen.getTitle().getString());
        });

        // --- Befehle: /gamblefx  (Alias /gfx) ---
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, ctx) -> {
            LiteralCommandNode<FabricClientCommandSource> node = dispatcher.register(
                    ClientCommands.literal("gamblefx")
                            .executes(c -> { requestMenu = true; return 1; })
                            .then(ClientCommands.literal("diana").executes(c -> {
                                AnimationManager.force(Drop.diana());
                                return 1;
                            }))
                            .then(ClientCommands.literal("list").executes(c -> {
                                c.getSource().sendFeedback(Component.literal("Kategorien: " + Arrays.toString(Category.values())));
                                return 1;
                            }))
                            .then(ClientCommands.literal("test")
                                    .executes(c -> {
                                        Category cc = Category.values()[RND.nextInt(Category.values().length)];
                                        AnimationManager.force(Drop.sample(cc, randomRarity()));
                                        return 1;
                                    })
                                    .then(ClientCommands.argument("kategorie", StringArgumentType.word())
                                            .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                                    Arrays.stream(Category.values()).map(Enum::name), b))
                                            .executes(c -> test(c.getSource(), StringArgumentType.getString(c, "kategorie"), null))
                                            .then(ClientCommands.argument("seltenheit", StringArgumentType.word())
                                                    .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                                            Arrays.stream(Rarity.values()).map(Enum::name), b))
                                                    .executes(c -> test(c.getSource(),
                                                            StringArgumentType.getString(c, "kategorie"),
                                                            StringArgumentType.getString(c, "seltenheit")))))));
            dispatcher.register(ClientCommands.literal("gfx").redirect(node));
        });
    }

    private static Rarity randomRarity() {
        return Rarity.values()[Rarity.RARE.ordinal() + RND.nextInt(4)];
    }

    private static int test(FabricClientCommandSource src, String cat, String rar) {
        Category c;
        try {
            c = Category.valueOf(cat.toUpperCase());
        } catch (IllegalArgumentException e) {
            src.sendError(Component.literal("Unbekannte Kategorie. /gamblefx list"));
            return 0;
        }
        Rarity r = randomRarity();
        if (rar != null) {
            try {
                r = Rarity.valueOf(rar.toUpperCase());
            } catch (IllegalArgumentException e) {
                src.sendError(Component.literal("Unbekannte Seltenheit (COMMON..DIVINE)"));
                return 0;
            }
        }
        AnimationManager.force(Drop.sample(c, r));
        return 1;
    }
}
