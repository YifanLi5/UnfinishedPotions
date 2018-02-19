package Nodes.CreationNodes;

import Nodes.ExecutableNode;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import static ScriptClasses.Statics.CLEAN_HERB;
import static ScriptClasses.Statics.VIAL_OF_WATER;

public class MouseOffscreen extends AbstractCreationNode{

    private static ExecutableNode singleton;

    MouseOffscreen(Script hostScriptRefence) {
        super(hostScriptRefence);
    }

    public static ExecutableNode getInstance(Script hostScriptRefence){
        if(singleton == null){
            singleton = new MouseOffscreen(hostScriptRefence);
        }
        return singleton;
    }

    @Override
    int waitForPotions() {
        Mouse mouse = hostScriptRefence.getMouse();
        Inventory inv = hostScriptRefence.getInventory();
        //wait until all potions are finished
        mouse.moveOutsideScreen();
        new ConditionalSleep(10000) {
            @Override
            public boolean condition() throws InterruptedException {
                return !inv.contains(VIAL_OF_WATER) || !inv.contains(CLEAN_HERB);
            }
        }.sleep();


        return (int) Statics.randomNormalDist(5000, 2000);
    }

}
