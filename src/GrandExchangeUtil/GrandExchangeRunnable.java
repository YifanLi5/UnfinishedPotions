package GrandExchangeUtil;

import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.script.Script;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GrandExchangeRunnable implements Runnable {

    private AtomicBoolean running = new AtomicBoolean(false);
    private Script script;
    private HashMap<GrandExchange.Box, Integer> amountTradedMap;
    private List<GrandExchangeObserver> observers;

    public GrandExchangeRunnable(List<GrandExchangeObserver> observers, Script script){
        this.observers = observers;
        this.script = script;
        amountTradedMap = new HashMap<>();
    }

    @Override
    public void run() {
        script.log("starting ge query thread");
        running.set(true);
        GrandExchange ge = script.getGrandExchange();
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
                if(ge.getStatus(box) != GrandExchange.Status.EMPTY){
                    int prevAmtTraded = amountTradedMap.get(box);
                    int amtTraded = ge.getAmountTraded(box);
                    if(prevAmtTraded != -1){
                        if(amtTraded != prevAmtTraded
                                || ge.getStatus(box) == GrandExchange.Status.FINISHED_BUY
                                || ge.getStatus(box) == GrandExchange.Status.FINISHED_SALE){
                            Collections.synchronizedList(observers).forEach(item -> item.onGEUpdate(box));
                        }
                    }
                    amountTradedMap.put(box, amtTraded);
                } else
                    amountTradedMap.put(box, -1);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        script.log("stopping ge query thread");
    }

    public void stop(){
        running.set(false);
    }

    public boolean isRunning(){
        return running.get();
    }

}
