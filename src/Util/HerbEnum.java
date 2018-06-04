package Util;

public enum HerbEnum {
    AVANTOE(261, -1, "Avantoe", "avantoe"),
    TOADFLAX(2998, -1, "Toadflax", "toadflax"),
    RANARR(257, -1, "Ranarr", "ranarr"),
    VIAL_OF_WATER(227, 228, "Vial of water", "vial");

    private int itemID;
    private int notedItemID;
    private String itemName;
    private String geSearchTerm;


    HerbEnum(int itemID, int notedItemID, String itemName, String geSearchTerm) {
        this.itemID = itemID;
        this.notedItemID = notedItemID;
        this.itemName = itemName;
        this.geSearchTerm = geSearchTerm;
    }

    public int getItemID() {
        return itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public String getGeSearchTerm() {
        return geSearchTerm;
    }

    public int getNotedItemID() {
        return notedItemID;
    }
}
