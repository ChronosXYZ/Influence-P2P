package io.github.chronosx88.influence.presenters

import android.content.Intent
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.helpers.LocalDBWrapper
import io.github.chronosx88.influence.logic.MainLogic
import io.github.chronosx88.influence.models.appEvents.AuthenticationStatusEvent
import io.github.chronosx88.influence.models.appEvents.NewChatEvent
import io.github.chronosx88.influence.views.LoginActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainPresenter(private val view: CoreContracts.IMainViewContract) : CoreContracts.IMainPresenterContract {
    private val logic: CoreContracts.IMainLogicContract = MainLogic()

    override fun initConnection() {
        logic.startService()
    }

    override fun startChatWithPeer(username: String) {
        LocalDBWrapper.createChatEntry(username, username)
        EventBus.getDefault().post(NewChatEvent(username))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAuthenticate(event: AuthenticationStatusEvent) {
        when(event.authenticationStatus) {
            AuthenticationStatusEvent.INCORRECT_LOGIN_OR_PASSWORD -> {
                val intent = Intent(AppHelper.getContext(), LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                AppHelper.getContext().startActivity(intent)
            }
        }
    }

    override fun logoutFromAccount() {
        logic.logout()
    }

    override fun onStart() {
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
    }
}
