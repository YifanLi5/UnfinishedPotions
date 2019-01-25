package Nodes.GENodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Nodes.StartingNode;
import Util.CombinationRecipes;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.Margins;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Collections;
import java.util.List;


public class WaitUntilBuy extends AbstractGENode implements ExecutableNode {
    GrandExchangeOperations operations;
    Margins margins;

    private boolean isJumping = false;
    private Class<? extends ExecutableNode> jumpTarget;

    public WaitUntilBuy(Bot bot){
        super(bot);
    }

    @Override
    public boolean canExecute() {
        NPC clerk = npcs.closest("Grand Exchange Clerk");
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
            if(inventory.getAmount(995) >= 300000){ //withdraw ge and check if enough
                lastSuccess = "gp >= 300k";
                CombinationRecipes nextRecipe = margins.findAndSetNextRecipe();
                int buyPrice = margins.getCachedConversionMargin(nextRecipe)[0];
                if (operations.buyUpToLimit(nextRecipe.getPrimary(), buyPrice, 1000)) {
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
                        warn("waitForBuy failed, likely due to random price shift, re-running from abort");
                        isJumping = true;
                        jumpTarget = AbortRelevantOffers.class;
                    }
                }
            }
        }
        warn(getClass().getSimpleName() + "FAILED: last Success " + lastSuccess);
        return 1000;
    }

    boolean waitForBuy() throws InterruptedException {
        int timer = 0;
        boolean lock = true;
        log("entering spinlocking in GEBuy");
        while (lock) {
            if (timer > 60) {
                timer = 0;
                if(resubmitHigherOffer()){
                    log("increase offer successful");
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
        log("released GEBuy spinlock");
        return true;
    }

    private boolean doContinueLocking(){
        GrandExchange.Box buyingBox = findPrimaryIngredientBuyingBox();
        if(buyingBox != null){
            double percentComplete = operations.getOfferCompletionPercentage(buyingBox);
            int invPrimaryCount = (int) inventory.getAmount(margins.getCurrentRecipe().getPrimary());
            int amtTraded = grandExchange.getAmountTraded(buyingBox);
            if(invPrimaryCount + amtTraded >= 100){
                log("release lock: >= 100 primary ingredients");
                return false;
            }
            else {
                boolean isUnder50Percent = percentComplete < 0.5;
                if(!isUnder50Percent){
                    log("release lock: GE offer over 50% complete");
                }
                return isUnder50Percent;
            }
        }
        warn("release lock: no valid ge box found");
        return false;
    }

    private boolean resubmitHigherOffer() throws InterruptedException {
        GrandExchange.Box buyingBox = findPrimaryIngredientBuyingBox();
        if(buyingBox != null){
            int amtLeftToBuy = operations.getAmountRemaining(buyingBox);
            CombinationRecipes currentRecipe = margins.getCurrentRecipe();
            if(operations.abortOffersWithItem(currentRecipe.getPrimary())){
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
                        log("increasing offer to " + newBuyPrice);
                        MethodProvider.sleep(1000);
                        return operations.buyUpToLimit(currentRecipe.getPrimary(), newBuyPrice, amtLeftToBuy);
                    }
                    //if it goes above the threshold, return false.
                }
            }
        }
        return false;
    }

    boolean withdrawCashLeave10k() throws InterruptedException {
        if(bank.open()){
            boolean success = new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return bank.isOpen();
                }
            }.sleep();
            if(success){
                int bankedCoins = (int) bank.getAmount(995);
                int invCoins = (int) inventory.getAmount(995);
                int withdrawAmt = (bankedCoins - 10000)/ 1000 * 1000;
                if(withdrawAmt > 0 && bank.withdraw(995, withdrawAmt)){
                    invCoins = (int) inventory.getAmount(995);
                }
                if(invCoins >= 300000) {
                    bankedCoins = (int) bank.getAmount(995);
                    return bankedCoins >= 10000 || bank.deposit(995, 10000);
                } else {
                    warn("not enough gp, but may be able to find some unf pots to sell");
                    if(margins.getCurrentRecipe() == null){
                        log("current recipe is null, goto startingNode");
                        isJumping = true;
                        jumpTarget = StartingNode.class;
                    }
                    else if(isSellItemPending() || isBuyItemPending()){
                        log("found a ge cancelable ge offer");
                        isJumping = true;
                        jumpTarget = AbortRelevantOffers.class;
                    } else if(bank.getAmount(margins.getCurrentRecipe().getProduct()) >= 100){
                        log("found banked sellable unf pots");
                        isJumping = true;
                        jumpTarget = AbortRelevantOffers.class;
                    } else if(inventory.contains(margins.getCurrentRecipe().getPrimary())){
                        log("inventory has primary items to convert");
                        isJumping = true;
                        jumpTarget = DepositNode.class;
                    }
                    else{
                        warn("did not have an failsafe");
                        bot.getScriptExecutor().stop(false);
                    }
                }
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
}
