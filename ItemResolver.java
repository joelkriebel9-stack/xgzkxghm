package dev.gamblefx;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.regex.Pattern;

/**
 * Ordnet einem Drop ein Vanilla-Item als Symbol zu (Hypixel-Items gibt es als solche nicht).
 * Eigene Zuordnungen einfach hier ergaenzen.
 */
public final class ItemResolver {
    private static final Pattern ROMAN_SUFFIX = Pattern.compile(".*\\s(?:[IVX]{1,4}|10|[1-9])$");

    private ItemResolver() {}

    public static ItemStack resolve(Drop d) {
        return new ItemStack(pick(d));
    }

    private static Item pick(Drop d) {
        String n = d.name().toLowerCase();
        switch (d.category()) {
            case PET_DROPS: return Items.PLAYER_HEAD;
            case DUNGEON_CHESTS: return Items.CHEST;
            case KUUDRA_CHESTS: return Items.ENDER_CHEST;
            case DUNGEON_SCORE: return Items.NETHER_STAR;
            case SKILL_LEVEL: return Items.EXPERIENCE_BOTTLE;
            case COLLECTIONS: return Items.BOOK;
            case AUCTION: return Items.GOLD_INGOT;
            case BAZAAR: return Items.EMERALD;
            case NPC_SALES: return Items.GOLD_NUGGET;
            case SLAYER: return Items.DIAMOND_SWORD;
            default: break;
        }
        if (ROMAN_SUFFIX.matcher(d.name()).matches() || n.contains("enchanted book")) return Items.ENCHANTED_BOOK;
        if (n.contains("sword") || n.contains("blade") || n.contains("hyperion") || n.contains("scylla")
                || n.contains("valkyrie") || n.contains("astraea")) return Items.DIAMOND_SWORD;
        if (n.contains("bow")) return Items.BOW;
        if (n.contains("helmet")) return Items.DIAMOND_HELMET;
        if (n.contains("chestplate")) return Items.DIAMOND_CHESTPLATE;
        if (n.contains("leggings")) return Items.DIAMOND_LEGGINGS;
        if (n.contains("boots")) return Items.DIAMOND_BOOTS;
        if (n.contains("pickaxe") || n.contains("drill")) return Items.DIAMOND_PICKAXE;
        if (n.contains("rod")) return Items.FISHING_ROD;
        if (n.contains("egg")) return Items.EGG;
        if (n.contains("gemstone") || n.contains("amethyst")) return Items.AMETHYST_SHARD;
        if (n.contains("ruby") || n.contains("emerald") || n.contains("jade")) return Items.EMERALD;
        if (n.contains("scroll")) return Items.PAPER;
        if (n.contains("fish") || n.contains("catch")) return Items.TROPICAL_FISH;
        if (n.contains("treasure")) return Items.CHEST;
        return Items.NETHER_STAR;
    }
}
