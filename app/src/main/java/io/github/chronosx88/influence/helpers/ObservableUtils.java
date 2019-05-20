package io.github.chronosx88.influence.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ObservableUtils {
    public static void notifyUI(int action) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", action);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AppHelper.getObservable().notifyUIObservers(jsonObject);
    }

    public static void notifyUI(int action, String... additional) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", action);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = new JSONArray();
        for(String info : additional) {
            jsonArray.put(info);
        }
        try {
            jsonObject.put("additional", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AppHelper.getObservable().notifyUIObservers(jsonObject);
    }

    public static void notifyUI(int action, int additional) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", action);
            jsonObject.put("additional", additional);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AppHelper.getObservable().notifyUIObservers(jsonObject);
    }
}
