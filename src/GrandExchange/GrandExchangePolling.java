package GrandExchange;

import org.osbot.rs07.Bot;
import org.osbot.rs07.script.API;
import org.osbot.rs07.script.MethodProvider;

import java.util.ArrayList;

public class GrandExchangePolling extends API {

    private ArrayList<GrandExchangeObserver> observers; //subscribed classes to ge offer changes
    private Thread geQuery;
    GrandExchangeRunnable queryRunnable;

    @Override
    public void initializeModule() {
    }

    @Override
    public MethodProvider exchangeContext(Bot iIiiiiiiIiii) {
        return super.exchangeContext(iIiiiiiiIiii);
    }

    public GrandExchangePolling() {
        this.observers = new ArrayList<>();
        queryRunnable = new GrandExchangeRunnable(observers);
    }

    public void registerObserver(GrandExchangeObserver o){
        this.observers.add(o);
    }

    public void removeObserver(GrandExchangeObserver o){
        this.observers.remove(o);
    }

    public void stopQueryingOffers(){
        queryRunnable.stop();
    }

    public void startQueryingOffers(){
        if(geQuery == null){
            geQuery = new Thread(queryRunnable);
            geQuery.start();
        }
    }
}


