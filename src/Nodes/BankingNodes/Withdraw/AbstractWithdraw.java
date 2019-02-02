package Nodes.BankingNodes.Withdraw;

import Nodes.BankingNodes.DepositNode;
import Nodes.CreationNodes.AFKCreation;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.CreationNodes.PrematureStopCreation;
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
    boolean isJumping = false;

    AbstractWithdraw(Bot bot) {
        exchangeContext(bot);
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(recipe == null){
            log("recipe is null!");
            bot.getScriptExecutor().stop(false);
        }
        if(!invOnlyHasRecipeItems()) {
            isJumping = true;
            return 0;
        }
        if(bank.open() && bank.enableMode(Bank.BankMode.WITHDRAW_ITEM)){
            withdrawItem();
        }
        return 0;
    }

    abstract void withdrawItem();

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

    //sanity check for non recipe items are not in the inventory. Cannot proceed, or script will be stuck.
    private boolean invOnlyHasRecipeItems(){
        return inventory.onlyContains(item -> recipe.getPrimary().match(item) || recipe.getSecondary().match(item));
    }

    //not used for this node
    @Override
    public boolean isJumping() {
        if(isJumping) {
            isJumping = false;
            return true;
        }
        return false;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return DepositNode.class;
    }

    @Override
    public void logNode() {
        log(this.getClass().getSimpleName());
    }
}
