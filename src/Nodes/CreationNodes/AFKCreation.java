package Nodes.CreationNodes;

import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class AFKCreation extends AbstractCreationNode {
    public AFKCreation(Script script, ComponentsEnum components) {
        super(script, components);
    }

    @Override
    int waitForPotions() {
        script.getMouse().moveOutsideScreen();


        Inventory inv = script.getInventory();
        new ConditionalSleep(25000) {
            @Override
            public boolean condition() throws InterruptedException {
                return !inv.contains(components.getPrimaryItemName()) || !inv.contains(components.getSecondaryItemName());
            }
        }.sleep();

        return (int) Statics.randomNormalDist(4000, 750);
    }
}
