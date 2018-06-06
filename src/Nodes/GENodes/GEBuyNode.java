package Nodes.GENodes;

import GrandExchangeUtil.GrandExchangeObserver;
import GrandExchangeUtil.GrandExchangeOperations;
import GrandExchangeUtil.GrandExchangePolling;
import Util.HerbAndPotionsEnum;
import ScriptClasses.MarkovNodeExecutor;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;


public class GEBuyNode implements MarkovNodeExecutor.ExecutableNode, GrandExchangeObserver {

    private final Script script;
    private GrandExchangeOperations operations;
    private HerbAndPotionsEnum buy = HerbAndPotionsEnum.VIAL_OF_WATER;
    private GrandExchangePolling polling;

    private boolean offerUpdated, offerFinished;

    public GEBuyNode(Script script){
        operations = new GrandExchangeOperations();
        polling = new GrandExchangePolling(script);
        operations.exchangeContext(script.bot);
        this.script = script;
    }

    @Override
    public void onGEUpdate(GrandExchange.Box box) {
        GrandExchange ge = script.getGrandExchange();
        if(ge.getStatus(box) == GrandExchange.Status.FINISHED_BUY && ge.getItemId(box) == buy.getHerbItemID()){
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
        if(isBuyItemPending() || operations.buyItem(buy.getHerbItemID(), buy.getItemName(), 1)){

            polling.registerObserver(this);

            if(offerUpdated){
                boolean successfulCollect = false;
                int attempts = 0;
                while(!successfulCollect && attempts < 5){
                    successfulCollect = operations.collectAll();
                    attempts++;
                    MethodProvider.sleep(1000);
                }

            }
            if(offerFinished)
                polling.removeObserver(this);
        }
        return 1000;
    }

    private boolean isBuyItemPending(){
        GrandExchange ge = script.getGrandExchange();
        for (GrandExchange.Box box : GrandExchange.Box.values())
            return ge.getItemId(box) == buy.getHerbItemID() &&
                    (ge.getStatus(box) == GrandExchange.Status.COMPLETING_BUY ||
                            ge.getStatus(box) == GrandExchange.Status.FINISHED_BUY);
        return false;
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
