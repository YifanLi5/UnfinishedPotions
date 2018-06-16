package Util;

public enum UnfPotionRecipes {

    AVANTOE("Avantoe", "Vial of water", "Avantoe potion (unf)", "avan", 261, 227, 103),
    TOADFLAX("Toadflax", "Vial of water", "Toadflax potion (unf)", "toadfl", 2998, 227, 3002),
    RANARR("Ranarr", "Vial of water", "Ranarr potion (unf)", "ranar", 257, 227, 99),
    IRIT("Irit leaf", "Vial of water", "Irit potion (unf)", "irit", 259, 227, 101),
    KWUARM("Kwuarm", "Vial of water", "Kwuarm potion (unf)", "Kwu", 263, 227, 105),
    CLAY("Clay", "Jug of water", "Soft clay", "clay", 434, 1937, 1761);

    private String primaryItemName, secondaryItemName, finishedItemName, geSearchTerm;
    private int primaryItemID, secondaryItemID, finishedItemID;

    UnfPotionRecipes(String primaryItemName, String secondaryItemName, String finishedItemName, String geSearchTerm, int primaryItemID, int secondaryItemID, int finishedItemID) {
        this.primaryItemName = primaryItemName;
        this.secondaryItemName = secondaryItemName;
        this.finishedItemName = finishedItemName;
        this.geSearchTerm = geSearchTerm;
        this.primaryItemID = primaryItemID;
        this.secondaryItemID = secondaryItemID;
        this.finishedItemID = finishedItemID;
    }

    public String getPrimaryItemName() {
        return primaryItemName;
    }

    public String getSecondaryItemName() {
        return secondaryItemName;
    }

    public String getFinishedItemName() {
        return finishedItemName;
    }

    public String getGeSearchTerm() {
        return geSearchTerm;
    }

    public int getPrimaryItemID() {
        return primaryItemID;
    }

    public int getSecondaryItemID() {
        return secondaryItemID;
    }

    public int getFinishedItemID() {
        return finishedItemID;
    }
}
