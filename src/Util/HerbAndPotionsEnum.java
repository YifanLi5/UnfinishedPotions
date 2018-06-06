package Util;

public enum HerbAndPotionsEnum {
    AVANTOE(261, 262, "Avantoe", "avantoe", 103, 104, "Avantoe potion (unf)"),
    TOADFLAX(2998, 2999, "Toadflax", "toadflax", 3002, 3003, "Toadflax potion (unf)"),
    RANARR(257, 258, "Ranarr", "ranarr", 99, 100, "Ranarr potion (unf)"),
    VIAL_OF_WATER(227, 228, "Vial of water", "vial", -1, -1, "");

    private int herbItemID, notedHerbItemID;
    private String itemName, geSearchTerm;

    private int unfPotionItemID, notedUnfPotionItemID;
    private String unfPotionName;

    HerbAndPotionsEnum(int herbItemID, int notedHerbItemID, String itemName, String geSearchTerm, int unfPotionItemID, int notedUnfPotionItemID, String unfPotionName) {
        this.herbItemID = herbItemID;
        this.notedHerbItemID = notedHerbItemID;
        this.itemName = itemName;
        this.geSearchTerm = geSearchTerm;
        this.unfPotionItemID = unfPotionItemID;
        this.notedUnfPotionItemID = notedUnfPotionItemID;
        this.unfPotionName = unfPotionName;
    }

    public int getHerbItemID() {
        return herbItemID;
    }

    public String getItemName() {
        return itemName;
    }

    public String getGeSearchTerm() {
        return geSearchTerm;
    }

    public int getNotedHerbItemID() {
        return notedHerbItemID;
    }

    public int getUnfPotionItemID() {
        return unfPotionItemID;
    }

    public int getNotedUnfPotionItemID() {
        return notedUnfPotionItemID;
    }

    public String getUnfPotionName() {
        return unfPotionName;
    }
}
