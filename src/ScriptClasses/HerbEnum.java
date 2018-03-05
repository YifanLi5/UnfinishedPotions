package ScriptClasses;

public enum HerbEnum {
    AVANTOE(261, 3500, "Avantoe", "avantoe"), TOADFLAX(2998, 3500, "Toadflax", "toadflax"), RANARR(257, 10000, "Ranarr", "ranarr");

    private int itemID;
    private int estimatedHighPrice;
    private String itemName;
    private String geSearchTerm;


    HerbEnum(int itemID, int estimatedHighPricem, String itemName, String geSearchTerm) {
        this.itemID = itemID;
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

    public int getEstimatedHighPrice() {
        return estimatedHighPrice;
    }
}
