package GrandExchange_Util;

import ScriptClasses.Statics;
import org.osbot.Con;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.Inventory;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.input.mouse.WidgetDestination;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class GrandExchangeHandler {

    /*
    widget ids for GE boxes
    [0] = root, [1] = child+, [2] = child++ for buy item, [3] child++ for sell item
     */
    private static final int[] GE_BOX1 = {465, 7, 26, 27};
    private static final int[] GE_BOX2 = {465, 8, 26, 27};
    private static final int[] GE_BOX3 = {465, 9, 26, 27};
    private static final int[] GE_BOX4 = {465, 10, 26, 27};
    private static final int[] GE_BOX5 = {465, 11, 26, 27};
    private static final int[] GE_BOX6 = {465, 12, 26, 27};
    private static final int[] GE_BOX7 = {465, 13, 26, 27};
    private static final int[] GE_BOX8 = {465, 14, 26, 27};

    private static final int[] NUMBER_ENTRY = {162, 35};
    private static final int[] GE_ITEM_ENTRY = {162, 36};
    private static final int[] GE_ITEM_SELECTION = {162, 41};
    private static final int[] SET_ITEM_QUANTITY = {465, 24, 7};
    private static final int[] SET_ITEM_PRICE = {465, 24, 12};
    private static final int[] GE_CONFIRM = {465, 27};
    private static final int[] ITEM_QUANTITY = {465, 24, 32};
    private static final int[] ITEM_PRICE = {465, 24, 39};

    private Script hostScriptReference;

    public GrandExchangeHandler(Script hostScriptReference) {
        this.hostScriptReference = hostScriptReference;
    }

    public GrandExchangeOffer buyItem(int itemID, String searchTerm, int quantity, int price) throws InterruptedException {
        hostScriptReference.getWidgets().closeOpenInterface();
        GrandExchange ge = hostScriptReference.getGrandExchange();
        if(openGE()){
            //interact with next available box
            GrandExchange.Box box = findFreeGEBox();
            RS2Widget geBoxWidget = getGEBoxWidget(true, box);
            if(geBoxWidget != null && geBoxWidget.isVisible()){
                WidgetDestination destination = new WidgetDestination(hostScriptReference.getBot(), geBoxWidget);
                if(hostScriptReference.getMouse().click(destination)){
                    //wait until buy screen opens
                    new ConditionalSleep(1500){
                        @Override
                        public boolean condition() throws InterruptedException {
                            return ge.isBuyOfferOpen() && isGEItemEntryOpen();
                        }
                    }.sleep();

                    //enter in search term
                    hostScriptReference.getKeyboard().typeString(searchTerm, true);
                    MethodProvider.sleep(500);

                    //get all queried items off of search term and find correct item to buy
                    RS2Widget geQueriedItemWidgets = hostScriptReference.getWidgets().get(GE_ITEM_SELECTION[0], GE_ITEM_SELECTION[1]);
                    if(geQueriedItemWidgets != null && geQueriedItemWidgets.isVisible()){
                        int[] geItemSelectionIDs = geQueriedItemWidgets.getInv();
                        int thirdLvlID = -1;
                        for(int i = 0; i < geItemSelectionIDs.length; i++){
                            if(geItemSelectionIDs[i] == itemID){
                                thirdLvlID = i;
                                break;
                            }
                        }
                        //select the correct item to buy
                        if(thirdLvlID != -1){
                            RS2Widget targetItemWidget = hostScriptReference.getWidgets().get(GE_ITEM_SELECTION[0], GE_ITEM_SELECTION[1], thirdLvlID);
                            if(targetItemWidget != null && targetItemWidget.isVisible()){
                                destination = new WidgetDestination(hostScriptReference.getBot(), targetItemWidget);
                                hostScriptReference.getMouse().click(destination);
                                MethodProvider.sleep(500);

                                if(setItemQuantity(quantity) && setItemPrice(price)){
                                    if(confirmOffer()){
                                        return new GrandExchangeOffer(true, box, itemID, quantity);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else{
            hostScriptReference.log("no available box");
        }

        return null;
    }

    public GrandExchangeOffer sellItem(int itemID, int quantity, int price) throws InterruptedException {
        hostScriptReference.getWidgets().closeOpenInterface();
        GrandExchange ge = hostScriptReference.getGrandExchange();
        if(openGE()){
            //find a free box to use
            GrandExchange.Box box = findFreeGEBox();
            RS2Widget geBoxWidget = getGEBoxWidget(false, box);
            //interact with it
            if(geBoxWidget != null && geBoxWidget.isVisible()){
                WidgetDestination destination = new WidgetDestination(hostScriptReference.getBot(), geBoxWidget);
                if(hostScriptReference.getMouse().click(destination)){
                    //wait until sell screen opens
                    new ConditionalSleep(1000){
                        @Override
                        public boolean condition() throws InterruptedException {
                            return ge.isSellOfferOpen();
                        }
                    }.sleep();

                    Inventory inv = hostScriptReference.getInventory();
                    if(inv.interact("Offer", itemID)){
                        MethodProvider.sleep(750);
                        if(setItemQuantity(quantity) && setItemPrice(price)){
                            if(confirmOffer()){
                                return new GrandExchangeOffer(true, box, itemID, quantity);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean openGE() throws InterruptedException {
        hostScriptReference.log("opening GE");
        GrandExchange ge = hostScriptReference.getGrandExchange();
        if(!ge.isOpen()){
            NPC grandExchangeClerk = hostScriptReference.getNpcs().closest("Grand Exchange Clerk");
            if(grandExchangeClerk != null){
                boolean didInteraction = grandExchangeClerk.interact("Exchange");
                final boolean[] isOpen = {false};
                new ConditionalSleep(1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        isOpen[0] = didInteraction && ge.isOpen();
                        return isOpen[0];
                    }
                }.sleep();
                return isOpen[0];
            }
        }
        return true;
    }

    private RS2Widget getGEBoxWidget(boolean buying, GrandExchange.Box box){
        int thirdLvlID;
        if(buying){
            thirdLvlID = GE_BOX1[2];
        } else{
            thirdLvlID = GE_BOX1[3];
        }
        RS2Widget geBoxWidget = null;

        switch(box){
            case BOX_1:
                geBoxWidget = hostScriptReference.getWidgets().get(GE_BOX1[0], GE_BOX1[1], thirdLvlID);
                break;
            case BOX_2:
                geBoxWidget = hostScriptReference.getWidgets().get(GE_BOX2[0], GE_BOX2[1], thirdLvlID);
                break;
            case BOX_3:
                geBoxWidget = hostScriptReference.getWidgets().get(GE_BOX3[0], GE_BOX3[1], thirdLvlID);
                break;
            case BOX_4:
                geBoxWidget = hostScriptReference.getWidgets().get(GE_BOX4[0], GE_BOX4[1], thirdLvlID);
                break;
            case BOX_5:
                geBoxWidget = hostScriptReference.getWidgets().get(GE_BOX5[0], GE_BOX5[1], thirdLvlID);
                break;
            case BOX_6:
                geBoxWidget = hostScriptReference.getWidgets().get(GE_BOX6[0], GE_BOX6[1], thirdLvlID);
                break;
            case BOX_7:
                geBoxWidget = hostScriptReference.getWidgets().get(GE_BOX7[0], GE_BOX7[1], thirdLvlID);
                break;
            case BOX_8:
                geBoxWidget = hostScriptReference.getWidgets().get(GE_BOX8[0], GE_BOX8[1], thirdLvlID);
                break;

        }
        return geBoxWidget;
    }

    private boolean setItemQuantity(int quantity) throws InterruptedException {
        RS2Widget quantitySelection = hostScriptReference.getWidgets().get(SET_ITEM_QUANTITY[0], SET_ITEM_QUANTITY[1], SET_ITEM_QUANTITY[2]);
        if(quantitySelection != null && quantitySelection.isVisible()){
            if(quantitySelection.interact("Enter quantity")){
                new ConditionalSleep(1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return isNumberEntryOpen();
                    }
                }.sleep();
                hostScriptReference.getKeyboard().typeString(String.valueOf(quantity));
                MethodProvider.sleep(500);
                return true;
            }
        }

        return false;
    }

    private boolean setItemPrice(int price) throws InterruptedException {
        RS2Widget priceSelection = hostScriptReference.getWidgets().get(SET_ITEM_PRICE[0], SET_ITEM_PRICE[1], SET_ITEM_PRICE[2]);
        if(priceSelection != null && priceSelection.isVisible())
            if(priceSelection.interact("Enter price")){
                new ConditionalSleep(1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return isNumberEntryOpen();
                    }
                }.sleep();
                hostScriptReference.getKeyboard().typeString(String.valueOf(price));
                MethodProvider.sleep(500);
                return true;
            }
        return false;
    }

    private boolean confirmOffer(){
        RS2Widget confirm = hostScriptReference.getWidgets().get(GE_CONFIRM[0], GE_CONFIRM[1]);
        if(confirm != null && confirm.isVisible()){
            return confirm.interact("Confirm");
        }
        return false;
    }

    private boolean isNumberEntryOpen(){
        RS2Widget numberEntry = hostScriptReference.getWidgets().get(NUMBER_ENTRY[0], NUMBER_ENTRY[1]);
        return numberEntry.isVisible();
    }

    private boolean isGEItemEntryOpen(){
        RS2Widget itemEntry = hostScriptReference.getWidgets().get(GE_ITEM_ENTRY[0], GE_ITEM_ENTRY[1]);
        return itemEntry.isVisible();
    }

    private GrandExchange.Box findFreeGEBox(){
        GrandExchange ge = hostScriptReference.getGrandExchange();
        for (GrandExchange.Box box : GrandExchange.Box.values()) {
            if(ge.getStatus(box) == GrandExchange.Status.EMPTY){
                return box;
            }
        }
        return null; //indicates no boxes are empty
    }
}
