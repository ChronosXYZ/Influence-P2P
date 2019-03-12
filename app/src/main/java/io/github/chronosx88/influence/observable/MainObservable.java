package io.github.chronosx88.influence.observable;

import org.json.JSONObject;

import java.util.ArrayList;

import io.github.chronosx88.influence.contracts.observer.Observable;
import io.github.chronosx88.influence.contracts.observer.Observer;

public class MainObservable implements Observable {
    private ArrayList<Observer> observers;

    public MainObservable() {
        this.observers = new ArrayList<>();
    }

    @Override
    public void register(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void unregister(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(JSONObject jsonObject) {
        for (Observer observer : observers) {
            observer.handleEvent(jsonObject);
        }
    }
}
