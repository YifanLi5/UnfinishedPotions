package Nodes.CreationNodes;

import Util.HerbAndPotionsEnum;
import Util.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class MouseOffscreen extends AbstractCreationNode {
    public MouseOffscreen(Script script, HerbAndPotionsEnum herbAndPotionsEnum) {
        super(script, herbAndPotionsEnum);
    }

    @Override
    int waitForPotions() {
        Mouse mouse = script.getMouse();
        Inventory inv = script.getInventory();
        //wait until all potions are finished
        mouse.moveOutsideScreen();
        new ConditionalSleep(20000) {
            @Override
            public boolean condition() throws InterruptedException {
                return !inv.contains(HerbAndPotionsEnum.VIAL_OF_WATER.getItemName()) || !inv.contains(cleanHerb.getItemName());
            }
        }.sleep();


        return (int) Statics.randomNormalDist(5000, 2000);
    }


}
