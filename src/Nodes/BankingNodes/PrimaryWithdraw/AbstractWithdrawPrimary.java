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
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractWithdrawPrimary extends MethodProvider implements ExecutableNode {
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

    AbstractWithdrawPrimary(Bot bot){
        exchangeContext(bot);
        this.recipe = Margins.getInstance(bot).getCurrentRecipe();
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        return !inventory.contains(recipe.getPrimary())
                || inventory.isEmpty();
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(Statics.logNodes){
            logNode();
        }
        if(bank.open()){
            if(bank.enableMode(Bank.BankMode.WITHDRAW_ITEM)){
                if(withdrawPrimary())
                    return randomNormalDist(500, 100);
            }
        }

        return 0;
    }

    abstract boolean withdrawPrimary() throws InterruptedException;

    @Override
    public void logNode() {
        log(this.getClass().getSimpleName());
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        boolean hasSecondary = inventory.contains(recipe.getSecondary());
        return hasSecondary ? postSecondaryEdges : preSecondaryEdges;
    }

    @Override
    public boolean isJumping() {
        if(isJumping){
            log("going back to deposit node");
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
        return !inventory.isEmptyExcept(recipe.getPrimary().getName(), recipe.getSecondary().getName());
    }

}
