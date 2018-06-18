package Nodes.GENodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.GrandExchangeUtil.GrandExchangePolling;
import Util.ItemCombinationRecipes;
import Util.Margins;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Tabs;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class GESpinLockBuyNode implements ExecutableNode {

    private final Script script;
    private GrandExchangeOperations operations;
    private GrandExchangePolling polling;
    private boolean doPreventIdleAction = true;
    private Margins margins;
    private ItemCombinationRecipes recipe;
    private List<Edge> adjNodes = Collections.singletonList(new Edge(DepositNode.class, 1));

    public GESpinLockBuyNode(Script script){
        operations = GrandExchangeOperations.getInstance(script.bot);
        polling = GrandExchangePolling.getInstance(script);
        this.script = script;
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
        recipe = margins.getCurrentRecipe();
        if(Statics.logNodes)
            logNode();
        Inventory inv = script.getInventory();
        boolean doConversionMarginCheck = false;
        if(withdrawCash()){
            MethodProvider.sleep(1000);
            if(isBuyItemPending()){
                if(operations.collect()){
                    Statics.longRandomNormalDelay();
                    if(inv.getAmount(recipe.getPrimaryItemName()) >= 14){
                        return 1000;
                    }
                }
                script.log("canceling previous buy offer");
                if(operations.abortOffersWithItem(recipe.getPrimaryItemName())){
                    MethodProvider.sleep(1000);
                    operations.collect();
                    doConversionMarginCheck = true;
                }
            }

            MethodProvider.sleep(1000);
            if(inv.getAmount(995) >= 300000){
                int buyPrice = findBuyPrice(doConversionMarginCheck);
                spinLockUntilOfferUpdates(buyPrice);
                Statics.longRandomNormalDelay();
                if(operations.collect()){
                    script.log("collected bought items");
                }
            }
        }
        return 1000;
    }

    private void spinLockUntilOfferUpdates(int buyPrice) throws InterruptedException {
        if (operations.buyUpToLimit(recipe.getPrimaryItemID(), recipe.getGeSearchTerm(), buyPrice, 1000)) {
            int loops = 0;
            GrandExchange.Box buyingBox = findPrimaryIngredientBuyingBox();
            boolean lock = true;
            script.log("Thread: " + Thread.currentThread().getId() + " entering spinlocking in GEBuy");
            while (lock) {
                loops++;
                Thread.sleep(1000);
                if (loops > 60) {
                    loops = 0;
                    if(increaseOffer(buyingBox)){
                        script.log("increase offer successful");
                    } else{
                        script.warn("increase offer unsuccessful");
                    }

                }
                lock = doContinueLocking(buyingBox);
            }
            script.log("Thread: " + Thread.currentThread().getId() + " has released GEBuy spinlock");
        }
    }

    private boolean doContinueLocking(GrandExchange.Box buyingBox){
        GrandExchange ge = script.getGrandExchange();
        double percentComplete = ge.getAmountTraded(buyingBox) / ge.getAmountToTransfer(buyingBox);
        return percentComplete < 0.25;
    }

    private boolean increaseOffer(GrandExchange.Box buyingBox) throws InterruptedException {
        int prevOffer = script.grandExchange.getItemPrice(buyingBox);
        script.log("increasing offer to " + (prevOffer + 25));
        if(operations.abortOffersWithItem(recipe.getPrimaryItemName())){
            MethodProvider.sleep(1000);
            if(operations.collect()){
                return operations.buyUpToLimit(recipe.getPrimaryItemID(), recipe.getGeSearchTerm(), prevOffer + 25, 1000);
            }
        }
        return false;
    }

    private GrandExchange.Box findPrimaryIngredientBuyingBox(){
        for(GrandExchange.Box box: GrandExchange.Box.values()){
            if(operations.getItemId(box) == recipe.getPrimaryItemID()){
                return box;
            }
        }
        return null;
    }

    private int findBuyPrice(boolean doConversionMarginCheck) throws InterruptedException {
        int[] margin;
        if(margins.getSecondsSinceLastUpdate(recipe) > 900 || doConversionMarginCheck){
            script.log("preforming price check on conversion for: " + recipe.name());
            margin = margins.findSpecificConversionMargin(recipe);
            if(margin[1] - margin[0] < Margins.SWITCH_RECIPE_IF_LOWER){
                ItemCombinationRecipes nextRecipe = margins.findAllConversionMargins();
                margins.setCurrentRecipe(nextRecipe);
                recipe = margins.getCurrentRecipe();
                margin = margins.getMarginOfCurrentRecipe();
            } else {
                margins.updateConversionMarginEntry(recipe, margin);
            }

        } else {
            script.log("cached margin is acceptable");
            margin = margins.getMarginOfCurrentRecipe();
        }
        return margin[0];

    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return adjNodes;
    }



    private boolean isBuyItemPending(){
        GrandExchange ge = script.getGrandExchange();
        for (GrandExchange.Box box : GrandExchange.Box.values())
            if(ge.getItemId(box) == recipe.getPrimaryItemID()){
                script.log(ge.getStatus(box));
                if(ge.getStatus(box) == GrandExchange.Status.PENDING_BUY ||
                        ge.getStatus(box) == GrandExchange.Status.COMPLETING_BUY ||
                        ge.getStatus(box) == GrandExchange.Status.FINISHED_BUY){
                    script.log("buy is pending");
                    return true;
                }

            }

        return false;
    }

    private boolean withdrawCash() throws InterruptedException {
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
                if(bankedCoins > 10000){
                    int withdrawAmt = (bankedCoins - 10000)/ 1000 * 1000;
                    if(withdrawAmt == 0){
                        return true;
                    } else return bank.withdraw(995, withdrawAmt) && bank.close();
                } else return bankedCoins < 10000 && script.getInventory().contains(995);
            }
        }
        return false;
    }

    private void preventIdleLogout() throws InterruptedException {
        if(doPreventIdleAction){
            doPreventIdleAction = false;
            Tabs tabs = script.getTabs();
            tabs.open(org.osbot.rs07.api.ui.Tab.SKILLS);
            Statics.shortRandomNormalDelay();
            tabs.open(org.osbot.rs07.api.ui.Tab.INVENTORY);
            int nextAction = (int) Statics.randomNormalDist(200000, 25000);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    doPreventIdleAction = true;
                }
            }, nextAction);
        }
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
