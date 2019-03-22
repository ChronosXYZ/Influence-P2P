package io.github.chronosx88.influence.observable;

import com.google.gson.JsonObject;

import java.util.ArrayList;

import io.github.chronosx88.influence.contracts.observer.NetworkObserver;
import io.github.chronosx88.influence.contracts.observer.Observable;
import io.github.chronosx88.influence.contracts.observer.Observer;

public class MainObservable implements Observable {
    public static final int UI_ACTIONS_CHANNEL = 0;
    public static final int OTHER_ACTIONS_CHANNEL = 1;

    private ArrayList<Observer> uiObservers;
    private ArrayList<NetworkObserver> networkObservers;

    public MainObservable() {
        this.uiObservers = new ArrayList<>();
        this.networkObservers = new ArrayList<>();
    }

    @Override
    public void register(Observer observer) {
        uiObservers.add(observer);
    }

    @Override
    public void register(NetworkObserver observer) {
        networkObservers.add(observer);
    }

    @Override
    public void unregister(Observer observer) {
        uiObservers.remove(observer);
    }

    @Override
    public void unregister(NetworkObserver observer) {
        networkObservers.remove(observer);
    }

    @Override
    public void notifyUIObservers(JsonObject jsonObject) {
        for (Observer observer : uiObservers) {
            observer.handleEvent(jsonObject);
        }
    }

    @Override
    public void notifyNetworkObservers(Object object) {
        for (NetworkObserver observer : networkObservers) {
            observer.handleEvent(object);
        }
    }
}
