package io.github.chronosx88.influence.contracts.observer;

import com.google.gson.JsonObject;

public interface IObservable {
    void register(IObserver observer);
    void register(INetworkObserver networkObserver);
    void unregister(IObserver observer);
    void unregister(INetworkObserver networkObserver);
    void notifyUIObservers(JsonObject jsonObject);
    void notifyNetworkObservers(Object object);
}
