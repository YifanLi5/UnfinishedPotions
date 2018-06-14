package Nodes.GENodes;

import GrandExchangeUtil.GrandExchangeObserver;
import GrandExchangeUtil.GrandExchangeOperations;
import GrandExchangeUtil.GrandExchangePolling;
import ScriptClasses.MarkovNodeExecutor;
import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Tabs;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Timer;
import java.util.TimerTask;


public class GEBuyNode implements MarkovNodeExecutor.ExecutableNode, GrandExchangeObserver {

    private final Script script;
    private GrandExchangeOperations operations;
    private ComponentsEnum buy;
    private GrandExchangePolling polling;
    private boolean doPreventIdleAction = true;

    int amtTraded;
    GrandExchange.Box box;

    private boolean offerUpdated;

    public GEBuyNode(Script script, ComponentsEnum buy){
        operations = new GrandExchangeOperations();
        polling = GrandExchangePolling.getInstance(script);
        operations.exchangeContext(script.bot);
        this.script = script;
        this.buy = buy;
    }

    @Override
    public void onGEUpdate(GrandExchange.Box box) {
        GrandExchange ge = script.getGrandExchange();
        if(ge.getItemId(box) == buy.getPrimaryItemID()){
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
        logNode();
        Inventory inv = script.getInventory();
        if(withdrawCash()){
            MethodProvider.sleep(1000);
            if(isBuyItemPending()){
                script.log("canceling previous buy offer");
                if(operations.abortOffersWithItem(buy.getPrimaryItemName())){
                    collect();
                }
            }
            MethodProvider.sleep(1000);
            if(inv.getAmount(995) >= 300000){
                int[] margin = operations.priceCheckItem(buy.getPrimaryItemID(), buy.getGeSearchTerm());
                offerUpdated = false;
                polling.registerObserver(this);
                if(selectBuyOperation(margin)){
                    int loops = 0;
                    while(!offerUpdated && amtTraded > 14){
                        loops++;
                        Thread.sleep(1000);
                        script.log("Thread: " + Thread.currentThread().getId() + " is spinlocking in GEBuy");
                        if(loops > 90){
                            loops = 0;
                            increaseOffer();
                        }
                    }
                    script.log("Thread: " + Thread.currentThread().getId() + " has released GEBuy spinlock");
                    collect();
                }
            }
        }
        return 1000;
    }

    private boolean increaseOffer() throws InterruptedException {
        int prevOffer = script.grandExchange.getItemPrice(box);
        script.log("increasing offer to " + (prevOffer + 25));
        if(operations.abortOffersWithItem(buy.getPrimaryItemName())){
            if(collect()){
                return operations.buyItem(buy.getPrimaryItemID(), buy.getGeSearchTerm(), prevOffer + 25, 1000);
            }
        }
        return false;
    }

    private boolean collect() throws InterruptedException {
        boolean successfulCollect = false;
        int attempts = 0;
        while(!successfulCollect && attempts < 5){
            successfulCollect = operations.collectAll();
            attempts++;
            MethodProvider.sleep(1000);
        }
        if(!successfulCollect){
            script.log("error in collection");
            script.stop(false);
        }
        return successfulCollect;
    }

    private boolean isBuyItemPending(){
        GrandExchange ge = script.getGrandExchange();
        for (GrandExchange.Box box : GrandExchange.Box.values())
            if(ge.getItemId(box) == buy.getPrimaryItemID()){
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

    private boolean selectBuyOperation(int[] margin) throws InterruptedException {
        if(margin[1] - margin[0] <= 75){
            return operations.buyItem(buy.getPrimaryItemID(), buy.getGeSearchTerm(), margin[1], 1000);
        } else {
            return operations.buyItem(buy.getPrimaryItemID(), buy.getGeSearchTerm(), margin, 1000);
        }
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
                if(bank.getAmount(995) > 0){
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
    public boolean doConditionalTraverse() {
        return false;
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
