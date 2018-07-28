package Nodes.CreationNodes;

import Util.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class PrematureStopCreation extends HoverBankerCreation {
    public PrematureStopCreation(Script script) {
        super(script);
    }

    @Override
    int waitForPotions() throws InterruptedException {
        MethodProvider.sleep(Statics.randomNormalDist(3000, 1000));
        Inventory inv = script.getInventory();
        boolean hovered = hoverOverBankOption();
        int maxCreatable = primaryCount > secondaryCount ? secondaryCount : primaryCount;
        new ConditionalSleep(25000) {
            @Override
            public boolean condition() {
                return inv.getAmount(recipe.getFinishedItemName()) >= maxCreatable-1;
            }
        }.sleep();

        if(hovered){
            Statics.shortRandomNormalDelay();
            script.getMouse().click(false);
        }
        return (int) Statics.randomNormalDist(1200, 200);
    }
}
