package io.github.chronosx88.influence.observable;

import com.google.gson.JsonObject;

import java.util.ArrayList;

import io.github.chronosx88.influence.contracts.observer.INetworkObserver;
import io.github.chronosx88.influence.contracts.observer.IObservable;
import io.github.chronosx88.influence.contracts.observer.IObserver;

public class MainObservable implements IObservable {
    public static final int UI_ACTIONS_CHANNEL = 0;
    public static final int OTHER_ACTIONS_CHANNEL = 1;

    private ArrayList<IObserver> uiObservers;
    private ArrayList<INetworkObserver> networkObservers;

    public MainObservable() {
        this.uiObservers = new ArrayList<>();
        this.networkObservers = new ArrayList<>();
    }

    @Override
    public void register(IObserver observer) {
        uiObservers.add(observer);
    }

    @Override
    public void register(INetworkObserver observer) {
        networkObservers.add(observer);
    }

    @Override
    public void unregister(IObserver observer) {
        uiObservers.remove(observer);
    }

    @Override
    public void unregister(INetworkObserver observer) {
        networkObservers.remove(observer);
    }

    @Override
    public void notifyUIObservers(JsonObject jsonObject) {
        for (IObserver observer : uiObservers) {
            observer.handleEvent(jsonObject);
        }
    }

    @Override
    public void notifyNetworkObservers(Object object) {
        for (INetworkObserver observer : networkObservers) {
            observer.handleEvent(object);
        }
    }
}
