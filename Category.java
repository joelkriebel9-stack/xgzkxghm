package dev.gamblefx;

public enum Category {
    RARE_DROPS("Seltene Drops", Style.CRATE),
    PET_DROPS("Pet Drops", Style.CRATE),
    DUNGEON_CHESTS("Dungeon-Kisten", Style.CRATE),
    DUNGEON_SCORE("Dungeon-Score", Style.SLOTS),
    SLAYER("Slayer", Style.STRIP),
    FISHING("Angeln", Style.STRIP),
    MINING("Mining", Style.STRIP),
    AUCTION("Auktionshaus", Style.SLOTS),
    BAZAAR("Basar", Style.SLOTS),
    NPC_SALES("NPC-Verkaeufe", Style.SLOTS),
    SKILL_LEVEL("Skill-Level", Style.SLOTS),
    COLLECTIONS("Collections", Style.STRIP),
    KUUDRA_CHESTS("Kuudra-Kisten", Style.CRATE);

    public final String title;
    public final Style defaultStyle;

    Category(String title, Style style) {
        this.title = title;
        this.defaultStyle = style;
    }
}
