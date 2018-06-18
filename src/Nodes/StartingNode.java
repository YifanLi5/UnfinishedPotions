package Nodes;

import Nodes.BankingNodes.DecideRestockNode;
import Nodes.BankingNodes.DepositNode;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.ItemCombinationRecipes;
import Util.Margins;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.Script;

import java.util.List;

public class StartingNode implements ExecutableNode {
    private Script script;
    private Class<? extends ExecutableNode> jumpTarget;
    private Margins margins;

    public StartingNode(Script script) {
        this.script = script;
        margins = Margins.getInstance(script);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return true;
    }

    @Override
    public int executeNode() throws InterruptedException {
        Inventory inv = script.getInventory();
        if(script.getBank().open()){
            ItemCombinationRecipes todo = whichRecipeToDo();
            if(todo != null) //means that there are already herbs to do
                margins.setCurrentRecipe(todo);
            else if(script.getBank().close())
                    margins.setCurrentRecipe(margins.findAllConversionMargins());

        }
        if(invContainsPrimaryComponent() && inv.contains(margins.getCurrentRecipe().getSecondaryItemName())){
            jumpTarget = HoverBankerCreation.class;
        } else if(inv.isEmpty()){
            jumpTarget = DecideRestockNode.class;
        } else {
            jumpTarget = DepositNode.class;
        }
        return 0;
    }

    private boolean invContainsPrimaryComponent(){
        Inventory inv = script.getInventory();
        return inv.contains(ItemCombinationRecipes.AVANTOE.getPrimaryItemName())
                || inv.contains(ItemCombinationRecipes.TOADFLAX.getPrimaryItemName())
                || inv.contains(ItemCombinationRecipes.RANARR.getPrimaryItemName())
                || inv.contains(ItemCombinationRecipes.IRIT.getPrimaryItemName())
                || inv.contains(ItemCombinationRecipes.KWUARM.getPrimaryItemName());
    }

    private ItemCombinationRecipes whichRecipeToDo(){
        Bank bank = script.getBank();
        int highestIngredientAmt = 14; //only if highest is over 14 do I care
        ItemCombinationRecipes todo = null;
        for(ItemCombinationRecipes recipe: ItemCombinationRecipes.values()){
            if(recipe == ItemCombinationRecipes.CLAY)
                continue;
            int amt = (int) bank.getAmount(recipe.getPrimaryItemName());
            if(amt > highestIngredientAmt){
                highestIngredientAmt = amt;
                todo = recipe;
            }
        }
        return todo;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return null;
    }

    @Override
    public boolean isJumping() {
        return true;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return jumpTarget;
    }

    @Override
    public void logNode() {
        this.getClass().getSimpleName();
    }
}
