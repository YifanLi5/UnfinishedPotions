package Util.GrandExchangeUtil;

import Util.Statics;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.input.mouse.WidgetDestination;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Arrays;
import java.util.List;

/**
 * Extended GE api to include special methods for buying/selling ingredients and finished products
 */
public class GrandExchangeOperations extends GrandExchange{

    private static GrandExchangeOperations singleton;

    private GrandExchangeOperations(){}

    public static GrandExchangeOperations getInstance(Bot bot){
        if(singleton == null){
            singleton = new GrandExchangeOperations();
            singleton.exchangeContext(bot);
            singleton.initializeModule();
        }
        return singleton;
    }

    /**
     * buys 1 copy of some item at a high price then sells it.
     * @param itemID the item to price check
     * @param searchTerm used to search the GE for the item
     * @return item's instant sell (margin[1]) and instant buy (margin[0])
     */
    public int[] priceCheckItemMargin(int itemID, String searchTerm) throws InterruptedException {
        sleep(2000);
        int[] margin = new int[2];
        Box buyPredictedBox = findFreeGEBox();
        String lastSuccess = "";
        if(withdrawCash()){
            lastSuccess = "withdrawCash";
            if(openGE()){
                lastSuccess = "openGE";
                if(buyItem(itemID, searchTerm, random(3500, 5000), 1)){
                    lastSuccess = "buyItem";
                    if(buyPredictedBox != null){
                        lastSuccess = "buyPredictedBox != null";
                        boolean buyComplete = new ConditionalSleep(5000){
                            @Override
                            public boolean condition() throws InterruptedException {
                                return getStatus(buyPredictedBox) == Status.FINISHED_BUY && getItemId(buyPredictedBox) == itemID;
                            }
                        }.sleep();
                        if(buyComplete){
                            lastSuccess = "buyComplete";
                            margin[1] = getAmountSpent(buyPredictedBox);
                            boolean buyCollected = new ConditionalSleep(5000){
                                @Override
                                public boolean condition() throws InterruptedException {
                                    return collect();
                                }
                            }.sleep();
                            if(buyCollected){
                                lastSuccess = "buyCollected";
                                boolean invHasItem = new ConditionalSleep(5000){
                                    @Override
                                    public boolean condition() throws InterruptedException {
                                        return inventory.contains(itemID);
                                    }
                                }.sleep();
                                Box sellPredictedBox = findFreeGEBox();
                                if(invHasItem && sellItem(itemID, 1, 1)){
                                    lastSuccess = "invHasItem && sellItem";
                                    if(sellPredictedBox != null){
                                        lastSuccess = "sellPredictedBox != null";
                                        boolean sellComplete = new ConditionalSleep(5000){
                                            @Override
                                            public boolean condition() throws InterruptedException {
                                                return getStatus(sellPredictedBox) == Status.FINISHED_SALE && getItemId(sellPredictedBox) == itemID;
                                            }
                                        }.sleep();
                                        if(sellComplete){
                                            lastSuccess = "sellComplete";
                                            margin[0] = getAmountSpent(sellPredictedBox);
                                            boolean sellCollected = new ConditionalSleep(5000){
                                                @Override
                                                public boolean condition() throws InterruptedException {
                                                    return collect();
                                                }
                                            }.sleep();
                                            if(sellCollected){
                                                log("margin for item: " + itemID + " is " + Arrays.toString(margin));
                                                new ConditionalSleep(3000, 500) {
                                                    @Override
                                                    public boolean condition() {
                                                        return isOpen() && !isOfferScreenOpen(); //interface where user is prompted to buy/sell
                                                    }
                                                }.sleep();
                                                return margin;
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
        log("last success: " + lastSuccess);
        return new int[]{100000, 100000};
    }

    /**
     * Sell all copies of some item in the inventory regardless of whether it is noted or unnoted
     * @param itemID item to buy
     * @param itemName item's name
     * @param price sell price
     * @return true is successful. false otherwise
     */
    public boolean sellAll(int itemID, String itemName, int price) throws InterruptedException {
        if(openGE()){
            sleep(1000);
            return sellItem(itemID, price, (int) inventory.getAmount(itemName));
        }
        return false;
    }

    /**
     * buy some item with all the gp present in inventory with a max buy quantity of buyQuantityLimit.
     * ex1) if irits cost 1k and inventory contains 500k and buyQuantityLimit is set to 1000
     * only 500 will be bought
     * ex2) if irits cost 1k and inventory contains 2000k and buyQuantityLimit is set to 1000
     * only 1000 will be bought
     * @param itemID item to buy
     * @param searchTerm search term for ge
     * @param price price to buy at
     * @param buyQuantityLimit max quantity to buy
     * @return true is successful
     */
    public boolean buyUpToLimit(int itemID, String searchTerm, int price, int buyQuantityLimit){
        if(inventory.contains(995)){
            if(openGE()){
                int coins = (int) inventory.getAmount(995);
                int buyableQuantity = coins/price;
                int actualBuyQuantity = buyQuantityLimit > buyableQuantity ? buyableQuantity : buyQuantityLimit;
                return buyItem(itemID, searchTerm, price, actualBuyQuantity) &&
                    new ConditionalSleep(3000, 500) {
                        @Override
                        public boolean condition() {
                            return isOpen() && !isOfferScreenOpen(); //interface where user is prompted to buy/sell
                        }
                    }.sleep();
            }
        }
        return false;
    }

    /**
     * Aborts all offers for some item
     * @param itemName name of item to abort
     * @return true if successful
     */
    public boolean abortOffersWithItem(String itemName) throws InterruptedException {
        if(openGE()){
            boolean collected = new ConditionalSleep(5000){
                @Override
                public boolean condition() throws InterruptedException {
                    return collect();
                }
            }.sleep();
            Statics.longRandomNormalDelay();
            List<RS2Widget> pendingOffers = getWidgets().containingText(465, itemName);
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
            } else return collected && pendingOffers == null; //if the offer is 100% complete collecting is also the same as aborting
        }
        return false;
    }

    /**
     * returns the offer completion percent of a buy or sell offer
     * @param box box to query
     * @return percent completion
     */
    public double getOfferCompletionPercentage(Box box){
        return ((double) getAmountTraded(box)) / getAmountToTransfer(box);
    }

    private GrandExchange.Box findFreeGEBox(){
        for (GrandExchange.Box box : GrandExchange.Box.values()) {
            if(getStatus(box) == GrandExchange.Status.EMPTY){
                return box;
            }
        }
        return null; //indicates no boxes are empty
    }

    private boolean openGE() {
        if(!isOpen()){ // Osbot API NPEs here!!!
            NPC grandExchangeClerk = npcs.closest("Grand Exchange Clerk");
            if(grandExchangeClerk != null){
                boolean didInteraction = grandExchangeClerk.interact("Exchange");
                return new ConditionalSleep(1000){
                    @Override
                    public boolean condition() throws InterruptedException {
                        return didInteraction && isOpen();
                    }
                }.sleep();
            }
            return false;
        }
        return true;
    }

    private boolean withdrawCash() throws InterruptedException {
        if(inventory.getAmount(995) >= 10000){
            return true;
        }

        if(bank.open()){
            boolean success = new ConditionalSleep(1000){
                @Override
                public boolean condition() throws InterruptedException {
                    return bank.isOpen();
                }
            }.sleep();
            if(success){
                if(bank.getAmount(995) > 0){
                    return bank.withdraw(995, Bank.WITHDRAW_ALL) && bank.close();
                }
            }
        }
        return false;
    }
}
