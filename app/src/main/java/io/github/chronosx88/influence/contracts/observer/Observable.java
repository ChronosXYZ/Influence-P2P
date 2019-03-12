package io.github.chronosx88.influence.contracts.observer;

import org.json.JSONObject;

public interface Observable {
    void register(Observer observer);
    void unregister(Observer observer);
    void notifyObservers(JSONObject jsonObject);
}
