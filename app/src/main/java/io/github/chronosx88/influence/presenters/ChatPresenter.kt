package io.github.chronosx88.influence.presenters

import android.widget.Toast

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.instacart.library.truetime.TrueTime

import java.io.IOException
import java.util.ArrayList
import java.util.UUID

import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.contracts.observer.IObserver
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.helpers.LocalDBWrapper
import io.github.chronosx88.influence.helpers.actions.NetworkActions
import io.github.chronosx88.influence.helpers.actions.UIActions
import io.github.chronosx88.influence.logic.ChatLogic
import io.github.chronosx88.influence.models.roomEntities.ChatEntity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult

class ChatPresenter(private val view: CoreContracts.IChatViewContract, private val chatID: String) : CoreContracts.IChatPresenterContract, IObserver {
    private val logic: CoreContracts.IChatLogicContract
    private val chatEntity: ChatEntity?
    private val gson: Gson

    init {
        this.logic = ChatLogic(LocalDBWrapper.getChatByChatID(chatID)!!)
        this.chatEntity = LocalDBWrapper.getChatByChatID(chatID)

        AppHelper.getObservable().register(this)
        gson = Gson()
    }

    override fun sendMessage(text: String) {
        doAsync {
            val message = LocalDBWrapper.createMessageEntry(NetworkActions.TEXT_MESSAGE, UUID.randomUUID().toString(), chatID, AppHelper.getPeerID(), AppHelper.getPeerID(), TrueTime.now().time, text, false, false)
            logic.sendMessage(message!!)
            view.updateMessageList(message)
        }
    }

    override fun handleEvent(obj: JsonObject) {
        when (obj.get("action").asInt) {
            UIActions.MESSAGE_RECEIVED -> {
                val jsonArray = obj.getAsJsonArray("additional")
                if (jsonArray.get(0).asString != chatID) {
                    return
                }
                val messageEntity = LocalDBWrapper.getMessageByID(jsonArray.get(1).asString)
                view.updateMessageList(messageEntity!!)
            }

            UIActions.NODE_IS_OFFLINE -> {
                Toast.makeText(AppHelper.getContext(), "Нода не запущена!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun updateAdapter() {
        val entities = LocalDBWrapper.getMessagesByChatID(chatID)
        view.updateMessageList(entities ?: ArrayList())
    }

    override fun onDestroy() {
        logic.stopTrackingForNewMsgs()
    }
}
