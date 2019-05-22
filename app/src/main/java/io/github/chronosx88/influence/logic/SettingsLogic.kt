package io.github.chronosx88.influence.logic

import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.helpers.KeyPairManager

class SettingsLogic : CoreContracts.ISettingsLogic {
    companion object {
        private val LOG_TAG: String = "SettingsLogic"
        private val keyPairManager = KeyPairManager()
    }
}