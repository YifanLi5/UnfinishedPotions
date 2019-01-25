package Nodes.GENodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Collections;
import java.util.List;

public class IntermittentSell extends AbstractGENode implements ExecutableNode {

    public IntermittentSell(Bot bot) {
        super(bot);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        NPC clerk = npcs.closest("Grand Exchange Clerk");
        return clerk != null && clerk.exists();
    }

    @Override
    public int executeNode() throws InterruptedException {
        log("executing IntermittentSell");
        recipe = margins.getCurrentRecipe();
        if(withdrawSellItem(recipe.getProduct())){
            int[] cachedMargin = margins.getCachedFinishedProductMargin(recipe);
            if(cachedMargin[0] == Margins.DEFAULT_MARGIN[0] || cachedMargin[1] == Margins.DEFAULT_MARGIN[1]){
                if(withdrawCashForMarginCheck()){
                    cachedMargin = margins.findFinishedProductMargin(recipe);
                } else{
                    warn("IntermittentSell, withdraw gp for price check failed");
                    return -1;
                }
            }
            GrandExchange.Box sellingBox = findFinishedProductSellingBox();
            if(sellingBox != null){ //there exists a box that is currently selling the finished product
                log("aborting previous offer");
                if(operations.abortOffersWithItem(recipe.getProduct())){
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
            operations.sellAll(recipe.getProduct(), sellPrice);

        }
        return (int) Statics.randomNormalDist(1500, 500);
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
                    log("banked coins: " + bankedCoins + " inv coins: " + invCoins);
                    if(invCoins + bankedCoins >= 5000) {
                        return bank.withdrawAll(995);
                    } else{
                        log("not enough gp");
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
        return false;
    }

    @Override
    public Class<? extends ExecutableNode> setJumpTarget() {
        return null;
    }

}
