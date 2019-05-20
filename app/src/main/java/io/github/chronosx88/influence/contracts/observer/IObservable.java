package io.github.chronosx88.influence.contracts.observer;

import org.json.JSONObject;

public interface IObservable {
    void register(IObserver observer);
    void unregister(IObserver observer);
    void notifyUIObservers(JSONObject jsonObject);
}
