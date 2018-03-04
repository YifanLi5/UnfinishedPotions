package GrandExchange_Util;

import org.osbot.rs07.api.GrandExchange;

class GrandExchangeOffer {
    private boolean isBuyOffer;
    private GrandExchange.Box selectedBox;
    private int itemID;
    private int offerQuantity;

    GrandExchangeOffer(boolean isBuyOffer, GrandExchange.Box selectedBox, int itemID, int offerQuantity) {
        this.isBuyOffer = isBuyOffer;
        this.selectedBox = selectedBox;
        this.itemID = itemID;
        this.offerQuantity = offerQuantity;
    }

    public boolean isBuyOffer() {
        return isBuyOffer;
    }

    public GrandExchange.Box getSelectedBox() {
        return selectedBox;
    }

    public int getItemID() {
        return itemID;
    }

    public int getOfferQuantity() {
        return offerQuantity;
    }
}
