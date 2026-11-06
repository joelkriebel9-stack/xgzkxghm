package dev.gamblefx;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Erkennt SkyBlock-Ereignisse (Chat + Kisten-Fenstertitel) und startet Animationen.
 * Die Muster sind "best effort" - Hypixel aendert Texte gelegentlich. Neue Muster mit add(...) ergaenzen.
 */
public final class Triggers {
    private interface Maker { Drop make(Matcher m, Component msg); }

    private record Entry(Pattern pattern, Maker maker) {}

    private static final List<Entry> CHAT = new ArrayList<>();
    private static final Map<String, Rarity> DUNGEON_CHESTS = new HashMap<>();
    private static final Map<String, Rarity> KUUDRA_CHESTS = new HashMap<>();

    private static void add(String regex, Maker maker) {
        CHAT.add(new Entry(Pattern.compile(regex, Pattern.CASE_INSENSITIVE), maker));
    }

    static {
        // --- Drops (wie im Diana-Beispiel: "RARE DROP! Chimera I (+x% Magic Find)") ---
        add("^(VERY RARE|CRAZY RARE|RARE|INSANE|ULTRA RARE|PRAY TO RNGESUS) DROP! (.+?)(?: \\((.*)\\))?$",
                (m, msg) -> new Drop(Category.RARE_DROPS, m.group(1).toUpperCase() + " DROP!", m.group(2),
                        clean(m.group(3)), Rarity.max(dropRarity(m.group(1)), colorOf(msg, m.group(2), Rarity.COMMON))));
        add("^PET DROP! (.+?)(?: \\((.*)\\))?$",
                (m, msg) -> new Drop(Category.PET_DROPS, "PET DROP!", m.group(1), clean(m.group(2)),
                        colorOf(msg, m.group(1), Rarity.LEGENDARY)));

        // --- Dungeons ---
        add("^Team Score: (\\d+) \\(([SABCD]\\+?)\\)",
                (m, msg) -> new Drop(Category.DUNGEON_SCORE, Category.DUNGEON_SCORE.title,
                        "Score " + m.group(1) + " (" + m.group(2) + ")", "", gradeRarity(m.group(2))));

        // --- Slayer ---
        add("^SLAYER QUEST COMPLETE!",
                (m, msg) -> new Drop(Category.SLAYER, Category.SLAYER.title, "Quest abgeschlossen", "", Rarity.EPIC));

        // --- Angeln ---
        add("^TROPHY FISH! You caught an? (.+?) (BRONZE|SILVER|GOLD|DIAMOND)\\.?$",
                (m, msg) -> new Drop(Category.FISHING, "TROPHY FISH!", m.group(1) + " (" + m.group(2) + ")", "",
                        trophyRarity(m.group(2))));
        add("^(GOOD|GREAT) CATCH! (.+)$",
                (m, msg) -> new Drop(Category.FISHING, m.group(1).toUpperCase() + " CATCH!", m.group(2), "",
                        m.group(1).equalsIgnoreCase("GREAT") ? Rarity.EPIC : Rarity.RARE));

        // --- Mining ---
        add("^PRISTINE! You found (.+?)!?$",
                (m, msg) -> new Drop(Category.MINING, "PRISTINE!", m.group(1), "", Rarity.EPIC));
        add("^You uncovered a treasure chest!",
                (m, msg) -> new Drop(Category.MINING, Category.MINING.title, "Treasure Chest", "", Rarity.RARE));

        // --- Wirtschaft (Seltenheit nach Coin-Wert) ---
        add("^You purchased (.+?) for ([\\d,\\.]+) coins!$",
                (m, msg) -> new Drop(Category.AUCTION, "GEKAUFT!", m.group(1), m.group(2) + " Coins",
                        Rarity.fromCoins(num(m.group(2)))));
        add("^\\[Auction\\] .+? bought (.+?) for ([\\d,\\.]+) coins CLAIM!",
                (m, msg) -> new Drop(Category.AUCTION, "VERKAUFT!", m.group(1), m.group(2) + " Coins",
                        Rarity.fromCoins(num(m.group(2)))));
        add("^\\[Bazaar\\] (Bought|Sold) ([\\d,]+)x (.+?) for ([\\d,\\.]+) coins!",
                (m, msg) -> new Drop(Category.BAZAAR, m.group(1).toUpperCase() + "!", m.group(2) + "x " + m.group(3),
                        m.group(4) + " Coins", Rarity.fromCoins(num(m.group(4)))));
        add("^You sold (.+?) for ([\\d,\\.]+) Coins!$",
                (m, msg) -> new Drop(Category.NPC_SALES, "VERKAUFT!", m.group(1), m.group(2) + " Coins",
                        Rarity.fromCoins(num(m.group(2)))));

        // --- Fortschritt ---
        add("^SKILL LEVEL UP (.+)$",
                (m, msg) -> new Drop(Category.SKILL_LEVEL, "SKILL LEVEL UP!", m.group(1), "", Rarity.EPIC));
        add("^COLLECTION (?:LEVEL UP|UNLOCKED)\\s*(.*)$",
                (m, msg) -> new Drop(Category.COLLECTIONS, "COLLECTION!", m.group(1).isEmpty() ? "Collection" : m.group(1),
                        "", Rarity.RARE));

        // --- Kisten-GUIs (Fenstertitel) ---
        DUNGEON_CHESTS.put("Wood Chest", Rarity.COMMON);
        DUNGEON_CHESTS.put("Gold Chest", Rarity.UNCOMMON);
        DUNGEON_CHESTS.put("Diamond Chest", Rarity.RARE);
        DUNGEON_CHESTS.put("Emerald Chest", Rarity.EPIC);
        DUNGEON_CHESTS.put("Obsidian Chest", Rarity.LEGENDARY);
        DUNGEON_CHESTS.put("Bedrock Chest", Rarity.MYTHIC);
        KUUDRA_CHESTS.put("Free Chest", Rarity.COMMON);
        KUUDRA_CHESTS.put("Paid Chest", Rarity.LEGENDARY);
    }

