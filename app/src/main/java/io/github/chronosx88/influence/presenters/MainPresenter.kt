package io.github.chronosx88.influence.presenters

import com.google.gson.JsonObject
import io.github.chronosx88.influence.R
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.contracts.observer.IObserver
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.helpers.actions.UIActions
import io.github.chronosx88.influence.logic.MainLogic
import org.jetbrains.anko.doAsync

class MainPresenter(private val view: CoreContracts.IMainViewContract) : CoreContracts.IMainPresenterContract, IObserver {
    private val logic: CoreContracts.IMainLogicContract = MainLogic()

    init {
        AppHelper.getObservable().register(this)
    }

    override fun initPeer() {
        if (AppHelper.getPeerDHT() == null) {
            logic.initPeer()
        } else {
            view.showSnackbar(AppHelper.getContext().getString(R.string.node_already_running))
            view.showProgressBar(false)
        }
    }

    override fun startChatWithPeer(username: String) {
        doAsync {
            logic.sendStartChatMessage(username)
        }
    }

    override fun handleEvent(obj: JsonObject) {
        when(obj.get("action").asInt) {
            UIActions.PEER_NOT_EXIST -> {
                view.showProgressBar(false)
                view.showSnackbar("Данный узел не существует!")
            }

            UIActions.NEW_CHAT -> {
                view.showProgressBar(false)
                view.showSnackbar("Чат успешно создан!")
            }

            UIActions.NODE_IS_OFFLINE -> {
                view.showProgressBar(false)
                view.showSnackbar("Нода не запущена!")
            }
        }
    }

    override fun onDestroy() {
        logic.shutdownPeer()
    }
}
