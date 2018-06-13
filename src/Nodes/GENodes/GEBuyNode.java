package Nodes.GENodes;

import GrandExchangeUtil.GrandExchangeObserver;
import GrandExchangeUtil.GrandExchangeOperations;
import GrandExchangeUtil.GrandExchangePolling;
import ScriptClasses.MarkovNodeExecutor;
import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
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

    private boolean offerUpdated, offerFinished;

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
            script.log("Buy offer updated");
            offerUpdated = true;
            if(ge.getStatus(box) == GrandExchange.Status.FINISHED_BUY){
                script.log("Buy offer finished");
                offerFinished = true;
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
        if(withdrawCash()){
            polling.registerObserver(this);
            if(isBuyItemPending() || operations.buyItem(buy.getPrimaryItemID(), buy.getGeSearchTerm(), 1000)){

                while(!offerUpdated)
                    Thread.sleep(1000);

                offerUpdated = false;
                boolean successfulCollect = false;
                int attempts = 0;
                while(!successfulCollect && attempts < 5){
                    successfulCollect = operations.collectAll();
                    attempts++;
                    MethodProvider.sleep(1000);
                }

            }
            if(offerFinished){
                polling.removeObserver(this);
                offerFinished = false;
            }
        }
        return 1000;
    }

    private boolean isBuyItemPending(){
        GrandExchange ge = script.getGrandExchange();
        for (GrandExchange.Box box : GrandExchange.Box.values())
            if(ge.getItemId(box) == buy.getPrimaryItemID()){
                return ge.getStatus(box) == GrandExchange.Status.COMPLETING_BUY ||
                        ge.getStatus(box) == GrandExchange.Status.FINISHED_BUY;
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
                if(bank.getAmount(995) > 0){
                    return bank.withdraw(995, Bank.WITHDRAW_ALL);
                }
                return true;
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
