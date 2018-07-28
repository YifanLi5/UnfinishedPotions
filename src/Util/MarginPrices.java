package Util;

import java.time.Instant;

public class MarginPrices {
    public static final int DEFAULT_VAL = 100000;
    private int primaryInstantSell, primaryInstantBuy, productInstantSell, productInstantBuy;
    private Instant lastUpdate;

    public MarginPrices(){
        primaryInstantSell = DEFAULT_VAL;
        primaryInstantBuy = DEFAULT_VAL;
        productInstantSell = DEFAULT_VAL;
        productInstantBuy = DEFAULT_VAL;
    }

    public int getConversionProfit(){
        return productInstantSell - primaryInstantBuy;
    }

    public int[] getConversionMargin(){
        return new int[]{primaryInstantBuy, productInstantSell};
    }

    public int getPrimaryInstantSell() {
        return primaryInstantSell;
    }

    public void setPrimaryInstantSell(int primaryInstantSell) {
        this.primaryInstantSell = primaryInstantSell;
    }

    public int getPrimaryInstantBuy() {
        return primaryInstantBuy;
    }

    public void setPrimaryInstantBuy(int primaryInstantBuy) {
        this.primaryInstantBuy = primaryInstantBuy;
    }

    public int getProductInstantSell() {
        return productInstantSell;
    }

    public void setProductInstantSell(int productInstantSell) {
        this.productInstantSell = productInstantSell;
    }

    public int getProductInstantBuy() {
        return productInstantBuy;
    }

    public void setProductInstantBuy(int productInstantBuy) {
        this.productInstantBuy = productInstantBuy;
    }

    public void setLastUpdate(){
        this.lastUpdate = Instant.now();
    }

    public int getSecondsSinceLastUpdate(){
        if(lastUpdate != null)
            return (int) (Instant.now().getEpochSecond() - lastUpdate.getEpochSecond());
        return Integer.MAX_VALUE;
    }

}
