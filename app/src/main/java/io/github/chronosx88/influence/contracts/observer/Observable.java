package io.github.chronosx88.influence.contracts.observer;

import com.google.gson.JsonObject;

public interface Observable {
    void register(Observer observer, int channelID);
    void unregister(Observer observer, int channelID);
    void notifyObservers(JsonObject jsonObject, int channelID);
}
