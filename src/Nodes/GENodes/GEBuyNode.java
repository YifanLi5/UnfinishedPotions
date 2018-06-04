package Nodes.GENodes;

import GrandExchange.GrandExchangeObserver;
import GrandExchange.GrandExchangeOperations;
import GrandExchange.GrandExchangePolling;
import Util.HerbEnum;
import ScriptClasses.MarkovNodeExecutor;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.awt.geom.Area;


public class GEBuyNode implements MarkovNodeExecutor.ExecutableNode, GrandExchangeObserver {

    private final Script script;
    private GrandExchangeOperations operations;
    private HerbEnum buy = HerbEnum.VIAL_OF_WATER;
    private GrandExchangePolling polling;
    private static MarkovNodeExecutor.ExecutableNode singleton;
    private static final Rectangle GE_AREA = new Rectangle(3159, 3494, 10, 10);

    private boolean offerUpdated, offerFinished;

    private GEBuyNode(Script script){
        operations = new GrandExchangeOperations();
        polling = new GrandExchangePolling();
        operations.exchangeContext(script.bot);
        polling.exchangeContext(script.bot);
        this.script = script;
    }

    public static MarkovNodeExecutor.ExecutableNode getInstance(Script script){
        if(singleton == null){
            singleton = new GEBuyNode(script);
        }
        return singleton;
    }


    @Override
    public void onGEUpdate(GrandExchange.Box box) {
        GrandExchange ge = script.getGrandExchange();
        if(ge.getStatus(box) == GrandExchange.Status.FINISHED_BUY && ge.getItemId(box) == buy.getItemID()){
            offerFinished = true;
        }
        offerUpdated = true;
    }

    @Override
    public boolean canExecute() throws InterruptedException {
        Position pos = script.myPlayer().getPosition();
        return GE_AREA.contains(pos.getX(), pos.getY());
    }

    @Override
    public int executeNode() throws InterruptedException {
        if(isBuyItemPending() || operations.buyItem(buy.getItemID(), buy.getItemName(), 1)){

            polling.registerObserver(this);
            polling.startQueryingOffers();

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
                polling.stopQueryingOffers();
        }
        return 1000;
    }

    private boolean isBuyItemPending(){
        for (GrandExchange.Box box : GrandExchange.Box.values())
            return script.getGrandExchange().getItemId(box) == buy.getItemID();
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
