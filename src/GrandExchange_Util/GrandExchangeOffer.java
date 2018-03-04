package GrandExchange_Util;

import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.script.Script;

public class GrandExchangeOffer {

    private Script hostScriptReference;
    private boolean isBuyOffer;
    private GrandExchange.Box selectedBox;
    private int itemID;
    private int totalAmountToTrade;

    int amountTraded = 0;

    GrandExchangeOffer(Script hostScriptReference, boolean isBuyOffer, GrandExchange.Box selectedBox, int itemID, int totalAmountToTrade) {
        this.isBuyOffer = isBuyOffer;
        this.selectedBox = selectedBox;
        this.itemID = itemID;
        this.totalAmountToTrade = totalAmountToTrade;
        this.hostScriptReference = hostScriptReference;
    }

    public boolean isBuyOffer() {
        return isBuyOffer;
    }

    public GrandExchange.Box getSelectedBox() {
        return selectedBox;
    }

    public GrandExchange.Status getBoxStatus(){
        return hostScriptReference.getGrandExchange().getStatus(selectedBox);
    }

    public int getItemID() {
        return itemID;
    }

    public int getTotalAmountToTrade() {
        return totalAmountToTrade;
    }

    /**
     *
     * @param amountTraded the current amount traded, received from GrandExchange.getAmountTraded(GrandExchange.Box box)
     * @return True if the current amount traded differed from the previous amount traded (if amountTraded variable changed), this means need to notify listeners that this offer has changed
     * False otherwise
     */
    public boolean updateOffer(int amountTraded){
        if(this.amountTraded != amountTraded){
            this.amountTraded = amountTraded;
            return true;
        }
        return false;
    }
}
