package io.github.chronosx88.influence.observable;

import org.json.JSONObject;

import java.util.ArrayList;

import io.github.chronosx88.influence.contracts.observer.Observable;
import io.github.chronosx88.influence.contracts.observer.Observer;

public class MainObservable implements Observable {
    public static final int UI_ACTIONS_CHANNEL = 0;
    public static final int OTHER_ACTIONS_CHANNEL = 1;

    private ArrayList<Observer> uiObservers;
    private ArrayList<Observer> otherObservers;

    public MainObservable() {
        this.uiObservers = new ArrayList<>();
        this.otherObservers = new ArrayList<>();
    }

    @Override
    public void register(Observer observer, int channelID) {
        switch (channelID) {
            case UI_ACTIONS_CHANNEL: {
                uiObservers.add(observer);
                break;
            }

            case OTHER_ACTIONS_CHANNEL: {
                otherObservers.add(observer);
                break;
            }

            default: {
                otherObservers.add(observer);
                break;
            }
        }
    }

    @Override
    public void unregister(Observer observer, int channelID) {
        switch (channelID) {
            case UI_ACTIONS_CHANNEL: {
                uiObservers.remove(observer);
                break;
            }

            case OTHER_ACTIONS_CHANNEL: {
                otherObservers.remove(observer);
                break;
            }
        }
    }

    @Override
    public void notifyObservers(JSONObject jsonObject, int channelID) {
        switch (channelID) {
            case UI_ACTIONS_CHANNEL: {
                for (Observer observer : uiObservers) {
                    observer.handleEvent(jsonObject);
                }
                break;
            }

            case OTHER_ACTIONS_CHANNEL: {
                for (Observer observer : otherObservers) {
                    observer.handleEvent(jsonObject);
                }
                break;
            }
            default: {
                for (Observer observer : otherObservers) {
                    observer.handleEvent(jsonObject);
                }
                break;
            }
        }
    }
}
