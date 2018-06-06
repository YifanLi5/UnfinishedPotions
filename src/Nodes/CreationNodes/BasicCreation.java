package Nodes.CreationNodes;

import Util.HerbAndPotionsEnum;
import Util.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class BasicCreation extends AbstractCreationNode {
    public BasicCreation(Script script, HerbAndPotionsEnum herbAndPotionsEnum) {
        super(script, herbAndPotionsEnum);
    }

    @Override
    int waitForPotions() {
        Inventory inv = script.getInventory();
        new ConditionalSleep(20000) {
            @Override
            public boolean condition() throws InterruptedException {
                return !inv.contains(HerbAndPotionsEnum.VIAL_OF_WATER.getItemName()) || !inv.contains(item.getItemName());
            }
        }.sleep();

        return (int) Statics.randomNormalDist(2000, 2000);
    }


}
