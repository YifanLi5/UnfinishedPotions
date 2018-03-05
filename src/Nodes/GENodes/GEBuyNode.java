package Nodes.GENodes;

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


public class GEBuyNode implements ExecutableNode {

    private Script hostScriptRefence;
    private static ExecutableNode singleton;
    private HerbEnum cleanHerb;

    private static final int COINS = 995;

    private GEBuyNode(Script hostScriptRefence, HerbEnum cleanHerb) {
        this.hostScriptRefence = hostScriptRefence;
        this.cleanHerb = cleanHerb;
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
        GrandExchangeOperations operations = new GrandExchangeOperations(hostScriptRefence);
        int[] margin = operations.priceCheckItem(cleanHerb.getItemID(), cleanHerb.getGeSearchTerm(), cleanHerb.getEstimatedHighPrice());
        int estimatedBuyAmt = findEstimatedBuyableQuantity(margin[0]);
        int half = estimatedBuyAmt / 2;
        //buy about half rounded down to nearest 100 at the high price
        int buyAtHighPrice = half - half % 100;

        GrandExchangeOffer highOffer = operations.buyItem(cleanHerb.getItemID(), cleanHerb.)

        return 0;
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

}
