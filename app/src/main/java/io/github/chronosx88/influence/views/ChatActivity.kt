package io.github.chronosx88.influence.views

import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.chronosx88.influence.R
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.helpers.ChatAdapter
import io.github.chronosx88.influence.models.roomEntities.MessageEntity
import io.github.chronosx88.influence.presenters.ChatPresenter

class ChatActivity : AppCompatActivity(), CoreContracts.IChatViewContract {
    private var chatAdapter: ChatAdapter? = null
    private var messageList: RecyclerView? = null
    private var sendMessageButton: ImageButton? = null
    private var messageTextEdit: EditText? = null
    private var contactUsernameTextView: TextView? = null
    private var presenter: ChatPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intent = intent

        presenter = ChatPresenter(this, intent.getStringExtra("chatID"))
        val toolbar = findViewById<Toolbar>(R.id.toolbar_chat_activity)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        messageList = findViewById(R.id.message_list)
        chatAdapter = ChatAdapter()
        presenter!!.updateAdapter()
        messageList!!.adapter = chatAdapter
        messageList!!.layoutManager = LinearLayoutManager(this)
        contactUsernameTextView = findViewById(R.id.appbar_username)
        messageTextEdit = findViewById(R.id.message_input)
        sendMessageButton = findViewById(R.id.send_button)
        sendMessageButton!!.setOnClickListener sendMessageButton@{
            if (messageTextEdit!!.text.toString() == "") {
                return@sendMessageButton
            }
            presenter!!.sendMessage(messageTextEdit!!.text.toString())
            messageTextEdit!!.setText("")
        }
        contactUsernameTextView!!.text = intent.getStringExtra("contactUsername")
        messageList!!.scrollToPosition(chatAdapter!!.itemCount - 1)
    }

    override fun updateMessageList(message: MessageEntity) {
        runOnUiThread {
            chatAdapter!!.addMessage(message)
            messageList!!.scrollToPosition(chatAdapter!!.itemCount - 1)
            chatAdapter!!.notifyDataSetChanged()
        }
    }

    override fun updateMessageList(messages: List<MessageEntity>) {
        runOnUiThread {
            chatAdapter!!.addMessages(messages)
            messageList!!.scrollToPosition(chatAdapter!!.itemCount - 1)
            chatAdapter!!.notifyDataSetChanged()
        }
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
}
