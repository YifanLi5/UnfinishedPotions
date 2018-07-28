package Nodes.BankingNodes.PrimaryWithdraw;

import Nodes.BankingNodes.DepositNode;
import Nodes.BankingNodes.OptionalInvFixNode;
import Nodes.BankingNodes.SecondaryWithdraw.Withdraw10Secondary;
import Nodes.BankingNodes.SecondaryWithdraw.Withdraw14Secondary;
import Nodes.BankingNodes.SecondaryWithdraw.WithdrawXSecondary;
import Nodes.CreationNodes.AFKCreation;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.CreationNodes.PrematureStopCreation;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.Script;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractWithdrawPrimary implements ExecutableNode{
    Script script;
    CombinationRecipes recipe;
    boolean isJumping = false;

    //edges for if inventory is initially empty (item2 has not been withdrawn)
    private List<Edge> preSecondaryEdges = Arrays.asList(
            new Edge(Withdraw10Secondary.class, 1),
            new Edge(Withdraw14Secondary.class, 100),
            new Edge(WithdrawXSecondary.class, 1));

    //edges for if inventory already has item2.
    private List<Edge> postSecondaryEdges = Arrays.asList(
            new Edge(OptionalInvFixNode.class, 150),
            new Edge(AFKCreation.class, 100),
            new Edge(HoverBankerCreation.class, 50),
            new Edge(PrematureStopCreation.class, 10));

    AbstractWithdrawPrimary(Script script){
        this.script = script;
        this.recipe = Margins.getInstance(script).getCurrentRecipe();
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        this.recipe = Margins.getInstance(script).getCurrentRecipe();
        return !script.getInventory().contains(recipe.getPrimaryItemName())
                || script.getInventory().isEmpty();
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(Statics.logNodes){
            logNode();
        }
        Bank bank = script.getBank();
        if(bank.open()){
            if(bank.enableMode(Bank.BankMode.WITHDRAW_ITEM)){
                if(withdrawPrimary())
                    return (int) Statics.randomNormalDist(500, 100);
            }
        }

        return 0;
    }

    abstract boolean withdrawPrimary() throws InterruptedException;

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        boolean hasSecondary = script.getInventory().contains(recipe.getSecondaryItemName());
        return hasSecondary ? postSecondaryEdges : preSecondaryEdges;
    }

    @Override
    public boolean isJumping() {
        if(isJumping){
            script.log("going back to deposit node");
            isJumping = false;
            return true;
        }
        return false;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return DepositNode.class;
    }

    boolean containsForeignItem(){
        return !script.getInventory().isEmptyExcept(recipe.getPrimaryItemID(), recipe.getSecondaryItemID());
    }

}
