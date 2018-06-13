package GrandExchangeUtil;

import Util.Statics;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.input.mouse.WidgetDestination;
import org.osbot.rs07.script.API;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.util.List;

public class GrandExchangeOperations extends API{

    @Override
    public void initializeModule() {

    }

    private static final int ROOT_ID = 465, SELL_SPRITE_ID = 1106, BUY_SPRITE_ID = 1108;

    private static final Point BOX1_BUY_PT = new Point(40,131)
            , BOX2_BUY_PT = new Point(157,131)
            , BOX3_BUY_PT = new Point(274,131)
            , BOX4_BUY_PT = new Point(391, 131)
            , BOX5_BUY_PT = new Point(40, 251)
            , BOX6_BUY_PT = new Point(157, 251)
            , BOX7_BUY_PT = new Point(274, 251)
            , BOX8_BUY_PT = new Point(391, 251);

    private static final Point BOX1_SELL_PT = new Point(93,131)
            , BOX2_SELL_PT = new Point(210,131)
            , BOX3_SELL_PT = new Point(327,131)
            , BOX4_SELL_PT = new Point(444, 131)
            , BOX5_SELL_PT = new Point(93, 251)
            , BOX6_SELL_PT = new Point(210, 251)
            , BOX7_SELL_PT = new Point(327, 251)
            , BOX8_SELL_PT = new Point(444, 251);

    public enum BuyPriceModifier {
        LOW_PRICE_X_101(1.01), LOW_PRICE_X_102(1.02), LOW_PRICE_X_103(1.03), DEFAULT_PRICE_X_105(1.05);

        private double modifier;
        BuyPriceModifier(double modifier) {
            this.modifier = modifier;
        }
    }

    @Override
    public MethodProvider exchangeContext(Bot iIiiiiiiIiii) {
        return super.exchangeContext(iIiiiiiiIiii);
    }

    public boolean buyItem(int itemID, String searchTerm, int quantity) throws InterruptedException {
        getWidgets().closeOpenInterface();
        if(openGE()){
            GrandExchange.Box box = findFreeGEBox();
            int[] margin = priceCheckItem(itemID, searchTerm);
            if(margin[0] < 0 || margin[1] < 0){
                margin[0] = 2800;
                margin[1] = 2800;
            }
            if(interactBuyOption(box))
                if(searchAndSelectItem(searchTerm, itemID))
                    if(setPrice((margin[0] + margin[1])/2))
                        if(setBuyQuantity(quantity))
                            return confirmOffer();
        }

        return false;
    }

    public boolean sellItem(int itemID) throws InterruptedException {
        getWidgets().closeOpenInterface();
        if(openGE()){
            if(offerItem(itemID)){
                if(set5PercentLower()){
                    return confirmOffer();
                }
            }
        }
        return false;
    }

    public boolean collectAll(){
        if(openGE()){
            List<RS2Widget> list = getWidgets().containingText(ROOT_ID, "Collect");
            if(list != null && list.size() > 0){
                RS2Widget collect = list.get(0);
                if(collect.isVisible())
                    return collect.interact();
            }
        }
        return false;
    }

