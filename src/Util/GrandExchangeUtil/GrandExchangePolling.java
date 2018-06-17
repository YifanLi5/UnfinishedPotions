package Util.GrandExchangeUtil;

import org.osbot.rs07.script.Script;

import java.util.ArrayList;
import java.util.List;

public class GrandExchangePolling {

    private List<GrandExchangeObserver> observers; //subscribed classes to ge offer changes
    private Thread geQuery;
    GrandExchangeRunnable queryRunnable;
    private static GrandExchangePolling singleton;
    private Script script;

    public static GrandExchangePolling getInstance(Script script){
        if(singleton == null)
            singleton = new GrandExchangePolling(script);
        return singleton;
    }

    private GrandExchangePolling(Script script) {
        this.observers = new ArrayList<>();
        queryRunnable = new GrandExchangeRunnable(observers, script);
        this.script = script;
    }

    public void registerObserver(GrandExchangeObserver o){
        if(!observers.contains(o)){
            observers.add(o);
            script.log(o.getClass().getSimpleName() + " is now an observer");
        }
        if(observers.size() > 0)
            startQueryingOffers();
    }

    public void removeObserver(GrandExchangeObserver o){
        observers.remove(o);
        if(observers.isEmpty())
            queryRunnable.stop();
    }

    public void stopQueryingOffers(){
        queryRunnable.stop();
    }

    private void startQueryingOffers(){
        if(geQuery == null || !queryRunnable.isRunning()){
            script.log("starting ge thread");
            geQuery = new Thread(queryRunnable);
            geQuery.start();
        }
    }
}


