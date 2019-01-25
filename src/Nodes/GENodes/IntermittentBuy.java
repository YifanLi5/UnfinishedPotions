package Nodes.GENodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IntermittentBuy extends AbstractGENode implements ExecutableNode  {
    public IntermittentBuy(Bot bot) {
        super(bot);
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        NPC clerk = npcs.closest("Grand Exchange Clerk");
        return clerk != null && clerk.exists();
    }

    @Override
    public int executeNode() throws InterruptedException {
        log("executing IntermittentBuy");
        recipe = margins.getCurrentRecipe();
        int[] cachedMargin = margins.getCachedPrimaryIngredientMargin(recipe);
        if(withdrawCashForMarginCheck()){
            if(cachedMargin[0] == Margins.DEFAULT_MARGIN[0] || cachedMargin[1] == Margins.DEFAULT_MARGIN[1]){
                log("cachedMargin not properly initialized, finding margin for " + recipe.name());
                cachedMargin = margins.findPrimaryIngredientMargin(recipe);
            }
            log("margin for IntermittentBuy: " + Arrays.toString(cachedMargin));
            if(openGE()){
                GrandExchange.Box buyingBox = findPrimaryIngredientBuyingBox();
                boolean buyBoxExists = buyingBox != null;
                int buyPrice = -1;
                if(buyBoxExists){
                    buyPrice = operations.getItemPrice(buyingBox);
                }

                GrandExchange.Box sellingBox = findFinishedProductSellingBox();
                boolean sellBoxExists = sellingBox != null;
                int amtProductSold = -1;
                if(sellBoxExists){
                    amtProductSold = operations.getAmountTraded(sellingBox);
                }

                if(operations.collect()){
                    log("There was something to collect");
                } else {
                    log("nothing to collect");
                }

                int cashStack = (int) inventory.getAmount(995);
                if(buyBoxExists){
                    if(operations.abortOffersWithItem(recipe.getPrimary())){
                        Statics.longRandomNormalDelay();
                        if(operations.collect()){
                            Statics.shortRandomNormalDelay();
                            increaseOffer(cachedMargin, buyPrice);
                        }
                    }
                }
                else if(sellBoxExists && cashStack >= 100000){
                    log("selling box: " + sellingBox);
                    int marginMid = (cachedMargin[0] + cachedMargin[1]) / 2;
                    if(amtProductSold >= 0)
                        operations.buyUpToLimit(recipe.getPrimary(), marginMid, amtProductSold);
                    else operations.buyUpToLimit(recipe.getPrimary(), marginMid, 500);
                }
            }
        }
        return (int) Statics.randomNormalDist(1500, 500);
    }

    private boolean increaseOffer(int[] margin, int prevBuyPrice) throws InterruptedException {
        int incrementFactor = (margin[1] - margin[0]) / 4;
        int newBuyPrice = prevBuyPrice + incrementFactor;
        log("increasing buy offer to " + newBuyPrice);
        if(operations.abortOffersWithItem(recipe.getProduct())){
            MethodProvider.sleep(1000);
            if(operations.collect()){
                MethodProvider.sleep(1000);
                GrandExchange.Box sellingBox = findFinishedProductSellingBox();
                if(sellingBox != null){
                    int soldFinishedProducts = operations.getAmountTraded(sellingBox);
                    return operations.buyUpToLimit(recipe.getPrimary(), newBuyPrice, soldFinishedProducts);
                }
                return operations.buyUpToLimit(recipe.getPrimary(), newBuyPrice, 500);
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

    private boolean openGE() {
        if(!grandExchange.isOpen()){
            NPC grandExchangeClerk = npcs.closest("Grand Exchange Clerk");
            if(grandExchangeClerk != null){
                boolean didInteraction = grandExchangeClerk.interact("Exchange");
                return new ConditionalSleep(1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return didInteraction && grandExchange.isOpen();
                    }
                }.sleep();
            }
        }
        return true;
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

    }
}
