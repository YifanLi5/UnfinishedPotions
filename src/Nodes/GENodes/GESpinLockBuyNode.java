package Nodes.GENodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Nodes.StartingNode;
import Util.CombinationRecipes;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.Margins;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Collections;
import java.util.List;


public class GESpinLockBuyNode implements ExecutableNode {

    final Script script;
    GrandExchangeOperations operations;
    Margins margins;

    private boolean isJumping = false;
    private Class<? extends ExecutableNode> jumpTarget;

    public GESpinLockBuyNode(Script script){
        operations = GrandExchangeOperations.getInstance(script.bot);
        this.script = script;
        margins = Margins.getInstance(script);

    }

    @Override
    public boolean canExecute() {
        NPC clerk = script.getNpcs().closest("Grand Exchange Clerk");
        return clerk != null && clerk.exists();
    }

    @Override
    public int executeNode() throws InterruptedException {
        logNode();
        String lastSuccess = "";
        boolean withdrawnGP = new ConditionalSleep(5000, 1000){
            @Override
            public boolean condition() throws InterruptedException {
                return withdrawCashLeave10k();
            }
        }.sleep();
        if(withdrawnGP){
            lastSuccess = "withdrawnGP";
            if(script.getInventory().getAmount(995) >= 300000){ //withdraw ge and check if enough
                lastSuccess = "gp >= 300k";
                CombinationRecipes nextRecipe = margins.findAndSetNextRecipe();
                int buyPrice = margins.getCachedConversionMargin(nextRecipe)[0];
                if (operations.buyUpToLimit(nextRecipe.getPrimaryItemID(), nextRecipe.getGeSearchTerm(), buyPrice, 1000)) {
                    if(waitForBuy()){
                        lastSuccess = "post buySpinLock";
                        boolean collect = new ConditionalSleep(5000, 1000){
                            @Override
                            public boolean condition() throws InterruptedException {
                                return operations.collect();
                            }
                        }.sleep();
                        if(collect){
                            lastSuccess = "ALL GOOD";
                        }
                    } else {
                        script.warn("waitForBuy failed, likely due to random price shift, re-running from abort");
                        isJumping = true;
                        jumpTarget = AbortRelevantOffers.class;
                    }
                }
            }
        }
        script.log("last Success: " + lastSuccess);
        return 1000;
    }

    boolean waitForBuy() throws InterruptedException {
        int timer = 0;
        boolean lock = true;
        script.log("entering spinlocking in GEBuy");
        while (lock) {
            if (timer > 60) {
                timer = 0;
                if(resubmitHigherOffer()){
                    script.log("increase offer successful");
                    lock = new ConditionalSleep(5000, 1000){
                        @Override
                        public boolean condition() throws InterruptedException {
                            return findPrimaryIngredientBuyingBox() != null;
                        }
                    }.sleep();
                    continue;
                } else{
                    return false;
                }
            }
            lock = doContinueLocking();
            timer++;
            Thread.sleep(1000);
        }
        script.log("released GEBuy spinlock");
        return true;
    }

    private boolean doContinueLocking(){
        GrandExchange.Box buyingBox = findPrimaryIngredientBuyingBox();
        if(buyingBox != null){
            double percentComplete = operations.getOfferCompletionPercentage(buyingBox);
            int invPrimaryCount = (int) script.getInventory().getAmount(margins.getCurrentRecipe().getPrimaryItemName());
            int amtTraded = script.getGrandExchange().getAmountTraded(buyingBox);
            if(invPrimaryCount + amtTraded >= 100){
                script.log("release lock: >= 100 primary ingredients");
                return false;
            }
            else {
                boolean isUnder50Percent = percentComplete < 0.5;
                if(!isUnder50Percent){
                    script.log("release lock: GE offer over 50% complete");
                }
                return isUnder50Percent;
            }
        }
        script.warn("release lock: no valid ge box found");
        return false;
    }

