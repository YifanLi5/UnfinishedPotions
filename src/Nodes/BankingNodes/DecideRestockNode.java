package Nodes.BankingNodes;

import Nodes.BankingNodes.HerbWithdraw.Withdraw10Primary;
import Nodes.BankingNodes.HerbWithdraw.Withdraw14Primary;
import Nodes.BankingNodes.HerbWithdraw.WithdrawXPrimary;
import Nodes.BankingNodes.VialWithdraw.Withdraw10Secondary;
import Nodes.BankingNodes.VialWithdraw.Withdraw14Secondary;
import Nodes.BankingNodes.VialWithdraw.WithdrawXSecondary;
import Nodes.GENodes.GESpinLockSellNode;
import Nodes.GENodes.IntermittentBuy;
import Nodes.GENodes.IntermittentSell;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.ItemCombinationRecipes;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.script.Script;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DecideRestockNode implements ExecutableNode {
    private Script script;
    private ItemCombinationRecipes recipe;
    private boolean isJumping = false;
    private Class<? extends ExecutableNode> jumpTarget;
    private List<Edge> adjNodes;

    private int unfCountMinThreshold;

    public DecideRestockNode(Script script) {
        this.script = script;
        this.recipe = Margins.getInstance(script).getCurrentRecipe();

        adjNodes = Arrays.asList(
                new Edge(Withdraw10Primary.class, 5),
                new Edge(Withdraw14Primary.class, 90),
                new Edge(WithdrawXPrimary.class, 10),
                new Edge(Withdraw10Secondary.class, 5),
                new Edge(Withdraw14Secondary.class, 90),
                new Edge(WithdrawXSecondary.class, 10
                ));
        unfCountMinThreshold = ThreadLocalRandom.current().nextInt(300, 600);
        script.log(IntermittentSell.class.getSimpleName() + " runs when there are " + unfCountMinThreshold + " unf potions");
    }

    @Override
    public boolean canExecute() {
        this.recipe = Margins.getInstance(script).getCurrentRecipe();
        return script.getBank().isOpen();
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(Statics.logNodes){
            logNode();
        }
        Bank bank = script.getBank();
        int primaryRemaining = (int) bank.getAmount(recipe.getPrimaryItemName());
        if(isDoingUNFPotions()){
            if(primaryRemaining < 14){
                isJumping = true;
                jumpTarget = GESpinLockSellNode.class;
            } else if(isJumpingToIntermittentSell()){
                isJumping = true;
                jumpTarget = IntermittentSell.class;
            } else if(isJumpingToIntermittentBuy()){
                isJumping = true;
                jumpTarget = IntermittentBuy.class;
            }
        }

        int secondaryRemaining = (int) bank.getAmount(recipe.getSecondaryItemName());
        if(secondaryRemaining < 14 ||(!isDoingUNFPotions() &&  primaryRemaining < 14)){
            script.stop(false);
            script.log("ran out of secondary or is not doing unf pots");
            //TODO: buy vials
        }
        return 0;
    }

    private boolean isDoingUNFPotions(){
        return !(recipe == ItemCombinationRecipes.CLAY || recipe == ItemCombinationRecipes.AIR_BATTLESTAFF);
    }

    private boolean isJumpingToIntermittentSell(){
        int unfCount = (int) script.getBank().getAmount(recipe.getFinishedItemName());
        if(unfCount > unfCountMinThreshold){
            int amtPrimary = (int) script.getBank().getAmount(recipe.getPrimaryItemName());
            int bound = amtPrimary - 200 < 600 ? amtPrimary : 600;
            unfCountMinThreshold = ThreadLocalRandom.current().nextInt(200, bound);
            script.log(IntermittentSell.class.getSimpleName() + " runs when there are " + unfCountMinThreshold + " unf potions");
            return true;
        }
        return false;
    }

    private boolean isJumpingToIntermittentBuy(){
        GrandExchange.Box finishedProductSellBox = findFinishedProductSellingBox();
        if(finishedProductSellBox != null){
            GrandExchange ge = script.getGrandExchange();
            int amountTraded = ge.getAmountTraded(finishedProductSellBox);
            int totalAmountToTrade = ge.getAmountToTransfer(finishedProductSellBox);
            double completionPercent = amountTraded / totalAmountToTrade;
            return completionPercent >= 0.5;
        }
        return false;
    }

    private GrandExchange.Box findPrimaryIngredientBuyingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(script.getGrandExchange().getItemId(box) == recipe.getFinishedItemID()){
                return box;
            }
        }
        return null;
    }

    private GrandExchange.Box findFinishedProductSellingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(script.getGrandExchange().getItemId(box) == recipe.getFinishedItemID()){
                return box;
            }
        }
        return null;
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
        return jumpTarget;
    }

    @Override
    public void logNode() {
        script.log(this.getClass().getSimpleName());
    }
}
