package Nodes;

import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;


public class GEBuyNode implements ExecutableNode {

    private Script hostScriptRefence;
    private static ExecutableNode singleton;
    private int itemID;
    private String searchTerm;

    private GEBuyNode(Script hostScriptRefence, int itemID, String searchTerm) {
        this.hostScriptRefence = hostScriptRefence;
        this.itemID = itemID;
        this.searchTerm = searchTerm;
    }


    public static ExecutableNode getInstance(Script hostScriptRefence, int itemID, String searchTerm){
        if(hostScriptRefence != null){
            if(singleton == null){
                singleton = new GEBuyNode(hostScriptRefence, itemID, searchTerm);
            }
            return singleton;
        }
        hostScriptRefence.stop();
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
        priceCheckItem();
        return 1000;
    }

    private boolean openGE(){
        NPC geNPC = hostScriptRefence.getNpcs().closest("Grand Exchange Clerk");
        if(geNPC != null){
            return geNPC.interact("Exchange");
        }
        return false;

    }

    private int priceCheckItem() throws InterruptedException {
        GrandExchange ge = hostScriptRefence.getGrandExchange();
        if(!ge.isOpen()) {
            openGE();

        }
        new ConditionalSleep(1000){
            @Override
            public boolean condition() throws InterruptedException {
                return ge.isOpen();
            }
        }.sleep();
        ge.buyItem(itemID, searchTerm, 5000, 1);
        new ConditionalSleep(1000){
            @Override
            public boolean condition() throws InterruptedException {
                return !ge.isBuyOfferOpen();
            }
        }.sleep();
        GrandExchange.Box interactedBox = getInteractedGEBox();
        if(interactedBox != null){
            GrandExchange.Box finalInteractedBox = interactedBox; //need to do this for conditional sleep
            new ConditionalSleep(60000){
                @Override
                public boolean condition() throws InterruptedException {
                    return ge.getStatus(finalInteractedBox) == GrandExchange.Status.FINISHED_BUY;
                }
            }.sleep();

            int instantBuyPrice = ge.getAmountSpent(interactedBox);
            hostScriptRefence.log("insta-buy: " + instantBuyPrice);
            if(ge.collect()){
                Inventory inv = hostScriptRefence.getInventory();
                new ConditionalSleep(1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return !ge.isSellOfferOpen() && inv.contains(itemID);
                    }
                }.sleep();
                if(ge.sellItem(itemID, 1, 1)){
                    MethodProvider.sleep(500);
                    interactedBox = getInteractedGEBox();
                    if(interactedBox != null){
                        GrandExchange.Box finalInteractedBox1 = interactedBox;
                        new ConditionalSleep(60000){
                            @Override
                            public boolean condition() throws InterruptedException {
                                return ge.getStatus(finalInteractedBox1) == GrandExchange.Status.FINISHED_SALE;
                            }
                        }.sleep();
                        int instantSellPrice = ge.getAmountSpent(interactedBox);
                        hostScriptRefence.log("insta-sell: " + instantSellPrice);
                        return 3000;
                    }
                }
            }
        }

        return -1;
    }

    private GrandExchange.Box getInteractedGEBox(){
        GrandExchange ge = hostScriptRefence.getGrandExchange();
        for (GrandExchange.Box box : GrandExchange.Box.values()) {
            if (ge.getItemId(box) == itemID)
                return box;
        }
        return null;
    }
}