    private boolean resubmitHigherOffer() throws InterruptedException {
        GrandExchange.Box buyingBox = findPrimaryIngredientBuyingBox();
        if(buyingBox != null){
            int amtLeftToBuy = operations.getAmountRemaining(buyingBox);
            CombinationRecipes currentRecipe = margins.getCurrentRecipe();
            if(operations.abortOffersWithItem(currentRecipe.getPrimaryItemName())){
                boolean collected = new ConditionalSleep(5000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return operations.collect();
                    }
                }.sleep();
                if(collected){
                    int newBuyPrice = margins.findPrimaryIngredientMargin(currentRecipe)[1];
                    int[] newMargin = margins.getCachedConversionMargin(currentRecipe);
                    int delta = newMargin[1] - newMargin[0];
                    if(delta >= Margins.switchRecipeIfLower){ //only increase the offer if the new margin doesn't go under the switch threshold
                        script.log("increasing offer to " + newBuyPrice);
                        MethodProvider.sleep(1000);
                        return operations.buyUpToLimit(currentRecipe.getPrimaryItemID(), currentRecipe.getGeSearchTerm(), newBuyPrice, amtLeftToBuy);
                    }
                    //if it goes above the threshold, return false.
                }
            }
        }
        return false;
    }

    private GrandExchange.Box findPrimaryIngredientBuyingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(operations.getItemId(box) == margins.getCurrentRecipe().getPrimaryItemID()){
                return box;
            }
        }
        script.warn("Expected to find the primary ingredient " + margins.getCurrentRecipe() + " but it didn't exist!");
        return null;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return Collections.singletonList(new Edge(DepositNode.class, 1));
    }

    boolean withdrawCashLeave10k() throws InterruptedException {
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
                int invCoins = (int) script.getInventory().getAmount(995);
                int withdrawAmt = (bankedCoins - 10000)/ 1000 * 1000;
                if(withdrawAmt > 0 && bank.withdraw(995, withdrawAmt)){
                    invCoins = (int) script.getInventory().getAmount(995);
                }
                if(invCoins >= 300000) {
                    bankedCoins = (int) bank.getAmount(995);
                    return bankedCoins >= 10000 || bank.deposit(995, 10000);
                } else {
                    script.warn("not enough gp, but may be able to find some unf pots to sell");
                    if(margins.getCurrentRecipe() == null){
                        script.log("current recipe is null, goto startingNode");
                        isJumping = true;
                        jumpTarget = StartingNode.class;
                    }
                    else if(isSellItemPending() || isBuyItemPending()){
                        script.log("found a ge cancelable ge offer");
                        isJumping = true;
                        jumpTarget = AbortRelevantOffers.class;
                    } else if(bank.getAmount(margins.getCurrentRecipe().getFinishedItemName()) >= 100){
                        script.log("found banked sellable unf pots");
                        isJumping = true;
                        jumpTarget = AbortRelevantOffers.class;
                    } else if(script.getInventory().contains(margins.getCurrentRecipe().getPrimaryItemName())){
                        script.log("inventory has primary items to convert");
                        isJumping = true;
                        jumpTarget = DepositNode.class;
                    }
                    else{
                        script.warn("did not have an failsafe");
                        script.stop(false);
                    }
                }
            }
        }
        return false;
    }

    private boolean isBuyItemPending(){
        GrandExchange ge = script.getGrandExchange();
        if(margins.getCurrentRecipe() != null) {
            for (GrandExchange.Box box : GrandExchange.Box.values()){
                if (ge.getItemId(box) == margins.getCurrentRecipe().getPrimaryItemID()) {
                    script.log(ge.getStatus(box));
                    if (ge.getStatus(box) == GrandExchange.Status.PENDING_BUY ||
                            ge.getStatus(box) == GrandExchange.Status.COMPLETING_BUY ||
                            ge.getStatus(box) == GrandExchange.Status.FINISHED_BUY) {
                        script.log("buy is pending");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSellItemPending(){
        GrandExchange ge = script.getGrandExchange();
        if(margins.getCurrentRecipe() != null){
            for (GrandExchange.Box box : GrandExchange.Box.values()){
                if (ge.getItemId(box) == margins.getCurrentRecipe().getFinishedItemID()) {
                    if (ge.getStatus(box) == GrandExchange.Status.COMPLETING_SALE ||
                            ge.getStatus(box) == GrandExchange.Status.PENDING_SALE ||
                            ge.getStatus(box) == GrandExchange.Status.FINISHED_SALE) {
                        script.log("sell is pending");
                        return true;
                    }
                }
            }
        }

        return false;
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
