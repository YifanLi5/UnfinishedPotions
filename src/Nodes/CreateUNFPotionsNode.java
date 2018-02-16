package Nodes;

import ScriptClasses.Statics;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Mouse;
import org.osbot.rs07.api.Widgets;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

import static ScriptClasses.Statics.*;

public class CreateUNFPotionsNode implements ExecutableNode{

    //keywords common to all herbs or vial or water
    private static final String USE = "Use";
    private static final String VIAL = "Vial";
    private static final String WEED = "weed";
    private static final String TOADFLAX = "flax";

    private static final Rectangle makeBounds = new Rectangle(212, 395, 95, 65);

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
        if(useHerbOnVial()){
            if(interactMakePotsWidget()){
                return (int) Statics.randomNormalDist(5000, 2000);
            }
        }
        else{
            MethodProvider.sleep(3000);
        }
        return 0;
    }

    //this implementation randomly selects an item around the middle inventory slots to better imitate a human player
    private boolean useHerbOnVial() throws InterruptedException {
        Inventory inv = hostScriptRefence.getInventory();
        if(inv.contains(CLEAN_HERB) && inv.contains(VIAL_OF_WATER)) {
            //inv slots 13 <= x <= 16 contain a herb or vial if the script withdrew 14,14.
            int itemSlotInteract = ThreadLocalRandom.current().nextInt(13, 17);
            Item[] items = inv.getItems();

            if (inv.interact(itemSlotInteract, USE)) {
                new ConditionalSleep(1000) {
                    @Override
                    public boolean condition() throws InterruptedException {
                        return inv.isItemSelected();
                    }
                }.sleep();

                if(inv.isItemSelected()){
                    if (inv.getSelectedItemName().contains(WEED) || inv.getSelectedItemName().contains(TOADFLAX)) { //temp hard code for now
                        int otherItemSlot = getOtherItemInvSlot(true, itemSlotInteract, items);
                        if(otherItemSlot != -1){ //-1 means failure to find slot
                            return inv.interact(otherItemSlot, USE);
                        }

                    } else if (inv.getSelectedItemName().contains(VIAL)) {
                        int otherItemSlot = getOtherItemInvSlot(false, itemSlotInteract, items);
                        if(otherItemSlot != -1){
                            return inv.interact(otherItemSlot, USE);
                        }
                    }
                }
            }
        }
        return false;
    }

    //randomly choose a close inv slot to interact with
    private int getOtherItemInvSlot(boolean herbSelected, int itemSlotInteract, Item[] items){
        int i = itemSlotInteract; //for searching forward
        int j = itemSlotInteract; // " " backwards
        int deferSelectionCount = 0;
        if(herbSelected){ //herb is selcted, search for close vial of water
            while(i < items.length && j >= 0){
                if(items[i].getId() == VIAL_OF_WATER){
                    boolean selectThisSlot = ThreadLocalRandom.current().nextBoolean();
                    if(selectThisSlot || deferSelectionCount > 3){
                        return i;
                    }
                    deferSelectionCount++;

                }
                if(items[j].getId() == VIAL_OF_WATER){
                    boolean selectThisSlot = ThreadLocalRandom.current().nextBoolean();
                    if(selectThisSlot || deferSelectionCount > 3){
                        return j;
                    }
                    deferSelectionCount++;
                }

                i++;
                j--;
            }

        }
        else{ //vial of water is selected, search for herb
            while(i < items.length && j >= 0){
                if(items[i].getId() == CLEAN_HERB){
                    boolean selectThisSlot = ThreadLocalRandom.current().nextBoolean();
                    if(selectThisSlot || deferSelectionCount > 3){
                        return i;
                    }
                    deferSelectionCount++;
                }
                if(items[j].getId() == CLEAN_HERB){
                    boolean selectThisSlot = ThreadLocalRandom.current().nextBoolean();
                    if(selectThisSlot || deferSelectionCount > 3){
                        return j;
                    }
                    deferSelectionCount++;
                }

                i++;
                j--;
            }

        }
        return -1;
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
            if(widget.interact(MAKE_UNF_POTION_PARENT_ID, MAKE_UNF_POTION_CHILD_ID, "Make")){
                return waitUntilPotionsMade();
            }
        }
        return false;

    }

    private boolean waitUntilPotionsMade(){
        Mouse mouse = hostScriptRefence.getMouse();
        Inventory inv = hostScriptRefence.getInventory();
        boolean moveMouseOutsideScreen = ThreadLocalRandom.current().nextBoolean();
        if(moveMouseOutsideScreen){
            mouse.moveOutsideScreen();
        }


        final boolean[] successful = {false};
        //wait until all potions are finished
        new ConditionalSleep(10000) {
            @Override
            public boolean condition() throws InterruptedException {
                successful[0] = !inv.contains(VIAL_OF_WATER) || !inv.contains(CLEAN_HERB);
                return successful[0];
            }
        }.sleep();
        return successful[0];

    }
}
