package Nodes.GENodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.Margins;
import Util.Statics;
import Util.UnfPotionRecipes;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Collections;
import java.util.List;

public class IntermittentSell implements ExecutableNode {

    private Script script;
    private Margins margins;
    private UnfPotionRecipes recipe;
    private GrandExchangeOperations operations;

    public IntermittentSell(Script script) {
        this.script = script;
        margins = Margins.getInstance(script);
        margins.setCurrentRecipe(UnfPotionRecipes.AVANTOE);
        recipe = margins.getCurrentRecipe();
        operations = GrandExchangeOperations.getInstance(script.bot);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        NPC clerk = script.getNpcs().closest("Grand Exchange Clerk");
        return clerk != null && clerk.exists();
    }

    @Override
    public int executeNode() throws InterruptedException {
        script.log("executing IntermittentSell");
        recipe = margins.getCurrentRecipe();
        int[] cachedMargin = margins.getFinishedProductMargin(recipe);
        if(cachedMargin == null || cachedMargin[0] <= 0 || cachedMargin[1] <= 0){
            cachedMargin = margins.findFinishedProductMargin(recipe);
        }
        if(withdrawSellItem(recipe.getFinishedItemID())){
            GrandExchange.Box sellingBox = findFinishedProductSellingBox();
            if(sellingBox != null){ //there exists a box that is currently selling the finished product
                if(operations.abortOffersWithItem(recipe.getFinishedItemName())){
                    Statics.longRandomNormalDelay();
                    if(operations.collect()){
                        Statics.shortRandomNormalDelay();
                        decreaseOffer(cachedMargin, sellingBox);
                    }
                }
            } else {
                operations.sellAll(recipe.getFinishedNotedItemID(), (cachedMargin[0] + cachedMargin[1]) / 2);
            }
        }

        return (int) Statics.randomNormalDist(1500, 500);
    }

    private GrandExchange.Box findFinishedProductSellingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(operations.getItemId(box) == recipe.getFinishedItemID()){
                return box;
            }
        }
        return null;
    }

    private boolean decreaseOffer(int[] margin, GrandExchange.Box sellingBox) throws InterruptedException {
        if(sellingBox != null){
            int oldSellOffer = operations.getItemPrice(sellingBox);
            int decrementFactor = (margin[1] - margin[0]) / 4;
            int newSellPrice = oldSellOffer - decrementFactor;
            script.log("decreasing sell offer to " + newSellPrice);
            if(operations.abortOffersWithItem(recipe.getFinishedItemName())){
                MethodProvider.sleep(1000);
                if(operations.collect()){
                    MethodProvider.sleep(1000);
                    return operations.sellAll(recipe.getFinishedNotedItemID(), newSellPrice);
                }
            }
        }
        return false;
    }

    private boolean withdrawSellItem(int itemID) throws InterruptedException {
        Bank bank = script.getBank();
        if (bank.open()) {
            boolean success = new ConditionalSleep(1000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return bank.isOpen();
                }
            }.sleep();
            if (success) {
                if(bank.getAmount(itemID) >= 50 && bank.enableMode(Bank.BankMode.WITHDRAW_NOTE)){
                    return bank.withdraw(itemID, Bank.WITHDRAW_ALL);
                } else return script.getInventory().contains(itemID) || script.getInventory().contains(itemID+1);
            }
        }
        return false;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return Collections.singletonList(new Edge(DepositNode.class, 1));
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
