package io.github.chronosx88.influence.presenters

import android.os.Handler
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.logic.SettingsLogic

class SettingsPresenter(private val view: CoreContracts.ISettingsView) : CoreContracts.ISettingsPresenter {
    private val mainThreadHandler: Handler = Handler(AppHelper.getContext().mainLooper)
    private val logic: SettingsLogic = SettingsLogic()


    override fun updateUsername(username: String) {
        /*view.loadingScreen(true)
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
        }*/
    }
}