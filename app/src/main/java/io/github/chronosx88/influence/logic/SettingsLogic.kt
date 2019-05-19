package io.github.chronosx88.influence.logic

import android.util.Log
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.helpers.KeyPairManager
import io.github.chronosx88.influence.helpers.ObservableUtils
import io.github.chronosx88.influence.helpers.actions.UIActions
import net.tomp2p.peers.Number640
import net.tomp2p.storage.Data
import java.io.IOException

class SettingsLogic : CoreContracts.ISettingsLogic {

    override fun checkUsernameExists(username: String) : Boolean {
        if (AppHelper.getPeerDHT() == null) {
            ObservableUtils.notifyUI(UIActions.NODE_IS_OFFLINE)
            return false
        }
        val usernameMap: MutableMap<Number640, Data>? = P2PUtils.get(username)
        usernameMap ?: return false
        return true
    }

    companion object {
        private val LOG_TAG: String = "SettingsLogic"
        private val keyPairManager = KeyPairManager()

        fun publishUsername(oldUsername: String?, username: String?) {
            if (AppHelper.getPeerDHT() == null) {
                ObservableUtils.notifyUI(UIActions.NODE_IS_OFFLINE)
                return
            }
            val mainKeyPair = keyPairManager.openMainKeyPair()
            oldUsername?.let {
                if(!oldUsername.equals("")) {
                    P2PUtils.remove(oldUsername, null, mainKeyPair)
                }
            }

            username?.let {
                var data: Data? = null
                try {
                    data = Data(AppHelper.getPeerID())
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                data!!.protectEntry(mainKeyPair)

                val isSuccess = P2PUtils.put(username, null, data, mainKeyPair)
                Log.i(LOG_TAG, if (isSuccess) "# Username $username is published!" else "# Username $username isn't published!")
            } ?: run {
                return
            }
        }
    }
}