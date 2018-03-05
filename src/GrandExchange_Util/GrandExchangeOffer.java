package GrandExchange_Util;

import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.script.Script;

public class GrandExchangeOffer {

    private static GrandExchangeOffer box1Offer;
    private static GrandExchangeOffer box2Offer;
    private static GrandExchangeOffer box3Offer;
    private static GrandExchangeOffer box4Offer;
    private static GrandExchangeOffer box5Offer;
    private static GrandExchangeOffer box6Offer;
    private static GrandExchangeOffer box7Offer;
    private static GrandExchangeOffer box8Offer;

    private Script hostScriptReference;

    private boolean isBuyOffer;
    private GrandExchange.Box selectedBox;
    private int itemID;
    private int totalAmountToTrade;
    private int amountTraded = 0;

    public static GrandExchangeOffer getInstance(Script hostScriptReference, GrandExchange.Box selectedBox){
        GrandExchange ge = hostScriptReference.getGrandExchange();
        switch(selectedBox){
            case BOX_1:
                if(box1Offer == null){
                    box1Offer = new GrandExchangeOffer(hostScriptReference, GrandExchangeOffer.isBuyOffer(hostScriptReference, selectedBox), selectedBox, ge.getItemId(selectedBox), ge.getAmountToTransfer(selectedBox));
                }
                return box1Offer;
            case BOX_2:
                if(box2Offer == null){
                    box2Offer = new GrandExchangeOffer(hostScriptReference, GrandExchangeOffer.isBuyOffer(hostScriptReference, selectedBox), selectedBox, ge.getItemId(selectedBox), ge.getAmountToTransfer(selectedBox));
                }
                return box2Offer;
            case BOX_3:
                if(box3Offer == null){
                    box3Offer = new GrandExchangeOffer(hostScriptReference, GrandExchangeOffer.isBuyOffer(hostScriptReference, selectedBox), selectedBox, ge.getItemId(selectedBox), ge.getAmountToTransfer(selectedBox));
                }
                return box3Offer;
            case BOX_4:
                if(box4Offer == null){
                    box4Offer = new GrandExchangeOffer(hostScriptReference, GrandExchangeOffer.isBuyOffer(hostScriptReference, selectedBox), selectedBox, ge.getItemId(selectedBox), ge.getAmountToTransfer(selectedBox));
                }
                return box4Offer;
            case BOX_5:
                if(box5Offer == null){
                    box5Offer = new GrandExchangeOffer(hostScriptReference, GrandExchangeOffer.isBuyOffer(hostScriptReference, selectedBox), selectedBox, ge.getItemId(selectedBox), ge.getAmountToTransfer(selectedBox));
                }
                return box5Offer;
            case BOX_6:
                if(box6Offer == null){
                    box6Offer = new GrandExchangeOffer(hostScriptReference, GrandExchangeOffer.isBuyOffer(hostScriptReference, selectedBox), selectedBox, ge.getItemId(selectedBox), ge.getAmountToTransfer(selectedBox));
                }
                return box6Offer;
            case BOX_7:
                if(box7Offer == null){
                    box7Offer = new GrandExchangeOffer(hostScriptReference, GrandExchangeOffer.isBuyOffer(hostScriptReference, selectedBox), selectedBox, ge.getItemId(selectedBox), ge.getAmountToTransfer(selectedBox));
                }
                return box7Offer;
            case BOX_8:
                if(box8Offer == null){
                    box8Offer = new GrandExchangeOffer(hostScriptReference, GrandExchangeOffer.isBuyOffer(hostScriptReference, selectedBox), selectedBox, ge.getItemId(selectedBox), ge.getAmountToTransfer(selectedBox));
                }
                return box8Offer;
        }
        return null;
    }

    public static void wipeInstance(GrandExchange.Box selectedBox){
        switch(selectedBox){
            case BOX_1:
                box1Offer = null;
                break;
            case BOX_2:
                box2Offer = null;
                break;
            case BOX_3:
                box3Offer = null;
                break;
            case BOX_4:
                box4Offer = null;
                break;
            case BOX_5:
                box5Offer = null;
                break;
            case BOX_6:
                box6Offer = null;
                break;
            case BOX_7:
                box7Offer = null;
                break;
            case BOX_8:
                box8Offer = null;
                break;
        }
    }


    GrandExchangeOffer(Script hostScriptReference, boolean isBuyOffer, GrandExchange.Box selectedBox, int itemID, int totalAmountToTrade) {
        this.isBuyOffer = isBuyOffer;
        this.selectedBox = selectedBox;
        this.itemID = itemID;
        this.totalAmountToTrade = totalAmountToTrade;
        this.hostScriptReference = hostScriptReference;
    }

    private static boolean isBuyOffer(Script hostScriptReference, GrandExchange.Box selectedBox){
        GrandExchange ge = hostScriptReference.getGrandExchange();
        GrandExchange.Status status = ge.getStatus(selectedBox);
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
        return hostScriptReference.getGrandExchange().getStatus(selectedBox);
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
        GrandExchange ge = hostScriptReference.getGrandExchange();
        return ge.getStatus(selectedBox) == GrandExchange.Status.FINISHED_SALE || ge.getStatus(selectedBox) == GrandExchange.Status.FINISHED_BUY;
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
