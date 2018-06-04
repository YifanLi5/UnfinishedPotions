package Nodes.CreationNodes;

import GrandExchange.GrandExchangeObserver;
import Util.HerbEnum;
import ScriptClasses.MarkovNodeExecutor;
import Util.Statics;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class MouseOffscreen extends AbstractCreationNode {

    public static final int NODE_EXECUTION_WEIGHT = 80;
    private static MarkovNodeExecutor.ExecutableNode singleton;

    private MouseOffscreen(Script script, HerbEnum herbEnum) {
        super(script, herbEnum);
    }

    public static MarkovNodeExecutor.ExecutableNode getInstance(Script script, HerbEnum herbEnum){
        if(singleton == null) singleton = new MouseOffscreen(script, herbEnum);
        return singleton;
    }

    @Override
    int waitForPotions() {
        Mouse mouse = script.getMouse();
        Inventory inv = script.getInventory();
        //wait until all potions are finished
        mouse.moveOutsideScreen();
        new ConditionalSleep(10000) {
            @Override
            public boolean condition() throws InterruptedException {
                return !inv.contains(HerbEnum.VIAL_OF_WATER.getItemName()) || !inv.contains(cleanHerb.getItemName());
            }
        }.sleep();


        return (int) Statics.randomNormalDist(5000, 2000);
    }


}
