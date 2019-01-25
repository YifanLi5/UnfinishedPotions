package Nodes.GENodes;

import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.Margins;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Collections;
import java.util.List;

public class WaitUntilSell extends AbstractGENode implements ExecutableNode {

    public WaitUntilSell(Bot bot) {
        super(bot);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        NPC clerk = npcs.closest("Grand Exchange Clerk");
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
                return withdrawSellItem(recipe.getProduct());
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
                    warn("ERROR: margin variable in GESpinLockSell never changed from default");
                    margin = margins.findSpecificConversionMargin(recipe);
                }
                int instaSell = margin[0];
                if(instaSell > 0 && operations.sellAll(recipe.getProduct(), instaSell)){
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
        log("last success: " + lastSuccess);
        return 1000;
    }

    private void waitUntilSold() throws InterruptedException {
        int loops = 0;
        boolean lock = true;
        log("Entering spinlocking in GESell");
        while(lock){
            loops++;
            Thread.sleep(1000);
            if(loops > 60){
                loops = 0;
                if(decreaseOffer()){
                    log("decrease offer successful");
                } else{
                    warn("decrease offer unsuccessful");
                }
            }
            lock = doContinueLocking();
        }
        log("GESell spinlock released");
    }

    private boolean doContinueLocking(){
        GrandExchange.Box sellingBox = findFinishedProductSellingBox();
        if(sellingBox != null){
            double percentComplete = operations.getOfferCompletionPercentage(sellingBox);
            boolean doLock = percentComplete < 0.75;
            if(!doLock){
                log("release lock, at least 75% complete");
            }
            return doLock;
        }
        return false;
    }

    private boolean decreaseOffer() throws InterruptedException {
        int newSell = margins.findFinishedProductMargin(recipe)[0];
        log("decreasing offer to " + newSell);
        if(operations.abortOffersWithItem(recipe.getProduct())){
            MethodProvider.sleep(1000);
            if(operations.collect()){
                MethodProvider.sleep(1000);
                return operations.sellAll(recipe.getProduct(), newSell);
            }
        }
        return false;
    }

    private boolean withdrawCashForMarginCheck() throws InterruptedException {
        int invCoins = (int) inventory.getAmount(995);
        if(invCoins >= 5000){
            return true;
        } else {
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
        return Collections.singletonList(new Edge(WaitUntilBuy.class, 1));
    }

    @Override
    public boolean isJumping() {
        return false;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return null;
    }
}
