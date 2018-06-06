package GrandExchangeUtil;

import org.osbot.Sc;
import org.osbot.rs07.Bot;
import org.osbot.rs07.script.API;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.util.ArrayList;

public class GrandExchangePolling {

    private ArrayList<GrandExchangeObserver> observers; //subscribed classes to ge offer changes
    private Thread geQuery;
    GrandExchangeRunnable queryRunnable;
    private static GrandExchangePolling singleton;

    public static GrandExchangePolling getInstance(Script script){
        if(singleton == null)
            singleton = new GrandExchangePolling(script);
        return singleton;
    }

    private GrandExchangePolling(Script script) {
        this.observers = new ArrayList<>();
        queryRunnable = new GrandExchangeRunnable(observers, script);
    }

    public void registerObserver(GrandExchangeObserver o){
        this.observers.add(o);
        if(observers.size() == 1)
            startQueryingOffers();
    }

    public void removeObserver(GrandExchangeObserver o){
        this.observers.remove(o);
        if(observers.isEmpty())
            queryRunnable.stop();
    }

    public void stopQueryingOffers(){
        queryRunnable.stop();
    }

    private void startQueryingOffers(){
        if(geQuery == null){
            geQuery = new Thread(queryRunnable);
            geQuery.start();
        }
    }
}


