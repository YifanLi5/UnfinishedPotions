package Nodes.CreationNodes;

import Util.HerbEnum;
import ScriptClasses.MarkovNodeExecutor;
import Util.Statics;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.concurrent.ThreadLocalRandom;

import static Util.Statics.*;
import static java.awt.event.KeyEvent.VK_SPACE;

public abstract class AbstractCreationNode implements MarkovNodeExecutor.ExecutableNode, GrandExchange.GrandExchangeObserver {

    //keywords common to all herbs or vial or water
    private static final String USE = "Use";
    private boolean geUpdated;
    HerbEnum cleanHerb;

    Script script;

    AbstractCreationNode(Script script, HerbEnum cleanHerb){
        this.script = script;
        this.cleanHerb = cleanHerb;
    }

    @Override
    public boolean canExecute() {
        return script.getInventory().contains(cleanHerb.getItemName()) && script.getInventory().contains("Vial of water");
    }

    @Override
    public int executeNode() throws InterruptedException {
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
        final RS2Widget[] makeWidget = new RS2Widget[1];
        new ConditionalSleep(2000){
            @Override
            public boolean condition() throws InterruptedException {
                makeWidget[0] = script.getWidgets().containingActions(270, "Make").get(0);
                return makeWidget[0] != null && makeWidget[0].isVisible();
            }
        }.sleep();

        if(makeWidget[0] != null && makeWidget[0].isVisible()){
            boolean useSpace = ThreadLocalRandom.current().nextBoolean();
            if(useSpace){
                script.getKeyboard().pressKey(VK_SPACE);
                return true;
            }
            return makeWidget[0].interact("Make");
        }
        return false;
    }

    private boolean combineComponents() throws InterruptedException {
        Inventory inv = script.getInventory();

        if(inv.contains(cleanHerb.getItemID()) && inv.contains(VIAL_OF_WATER)){
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
                script.log("detected foreign item");
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
                    return inv.interact("Use", cleanHerb.getItemID());
                }
            }
        }
        return false;
    }

    private boolean verifySlots(int slot1, int slot2, Item[] items){
        if(slot1 >= 0 && slot1 <= 28 && slot2 >= 0 && slot2 <= 28){
            int slot1ItemID = items[slot1].getId();
            int slot2ItemID = items[slot2].getId();
            return (slot1ItemID == VIAL_OF_WATER && slot2ItemID == cleanHerb.getItemID()) || (slot1ItemID == cleanHerb.getItemID() && slot2ItemID == VIAL_OF_WATER);
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


    void logNode(){
        script.log(this.getClass().getSimpleName());
    }

    //define what to do when waiting for potions to finish, returns the sleeptime for onloop.
    abstract int waitForPotions();

    @Override
    public boolean doConditionalTraverse() {
        return geUpdated;
    }

    @Override
    public void onGEUpdate(GrandExchange.Box box) {
        geUpdated = true;
        //TODO: determine if buy or sell and doConditionTraverse to either buy or sell nodes
    }
}
