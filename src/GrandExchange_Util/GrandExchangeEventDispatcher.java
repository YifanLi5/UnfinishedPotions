package GrandExchange_Util;

import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.script.Script;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GrandExchangeEventDispatcher {

    public interface GrandExchangeListener {
        void onGEUpdate(GrandExchangeOffer offer);
    }

    private Script hostScriptReference;

    private ArrayList<GrandExchangeListener> listeners; //subscribed classes to ge offer changes
    private ArrayList<GrandExchangeOffer> offers; //ongoing ge offers

    private ScheduledExecutorService geOfferQueryService;

    public GrandExchangeEventDispatcher(Script hostScriptReference) {
        this.listeners = new ArrayList<>();
        this.offers = new ArrayList<>();
        this.hostScriptReference = hostScriptReference;
    }

    public void addGEListenerForOffer(GrandExchangeListener listener, GrandExchangeOffer offer){
        this.offers.add(offer);
        this.listeners.add(listener);

        //if adding an initial listener, start querying ge-offers for changes
        if(listeners.size() == 1){
            continouslyQueryOffers();
        }
    }

    public void removeGEListenerAndOffer(GrandExchangeListener listener, GrandExchangeOffer offer){
        listeners.remove(listener);
        offers.remove(offer);

        //if all listeners detached, stop querying offers
        if(listeners.size() == 0){
            geOfferQueryService.shutdown();
        }
    }

    public void stopQueryingOffers(){
        geOfferQueryService.shutdown();
    }

    private void continouslyQueryOffers(){
        GrandExchange ge = hostScriptReference.getGrandExchange();
        hostScriptReference.log("starting querier");
        //schedule a thread to query ge offers every second. if a ge offer updates, the listening class that added the geOffer is notified
        geOfferQueryService = Executors.newScheduledThreadPool(5);
        Runnable queryRunable = () -> {
            try{
                hostScriptReference.log("Offer listener is running");
                for(GrandExchangeOffer offer: offers){
                    GrandExchange.Box box = offer.getSelectedBox();
                    if(box != null){
                        int amountTraded = ge.getAmountTraded(box);
                        boolean offerUpdated = offer.updateOffer(amountTraded);
                        if(offerUpdated){
                            notifyGEUpdate(offer);
                        }
                    }
                }
            }
            catch (Exception e){
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                hostScriptReference.log(exceptionAsString);
            }
        };
        geOfferQueryService.scheduleAtFixedRate(queryRunable, 2, 2, TimeUnit.SECONDS);
    }

    private void notifyGEUpdate(GrandExchangeOffer offer){
        for(GrandExchangeListener listener: listeners){
            listener.onGEUpdate(offer);
        }
    }
}


