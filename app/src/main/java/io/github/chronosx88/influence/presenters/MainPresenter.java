package io.github.chronosx88.influence.presenters;

import io.github.chronosx88.influence.contracts.mainactivity.IMainLogicContract;
import io.github.chronosx88.influence.contracts.mainactivity.IMainPresenterContract;
import io.github.chronosx88.influence.contracts.mainactivity.IMainViewContract;
import io.github.chronosx88.influence.logic.MainLogic;

public class MainPresenter implements IMainPresenterContract {
    private IMainLogicContract logic;
    private IMainViewContract view;

    public MainPresenter(IMainViewContract view) {
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
