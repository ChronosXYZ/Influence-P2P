package io.github.chronosx88.influence.presenters;

import io.github.chronosx88.influence.contracts.MainLogicContract;
import io.github.chronosx88.influence.contracts.MainPresenterContract;
import io.github.chronosx88.influence.contracts.MainViewContract;
import io.github.chronosx88.influence.logic.MainLogic;

public class MainPresenter implements MainPresenterContract {
    private MainLogicContract logic;
    private MainViewContract view;

    public MainPresenter(MainViewContract view) {
        this.view = view;
        logic = new MainLogic();
    }

    @Override
    public void initPeer() {
        logic.initPeer();
    }

    @Override
    public void onDestroy() {
        logic.shutdownPeer();
    }
}
