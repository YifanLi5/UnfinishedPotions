package Nodes.GENodes;

import GrandExchange_Util.GrandExchangeObserver;
import GrandExchange_Util.GrandExchangeOffer;
import GrandExchange_Util.GrandExchangeOperations;
import Nodes.ExecutableNode;
import ScriptClasses.HerbEnum;
import ScriptClasses.Statics;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.Widgets;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.input.mouse.WidgetDestination;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;


public class GEBuyNode implements ExecutableNode, GrandExchangeObserver.GrandExchangeListener {

    private Script hostScriptRefence;
    private static ExecutableNode singleton;
    private HerbEnum cleanHerb;

    private static final int COINS = 995;

    private GrandExchangeOffer highOffer;
    private boolean collectFromHigh = false;
    private GrandExchangeOffer lowOffer;
    private boolean collectFromLow = false;
    private GrandExchangeOperations operations;
    private GrandExchangeObserver observer;

    private GEBuyNode(Script hostScriptRefence, HerbEnum cleanHerb) {
        this.hostScriptRefence = hostScriptRefence;
        this.cleanHerb = cleanHerb;
        this.operations = new GrandExchangeOperations(hostScriptRefence);
        this.observer = new GrandExchangeObserver(hostScriptRefence);
    }


    public static ExecutableNode getInstance(Script hostScriptRefence, HerbEnum cleanHerb){
        if(hostScriptRefence != null){
            if(singleton == null){
                singleton = new GEBuyNode(hostScriptRefence, cleanHerb);
            }
            return singleton;
        }
        throw new IllegalStateException("script reference is null");
    }


    public static ExecutableNode getInstance(){
        if(singleton != null){
            return singleton;
        }
        throw new IllegalStateException("singleton is null, call the other overloaded getInstance method first");
    }

    @Override
    public int executeNodeAction() throws InterruptedException {
        int[] margin = operations.priceCheckItem(cleanHerb.getItemID(), cleanHerb.getGeSearchTerm(), cleanHerb.getEstimatedHighPrice());
        int estimatedBuyAmt = findEstimatedBuyableQuantity(margin[0]);
        int aboutHalf = (estimatedBuyAmt / 2) - ((estimatedBuyAmt / 2) % 100);

        hostScriptRefence.log("(high offer) buying " + aboutHalf + " " + cleanHerb.getItemName() + " at " + margin[0]);
        highOffer = operations.buyItem(cleanHerb.getItemID(), cleanHerb.getGeSearchTerm(), aboutHalf, margin[0]);
        observer.addGEListenerForOffer(this, highOffer);

        hostScriptRefence.log("(low offer) buying " + aboutHalf + " " + cleanHerb.getItemName() + " at " + margin[0]);
        lowOffer = operations.buyItem(cleanHerb.getItemID(), cleanHerb.getGeSearchTerm(), aboutHalf, margin[1]);
        observer.addGEListenerForOffer(this, lowOffer);

        new ConditionalSleep(60000, 1000){
            @Override
            public boolean condition() throws InterruptedException {
                hostScriptRefence.log("awaiting GE update");
                return collectFromHigh || collectFromLow;
            }
        }.sleep();

        if(collectFromHigh){
            operations.collectFromBox(highOffer.getSelectedBox());
            if(highOffer.isOfferFinished()){
                observer.removeGEOffer(highOffer);
            }
        }
        if(collectFromLow){
            operations.collectFromBox(lowOffer.getSelectedBox());
            if(lowOffer.isOfferFinished()){
                observer.removeGEOffer(lowOffer);
            }
        }

        return 1000;
    }

    private int findEstimatedBuyableQuantity(int highPrice){
        hostScriptRefence.getWidgets().closeOpenInterface();
        int cashStack = (int) hostScriptRefence.getInventory().getAmount(COINS);
        return highPrice / cashStack;
    }

    //not used in this node
    @Override
    public int getDefaultEdgeWeight() {
        return 0;
    }

    @Override
    public void onGEUpdate(GrandExchangeOffer offer) {
        if(offer == highOffer){
            hostScriptRefence.log("high offer updated");
            collectFromHigh = true;
        }
        else if(offer == lowOffer){
            hostScriptRefence.log("low offer updated");
            collectFromLow = true;
        }
    }
}
