package io.github.chronosx88.influence.presenters;

import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.logic.MainLogic;

public class MainPresenter implements CoreContracts.IMainPresenterContract {
    private CoreContracts.IMainLogicContract logic;
    private CoreContracts.IMainViewContract view;

    public MainPresenter(CoreContracts.IMainViewContract view) {
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
