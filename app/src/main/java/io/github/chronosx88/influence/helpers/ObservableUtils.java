package io.github.chronosx88.influence.helpers;

import com.google.gson.JsonObject;

public class ObservableUtils {
    public static void notifyUI(int action) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", action);
        AppHelper.getObservable().notifyUIObservers(jsonObject);
    }
}
