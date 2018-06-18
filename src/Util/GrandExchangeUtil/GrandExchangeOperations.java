package Util.GrandExchangeUtil;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.input.mouse.WidgetDestination;
import org.osbot.rs07.utility.ConditionalSleep;

import java.util.Arrays;
import java.util.List;

public class GrandExchangeOperations extends GrandExchange{

    private static GrandExchangeOperations singleton;

    private GrandExchangeOperations(){}

    public static GrandExchangeOperations getInstance(Bot bot){
        if(singleton == null){
            singleton = new GrandExchangeOperations();
            singleton.exchangeContext(bot);
        }
        return singleton;
    }

    public int[] priceCheckItemMargin(int itemID, String searchTerm) throws InterruptedException {
        if(!inventory.contains(995)){
            withdrawCash();
        }
        int[] margin = new int[2];
        Box buyPredictedBox = findFreeGEBox();
        if(openGE()){
            if(buyItem(itemID, searchTerm, 4000, 1)){
                if(buyPredictedBox != null){
                    boolean buyComplete = new ConditionalSleep(5000){
                        @Override
                        public boolean condition() throws InterruptedException {
                            return getStatus(buyPredictedBox) == Status.FINISHED_BUY && getItemId(buyPredictedBox) == itemID;
                        }
                    }.sleep();
                    if(buyComplete){
                        margin[1] = getAmountSpent(buyPredictedBox);
                        sleep(1000);
                        if(collect()){
                            sleep(1000);
                            Box sellPredictedBox = findFreeGEBox();
                            if(sellItem(itemID, 1, 1)){
                                if(sellPredictedBox != null){
                                    boolean sellComplete = new ConditionalSleep(5000){
                                        @Override
                                        public boolean condition() throws InterruptedException {
                                            return getStatus(sellPredictedBox) == Status.FINISHED_SALE && getItemId(sellPredictedBox) == itemID;
                                        }
                                    }.sleep();
                                    if(sellComplete){
                                        sleep(1000);
                                        margin[0] = getAmountSpent(sellPredictedBox);
                                        if(collect()){
                                            log("margin for item: " + itemID + " is " + Arrays.toString(margin));
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
        return new int[]{0,0};
    }

    public boolean sellAll(int itemID, int price) throws InterruptedException {
        if(openGE()){
            sleep(1000);
            return sellItem(itemID, price, (int) inventory.getAmount(itemID));
        }
        return false;
    }

    public boolean buyUpToLimit(int itemID, String searchTerm, int price, int buyQuantityLimit){
        if(inventory.contains(995)){
            if(openGE()){
                int coins = (int) inventory.getAmount(995);
                int buyableQuantity = coins/price;
                int actualBuyQuantity = buyQuantityLimit > buyableQuantity ? buyableQuantity : buyQuantityLimit;
                return buyItem(itemID, searchTerm, price, actualBuyQuantity);
            }
        }
        return false;
    }

    public boolean abortOffersWithItem(String itemName) throws InterruptedException {
        if(openGE()){
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
            }
        }
        return false;
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
        if(!isOpen()){
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
        }
        return true;
    }

    private boolean withdrawCash() throws InterruptedException {
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
