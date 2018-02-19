package Nodes;

import ScriptClasses.Statics;
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
    private int itemID;
    private String searchTerm;

    private static final int[] GE_BOX1_WIDGET_ID = {465, 7};
    private static final int[] GE_BOX2_WIDGET_ID = {465, 8};
    private static final int[] GE_BOX3_WIDGET_ID = {465, 9};
    private static final int[] GE_BOX4_WIDGET_ID = {465, 10};
    private static final int[] GE_BOX5_WIDGET_ID = {465, 11};
    private static final int[] GE_BOX6_WIDGET_ID = {465, 12};
    private static final int[] GE_BOX7_WIDGET_ID = {465, 13};
    private static final int[] GE_BOX8_WIDGET_ID = {465, 14};

    private static final int[] RSGP_COLLECTION_WIDGET_ID = {465, 23, 2};


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
        hostScriptRefence.log("calling priceCheckItem");
        int[] margin = priceCheckItem();
        if(margin != null){
            hostScriptRefence.log("buy: " + margin[0] + " sell: " + margin[1]);
        }

        return 1000;
    }

    private int[] priceCheckItem() throws InterruptedException {
        boolean openedGe = openGE();
        hostScriptRefence.log("opened GE: " + openedGe);
        if(openedGe) {
            boolean boughtItem = buyItem();
            hostScriptRefence.log("bought item: " + boughtItem);
            if(boughtItem) {
                int instantBuyPrice = findInstaBuyPrice();
                hostScriptRefence.log("insta-buy: " + instantBuyPrice);
                boolean collected = collect();
                hostScriptRefence.log("collected: " + collected);
                if(collected){
                    boolean sellItem = sellItem();
                    hostScriptRefence.log("sold item: " + sellItem);
                    if(sellItem){
                        int instantSellPrice = findInstaSellPrice();
                        hostScriptRefence.log("sell price: " + instantSellPrice);
                        if(instantBuyPrice != -1 && instantSellPrice != -1){
                            return new int[]{instantBuyPrice, instantSellPrice};
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean openGE() throws InterruptedException {
        hostScriptRefence.log("opening GE");
        GrandExchange ge = hostScriptRefence.getGrandExchange();
        if(!ge.isOpen()){
            NPC grandExchangeClerk = hostScriptRefence.getNpcs().closest("Grand Exchange Clerk");
            if(grandExchangeClerk != null){
                boolean didInteraction = grandExchangeClerk.interact("Exchange");
                MethodProvider.sleep(Statics.randomNormalDist(300,75));
                return didInteraction;
            }
        }
        return true;
    }

    private boolean buyItem() throws InterruptedException {
        hostScriptRefence.log("buy item");
        GrandExchange ge = hostScriptRefence.getGrandExchange();
        if(ge.isOpen()){
            boolean didInteraction = ge.buyItem(itemID, searchTerm, 5000, 1);
            MethodProvider.sleep(Statics.randomNormalDist(300,75));
            return didInteraction;
        }
        return false;
    }

    private int findInstaBuyPrice() throws InterruptedException {
        hostScriptRefence.log("find insta buy price");
        GrandExchange ge = hostScriptRefence.getGrandExchange();
        GrandExchange.Box interactedBox = getInteractedGEBox();
        hostScriptRefence.log("target box: " + interactedBox);
        if(interactedBox != null){
            GrandExchange.Box finalInteractedBox = interactedBox; //need to do this for conditional sleep
            new ConditionalSleep(60000){
                @Override
                public boolean condition() throws InterruptedException {
                    return ge.getStatus(finalInteractedBox) == GrandExchange.Status.FINISHED_BUY;
                }
            }.sleep();
            return ge.getAmountSpent(interactedBox);
        }
        return -1;

    }

    private boolean collect() {
        hostScriptRefence.log("collecting");
        GrandExchange ge = hostScriptRefence.getGrandExchange();
        boolean collected = ge.collect();
        if(collected) {
            Inventory inv = hostScriptRefence.getInventory();
            new ConditionalSleep(1000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return !ge.isSellOfferOpen() && inv.contains(itemID);
                }
            }.sleep();
        }
        return collected;

    }

    private boolean sellItem() throws InterruptedException {
        GrandExchange ge = hostScriptRefence.getGrandExchange();
        if(ge.isOpen()){
            hostScriptRefence.log("selling item");
            boolean sellOfferUp = ge.sellItem(itemID, 1, 1);
            MethodProvider.sleep(1000);
            return sellOfferUp;
        }
        return false;
    }

    private int findInstaSellPrice() throws InterruptedException {
        hostScriptRefence.log("finding sell price");
        GrandExchange ge = hostScriptRefence.getGrandExchange();
        GrandExchange.Box interactedBox = getInteractedGEBox();
        hostScriptRefence.log("target box: " + interactedBox);
        if(interactedBox != null) {
            new ConditionalSleep(60000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return ge.getStatus(interactedBox) == GrandExchange.Status.FINISHED_SALE;
                }
            }.sleep();
            return getSellPrice(interactedBox);
        }
        return -1;
    }

    private GrandExchange.Box getInteractedGEBox() throws InterruptedException {
        GrandExchange ge = hostScriptRefence.getGrandExchange();
        int attempts = 0;
        while(attempts < 5){
            for (GrandExchange.Box box : GrandExchange.Box.values()) {
                if (ge.getItemId(box) == itemID)
                    return box;
            }
            attempts++;
            MethodProvider.sleep(1000);
        }


        return null;
    }

    private int getSellPrice(GrandExchange.Box box) throws InterruptedException {
        RS2Widget geBox = null;
        switch(box){
            case BOX_1:
                geBox = hostScriptRefence.getWidgets().get(GE_BOX1_WIDGET_ID[0], GE_BOX1_WIDGET_ID[1]);
                break;
            case BOX_2:
                geBox = hostScriptRefence.getWidgets().get(GE_BOX2_WIDGET_ID[0], GE_BOX2_WIDGET_ID[1]);
                break;
            case BOX_3:
                geBox = hostScriptRefence.getWidgets().get(GE_BOX3_WIDGET_ID[0], GE_BOX3_WIDGET_ID[1]);
                break;
            case BOX_4:
                geBox = hostScriptRefence.getWidgets().get(GE_BOX4_WIDGET_ID[0], GE_BOX4_WIDGET_ID[1]);
                break;
            case BOX_5:
                geBox = hostScriptRefence.getWidgets().get(GE_BOX5_WIDGET_ID[0], GE_BOX5_WIDGET_ID[1]);
                break;
            case BOX_6:
                geBox = hostScriptRefence.getWidgets().get(GE_BOX6_WIDGET_ID[0], GE_BOX6_WIDGET_ID[1]);
                break;
            case BOX_7:
                geBox = hostScriptRefence.getWidgets().get(GE_BOX7_WIDGET_ID[0], GE_BOX7_WIDGET_ID[1]);
                break;
            case BOX_8:
                geBox = hostScriptRefence.getWidgets().get(GE_BOX8_WIDGET_ID[0], GE_BOX8_WIDGET_ID[1]);
                break;
        }
        if(geBox != null && geBox.isVisible()){
            WidgetDestination destination = new WidgetDestination(hostScriptRefence.getBot(), geBox);
            if(hostScriptRefence.getMouse().click(destination)){
                //should now be in interface that shows the status for a specific item
                Widgets widgets = hostScriptRefence.getWidgets();
                if(widgets.isVisible(RSGP_COLLECTION_WIDGET_ID[0], RSGP_COLLECTION_WIDGET_ID[1], RSGP_COLLECTION_WIDGET_ID[2])){
                    RS2Widget rsgpCollection = widgets.get(RSGP_COLLECTION_WIDGET_ID[0], RSGP_COLLECTION_WIDGET_ID[1], RSGP_COLLECTION_WIDGET_ID[2]);
                    if(rsgpCollection != null){
                        int sellPrice = rsgpCollection.getItemAmount();
                        rsgpCollection.interact("Collect");
                        //collecting gp return us to the main ge interface
                        MethodProvider.sleep(Statics.randomNormalDist(300,75));
                        return sellPrice;
                    }
                }

            }

        }
        return -1;

    }
}
