package io.github.chronosx88.influence.contracts

import android.content.Context
import android.content.Intent
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import com.stfalcon.chatkit.messages.MessagesListAdapter

import io.github.chronosx88.influence.models.GenericDialog
import io.github.chronosx88.influence.models.GenericMessage
import io.github.chronosx88.influence.models.roomEntities.ChatEntity
import io.github.chronosx88.influence.models.roomEntities.MessageEntity

interface CoreContracts {

    interface ViewWithLoadingScreen {
        fun loadingScreen(state: Boolean);
    }

    // -----ChatList-----

    interface IDialogListLogicContract {
        fun loadAllChats(): List<ChatEntity>
    }

    interface IDialogListPresenterContract {
        fun openChat(chatID: String)
    }

    interface IChatListViewContract {
        fun setDialogAdapter(adapter: DialogsListAdapter<GenericDialog>)
        fun startActivity(intent: Intent)
        fun getActivityContext(): Context?
    }

    // -----MainActivity-----

    interface IMainLogicContract {
    }

    interface IMainPresenterContract {
        fun initPeer()
        fun startChatWithPeer(username: String)
        fun onDestroy()
    }

    interface IMainViewContract {
        fun showSnackbar(message: String)
        fun showProgressBar(state: Boolean)
    }

    // -----ChatActivity-----

    interface IChatLogicContract {
        fun sendMessage(text: String): MessageEntity
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

    interface ISettingsLogic {
        fun checkUsernameExists(username: String) : Boolean
    }

    interface ISettingsPresenter {
        fun updateUsername(username: String)
    }

    interface ISettingsView {
        fun loadingScreen(state: Boolean)
        fun showMessage(message: String)
        fun refreshScreen()
    }

    // -----LoginActivity-----
    interface ILoginViewContract : ViewWithLoadingScreen
}
