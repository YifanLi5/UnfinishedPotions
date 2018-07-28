package Nodes.GENodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.Margins;
import Util.Statics;
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
    private CombinationRecipes recipe;
    private GrandExchangeOperations operations;

    public IntermittentSell(Script script) {
        this.script = script;
        margins = Margins.getInstance(script);
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
        if(withdrawSellItem(recipe.getFinishedItemID())){
            int[] cachedMargin = margins.getCachedFinishedProductMargin(recipe);
            if(cachedMargin[0] == Margins.DEFAULT_MARGIN[0] || cachedMargin[1] == Margins.DEFAULT_MARGIN[1]){
                if(withdrawCashForMarginCheck()){
                    cachedMargin = margins.findFinishedProductMargin(recipe);
                } else{
                    script.warn("IntermittentSell, withdraw gp for price check failed");
                    script.stop(false);
                }
            }
            GrandExchange.Box sellingBox = findFinishedProductSellingBox();
            if(sellingBox != null){ //there exists a box that is currently selling the finished product
                script.log("aborting previous offer");
                if(operations.abortOffersWithItem(recipe.getFinishedItemName())){
                    boolean collected = new ConditionalSleep(5000){
                        @Override
                        public boolean condition() throws InterruptedException {
                            return operations.collect();
                        }
                    }.sleep();
                    if(collected)
                        if(withdrawCashForMarginCheck())
                            cachedMargin = margins.findFinishedProductMargin(recipe);
                }
            }

            int sellPrice = (cachedMargin[0] + cachedMargin[1]) / 2;
            if(cachedMargin[1] - cachedMargin[0] <= 10){
                sellPrice = cachedMargin[0];
            }
            operations.sellAll(recipe.getFinishedNotedItemID(), recipe.getFinishedItemName(), sellPrice);

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

    private boolean withdrawCashForMarginCheck() throws InterruptedException {
        int invCoins = (int) script.getInventory().getAmount(995);
        if(invCoins >= 5000){
            return true;
        } else {
            Bank bank = script.getBank();
            if(bank.open()){
                boolean success = new ConditionalSleep(1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return bank.isOpen();
                    }
                }.sleep();
                if(success){
                    int bankedCoins = (int) bank.getAmount(995);
                    script.log("banked coins: " + bankedCoins + " inv coins: " + invCoins);
                    if(invCoins + bankedCoins >= 5000) {
                        return bank.withdrawAll(995);
                    } else{
                        script.log("not enough gp");
                    }
                }
            }
        }
        return false;
    }

    private boolean decreaseOffer(int[] margin, GrandExchange.Box sellingBox) throws InterruptedException {
        if(sellingBox != null){
            int newSellPrice;
            if(margin[1] - margin[0] <= 10){
                newSellPrice = margin[0];
            } else {
                int oldSellOffer = operations.getItemPrice(sellingBox);
                int decrementFactor = (margin[1] - margin[0]) / 4;
                newSellPrice = oldSellOffer - decrementFactor;
            }

            script.log("decreasing sell offer to " + newSellPrice);
            MethodProvider.sleep(2000);
            if(operations.collect()){
                MethodProvider.sleep(2000);
                return operations.sellAll(recipe.getFinishedNotedItemID(), recipe.getFinishedItemName(), newSellPrice);
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
