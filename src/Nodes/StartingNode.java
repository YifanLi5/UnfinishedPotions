package Nodes;

import Nodes.BankingNodes.DecideRestockNode;
import Nodes.BankingNodes.DepositNode;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.Margins;
import Util.UnfPotionRecipes;
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
            UnfPotionRecipes todo = whichRecipeToDo();
            if(script.getBank().close()){
                if(todo != null){ //means that there are already herbs to do
                    margins.setCurrentRecipe(todo);
                } else {
                    margins.setCurrentRecipe(margins.findAllConversionMargins());
                }
            } else {
                throw new UnsupportedOperationException("bank didn't close???");
            }
        }

        if(invContainsPrimaryComponent() && inv.contains("Vial of water")){
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
        return inv.contains(UnfPotionRecipes.AVANTOE.getPrimaryItemName())
                || inv.contains(UnfPotionRecipes.TOADFLAX.getPrimaryItemName())
                || inv.contains(UnfPotionRecipes.RANARR.getPrimaryItemName())
                || inv.contains(UnfPotionRecipes.IRIT.getPrimaryItemName())
                || inv.contains(UnfPotionRecipes.KWUARM.getPrimaryItemName());
    }

    private UnfPotionRecipes whichRecipeToDo(){
        Bank bank = script.getBank();
        int highestIngredientAmt = 14; //only if highest is over 14 do I care
        UnfPotionRecipes todo = null;
        for(UnfPotionRecipes recipe: UnfPotionRecipes.values()){
            if(recipe == UnfPotionRecipes.CLAY)
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
