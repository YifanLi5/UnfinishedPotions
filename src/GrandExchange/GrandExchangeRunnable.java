package GrandExchange;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.script.API;
import org.osbot.rs07.script.MethodProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class GrandExchangeRunnable extends API implements Runnable {

    private AtomicBoolean running = new AtomicBoolean(false);
    private HashMap<GrandExchange.Box, Integer> amountTradedMap;
    private ArrayList<GrandExchangeObserver> observers;

    public GrandExchangeRunnable(ArrayList<GrandExchangeObserver> observers){
        this.observers = observers;
        amountTradedMap = new HashMap<>();
    }

    @Override
    public void run() {
        log("starting ge query thread");
        running.set(true);
        GrandExchange ge = getGrandExchange();
        for (GrandExchange.Box box : GrandExchange.Box.values()) {
            if(ge.getStatus(box) == GrandExchange.Status.EMPTY){
                amountTradedMap.put(box, -1);
            } else {
                int amtTraded = ge.getAmountTraded(box);
                amountTradedMap.put(box, amtTraded);
            }
        }
        while(running.get()){
            for (GrandExchange.Box box : GrandExchange.Box.values()) {
                if(ge.getStatus(box) == GrandExchange.Status.EMPTY){
                    amountTradedMap.put(box, -1);
                } else {
                    int amtTraded = ge.getAmountTraded(box);
                    int prevAmtTraded = amountTradedMap.get(box);
                    if(amtTraded != prevAmtTraded
                            || ge.getStatus(box) == GrandExchange.Status.FINISHED_BUY
                            || ge.getStatus(box) == GrandExchange.Status.FINISHED_SALE){
                        observers.forEach(item -> item.onGEUpdate(box));
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log("stopping ge query thread");
    }

    public void stop(){
        running.set(false);
    }

    @Override
    public void initializeModule() {

    }

    @Override
    public MethodProvider exchangeContext(Bot iIiiiiiiIiii) {
        return super.exchangeContext(iIiiiiiiIiii);
    }
}
