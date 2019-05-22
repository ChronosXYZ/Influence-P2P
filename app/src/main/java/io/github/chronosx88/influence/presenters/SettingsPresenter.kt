package io.github.chronosx88.influence.presenters

import android.os.Handler
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.logic.SettingsLogic

class SettingsPresenter(private val view: CoreContracts.ISettingsView) : CoreContracts.ISettingsPresenter {
    private val mainThreadHandler: Handler = Handler(AppHelper.getContext().mainLooper)
    private val logic: SettingsLogic = SettingsLogic()
}