package io.github.chronosx88.influence.views

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import io.github.chronosx88.influence.R
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.helpers.AppHelper
import io.github.chronosx88.influence.models.GenericMessage
import io.github.chronosx88.influence.models.roomEntities.MessageEntity
import io.github.chronosx88.influence.presenters.ChatPresenter

class ChatActivity : AppCompatActivity(), CoreContracts.IChatViewContract {
    private var messageList: MessagesList? = null
    private var messageInput: MessageInput? = null
    private var chatNameTextView: TextView? = null
    private var presenter: ChatPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intent = intent

        val toolbar = findViewById<Toolbar>(R.id.toolbar_chat_activity)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        messageList = findViewById(R.id.messages_list)
        messageList!!.layoutManager = LinearLayoutManager(this)
        chatNameTextView = findViewById(R.id.appbar_username)
        messageInput = findViewById(R.id.message_input)
        messageInput!!.setInputListener {
            presenter!!.sendMessage(it.toString())
        }
        chatNameTextView!!.text = intent.getStringExtra("chatName")
        presenter = ChatPresenter(this, intent.getStringExtra("chatID"))
        presenter!!.loadLocalMessages()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter!!.onDestroy()
    }

    override fun setAdapter(adapter: MessagesListAdapter<GenericMessage>) {
        messageList!!.setAdapter(adapter)
    }
}
