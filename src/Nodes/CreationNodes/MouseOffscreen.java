package Nodes.CreationNodes;

import Nodes.ExecutableNode;
import ScriptClasses.HerbEnum;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;

public class MouseOffscreen extends AbstractCreationNode{

    public static final int NODE_EXECUTION_WEIGHT = 80;
    private static ExecutableNode singleton;

    private MouseOffscreen(Script hostScriptRefence, HerbEnum herbEnum) {
        super(hostScriptRefence, herbEnum);
    }

    public static ExecutableNode getInstance(Script hostScriptRefence, HerbEnum herbEnum){
        if(singleton == null) singleton = new MouseOffscreen(hostScriptRefence, herbEnum);
        return singleton;
    }

    @Override
    int waitForPotions() {
        Mouse mouse = hostScriptReference.getMouse();
        Inventory inv = hostScriptReference.getInventory();
        //wait until all potions are finished
        mouse.moveOutsideScreen();
        new ConditionalSleep(10000) {
            @Override
            public boolean condition() throws InterruptedException {
                return !inv.contains(VIAL_OF_WATER) || !inv.contains(cleanHerb.getItemID());
            }
        }.sleep();


        return (int) Statics.randomNormalDist(5000, 2000);
    }

    @Override
    public int getDefaultEdgeWeight() {
        return NODE_EXECUTION_WEIGHT;
    }

}
