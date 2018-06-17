package Nodes.CreationNodes;

import Util.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class BasicCreation extends AbstractCreationNode {
    public BasicCreation(Script script) {
        super(script);
    }

    @Override
    int waitForPotions() {
        Inventory inv = script.getInventory();
        new ConditionalSleep(25000) {
            @Override
            public boolean condition() throws InterruptedException {
                return !inv.contains(recipe.getPrimaryItemName()) || !inv.contains(recipe.getSecondaryItemName());
            }
        }.sleep();

        return (int) Statics.randomNormalDist(2000, 2000);
    }


}
