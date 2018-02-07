package Nodes;

import ScriptClasses.Statics;
import org.osbot.Con;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Widgets;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import static ScriptClasses.Statics.CLEAN_RANARR;
import static ScriptClasses.Statics.VIAL_OF_WATER;

public class CreateUNFPotionsNode implements ExecutableNode{

    private static final int MAKE_UNF_POTION_PARENT_ID = 270;
    private static final int MAKE_UNF_POTION_CHILD_ID = 14;

    private Script hostScriptRefence;
    private static ExecutableNode singleton;

    private CreateUNFPotionsNode(Script hostScriptRefence){
        this.hostScriptRefence = hostScriptRefence;
    }

    public static ExecutableNode getInstance(Script hostScriptRefence){
        if(singleton == null){
            singleton = new CreateUNFPotionsNode(hostScriptRefence);
        }
        return singleton;
    }

    @Override
    public int executeNodeAction() throws InterruptedException {
        Inventory inv = hostScriptRefence.getInventory();
        Widgets widget = hostScriptRefence.getWidgets();
        if(inv.contains(CLEAN_RANARR) && inv.contains(VIAL_OF_WATER)){
            if(inv.interact("Use", VIAL_OF_WATER)){
                MethodProvider.sleep(Statics.randomNormalDist(1500, 500));
                if(inv.interact("Use", CLEAN_RANARR)){
                    new ConditionalSleep(5000){
                        @Override
                        public boolean condition() throws InterruptedException {
                            return widget.isVisible(MAKE_UNF_POTION_PARENT_ID, MAKE_UNF_POTION_CHILD_ID);
                        }
                    }.sleep();
                    if(widget.isVisible(MAKE_UNF_POTION_PARENT_ID, MAKE_UNF_POTION_CHILD_ID)){
                        widget.interact(MAKE_UNF_POTION_PARENT_ID, MAKE_UNF_POTION_CHILD_ID, "Make");
                        new ConditionalSleep(10000) {
                            @Override
                            public boolean condition() throws InterruptedException {
                                return !inv.contains(VIAL_OF_WATER) || !inv.contains(CLEAN_RANARR);
                            }
                        }.sleep();
                    }
                }
            }
        }

        return (int) Statics.randomNormalDist(5000, 2000);
    }
}
