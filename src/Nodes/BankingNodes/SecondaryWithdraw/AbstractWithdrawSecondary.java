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
import Util.ItemData;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractWithdrawSecondary extends MethodProvider implements ExecutableNode{
    CombinationRecipes recipe;
    boolean isJumping = false;

    private List<Edge> prePrimaryNodes = Arrays.asList(
            new Edge(Withdraw10Primary.class, 1),
            new Edge(Withdraw14Primary.class, 98),
            new Edge(WithdrawXPrimary.class, 1));

    private List<Edge> postPrimaryNodes = Arrays.asList(
            new Edge(OptionalInvFixNode.class, 150),
            new Edge(AFKCreation.class, 100),
            new Edge(HoverBankerCreation.class, 50),
            new Edge(PrematureStopCreation.class, 10));

    public AbstractWithdrawSecondary(Bot bot) {
        exchangeContext(bot);
        this.recipe = Margins.getInstance(bot).getCurrentRecipe();
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        this.recipe = Margins.getInstance(bot).getCurrentRecipe();
        return !inventory.contains(recipe.getSecondary());
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(Statics.logNodes){
            logNode();
        }
        if(bank.open()){
            if(bank.enableMode(Bank.BankMode.WITHDRAW_ITEM)){
                if(withdrawSecondary())
                    return randomNormalDist(500, 100);
            }
        }
        return 0;
    }

    abstract boolean withdrawSecondary() throws InterruptedException;

    @Override
    public void logNode() {
        log(this.getClass().getSimpleName());
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        boolean hasPrimary = invContainsPrimaryComponent();
        return hasPrimary ? postPrimaryNodes : prePrimaryNodes;
    }

    private boolean invContainsPrimaryComponent(){
        //obtain array of all clean herbs (primary component) from all recipes
        ItemData[] primaryComponents = Arrays.stream(CombinationRecipes.values()).map(CombinationRecipes::getPrimary).toArray(ItemData[]::new);
        return inventory.contains(primaryComponents);
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
        return !inventory.isEmptyExcept(recipe.getPrimary().getName(), recipe.getSecondary().getName());
    }
}
