package Nodes.BankingNodes;

import Nodes.CreationNodes.AFKCreation;
import Nodes.CreationNodes.HoverBankerCreation;
import Nodes.CreationNodes.PrematureStopCreation;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import Util.Margins;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;
import java.util.List;

public class OptionalInvFixNode extends MethodProvider implements ExecutableNode{
    private CombinationRecipes recipe;
    private boolean isJumping;
    private List<Edge> adjNodes = Arrays.asList(
            new Edge(AFKCreation.class, 75),
            new Edge(HoverBankerCreation.class, 20),
            new Edge(PrematureStopCreation.class, 5));

    public OptionalInvFixNode(Bot bot) {
        this.recipe = Margins.getInstance(bot).getCurrentRecipe();
        exchangeContext(bot);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        this.recipe = Margins.getInstance(bot).getCurrentRecipe();
        return inventory.getAmount(recipe.getPrimary()) != 14
                || inventory.getAmount(recipe.getSecondary()) != 14;
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(fixPrimaryCount() && fixSecondaryCount())
            return randomNormalDist(600, 200);
        isJumping = true;
        return 1000;
    }

    private boolean fixPrimaryCount() {
        long primaryCount = inventory.getAmount(recipe.getPrimary());
        long secondaryCount = inventory.getAmount(recipe.getSecondary());
        if(primaryCount != 14) {
            if(secondaryCount == 14) {
                return bank.withdraw(recipe.getPrimary(), Bank.WITHDRAW_ALL);
            } else {
                return bank.depositAll()
                        && bank.withdraw(recipe.getPrimary(), 14)
                        && bank.withdraw(recipe.getSecondary(), 14);
            }
        }
        return true;
    }

    private boolean fixSecondaryCount() {
        long primaryCount = inventory.getAmount(recipe.getPrimary());
        long secondaryCount = inventory.getAmount(recipe.getSecondary());
        if(secondaryCount != 14) {
            if(primaryCount == 14) {
                return bank.withdraw(recipe.getSecondary(), Bank.WITHDRAW_ALL);
            } else {
                return bank.depositAll()
                        && bank.withdraw(recipe.getPrimary(), 14)
                        && bank.withdraw(recipe.getSecondary(), 14);
            }
        }
        return true;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return adjNodes;
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

    @Override
    public void logNode() {
        log(this.getClass().getSimpleName());
    }
}
