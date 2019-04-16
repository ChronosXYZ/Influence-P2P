package io.github.chronosx88.influence.views.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.google.gson.JsonObject
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.chronosx88.influence.R
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.contracts.observer.IObserver
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.helpers.ChatListAdapter
import io.github.chronosx88.influence.helpers.actions.UIActions
import io.github.chronosx88.influence.models.roomEntities.ChatEntity
import io.github.chronosx88.influence.presenters.ChatListPresenter

class ChatListFragment : Fragment(), CoreContracts.IChatListViewContract, IObserver {
    private var presenter: CoreContracts.IChatListPresenterContract? = null
    private var chatList: RecyclerView? = null
    private var mainThreadHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppHelper.getObservable().register(this)
        this.mainThreadHandler = Handler(context!!.mainLooper)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.chatlist_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatList = view.findViewById(R.id.chatlist_container)
        chatList!!.layoutManager = LinearLayoutManager(context)
        presenter = ChatListPresenter(this)
        presenter!!.updateChatList()
        registerForContextMenu(chatList!!)
    }

    override fun setRecycleAdapter(adapter: ChatListAdapter) {
        chatList!!.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        presenter!!.updateChatList()
    }

    override fun handleEvent(`object`: JsonObject) {
        when (`object`.get("action").asInt) {
            UIActions.SUCCESSFUL_CREATE_CHAT, UIActions.NEW_CHAT -> {
                presenter!!.updateChatList()
            }
        }
    }

    override fun updateChatList(adapter: ChatListAdapter, chats: List<ChatEntity>) {
        mainThreadHandler!!.post {
            adapter.setChatList(chats)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        presenter!!.onContextItemSelected(item)
        return super.onContextItemSelected(item)
    }
}
