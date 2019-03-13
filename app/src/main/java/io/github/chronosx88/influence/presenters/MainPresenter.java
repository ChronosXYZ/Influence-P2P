package io.github.chronosx88.influence.presenters;

import io.github.chronosx88.influence.contracts.MainModelContract;
import io.github.chronosx88.influence.contracts.MainPresenterContract;
import io.github.chronosx88.influence.contracts.MainViewContract;
import io.github.chronosx88.influence.models.MainModel;

public class MainPresenter implements MainPresenterContract {
    private MainModelContract model;
    private MainViewContract view;

    public MainPresenter(MainViewContract view) {
        this.view = view;
        model = new MainModel();
    }

    @Override
    public void initPeer() {
        model.initPeer();
    }

    @Override
    public void onDestroy() {
        model.shutdownPeer();
    }
}
