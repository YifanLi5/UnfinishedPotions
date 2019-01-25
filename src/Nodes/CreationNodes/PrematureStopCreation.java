package Nodes.CreationNodes;

import Util.Statics;
import org.osbot.rs07.Bot;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalSleep;
/*
While items are combining, right click hover the bank's open option.
Before all items have combined, prematurely open the bank.
Emulate a human mistiming item combination speed, over eager to grind his virtual levels, and opening the bank too fast.
Efficiency scape is fun isn't it! What more efficient than writing a script to play the game for you?
 */
public class PrematureStopCreation extends HoverBankerCreation { //extend HoverBank to get hoverOverBankOption() method
    public PrematureStopCreation(Bot bot) {
        super(bot);
    }

    @Override
    int waitForPotions() throws InterruptedException {
        MethodProvider.sleep(Statics.randomNormalDist(3000, 1000));
        boolean hovered = hoverOverBankOption();
        int maxCreatable = primaryCount > secondaryCount ? secondaryCount : primaryCount;
        new ConditionalSleep(25000) {
            @Override
            public boolean condition() {
                return inventory.getAmount(recipe.getProduct()) >= maxCreatable-1; //stop when there is about 1 item left to combine.
            }
        }.sleep();

        if(hovered){
            mouse.click(false);
        }
        return (int) Statics.randomNormalDist(1200, 200);
    }
}