    private Triggers() {}

    public static void handleChat(Component msg) {
        String text = msg.getString().trim();
        if (text.isEmpty()) return;
        for (Entry en : CHAT) {
            Matcher m = en.pattern().matcher(text);
            if (m.find()) {
                try {
                    AnimationManager.trigger(en.maker().make(m, msg));
                } catch (Exception ignored) {
                }
                return;
            }
        }
    }

    public static boolean handleGui(String title) {
        Rarity r = DUNGEON_CHESTS.get(title);
        if (r != null) {
            AnimationManager.trigger(new Drop(Category.DUNGEON_CHESTS, "CHEST OPENED!", title, "", r));
            return true;
        }
        r = KUUDRA_CHESTS.get(title);
        if (r != null) {
            AnimationManager.trigger(new Drop(Category.KUUDRA_CHESTS, "CHEST OPENED!", title, "", r));
            return true;
        }
        return false;
    }

    // ---------- Helfer ----------
    private static String clean(String s) {
        if (s == null) return "";
        return s.replaceAll("[^\\x20-\\x7E]", "").replaceAll("\\s+", " ").trim();
    }

    private static double num(String s) {
        try { return Double.parseDouble(s.replace(",", "")); } catch (Exception e) { return 0; }
    }

    private static Rarity dropRarity(String prefix) {
        return switch (prefix.toUpperCase()) {
            case "RARE" -> Rarity.RARE;
            case "VERY RARE" -> Rarity.EPIC;
            case "CRAZY RARE", "ULTRA RARE" -> Rarity.LEGENDARY;
            case "INSANE" -> Rarity.MYTHIC;
            default -> Rarity.DIVINE; // PRAY TO RNGESUS
        };
    }

    private static Rarity gradeRarity(String g) {
        return switch (g.toUpperCase()) {
            case "S+" -> Rarity.MYTHIC;
            case "S" -> Rarity.LEGENDARY;
            case "A" -> Rarity.EPIC;
            case "B" -> Rarity.RARE;
            case "C" -> Rarity.UNCOMMON;
            default -> Rarity.COMMON;
        };
    }

    private static Rarity trophyRarity(String tier) {
        return switch (tier.toUpperCase()) {
            case "BRONZE" -> Rarity.COMMON;
            case "SILVER" -> Rarity.RARE;
            case "GOLD" -> Rarity.LEGENDARY;
            default -> Rarity.DIVINE;
        };
    }

    /** Liest die Textfarbe des Item-Namens aus der Chat-Nachricht (= SkyBlock-Seltenheitsfarbe). */
    private static Rarity colorOf(Component msg, String name, Rarity fallback) {
        String key = name.split(" ")[0];
        Rarity[] found = {null};
        msg.visit((style, s) -> {
            if (s.contains(key) && style.getColor() != null) {
                Rarity r = Rarity.fromRgb(style.getColor().getValue());
                if (r != null) found[0] = r;
            }
            return Optional.empty();
        }, Style.EMPTY);
        return found[0] != null ? found[0] : fallback;
    }
}
