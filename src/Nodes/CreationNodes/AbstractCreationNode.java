package Nodes.CreationNodes;

import Nodes.ExecutableNode;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.api.Widgets;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

import static ScriptClasses.Statics.*;

abstract class AbstractCreationNode implements ExecutableNode {

    //keywords common to all herbs or vial or water
    static final String USE = "Use";
    static final String VIAL = "Vial";
    static final String WEED = "weed";
    static final String TOADFLAX = "flax";

    static final Rectangle makeBounds = new Rectangle(212, 395, 95, 65);

    Script hostScriptRefence;

    AbstractCreationNode(Script hostScriptRefence){
        this.hostScriptRefence = hostScriptRefence;
    }

    @Override
    public int executeNodeAction() throws InterruptedException {
        logNode();
        if(combineComponents()){
            if(interactMakePotsWidget()){
                return waitForPotions();
            }
        }
        else{
            MethodProvider.sleep(3000);
        }
        return 0;
    }

    private boolean interactMakePotsWidget(){
        Widgets widget = hostScriptRefence.getWidgets();
        Mouse mouse = hostScriptRefence.getMouse();
        //hover over make pots widget
        mouse.move(new RectangleDestination(hostScriptRefence.getBot(), makeBounds));
        //wait until make all appears
        new ConditionalSleep(5000){
            @Override
            public boolean condition() throws InterruptedException {
                return widget.isVisible(MAKE_UNF_POTION_PARENT_ID, MAKE_UNF_POTION_CHILD_ID);
            }
        }.sleep();

        if(widget.isVisible(MAKE_UNF_POTION_PARENT_ID, MAKE_UNF_POTION_CHILD_ID)){
            return widget.interact(MAKE_UNF_POTION_PARENT_ID, MAKE_UNF_POTION_CHILD_ID, "Make");
        }
        return false;
    }

    private boolean combineComponents() throws InterruptedException {
        Inventory inv = hostScriptRefence.getInventory();

        if(inv.contains(CLEAN_HERB) && inv.contains(VIAL_OF_WATER)){
            Item[] items = inv.getItems();
            int slot1 = (int) Statics.randomNormalDist(14,2);
            int slot2;
            if(items[slot1].getId() == VIAL_OF_WATER){
                slot2 = searchForOtherItemInvSlot(CLEAN_HERB, slot1, items);
            }
            else if(items[slot1].getId() == CLEAN_HERB){
                slot2 = searchForOtherItemInvSlot(VIAL_OF_WATER, slot1, items);
            }
            else{
                hostScriptRefence.log("detected foreign item");
                return false;
            }

            if(verifySlots(slot1, slot2, items)){
                if(inv.interact(slot1, USE)){
                    MethodProvider.sleep(Statics.randomNormalDist(300,100));
                    if(inv.isItemSelected()){
                        return inv.interact(slot2, USE);
                    }
                }
            }

            //failsafe, if the above doesnt work
            if(inv.deselectItem()){
                if(inv.interact("Use", VIAL_OF_WATER)){
                    return inv.interact("Use", CLEAN_HERB);
                }
            }
        }
        return false;
    }

    private boolean verifySlots(int slot1, int slot2, Item[] items){
        if(slot1 >= 0 && slot1 <= 28 && slot2 >= 0 && slot2 <= 28){
            int slot1ItemID = items[slot1].getId();
            int slot2ItemID = items[slot2].getId();
            return (slot1ItemID == VIAL_OF_WATER && slot2ItemID == CLEAN_HERB) || (slot1ItemID == CLEAN_HERB && slot2ItemID == VIAL_OF_WATER);
        }
        return false;
    }

    private int searchForOtherItemInvSlot(int idToSearch, int itemSlotInteract, Item[] items){
        int deferSelectionCount = 0;
        for(int i = itemSlotInteract; i < items.length; i++){
            if(items[i] != null && items[i].getId() == idToSearch){
                boolean selectThisSlot = ThreadLocalRandom.current().nextBoolean();
                if(selectThisSlot || deferSelectionCount > 3){
                    return i;
                }
                deferSelectionCount++;
            }
        }

        for(int i = itemSlotInteract; i >= 0; i--){
            if(items[i] != null && items[i].getId() == idToSearch){
                boolean selectThisSlot = ThreadLocalRandom.current().nextBoolean();
                if(selectThisSlot || deferSelectionCount > 3){
                    return i;
                }
                deferSelectionCount++;
            }
        }
        return -1;
    }

    //randomly choose a close inv slot to interact with


    void logNode(){
        hostScriptRefence.log(this.getClass().getSimpleName());
    }

    //define what to do when waiting for potions to finish, returns the sleeptime for onloop.
    abstract int waitForPotions();
}
