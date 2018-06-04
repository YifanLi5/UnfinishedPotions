package GrandExchange;

import org.osbot.rs07.api.GrandExchange;

public interface GrandExchangeObserver {
    void onGEUpdate(GrandExchange.Box box);
}