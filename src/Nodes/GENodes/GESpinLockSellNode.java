package Nodes.GENodes;

import Nodes.MarkovChain.Edge;
import Nodes.MarkovChain.ExecutableNode;
import Util.ComponentsEnum;
import Util.GrandExchangeUtil.GrandExchangeObserver;
import Util.GrandExchangeUtil.GrandExchangeOperations;
import Util.GrandExchangeUtil.GrandExchangePolling;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GESpinLockSellNode implements ExecutableNode, GrandExchangeObserver {
    private Script script;
    private ComponentsEnum sell;
    private GrandExchangeOperations operations;
    private GrandExchangePolling polling;
    private boolean offerUpdated, doPreventIdleAction = true;
    int amtTraded;
    private static int primaryComponentBuyPrice = 0;

    GrandExchange.Box box;

    private List<Edge> adjNodes = Collections.singletonList(new Edge(GESpinLockBuyNode.class, 1));

    public GESpinLockSellNode(Script script, ComponentsEnum sell) {
        this.script = script;
        this.sell = sell;
        this.operations = GrandExchangeOperations.getInstance(script.bot);
        this.polling = GrandExchangePolling.getInstance(script);
    }

    @Override
    public void onGEUpdate(GrandExchange.Box box) {
        GrandExchange ge = script.getGrandExchange();
        if(ge.getItemId(box) == sell.getFinishedItemID()){
            this.box = box;
            amtTraded = ge.getAmountTraded(box);
            int amtTraded = ge.getAmountTraded(box);
            int totalAmtToTrade = ge.getAmountToTransfer(box);
            script.log("Sell offer updated, box: " + box.toString() + " has sold " + amtTraded + "/" + totalAmtToTrade + "items \nrecieved by " + this.getClass().getSimpleName());
            offerUpdated = true;
            if(ge.getStatus(box) == GrandExchange.Status.FINISHED_SALE){
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
        if(Statics.logNodes){
            logNode();
        }
        Inventory inv = script.getInventory();
        if(inv.contains(sell.getFinishedItemName()) || withdrawSellItem(sell.getFinishedItemID())) {
            if(inv.getAmount(sell.getFinishedItemName()) >= 50){
                if(isSellItemPending()){
                    script.log("canceling previous sell offer");
                    if(operations.abortOffersWithItem(sell.getFinishedItemName())){
                        collect();
                    }
                }
                MethodProvider.sleep(1000);
                if(inv.getAmount(995) < 5000){
                    script.log("withdrawing cash");
                    withdrawCash();
                }
                int[] margin = {-1,-1};
                if(inv.getAmount(995) >= 5000){
                    margin = operations.priceCheckItem(sell.getFinishedItemID(), sell.getGeSearchTerm());
                    GESpinLockBuyNode.setFinishedPotionMargin(margin);
                }
                offerUpdated = false;
                polling.registerObserver(this);
                if(selectSellOperation(margin)) {
                    int loops = 0;
                    while(!offerUpdated && amtTraded < 14){
                        loops++;
                        Thread.sleep(1000);
                        script.log("Thread: " + Thread.currentThread().getId() + " is spinlocking in GESell");
                        if(loops > 90){
                            loops = 0;
                            decreaseOffer();
                        }
                    }
                    script.log("Thread: " + Thread.currentThread().getId() + " GESell spinlock released");
                    collect();
                }
            }
        }
        return 1000;
    }

    @Override
    public List<Edge> getAdjacentNodes() {
        return adjNodes;
    }

    public static void setPrimaryComponentBuyPrice(int primaryComponentBuyPrice) {
        GESpinLockSellNode.primaryComponentBuyPrice = primaryComponentBuyPrice;
    }

    private boolean decreaseOffer() throws InterruptedException {
        int prevOffer = script.grandExchange.getItemPrice(box);
        script.log("increasing offer to " + (prevOffer - 25));
        if(operations.abortOffersWithItem(sell.getFinishedItemName())){
            MethodProvider.sleep(1000);
            if(collect()){
                MethodProvider.sleep(1000);
                return operations.sellAll(sell.getFinishedItemID(), prevOffer - 25);
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
                if(bank.enableMode(Bank.BankMode.WITHDRAW_NOTE)){
                    return bank.withdraw(itemID, Bank.WITHDRAW_ALL);
                }
            }
        }
        return false;
    }

    private boolean collect() throws InterruptedException {
        boolean successfulCollect = false;
        int attempts = 0;
        while(!successfulCollect && attempts < 5){
            successfulCollect = operations.collect();
            attempts++;
            MethodProvider.sleep(1000);
        }
        return successfulCollect;
    }

    private boolean isSellItemPending(){
        GrandExchange ge = script.getGrandExchange();
        for (GrandExchange.Box box : GrandExchange.Box.values())
            if(ge.getItemId(box) == sell.getFinishedItemID()){
                if(ge.getStatus(box) == GrandExchange.Status.COMPLETING_SALE ||
                        ge.getStatus(box) == GrandExchange.Status.PENDING_SALE ||
                        ge.getStatus(box) == GrandExchange.Status.FINISHED_SALE){
                    script.log("sell is pending");
                    return true;
                }

            }
        return false;
    }

    private void preventIdleLogout() throws InterruptedException {
        if(doPreventIdleAction){

            int yaw = script.camera.getYawAngle();
            if(script.camera.moveYaw(yaw + MethodProvider.random(-15, 15))){
                doPreventIdleAction = false;
                int nextAction = (int) Statics.randomNormalDist(200000, 25000);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        doPreventIdleAction = true;
                    }
                }, nextAction);
            }
        }
    }

    private boolean selectSellOperation(int[] margin) throws InterruptedException {
        if(margin[1] - margin[0] <= 75 || margin[0] - primaryComponentBuyPrice >= 175){
            return operations.sellAll(sell.getFinishedItemID()+1, margin[0]);
        } else {
            int price = (margin[1] + margin[0])/2;
            return operations.sellAll(sell.getFinishedItemID()+1, price);
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