package Nodes.BankingNodes;

import Nodes.CreationNodes.AFKCreation;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.CreationNodes.PrematureStopCreation;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.script.Script;

import java.util.Arrays;
import java.util.List;

public class OptionalInvFixNode implements ExecutableNode{
    private CombinationRecipes recipe;
    private Script script;

    private List<Edge> adjNodes = Arrays.asList(
            new Edge(AFKCreation.class, 75),
            new Edge(HoverBankerCreation.class, 20),
            new Edge(PrematureStopCreation.class, 5));

    public OptionalInvFixNode(Script script) {
        this.recipe = Margins.getInstance(script).getCurrentRecipe();
        this.script = script;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        this.recipe = Margins.getInstance(script).getCurrentRecipe();
        if(Statics.logNodes){
            logNode();
        }
        Inventory inv = script.getInventory();
        return inv.getAmount(recipe.getPrimaryItemName()) != 14
                || inv.getAmount(recipe.getSecondaryItemName()) != 14;
    }

    @Override
    public int executeNode() throws InterruptedException {
        //logNode();
        Inventory inv = script.getInventory();
        Bank bank = script.getBank();
        if(bank.isOpen()){
            if(inv.getAmount(recipe.getPrimaryItemName()) != 14
                    && inv.getAmount(recipe.getSecondaryItemName()) != 14){
                if(bank.depositAll()){
                    if(script.getBank().withdraw(recipe.getPrimaryItemName(), 14)){
                        Statics.shortRandomNormalDelay();
                        script.getBank().withdraw(recipe.getSecondaryItemName(), 14);
                    }
                }
            } else if(inv.getAmount(recipe.getPrimaryItemName()) != 14){
                if(script.getBank().withdraw(recipe.getPrimaryItemName(), 14)){

                }
            } else {
                script.getBank().withdraw(recipe.getSecondaryItemName(), 14);
            }
        }
        return (int) Statics.randomNormalDist(600, 200);
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return adjNodes;
    }

    @Override
    public boolean isJumping() {
        return false;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return null;
    }

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }
}
