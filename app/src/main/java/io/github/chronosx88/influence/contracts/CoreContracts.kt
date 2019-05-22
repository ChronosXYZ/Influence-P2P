package io.github.chronosx88.influence.contracts

import android.content.Context
import android.content.Intent
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import com.stfalcon.chatkit.messages.MessagesListAdapter

import io.github.chronosx88.influence.models.GenericDialog
import io.github.chronosx88.influence.models.GenericMessage
import io.github.chronosx88.influence.models.roomEntities.ChatEntity
import io.github.chronosx88.influence.models.roomEntities.MessageEntity
import org.jivesoftware.smack.roster.RosterEntry

interface CoreContracts {

    interface IGenericView {
        fun loadingScreen(state: Boolean)
    }

    // -----ChatList-----

    interface IDialogListLogicContract {
        fun loadLocalChats(): List<ChatEntity>
        fun getRemoteContacts(): Set<RosterEntry>?
    }

    interface IDialogListPresenterContract {
        fun openChat(chatID: String)
        fun onStart()
        fun onStop()
        fun loadRemoteContactList()
    }

    interface IChatListViewContract {
        fun setDialogAdapter(adapter: DialogsListAdapter<GenericDialog>)
        fun startActivity(intent: Intent)
        fun getActivityContext(): Context?
    }

    // -----MainActivity-----

    interface IMainLogicContract {
        fun startService()
    }

    interface IMainPresenterContract {
        fun initConnection()
        fun startChatWithPeer(username: String)
    }

    interface IMainViewContract {
        fun showSnackbar(message: String)
        fun showProgressBar(state: Boolean)
    }

    // -----ChatActivity-----

    interface IChatLogicContract {
        fun sendMessage(text: String): MessageEntity?
    }

    interface IChatPresenterContract {
        fun sendMessage(text: String): Boolean
        fun loadLocalMessages()
        fun onDestroy()
    }

    interface IChatViewContract {
        fun setAdapter(adapter: MessagesListAdapter<GenericMessage>)
    }

    // -----SettingsFragment-----

    interface ISettingsLogic // TODO

    interface ISettingsPresenter // TODO

    interface ISettingsView {
        fun loadingScreen(state: Boolean)
        fun showMessage(message: String)
    }

    // -----LoginActivity-----
    interface ILoginViewContract : IGenericView
}
