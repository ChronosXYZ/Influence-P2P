package io.github.chronosx88.influence.contracts.observer;

import org.json.JSONException;
import org.json.JSONObject;

public interface IObserver {
    void handleEvent(JSONObject object) throws JSONException;
}