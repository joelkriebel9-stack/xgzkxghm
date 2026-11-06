package dev.gamblefx;

/** Ein Ereignis, das animiert werden soll. */
public record Drop(Category category, String header, String name, String sub, Rarity rarity) {

    /** Das Beispiel aus dem Video: Diana / Minos Inquisitor. */
    public static Drop diana() {
        return new Drop(Category.RARE_DROPS, "RARE DROP!", "Chimera I",
                "1 in ~80 Minos Inquisitors \u00b7 test run", Rarity.MYTHIC);
    }

    /** Beispiel-Drop fuer Vorschau/Test einer Kategorie. */
    public static Drop sample(Category c, Rarity r) {
        String name = switch (c) {
            case RARE_DROPS -> "Chimera I";
            case PET_DROPS -> "Ender Dragon";
            case DUNGEON_CHESTS -> "Obsidian Chest";
            case KUUDRA_CHESTS -> "Paid Chest";
            case DUNGEON_SCORE -> "Score 317 (S+)";
            case SLAYER -> "Quest abgeschlossen";
            case FISHING -> "Blobfish (GOLD)";
            case MINING -> "Flawless Ruby Gemstone";
            case AUCTION -> "Hyperion";
            case BAZAAR -> "Bought 64x Enchanted Diamond";
            case NPC_SALES -> "Enchanted Coal x16";
            case SKILL_LEVEL -> "Mining XI";
            case COLLECTIONS -> "Mithril";
        };
        String header = c == Category.RARE_DROPS || c == Category.PET_DROPS
                ? (c == Category.PET_DROPS ? "PET DROP!" : "RARE DROP!") : c.title;
        return new Drop(c, header, name, "test run", r);
    }
}
