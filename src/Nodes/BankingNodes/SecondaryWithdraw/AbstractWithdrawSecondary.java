package Nodes.BankingNodes.SecondaryWithdraw;

import Nodes.BankingNodes.DepositNode;
import Nodes.BankingNodes.OptionalInvFixNode;
import Nodes.BankingNodes.PrimaryWithdraw.Withdraw10Primary;
import Nodes.BankingNodes.PrimaryWithdraw.Withdraw14Primary;
import Nodes.BankingNodes.PrimaryWithdraw.WithdrawXPrimary;
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

public abstract class AbstractWithdrawSecondary implements ExecutableNode{
    Script script;
    CombinationRecipes recipe;
    boolean isJumping = false;

    private List<Edge> prePrimaryNodes = Arrays.asList(
            new Edge(Withdraw10Primary.class, 1),
            new Edge(Withdraw14Primary.class, 100),
            new Edge(WithdrawXPrimary.class, 1));

    private List<Edge> postPrimaryNodes = Arrays.asList(
            new Edge(OptionalInvFixNode.class, 150),
            new Edge(AFKCreation.class, 100),
            new Edge(HoverBankerCreation.class, 50),
            new Edge(PrematureStopCreation.class, 10));

    public AbstractWithdrawSecondary(Script script) {
        this.recipe = Margins.getInstance(script).getCurrentRecipe();
        this.script = script;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        this.recipe = Margins.getInstance(script).getCurrentRecipe();
        return !script.getInventory().contains(recipe.getSecondaryItemName());
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(Statics.logNodes){
            logNode();
        }
        Bank bank = script.getBank();
        if(bank.open()){
            if(bank.enableMode(Bank.BankMode.WITHDRAW_ITEM)){
                if(withdrawSecondary())
                    return (int) Statics.randomNormalDist(500, 100);
            }
        }


        return 0;
    }

    abstract boolean withdrawSecondary() throws InterruptedException;

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        boolean hasPrimary = invContainsPrimaryComponent();
        return hasPrimary ? postPrimaryNodes : prePrimaryNodes;
    }

    private boolean invContainsPrimaryComponent(){
        Inventory inv = script.getInventory();
        return inv.contains(CombinationRecipes.AVANTOE.getPrimaryItemName())
                || inv.contains(CombinationRecipes.TOADFLAX.getPrimaryItemName())
                || inv.contains(CombinationRecipes.RANARR.getPrimaryItemName())
                || inv.contains(CombinationRecipes.IRIT.getPrimaryItemName())
                || inv.contains(CombinationRecipes.KWUARM.getPrimaryItemName());
    }

    @Override
    public boolean isJumping() {
        if(isJumping){
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
