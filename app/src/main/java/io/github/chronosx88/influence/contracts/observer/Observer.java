package io.github.chronosx88.influence.contracts.observer;

import com.google.gson.JsonObject;

public interface Observer {
    void handleEvent(JsonObject object);
}