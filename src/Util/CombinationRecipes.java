package Util;
import org.osbot.rs07.api.ui.Skill;

public enum CombinationRecipes {
    AVANTOE_UNF_RECIPE(ItemData.CLEAN_AVANTOE, ItemData.VIAL_OF_WATER, ItemData.UNF_AVANTOE_POTION, Skill.HERBLORE, 50, true),
    TOADFLAX_UNF_RECIPE(ItemData.CLEAN_TOADFLAX, ItemData.VIAL_OF_WATER, ItemData.UNF_TOADFLAX_POTION, Skill.HERBLORE, 34, true),
    IRIT_UNF_RECIPE(ItemData.CLEAN_IRIT, ItemData.VIAL_OF_WATER, ItemData.UNF_IRIT_POTION, Skill.HERBLORE, 48, true),
    KWUARM_UNF_RECIPE(ItemData.CLEAN_KWUARM, ItemData.VIAL_OF_WATER, ItemData.UNF_KWUARM_POTION, Skill.HERBLORE, 55, true),
    HARRALANDER_UNF_RECIPE(ItemData.CLEAN_HARRALANDER, ItemData.VIAL_OF_WATER, ItemData.UNF_HARRALANDER_POTION, Skill.HERBLORE, 22, true);

    private ItemData primary, secondary, product;
    private Skill skill;
    private int reqLvl;
    private boolean allowGERestock;

    CombinationRecipes(ItemData primary, ItemData secondary, ItemData product, Skill skill, int reqLvl, boolean allowGERestock) {
        this.primary = primary;
        this.secondary = secondary;
        this.product = product;
        this.skill = skill;
        this.reqLvl = reqLvl;
        this.allowGERestock = allowGERestock;
    }

    public ItemData getPrimary() {
        return primary;
    }

    public ItemData getSecondary() {
        return secondary;
    }

    public ItemData getProduct() {
        return product;
    }

    public Skill getSkill() {
        return skill;
    }

    public int getReqLvl() {
        return reqLvl;
    }

    public boolean canUseGE() {
        return allowGERestock;
    }
}
