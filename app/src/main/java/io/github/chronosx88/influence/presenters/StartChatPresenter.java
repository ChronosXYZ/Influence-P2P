package io.github.chronosx88.influence.presenters;

import com.google.gson.JsonObject;

import io.github.chronosx88.influence.contracts.observer.IObserver;
import io.github.chronosx88.influence.contracts.startchat.IStartChatLogicContract;
import io.github.chronosx88.influence.contracts.startchat.IStartChatPresenterContract;
import io.github.chronosx88.influence.contracts.startchat.IStartChatViewContract;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.logic.StartChatLogic;

public class StartChatPresenter implements IStartChatPresenterContract, IObserver {
    private IStartChatViewContract view;
    private IStartChatLogicContract logic;

    public StartChatPresenter(IStartChatViewContract view) {
        this.view = view;
        this.logic = new StartChatLogic();
        AppHelper.getObservable().register(this);
    }

    @Override
    public void startChatWithPeer(String peerID) {
        view.showProgressDialog(true);
        logic.sendStartChatMessage(peerID);
    }

    @Override
    public void handleEvent(JsonObject object) {
        switch (object.get("action").getAsInt()) {
            case UIActions.PEER_NOT_EXIST: {
                view.showProgressDialog(false);
                view.showMessage("Данный узел не существует!");
                break;
            }

            case UIActions.NEW_CHAT: {
                view.showProgressDialog(false);
                view.showMessage("Чат успешно создан!");
                break;
            }

            case UIActions.NODE_IS_OFFLINE: {
                view.showProgressDialog(false);
                view.showMessage("Нода не запущена!");
                break;
            }
        }
    }
}
