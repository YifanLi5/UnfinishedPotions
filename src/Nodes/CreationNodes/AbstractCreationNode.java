package Nodes.CreationNodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import Util.Margins;
import Util.Statics;
import Util.SupplierWithCE;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.awt.event.KeyEvent.VK_SPACE;

public abstract class AbstractCreationNode implements ExecutableNode {

    private static final String USE = "Use";
    CombinationRecipes recipe;
    int secondaryCount;
    int primaryCount;

    Script script;

    private List<Edge> adjNodes = Collections.singletonList(new Edge(DepositNode.class, 1));

    AbstractCreationNode(Script script){
        this.script = script;
        this.recipe = Margins.getInstance(script).getCurrentRecipe();
    }

    @Override
    public boolean canExecute() {
        this.recipe = Margins.getInstance(script).getCurrentRecipe();
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
                if(executeStep(this::interactCreateWidget)){
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

    private boolean interactCreateWidget(){
        final RS2Widget[] make = new RS2Widget[1];
        boolean success = new ConditionalSleep(2000){
            @Override
            public boolean condition() throws InterruptedException {
                List<RS2Widget> widgets = new ArrayList<>(script.getWidgets().containingActions(270, "Make", "String"));
                if(widgets.size() > 0 && widgets.get(0) != null){
                    make[0] = widgets.get(0);
                    return true;
                }
                return false;

            }
        }.sleep();

        if(success){
            boolean useHotKey = ThreadLocalRandom.current().nextBoolean();
            if(useHotKey){
                script.getKeyboard().pressKey(VK_SPACE);
                return true;
            }
            return make[0].interact("Make");
        }
        return false;
    }

    private boolean combineComponents() throws InterruptedException {
        Inventory inv = script.getInventory();
        int[] slots = bfsItemCombinationSlots(inv.getItems(),recipe.getPrimaryItemID(), recipe.getSecondaryItemID());
        //ensure algorithm did not fail. If you any other items in inventory or empty spaces, code may return {-1, -1}
        //if such happens, use a regular combination interaction
        if(slots[0] != -1 && slots[1] != -1){
            if(inv.interact(slots[0], USE)){
                MethodProvider.sleep(Statics.randomNormalDist(300,100));
                return inv.isItemSelected() && inv.interact(slots[1], USE);
            }
        } else {
            if(inv.interact(USE, recipe.getPrimaryItemID())){
                MethodProvider.sleep(Statics.randomNormalDist(300,100));
                return inv.isItemSelected() && inv.interact(USE, recipe.getSecondaryItemID());

            }
        }

        return false;
    }

    private int[] bfsItemCombinationSlots(Item[] invItems, int item1ID, int item2ID){
        int startIdx = ThreadLocalRandom.current().nextInt(10, 18);
        int otherIdx = -1;
        if(invItems[startIdx] != null){
            //find what item occupies invItems[startIdx] and bfs with the target being the corresponding item
            if(invItems[startIdx].getId() == item1ID){
                otherIdx = bfsTargetItemSlotHelper(invItems, item2ID, startIdx);

            } else if(invItems[startIdx].getId() == item2ID){
                otherIdx = bfsTargetItemSlotHelper(invItems, item1ID, startIdx);

            }
            //error check
            if(otherIdx != -1)
                return new int[]{startIdx, otherIdx};
        }
        //Some error occurred. invItems[startIdx] may be null or is an item that is not item1 or item2. Recommend doing normal inventory combine.
        return new int[]{-1, -1};
    }

    private int bfsTargetItemSlotHelper(Item[] invItems, int targetItemID, int startingInvIdx){
        if(startingInvIdx < 0 || startingInvIdx > 27){
            throw new UnsupportedOperationException("input needs to in range [0-27].");
        }
        Queue<Integer> bfsQ = new LinkedList<>();
        boolean[] visitedSlots = new boolean[28];
        bfsQ.add(startingInvIdx);
        visitedSlots[startingInvIdx] = true;
        while(!bfsQ.isEmpty()){
            int current = bfsQ.poll();
            if(invItems[current].getId() == targetItemID){
                return current;
            }
            List<Integer> successors = getSuccessors(current);
            successors.forEach(slot -> {
                if(!visitedSlots[slot]){
                    visitedSlots[slot] = true;
                    bfsQ.add(slot);
                }
            });
        }
        return -1;
    }

    private List<Integer> getSuccessors(int invSlot) {
        List<Integer> successors = new ArrayList<>();
        boolean canUp = false, canRight = false, canDown = false, canLeft = false;
        if(!(invSlot <= 3)){ //up, cannot search up if invSlot is top 4 slots
            successors.add(invSlot - 4);
            canUp = true;
        }
        if((invSlot + 1) % 4 != 0){ //right, cannot search right if invSlot is rightmost column
            successors.add(invSlot + 1);
            canRight = true;
        }
        if(!(invSlot >= 24)){ //down, cannot search down if invSlot is bottom 4 slots
            successors.add(invSlot + 4);
            canDown = true;
        }
        if(invSlot % 4 != 0){ //left, cannot search left if invSlot is leftmost column
            successors.add(invSlot - 1);
            canLeft = true;
        }
        //can search in diagonal directions if can search in its composite directions
        if(canUp && canRight){
            successors.add(invSlot - 3);
        }
        if(canUp && canLeft){
            successors.add(invSlot - 5);
        }
        if(canDown && canRight){
            successors.add(invSlot + 5);
        }
        if(canDown && canLeft){
            successors.add(invSlot + 3);
        }
        Collections.shuffle(successors); //randomize search order at the same search depth.
        return successors;
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
