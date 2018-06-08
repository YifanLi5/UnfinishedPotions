package Nodes.GENodes;

import GrandExchangeUtil.GrandExchangeObserver;
import GrandExchangeUtil.GrandExchangeOperations;
import GrandExchangeUtil.GrandExchangePolling;
import ScriptClasses.MarkovNodeExecutor;
import Util.ComponentsEnum;
import Util.Statics;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.Tabs;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.util.Timer;
import java.util.TimerTask;

public class GESellNode implements MarkovNodeExecutor.ExecutableNode, GrandExchangeObserver {
    private Script script;
    private ComponentsEnum sell;
    private GrandExchangeOperations operations;
    private GrandExchangePolling polling;
    private boolean offerUpdated, offerFinished, doPreventIdleAction = true;

    public GESellNode(Script script, ComponentsEnum sell) {
        this.script = script;
        this.sell = sell;
        this.operations = new GrandExchangeOperations();
        this.polling = GrandExchangePolling.getInstance(script);
        operations.exchangeContext(script.bot);
    }

    @Override
    public void onGEUpdate(GrandExchange.Box box) {
        GrandExchange ge = script.getGrandExchange();
        if(ge.getStatus(box) == GrandExchange.Status.FINISHED_BUY && ge.getItemId(box) == sell.getFinishedItemID()){
            offerFinished = true;
        }
        offerUpdated = true;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        NPC clerk = script.getNpcs().closest("Grand Exchange Clerk");
        return clerk != null && clerk.exists();
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(isSellItemPending() || operations.sellItem(sell.getFinishedItemID())){
            polling.registerObserver(this);
            while(!offerFinished)
                preventIdleLogout();
            boolean successfulCollect = false;
            int attempts = 0;
            while(!successfulCollect && attempts < 5){
                successfulCollect = operations.collectAll();
                attempts++;
                MethodProvider.sleep(1000);
            }
            polling.removeObserver(this);
        }
        return 0;
    }

    private boolean isSellItemPending(){
        GrandExchange ge = script.getGrandExchange();
        for (GrandExchange.Box box : GrandExchange.Box.values())
            if(ge.getItemId(box) == sell.getFinishedItemID()){
                return ge.getStatus(box) == GrandExchange.Status.COMPLETING_SALE ||
                        ge.getStatus(box) == GrandExchange.Status.FINISHED_SALE;
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

    public void stopThread(){
        if(polling != null)
            polling.stopQueryingOffers();
    }
}
