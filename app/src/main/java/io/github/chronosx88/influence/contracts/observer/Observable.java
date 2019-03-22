package io.github.chronosx88.influence.contracts.observer;

import com.google.gson.JsonObject;

public interface Observable {
    void register(Observer observer);
    void register(NetworkObserver networkObserver);
    void unregister(Observer observer);
    void unregister(NetworkObserver networkObserver);
    void notifyUIObservers(JsonObject jsonObject);
    void notifyNetworkObservers(Object object);
}
