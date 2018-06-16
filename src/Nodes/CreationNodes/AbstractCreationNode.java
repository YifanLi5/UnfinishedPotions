package Nodes.CreationNodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.ConversionMargins;
import Util.Statics;
import Util.SupplierWithCE;
import Util.UnfPotionRecipes;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.awt.event.KeyEvent.VK_SPACE;

public abstract class AbstractCreationNode implements ExecutableNode {

    private static final String USE = "Use";
    UnfPotionRecipes recipe;
    int secondaryCount;
    int primaryCount;

    Script script;

    private List<Edge> adjNodes = Arrays.asList(new Edge(DepositNode.class, 1));

    AbstractCreationNode(Script script){
        this.script = script;
        this.recipe = ConversionMargins.getInstance(script).getCurrentRecipe();
    }

    @Override
    public boolean canExecute() {
        this.recipe = ConversionMargins.getInstance(script).getCurrentRecipe();
        return new ConditionalSleep(1000){
            @Override
            public boolean condition() throws InterruptedException {
                return script.getInventory().contains(recipe.getPrimaryItemName()) && script.getInventory().contains(recipe.getSecondaryItemName());
            }
        }.sleep();
    }

    @Override
    public int executeNode() throws InterruptedException {
        //logNode();
        if(script.getWidgets().closeOpenInterface()){
            if(executeStep(this::combineComponents)){
                if(this instanceof PrematureStopCreation){
                    secondaryCount = (int) script.getInventory().getAmount(recipe.getSecondaryItemName());
                    primaryCount = (int) script.getInventory().getAmount(recipe.getPrimaryItemName());
                }
                if(executeStep(this::interactMakePotsWidget)){
                    return waitForPotions();
                }
            }
        }
        return 0;
    }

    private boolean executeStep(SupplierWithCE<Boolean, InterruptedException> f) throws InterruptedException{
        boolean result = f.get();
        int attempts = 0;
        while(!result && attempts < 5){
            result = f.get();
            attempts++;
        }
        return result;
    }

    private boolean interactMakePotsWidget(){
        final RS2Widget[] make = new RS2Widget[1];
        boolean success = new ConditionalSleep(2000){
            @Override
            public boolean condition() throws InterruptedException {
                List<RS2Widget> widgets = new ArrayList<>(script.getWidgets().containingActions(270, "Make"));
                if(widgets.size() > 0 && widgets.get(0) != null){
                    make[0] = widgets.get(0);
                    return true;
                }
                return false;

            }
        }.sleep();

        if(success){
            boolean useSpace = ThreadLocalRandom.current().nextBoolean();
            if(useSpace){
                script.getKeyboard().pressKey(VK_SPACE);
                return true;
            }
            return make[0].interact("Make");
        }
        return false;
    }

    private boolean combineComponents() throws InterruptedException {
        Inventory inv = script.getInventory();

        if(inv.contains(recipe.getPrimaryItemName()) && inv.contains(recipe.getSecondaryItemName())){
            Item[] items = inv.getItems();
            int slot1 = (int) Statics.randomNormalDist(14,2);
            int slot2;
            if(items[slot1].getName().equals(recipe.getPrimaryItemName())){
                slot2 = searchForOtherItemInvSlot(recipe.getSecondaryItemID(), slot1, items);
            }
            else if(items[slot1].getName().equals(recipe.getSecondaryItemName())){
                slot2 = searchForOtherItemInvSlot(recipe.getPrimaryItemID(), slot1, items);
            }
            else{
                script.log("detected foreign recipe");
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
                if(inv.interact("Use", recipe.getSecondaryItemName())){
                    return inv.interact("Use", recipe.getPrimaryItemID());
                }
            }
        }
        return false;
    }

    private boolean verifySlots(int slot1, int slot2, Item[] items){
        if(slot1 >= 0 && slot1 <= 28 && slot2 >= 0 && slot2 <= 28){
            int slot1ItemID = items[slot1].getId();
            int slot2ItemID = items[slot2].getId();
            return (slot1ItemID == recipe.getSecondaryItemID() && slot2ItemID == recipe.getPrimaryItemID())
                    || (slot1ItemID == recipe.getPrimaryItemID() && slot2ItemID == recipe.getSecondaryItemID());
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

    @Override
    public List<Edge> getAdjacentNodes() {
        return adjNodes;
    }

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }

    //define what to do when waiting for potions to finish, returns the sleeptime for onloop.
    abstract int waitForPotions() throws InterruptedException;

    @Override
    public boolean isJumping() {
        return false;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return null;
    }
}
