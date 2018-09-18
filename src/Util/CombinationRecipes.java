package Util;

public enum CombinationRecipes {

    AVANTOE("Avantoe", "Vial of water", "Avantoe potion (unf)", "avan", 261, 227, 103, 50, true),
    TOADFLAX("Toadflax", "Vial of water", "Toadflax potion (unf)", "toadfl", 2998, 227, 3002, 34, true),
    RANARR("Ranarr", "Vial of water", "Ranarr potion (unf)", "ranar", 257, 227, 99, 30, true),
    IRIT("Irit leaf", "Vial of water", "Irit potion (unf)", "irit", 259, 227, 101, 45, true),
    KWUARM("Kwuarm", "Vial of water", "Kwuarm potion (unf)", "kwu", 263, 227, 105, 55, true),
    HARRALANDER("Harralander", "Vial of water", "Harralander potion (unf)", "harra", 255, 227, 97, 21, true),

    ATTACK_POTION("Guam potion (unf)", "Eye of newt", "Attack potion (3)", "n/a", 91, 221, 121, 3, false),

    CLAY("Clay", "Jug of water", "Soft clay", "clay", 434, 1937, 1761, 1, false),
    AIR_BATTLESTAFF("Battlestaff", "Air orb", "Air Battlestaff", "", 1391, 573, 1397, 66, false),
    YEW_LONGBOW("Yew longbow (u)", "Bow string", "Yew longbow", "yew long", 66, 1777, 855, 70, false);

    private String primaryItemName, secondaryItemName, finishedItemName, geSearchTerm;
    private int primaryItemID, secondaryItemID, finishedItemID, reqLvl;
    private boolean isUnfPotion;

    CombinationRecipes(String primaryItemName, String secondaryItemName, String finishedItemName, String geSearchTerm, int primaryItemID, int secondaryItemID, int finishedItemID, int reqLvl, boolean isUnfPotion) {
        this.primaryItemName = primaryItemName;
        this.secondaryItemName = secondaryItemName;
        this.finishedItemName = finishedItemName;
        this.geSearchTerm = geSearchTerm;
        this.primaryItemID = primaryItemID;
        this.secondaryItemID = secondaryItemID;
        this.finishedItemID = finishedItemID;
        this.reqLvl = reqLvl;
        this.isUnfPotion = isUnfPotion;
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

    public int getPrimaryNotedItemID(){
        return primaryItemID+1;
    }

    public int getSecondaryItemID() {
        return secondaryItemID;
    }

    public int getSecondaryNotedItemID(){
        return secondaryItemID+1;
    }

    public int getFinishedItemID() {
        return finishedItemID;
    }

    public int getFinishedNotedItemID() {
        return getFinishedItemID()+1;
    }

    public int getReqLvl() {
        return reqLvl;
    }

    public boolean isUnfPotion() {
        return isUnfPotion;
    }
}
