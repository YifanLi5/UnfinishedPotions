package Util;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.Item;

//Original Author: LiveRare
public enum ItemData implements Filter<Item> {
    VIAL_OF_WATER("Vial of water", 227),

    CLEAN_AVANTOE("Avantoe", 261),
    CLEAN_TOADFLAX("Toadflax", 2998),
    CLEAN_IRIT("Irit leaf", 259),
    CLEAN_KWUARM("Kwuarm", 263),
    CLEAN_HARRALANDER("Harralander", 255),

    UNF_AVANTOE_POTION("Avantoe potion (unf)", 103),
    UNF_TOADFLAX_POTION("Toadflax potion (unf)", 3002),
    UNF_IRIT_POTION("Irit potion (unf)", 101),
    UNF_KWUARM_POTION("Kwuarm potion (unf)", 105),
    UNF_HARRALANDER_POTION("Harralander potion (unf)", 97);

    private String name;
    private int id;

    ItemData(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", id, name);
    }

    public String getGESearchTerm() {
        return name.split(" ")[0].substring(0, 4).toLowerCase();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean match(Item item) {
        return item.getId() == id || item.getName().equals(name);
    }
}