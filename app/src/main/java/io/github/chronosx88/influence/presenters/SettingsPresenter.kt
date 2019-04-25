package io.github.chronosx88.influence.presenters

import android.content.SharedPreferences
import android.os.Handler
import com.google.gson.JsonObject
import io.github.chronosx88.influence.R
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.contracts.observer.IObserver
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.helpers.ObservableUtils
import io.github.chronosx88.influence.helpers.actions.UIActions
import io.github.chronosx88.influence.logic.SettingsLogic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingsPresenter(private val view: CoreContracts.ISettingsView) : CoreContracts.ISettingsPresenter, IObserver {
    private val mainThreadHandler: Handler = Handler(AppHelper.getContext().mainLooper)
    private val logic: SettingsLogic = SettingsLogic()

    init {
        AppHelper.getObservable().register(this)
    }

    override fun updateUsername(username: String) {
        view.loadingScreen(true)
        val editor: SharedPreferences.Editor = AppHelper.getPreferences().edit()

        GlobalScope.launch {
            val oldUsername = AppHelper.getPreferences().getString("username", null)
            if(username.equals("")) {
                SettingsLogic.publishUsername(oldUsername, null)
                editor.remove("username")
                editor.apply()
                AppHelper.updateUsername(null)
                ObservableUtils.notifyUI(UIActions.USERNAME_AVAILABLE)
                return@launch
            }
            if(!logic.checkUsernameExists(username)) {
                // Save username in SharedPreferences
                if(username.equals("")) {
                    editor.remove("username")
                } else {
                    editor.putString("username", username)
                }
                editor.apply()
                AppHelper.updateUsername(if (username.equals("")) null else username)

                // Publish username on DHT network
                SettingsLogic.publishUsername(oldUsername, username)

                ObservableUtils.notifyUI(UIActions.USERNAME_AVAILABLE)
            } else {
                ObservableUtils.notifyUI(UIActions.USERNAME_ISNT_AVAILABLE)
            }
        }
    }

    override fun handleEvent(json: JsonObject) {
        val post = {
            when (json.get("action").asInt) {
                UIActions.USERNAME_AVAILABLE -> {
                    view.loadingScreen(false)
                    view.showMessage(AppHelper.getContext().getString(R.string.username_saved))
                }
                UIActions.USERNAME_ISNT_AVAILABLE -> {
                    view.loadingScreen(false)
                    view.showMessage(AppHelper.getContext().getString(R.string.username_isnt_saved))
                }
            }
        }
        mainThreadHandler.post(post)
    }
}