package io.github.chronosx88.influence.contracts.observer;

import org.json.JSONObject;

public interface Observable {
    void register(Observer observer, int channelID);
    void unregister(Observer observer, int channelID);
    void notifyObservers(JSONObject jsonObject, int channelID);
}
