package io.github.chronosx88.influence.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ObservableUtils {
    public static void notifyUI(int action) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", action);
        AppHelper.getObservable().notifyUIObservers(jsonObject);
    }

    public static void notifyUI(int action, String... additional) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", action);
        JsonArray jsonArray = new JsonArray();
        for(String info : additional) {
            jsonArray.add(info);
        }
        jsonObject.add("additional", jsonArray);
        AppHelper.getObservable().notifyUIObservers(jsonObject);
    }

    public static void notifyUI(int action, int additional) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", action);
        jsonObject.addProperty("additional", additional);
        AppHelper.getObservable().notifyUIObservers(jsonObject);
    }
}
