package Nodes.BankingNodes.Withdraw;

import Nodes.CreationNodes.*;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.MethodProvider;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractWithdraw extends MethodProvider implements ExecutableNode {

    private List<Edge> bothItemsWithdrawn = Arrays.asList(
            new Edge(AFKCreation.class, 100),
            new Edge(HoverBankerCreation.class, 50),
            new Edge(PrematureStopCreation.class, 10)
    );

    private List<Edge> havePrimary = Collections.singletonList(
            new Edge(WithdrawSecondary.class, 1)
    );

    private List<Edge> haveSecondary = Collections.singletonList(
            new Edge(WithdrawPrimary.class, 1)
    );

    CombinationRecipes recipe;

    AbstractWithdraw(Bot bot) {
        exchangeContext(bot);
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(bank.open() && bank.enableMode(Bank.BankMode.WITHDRAW_ITEM)){
            if(withdrawItem())
                return randomNormalDist(500, 100);
        }
        return 0;
    }

    abstract boolean withdrawItem();

    @Override
    public List<Edge> getAdjacentNodes() {
        if(inventory.contains(recipe.getPrimary()) && inventory.contains(recipe.getSecondary())){
            return bothItemsWithdrawn;
        } else if(inventory.contains(recipe.getPrimary())){
            return havePrimary;
        } else if(inventory.contains(recipe.getSecondary())){
            return haveSecondary;
        } else {
            logger.error("Expected inventory to contain either the primary or secondary item, rerunning the primary withdraw node");
            return havePrimary;
        }
    }

    //not used for this node
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
        log(this.getClass().getSimpleName());
    }
}
