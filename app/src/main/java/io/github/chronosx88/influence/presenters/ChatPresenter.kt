package io.github.chronosx88.influence.presenters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.google.gson.Gson
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessagesListAdapter
import io.github.chronosx88.influence.R
import io.github.chronosx88.influence.XMPPConnectionService
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.helpers.LocalDBWrapper
import io.github.chronosx88.influence.logic.ChatLogic
import io.github.chronosx88.influence.models.GenericMessage
import io.github.chronosx88.influence.models.roomEntities.ChatEntity
import io.github.chronosx88.influence.models.roomEntities.MessageEntity

class ChatPresenter(private val view: CoreContracts.IChatViewContract, private val chatID: String) : CoreContracts.IChatPresenterContract {
    private val logic: CoreContracts.IChatLogicContract
    private val chatEntity: ChatEntity?
    private val gson: Gson
    private val chatAdapter: MessagesListAdapter<GenericMessage>
    private lateinit var newMessageReceiver: BroadcastReceiver

    init {
        this.logic = ChatLogic(LocalDBWrapper.getChatByChatID(chatID)!!)
        this.chatEntity = LocalDBWrapper.getChatByChatID(chatID)
        gson = Gson()
        chatAdapter = MessagesListAdapter(AppHelper.getJid(), ImageLoader { imageView, _, _ -> imageView.setImageResource(R.mipmap.ic_launcher) })
        view.setAdapter(chatAdapter)

        setupIncomingMessagesReceiver()
    }

    override fun sendMessage(text: String): Boolean {
        val message: MessageEntity? = logic.sendMessage(text)
        if(message != null) {
            chatAdapter.addToStart(GenericMessage(message), true)
            return true
        }
        return false
    }

    override fun loadLocalMessages() {
        val entities: List<MessageEntity>? = LocalDBWrapper.getMessagesByChatID(chatID)
        val messages = ArrayList<GenericMessage>()
        if(entities != null) {
            entities.forEach {
                messages.add(GenericMessage(it))
            }
        }
        chatAdapter.addToEnd(messages, true)
    }

    override fun onDestroy() {
        //
    }

    private fun setupIncomingMessagesReceiver() {
        newMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if(intent.getStringExtra(XMPPConnectionService.MESSAGE_CHATID).equals(chatEntity!!.jid)) {
                    val messageID = intent.getLongExtra(XMPPConnectionService.MESSAGE_ID, -1)
                    chatAdapter.addToStart(GenericMessage(LocalDBWrapper.getMessageByID(messageID)), true)
                }
            }
        }
        val filter = IntentFilter()
        filter.addAction(XMPPConnectionService.INTENT_NEW_MESSAGE)
        AppHelper.getContext().registerReceiver(newMessageReceiver, filter)
    }

}
