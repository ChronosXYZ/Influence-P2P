package io.github.chronosx88.influence.presenters

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.github.chronosx88.influence.R
import io.github.chronosx88.influence.XMPPConnectionService
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.helpers.LocalDBWrapper
import io.github.chronosx88.influence.helpers.ObservableActions
import io.github.chronosx88.influence.helpers.ObservableUtils
import io.github.chronosx88.influence.logic.MainLogic
import io.github.chronosx88.influence.views.LoginActivity
import org.jetbrains.anko.doAsync

class MainPresenter(private val view: CoreContracts.IMainViewContract) : CoreContracts.IMainPresenterContract {
    private val logic: CoreContracts.IMainLogicContract = MainLogic()
    private var broadcastReceiver: BroadcastReceiver? = null


    override fun initPeer() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                when (action) {
                    XMPPConnectionService.INTENT_AUTHENTICATED -> {
                        view.showProgressBar(false)
                    }
                    XMPPConnectionService.INTENT_AUTHENTICATION_FAILED -> {
                        view.showProgressBar(false)
                        val intent = Intent(AppHelper.getContext(), LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        AppHelper.getContext().startActivity(intent)
                    }
                }
            }
        }
        val filter = IntentFilter()
        filter.addAction(XMPPConnectionService.INTENT_AUTHENTICATED)
        filter.addAction(XMPPConnectionService.INTENT_AUTHENTICATION_FAILED)
        AppHelper.getContext().registerReceiver(broadcastReceiver, filter)

        AppHelper.getContext().startService(Intent(AppHelper.getContext(), XMPPConnectionService::class.java))
    }

    override fun startChatWithPeer(username: String) {
        LocalDBWrapper.createChatEntry(username, username)
        ObservableUtils.notifyUI(ObservableActions.NEW_CHAT_CREATED, username)
    }

    override fun onDestroy() {
        //
    }

    // TODO
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = AppHelper.getContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