    public boolean abortOffersWithItem(String itemName) throws InterruptedException {
        if(openGE()){
            List<RS2Widget> pendingOffers = getWidgets().containingText(ROOT_ID, itemName);
            if(pendingOffers != null && pendingOffers.size() > 0){
                WidgetDestination offerDestination;
                for(RS2Widget offer: pendingOffers){
                    offerDestination = new WidgetDestination(bot, offer);
                    if(mouse.click(offerDestination,true)){
                        boolean open = new ConditionalSleep(500) {
                            @Override
                            public boolean condition() throws InterruptedException {
                                return menu.isOpen();
                            }
                        }.sleep();
                        if(open){
                            if(menu.selectAction("Abort offer")){
                                sleep(1000);
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean openGE() {
        GrandExchange ge = getGrandExchange();
        if(!ge.isOpen()){
            NPC grandExchangeClerk = getNpcs().closest("Grand Exchange Clerk");
            if(grandExchangeClerk != null){
                boolean didInteraction = grandExchangeClerk.interact("Exchange");
                return new ConditionalSleep(1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return didInteraction && ge.isOpen();
                    }
                }.sleep();
            }
        }
        return true;
    }

    public int[] priceCheckItem(int itemID, String searchTerm) throws InterruptedException {
        GrandExchange.Box box = findFreeGEBox();
        int[] result = {-1, -1};
        if(interactBuyOption(box))
            if(searchAndSelectItem(searchTerm, itemID)){
                if(set5PercentHigher() && set5PercentHigher()){
                    int coins = (int) inventory.getAmount(995);
                    if(confirmOffer()){
                        boolean offerComplete = new ConditionalSleep(60000){
                            @Override
                            public boolean condition() throws InterruptedException {
                                return grandExchange.getStatus(box) == GrandExchange.Status.FINISHED_BUY;
                            }
                        }.sleep();
                        if(offerComplete){
                            if(collectAll()){
                                sleep(1000);
                                int coinsAfter = (int) inventory.getAmount(995);
                                result[1] =  coins - coinsAfter;
                                if(offerItem(itemID)){
                                    if(set5PercentLower() && set5PercentLower()){
                                        if(confirmOffer()){
                                            offerComplete = new ConditionalSleep(60000){
                                                @Override
                                                public boolean condition() throws InterruptedException {
                                                    return grandExchange.getStatus(box) == GrandExchange.Status.FINISHED_SALE;
                                                }
                                            }.sleep();
                                            if(offerComplete){
                                                coins = (int) inventory.getAmount(995);
                                                if(collectAll()){
                                                    sleep(1000);
                                                    coinsAfter = (int) inventory.getAmount(995);
                                                    result[0] = coinsAfter - coins;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        return result;
    }

    //Buy Item helper methods
    private boolean interactBuyOption(GrandExchange.Box box){
        if(box == null){
            log("failed to find a box");
            getBot().stop();
        }
        RS2Widget geBoxWidget = getBuyWidgets(box);
        if(geBoxWidget != null && geBoxWidget.isVisible()){
            if(geBoxWidget.interact()){
                return new ConditionalSleep(1500){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return getGrandExchange().isBuyOfferOpen();
                    }
                }.sleep();
            }
        }
        return false;
    }

    private boolean searchAndSelectItem(String searchTerm, int itemID){
        final RS2Widget[] searchbar = new RS2Widget[1];
        boolean success = new ConditionalSleep(2000){
            @Override
            public boolean condition() throws InterruptedException {
                searchbar[0] = widgets.getWidgetContainingText(162, "What would you like to buy?");
                return searchbar[0] != null && searchbar[0].isVisible();
            }
        }.sleep();
        if(success){
            if(getKeyboard().typeString(searchTerm, true)){
                final RS2Widget[] foundItem = new RS2Widget[1];
                success = new ConditionalSleep(1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        foundItem[0] = getWidgets().singleFilter(162, rs2Widget -> rs2Widget.getItemId() == itemID);
                        return foundItem[0] != null && foundItem[0].isVisible();
                    }
                }.sleep();
                if(success){
                    if(foundItem[0].interact()){
                        success = new ConditionalSleep(1000){
                            @Override
                            public boolean condition() throws InterruptedException {
                                return isSelectedItemCorrect(itemID);
                            }
                        }.sleep();
                        return success;
                    }
                }
            }
        }
        return false;
    }

    private boolean setPrice(int[] margin, BuyPriceModifier modifier) throws InterruptedException {
        List<RS2Widget> list = widgets.containingActions(ROOT_ID, "Enter price");
        if(list != null && list.size() > 0){
            RS2Widget modifyPrice = list.get(0);
            if(modifyPrice.interact("Enter price")){
                boolean entryOpen = new ConditionalSleep(1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return isNumberEntryOpen("Set a price for each item");
                    }
                }.sleep();
                return entryOpen && keyboard.typeString(String.valueOf(margin[0] * modifier.modifier));

            }
        }
        return false;
    }

    private boolean setPrice(int price){
        List<RS2Widget> list = widgets.containingActions(ROOT_ID, "Enter price");
        if(list != null && list.size() > 0){
            RS2Widget modifyPrice = list.get(0);
            if(modifyPrice.interact("Enter price")){
                boolean entryOpen = new ConditionalSleep(1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return isNumberEntryOpen("Set a price for each item");
                    }
                }.sleep();
                return entryOpen && keyboard.typeString(String.valueOf(price));

            }
        }
        return false;
    }

    private boolean set5PercentHigher() throws InterruptedException {
        List<RS2Widget> incPrice = getWidgets().containingActions(ROOT_ID, "+5%");
        if(incPrice != null){
            return incPrice.size() > 0 && incPrice.get(0).interact("+5%");
        }
        return false;
    }

    private boolean setBuyQuantity(int quantity) throws InterruptedException {
        RS2Widget currentQuantityWidget = getWidgets().singleFilter(ROOT_ID,widget -> positionEquals(widget.getPosition(), new Point(39, 177)));
        if(currentQuantityWidget != null){
            int currentBuyQuantity = Integer.parseInt(currentQuantityWidget.getMessage());
            if(quantity == currentBuyQuantity)
                return true;
        }

        RS2Widget quantitySelection = getWidgets().getWidgetContainingText(ROOT_ID, "...");
        if(quantitySelection != null && quantitySelection.isVisible()){
            if(quantitySelection.interact()){
                boolean success = new ConditionalSleep(1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return isNumberEntryOpen("How many do you wish to buy?");
                    }
                }.sleep();
                if(success && getKeyboard().typeString(String.valueOf(quantity))){
                    //check If enough GP
                    Statics.longRandomNormalDelay();
                    RS2Widget totalPriceWidget = widgets.singleFilter(ROOT_ID, widget -> positionEquals(widget.getPosition(), new Point(24, 231)));
                    String msg = totalPriceWidget.getMessage();
                    int totalPrice = Integer.parseInt(msg.substring(0, msg.lastIndexOf('c')-1).replace(",", ""));
                    if(totalPrice < inventory.getAmount(995)){
                        return true;
                    } else {
                        RS2Widget perItemPrice = widgets.singleFilter(ROOT_ID, widget -> positionEquals(widget.getPosition(), new Point(260, 177)));
                        if(perItemPrice != null){
                            msg = perItemPrice.getMessage();
                            int highPrice = Integer.parseInt(msg.substring(0, msg.lastIndexOf('c')-1).replace(",", ""));
                            int buyableQuantity = (int) (inventory.getAmount(995)/highPrice);
                            if(quantitySelection.isVisible() && quantitySelection.interact("Enter quantity")){
                                boolean entryAllowed = new ConditionalSleep(1000){
                                    @Override
                                    public boolean condition() throws InterruptedException {
                                        return isNumberEntryOpen("How many do you wish to buy?");
                                    }
                                }.sleep();
                                return entryAllowed && getKeyboard().typeString(String.valueOf(buyableQuantity));
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    //Sell Item helper methods
    private boolean offerItem(int itemID){
        if(inventory.contains(itemID)){
            return inventory.interact("Offer", itemID);
        }
        return false;
    }

    private boolean set5PercentLower() throws InterruptedException {
        boolean ready = new ConditionalSleep(1500){
            @Override
            public boolean condition() throws InterruptedException {
                return getGrandExchange().isSellOfferOpen();
            }
        }.sleep();
        if(ready){
            List<RS2Widget> incPrice = getWidgets().containingActions(ROOT_ID, "-5%");
            if(incPrice != null){
                return incPrice.size() > 0 && incPrice.get(0).interact("-5%");
            }
        }

        return false;
    }

    private RS2Widget getBuyWidgets(GrandExchange.Box box){
        List<RS2Widget> buyWidgets = getWidgets().containingSprite(465, BUY_SPRITE_ID);
        if(buyWidgets != null && buyWidgets.size() > 0){
            switch(box){
                case BOX_1:
                    return buyWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX1_BUY_PT)).findFirst().orElse(null);
                case BOX_2:
                    return buyWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX2_BUY_PT)).findFirst().orElse(null);
                case BOX_3:
                    return buyWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX3_BUY_PT)).findFirst().orElse(null);
                case BOX_4:
                    return buyWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX4_BUY_PT)).findFirst().orElse(null);
                case BOX_5:
                    return buyWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX5_BUY_PT)).findFirst().orElse(null);
                case BOX_6:
                    return buyWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX6_BUY_PT)).findFirst().orElse(null);
                case BOX_7:
                    return buyWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX7_BUY_PT)).findFirst().orElse(null);
                case BOX_8:
                    return buyWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX8_BUY_PT)).findFirst().orElse(null);
            }
        }
        return null;
    }

    private RS2Widget getSellWidgets(GrandExchange.Box box){
        List<RS2Widget> sellWidgets = getWidgets().containingSprite(465, SELL_SPRITE_ID);
        if(sellWidgets != null && sellWidgets.size() > 0){
            switch(box){
                case BOX_1:
                    return sellWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX1_SELL_PT)).findFirst().orElse(null);
                case BOX_2:
                    return sellWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX2_SELL_PT)).findFirst().orElse(null);
                case BOX_3:
                    return sellWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX3_SELL_PT)).findFirst().orElse(null);
                case BOX_4:
                    return sellWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX4_SELL_PT)).findFirst().orElse(null);
                case BOX_5:
                    return sellWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX5_SELL_PT)).findFirst().orElse(null);
                case BOX_6:
                    return sellWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX6_SELL_PT)).findFirst().orElse(null);
                case BOX_7:
                    return sellWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX7_SELL_PT)).findFirst().orElse(null);
                case BOX_8:
                    return sellWidgets.stream().filter(widget -> positionEquals(widget.getPosition(), BOX8_SELL_PT)).findFirst().orElse(null);
            }
        }
        return null;
    }

    private boolean positionEquals(Point p1, Point p2){
        return p1.x == p2.x && p1.y == p2.y;
    }

    private boolean isSelectedItemCorrect(int itemID){
        RS2Widget widget = getWidgets().singleFilter(ROOT_ID, rs2Widget -> rs2Widget.getItemId() == itemID && positionEquals(rs2Widget.getPosition(), new Point(96, 92)));
        return  widget != null && widget.isVisible();
    }

    private boolean confirmOffer() {
        RS2Widget confirm = getWidgets().getWidgetContainingText(465, "Confirm");
        return confirm != null && confirm.isVisible() && confirm.interact("Confirm");
    }

    private boolean isNumberEntryOpen(String textLabel){
        RS2Widget numberEntry = getWidgets().getWidgetContainingText(162, "How many do you wish to buy?");
        return numberEntry != null && numberEntry.isVisible();
    }

    private GrandExchange.Box findFreeGEBox(){
        GrandExchange ge = getGrandExchange();
        for (GrandExchange.Box box : GrandExchange.Box.values()) {
            if(ge.getStatus(box) == GrandExchange.Status.EMPTY){
                return box;
            }
        }
        return null; //indicates no boxes are empty
    }
}
