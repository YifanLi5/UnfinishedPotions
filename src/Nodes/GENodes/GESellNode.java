package Nodes.GENodes;

import GrandExchangeUtil.GrandExchangeObserver;
import GrandExchangeUtil.GrandExchangeOperations;
import GrandExchangeUtil.GrandExchangePolling;
import ScriptClasses.MarkovNodeExecutor;
import Util.HerbAndPotionsEnum;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

public class GESellNode implements MarkovNodeExecutor.ExecutableNode, GrandExchangeObserver {
    private Script script;
    private HerbAndPotionsEnum sell = HerbAndPotionsEnum.VIAL_OF_WATER;
    private GrandExchangeOperations operations;
    private GrandExchangePolling polling;
    private boolean offerUpdated, offerFinished;

    public GESellNode(Script script) {
        this.script = script;
        this.operations = new GrandExchangeOperations();
        this.polling = new GrandExchangePolling(script);

        operations.exchangeContext(script.bot);
    }

    @Override
    public void onGEUpdate(GrandExchange.Box box) {
        GrandExchange ge = script.getGrandExchange();
        if(ge.getStatus(box) == GrandExchange.Status.FINISHED_BUY && ge.getItemId(box) == sell.getHerbItemID()){
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
        if(isSellItemPending() || operations.sellItem(sell.getHerbItemID())){
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
        return 0;
    }

    private boolean isSellItemPending(){
        GrandExchange ge = script.getGrandExchange();
        for (GrandExchange.Box box : GrandExchange.Box.values())
            return ge.getItemId(box) == sell.getHerbItemID() &&
                    (ge.getStatus(box) == GrandExchange.Status.COMPLETING_SALE ||
                            ge.getStatus(box) == GrandExchange.Status.FINISHED_SALE);
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
