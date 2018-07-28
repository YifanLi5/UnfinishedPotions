package Nodes.GENodes;

import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.CombinationRecipes;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.GrandExchangeUtil.GrandExchangePolling;
import Util.Margins;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Collections;
import java.util.List;

public class GESpinLockSellNode implements ExecutableNode {
    private Script script;
    private GrandExchangeOperations operations;
    private GrandExchangePolling polling;
    private Margins margins;
    private CombinationRecipes recipe;

    public GESpinLockSellNode(Script script) {
        this.script = script;
        this.operations = GrandExchangeOperations.getInstance(script.bot);
        this.polling = GrandExchangePolling.getInstance(script);
        margins = Margins.getInstance(script);
        recipe = margins.getCurrentRecipe();
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        NPC clerk = script.getNpcs().closest("Grand Exchange Clerk");
        return clerk != null && clerk.exists();
    }

    @Override
    public int executeNode() throws InterruptedException {
        logNode();
        String lastSuccess = "";
        recipe = margins.getCurrentRecipe();
        boolean withdrawnSellItem = new ConditionalSleep(5000, 1000) {
            @Override
            public boolean condition() throws InterruptedException {
                return withdrawSellItem(recipe.getFinishedItemID());
            }
        }.sleep();
        if(withdrawnSellItem) {
            lastSuccess = "withdrawnSellItem";
            boolean withdrawnMarginCheckGP = new ConditionalSleep(5000, 1000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return withdrawCashForMarginCheck();
                }
            }.sleep();
            if(withdrawnMarginCheckGP){
                lastSuccess = "withdrawnMarginCheckGP";
                int[] margin = margins.findFinishedProductMargin(recipe);
                if(margin[0] == Margins.DEFAULT_MARGIN[0] || margin[1] == Margins.DEFAULT_MARGIN[1]){
                    script.warn("ERROR: margin variable in GESpinLockSell never changed from default");
                    margin = margins.findSpecificConversionMargin(recipe);
                }
                int instaSell = margin[0];
                if(instaSell > 0 && operations.sellAll(recipe.getFinishedNotedItemID(), recipe.getFinishedItemName(), instaSell)){
                    waitUntilSold();
                    lastSuccess = "postSellSpinlock";
                }
                boolean collected = new ConditionalSleep(5000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return operations.collect();
                    }
                }.sleep();
                if(collected)
                    lastSuccess = "ALL GOOD";

            }
        }
        script.log("last success: " + lastSuccess);
        return 1000;
    }

    private void waitUntilSold() throws InterruptedException {
        int loops = 0;
        boolean lock = true;
        script.log("Entering spinlocking in GESell");
        while(lock){
            loops++;
            Thread.sleep(1000);
            if(loops > 60){
                loops = 0;
                if(decreaseOffer()){
                    script.log("decrease offer successful");
                } else{
                    script.warn("decrease offer unsuccessful");
                }
            }
            lock = doContinueLocking();
        }
        script.log("GESell spinlock released");
    }

    private boolean doContinueLocking(){
        GrandExchange.Box sellingBox = findFinishedProductSellingBox();
        if(sellingBox != null){
            double percentComplete = operations.getOfferCompletionPercentage(sellingBox);
            boolean doLock = percentComplete < 0.75;
            if(!doLock){
                script.log("release lock, at least 75% complete");
            }
            return doLock;
        }
        return false;
    }

    private GrandExchange.Box findFinishedProductSellingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(operations.getItemId(box) == recipe.getFinishedItemID()){
                return box;
            }
        }
        return null;
    }

    private boolean decreaseOffer() throws InterruptedException {
        int newSell = margins.findFinishedProductMargin(recipe)[0];
        script.log("decreasing offer to " + newSell);
        if(operations.abortOffersWithItem(recipe.getFinishedItemName())){
            MethodProvider.sleep(1000);
            if(operations.collect()){
                MethodProvider.sleep(1000);
                return operations.sellAll(recipe.getFinishedNotedItemID(), recipe.getFinishedItemName(), newSell);
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
                if(bank.getAmount(itemID) > 0 && bank.enableMode(Bank.BankMode.WITHDRAW_NOTE)){
                    return bank.withdraw(itemID, Bank.WITHDRAW_ALL);
                } else return script.getInventory().contains(itemID);
            }
        }
        return false;
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
                    if(invCoins + bankedCoins >= 5000) {
                        return bank.withdrawAll(995);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return Collections.singletonList(new Edge(GESpinLockBuyNode.class, 1));
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

    public void stopThread(){
        if(polling != null)
            polling.stopQueryingOffers();
    }
}
