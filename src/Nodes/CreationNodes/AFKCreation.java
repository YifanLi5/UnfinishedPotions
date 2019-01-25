package Nodes.CreationNodes;

import Util.Statics;
import org.osbot.rs07.Bot;
import org.osbot.rs07.utility.ConditionalSleep;

/*
After items are finished combining wait a bit before proceeding to bank
 */
public class AFKCreation extends AbstractCreationNode {
    public AFKCreation(Bot bot) {
        super(bot);
    }

    @Override
    int waitForPotions() {
        mouse.moveOutsideScreen();
        new ConditionalSleep(25000) {
            @Override
            public boolean condition() throws InterruptedException {
                return !inventory.contains(recipe.getPrimary()) || !inventory.contains(recipe.getSecondary());
            }
        }.sleep();

        return (int) Statics.randomNormalDist(8000, 2000);
    }
}
