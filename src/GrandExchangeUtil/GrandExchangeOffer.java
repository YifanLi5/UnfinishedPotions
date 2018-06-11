package GrandExchangeUtil;

import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.script.API;

public class GrandExchangeOffer extends API{


    private boolean isBuyOffer;
    private GrandExchange.Box selectedBox;
    private int itemID;
    private int totalAmountToTrade;
    private int amountTraded = 0;

    GrandExchangeOffer(boolean isBuyOffer, GrandExchange.Box selectedBox, int itemID, int totalAmountToTrade) {
        this.isBuyOffer = isBuyOffer;
        this.selectedBox = selectedBox;
        this.itemID = itemID;
        this.totalAmountToTrade = totalAmountToTrade;

    }

    private boolean isBuyOffer(GrandExchange.Box selectedBox){
        GrandExchange.Status status = getGrandExchange().getStatus(selectedBox);
        switch (status){
            case COMPLETING_BUY:
            case FINISHED_BUY:
            case CANCELLING_BUY:
            case INITIALIZING_BUY:
            case PENDING_BUY:
                return true;
            default:
                return false;
        }

    }

    public boolean isBuyOffer() {
        return isBuyOffer;
    }

    public GrandExchange.Box getSelectedBox() {
        return selectedBox;
    }

    public GrandExchange.Status getBoxStatus(){
        return getGrandExchange().getStatus(selectedBox);
    }

    public int getItemID() {
        return itemID;
    }

    public int getTotalAmountToTrade() {
        return totalAmountToTrade;
    }

    public int getAmountTraded() {
        return amountTraded;
    }

    public boolean isOfferFinished() {
        GrandExchange ge = getGrandExchange();
        return ge.getStatus(selectedBox) == GrandExchange.Status.FINISHED_SALE || ge.getStatus(selectedBox) == GrandExchange.Status.FINISHED_BUY;
    }

    public boolean updateOffer(int amountTraded){
        if(this.amountTraded != amountTraded){
            this.amountTraded = amountTraded;
            return true;
        }
        return false;
    }

    @Override
    public void initializeModule() {
        exchangeContext(getBot());
    }
}
