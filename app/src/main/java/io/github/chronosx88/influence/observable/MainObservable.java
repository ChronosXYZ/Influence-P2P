package io.github.chronosx88.influence.observable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.github.chronosx88.influence.contracts.observer.IObservable;
import io.github.chronosx88.influence.contracts.observer.IObserver;

public class MainObservable implements IObservable {
    private ArrayList<IObserver> uiObservers = new ArrayList<>();

    @Override
    public void register(IObserver observer) {
        uiObservers.add(observer);
    }


    @Override
    public void unregister(IObserver observer) {
        uiObservers.remove(observer);
    }

    @Override
    public void notifyUIObservers(JSONObject jsonObject) {
        for (IObserver observer : uiObservers) {
            try {
                observer.handleEvent(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
