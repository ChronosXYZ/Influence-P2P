package io.github.chronosx88.influence.contracts.observer;

import com.google.gson.JsonObject;

public interface IObserver {
    void handleEvent(JsonObject object);
}