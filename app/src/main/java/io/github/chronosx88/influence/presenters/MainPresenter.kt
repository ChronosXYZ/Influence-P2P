package io.github.chronosx88.influence.presenters

import io.github.chronosx88.influence.R
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.logic.MainLogic

class MainPresenter(private val view: CoreContracts.IMainViewContract) : CoreContracts.IMainPresenterContract {
    private val logic: CoreContracts.IMainLogicContract = MainLogic()

    override fun initPeer() {
        if (AppHelper.getPeerDHT() == null) {
            logic.initPeer()
        } else {
            view.showSnackbar(AppHelper.getContext().getString(R.string.node_already_running))
            view.showProgressBar(false)
        }
    }

    override fun onDestroy() {
        logic.shutdownPeer()
    }
}
