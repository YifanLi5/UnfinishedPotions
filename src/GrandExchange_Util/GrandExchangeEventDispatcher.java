package GrandExchange_Util;

import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.script.Script;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GrandExchangeEventDispatcher {

    public interface GrandExchangeListener {
        void onGEUpdate(GrandExchangeOffer offer);
    }

    private Script hostScriptReference;

    //bidirectional mapping
    private Hashtable<GrandExchangeOffer, GrandExchangeListener> offerToListenerMap; //used to get the listener associated with an offer
    private Hashtable<GrandExchangeListener, GrandExchangeOffer> listenerToOfferMap; //used to easily remove a listener and offer for removeGEListenerAndOffer method

    private ScheduledExecutorService geOfferQueryService;

    public GrandExchangeEventDispatcher(Script hostScriptReference) {
        this.offerToListenerMap = new Hashtable<>();
        this.listenerToOfferMap = new Hashtable<>();
        this.hostScriptReference = hostScriptReference;
    }

    public void addGEListenerForOffer(GrandExchangeListener listener, GrandExchangeOffer offer){

        offerToListenerMap.put(offer, listener);
        listenerToOfferMap.put(listener, offer);
        //start querying offers if adding an initial listeners
        if(offerToListenerMap.size() == 1){
            continouslyQueryOffers();
        }
    }

    public void removeGEListenerAndOffer(GrandExchangeListener listener){
        GrandExchangeOffer removeFromOtherMap = listenerToOfferMap.get(listener);
        listenerToOfferMap.remove(listener);
        offerToListenerMap.remove(removeFromOtherMap);

        if(offerToListenerMap.size() == 0){
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
                for(GrandExchangeOffer offer: offerToListenerMap.keySet()){
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
        GrandExchangeListener correctListener = offerToListenerMap.get(offer);
        if(correctListener != null){
            for(GrandExchangeListener listener: listenerToOfferMap.keySet()){
                if(listener.equals(correctListener)){
                    listener.onGEUpdate(offer);
                    break;
                }
            }
        }
    }
}


