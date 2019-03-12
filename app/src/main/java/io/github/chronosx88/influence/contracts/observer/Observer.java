package io.github.chronosx88.influence.contracts.observer;

import org.json.JSONObject;

public interface Observer {
    void handleEvent(JSONObject object);
}