package Nodes.GENodes;

import Nodes.BankingNodes.DepositNode;
import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.GrandExchangeUtil.GrandExchangeObserver;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.GrandExchangeUtil.GrandExchangePolling;
import Util.Margins;
import Util.Statics;
import Util.UnfPotionRecipes;
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


public class GESpinLockBuyNode implements ExecutableNode, GrandExchangeObserver {

    private final Script script;
    private GrandExchangeOperations operations;
    private GrandExchangePolling polling;
    private boolean doPreventIdleAction = true;
    private int amtTraded;
    private GrandExchange.Box box;
    private boolean offerUpdated;
    private Margins margins;
    private UnfPotionRecipes recipe;


    private List<Edge> adjNodes = Collections.singletonList(new Edge(DepositNode.class, 1));

    public GESpinLockBuyNode(Script script){
        operations = GrandExchangeOperations.getInstance(script.bot);
        polling = GrandExchangePolling.getInstance(script);
        this.script = script;
        margins = Margins.getInstance(script);
        recipe = margins.getCurrentRecipe();
    }

    @Override
    public void onGEUpdate(GrandExchange.Box box) {
        GrandExchange ge = script.getGrandExchange();
        if(ge.getItemId(box) == recipe.getPrimaryItemID()){
            this.box = box;
            amtTraded = ge.getAmountTraded(box);
            int totalAmtToTrade = ge.getAmountToTransfer(box);
            script.log("Buy offer updated, box: " + box.toString() + " has bought " + amtTraded + "/" + totalAmtToTrade + "items \nrecieved by " + this.getClass().getSimpleName());
            offerUpdated = true;
            if(ge.getStatus(box) == GrandExchange.Status.FINISHED_BUY){
                if(amtTraded == totalAmtToTrade){
                    script.log("Buy offer finished");
                } else {
                    script.log("Offer canceled");
                }
            }
        }
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
                int buyPrice = findInstantBuyPrice(doConversionMarginCheck);
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
        offerUpdated = false;
        polling.registerObserver(this);
        if (operations.buyUpToLimit(recipe.getPrimaryItemID(), recipe.getGeSearchTerm(), buyPrice, 1000)) {
            int loops = 0;
            amtTraded = 0;
            while (!offerUpdated && amtTraded < 14) {
                loops++;
                Thread.sleep(1000);
                script.log("Thread: " + Thread.currentThread().getId() + " is spinlocking in GEBuy, loops: " + loops);
                if (loops > 90) {
                    loops = 0;
                    increaseOffer();
                }
            }
            script.log("Thread: " + Thread.currentThread().getId() + " has released GEBuy spinlock");
        }
    }

    private int findInstantBuyPrice(boolean doConversionMarginCheck) throws InterruptedException {
        int[] margin;
        if(margins.getSecondsSinceLastUpdate(recipe) > 900 || doConversionMarginCheck){
            script.log("preforming price check on conversion for: " + recipe.name());
            margin = margins.findSpecificConversionMargin(recipe);
            if(margin[1] - margin[0] < Margins.SWITCH_RECIPE_IF_LOWER){
                UnfPotionRecipes nextRecipe = margins.findAllConversionMargins();
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

    private boolean increaseOffer() throws InterruptedException {
        int prevOffer = script.grandExchange.getItemPrice(box);
        script.log("increasing offer to " + (prevOffer + 25));
        if(operations.abortOffersWithItem(recipe.getPrimaryItemName())){
            MethodProvider.sleep(1000);
            if(operations.collect()){
                return operations.buyUpToLimit(recipe.getPrimaryItemID(), recipe.getGeSearchTerm(), prevOffer + 25, 1000);
            }
        }
        return false;
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
                if(bank.getAmount(995) >= 0){
                    return bank.withdraw(995, Bank.WITHDRAW_ALL) && bank.close();
                }
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
