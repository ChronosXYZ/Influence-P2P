package io.github.chronosx88.influence.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.contracts.chatactivity.ChatViewContract;
import io.github.chronosx88.influence.helpers.ChatAdapter;
import io.github.chronosx88.influence.models.roomEntities.MessageEntity;
import io.github.chronosx88.influence.presenters.ChatPresenter;

public class ChatActivity extends AppCompatActivity implements ChatViewContract {
    private ChatAdapter chatAdapter;
    private RecyclerView messageList;
    private ImageButton sendMessageButton;
    private EditText messageTextEdit;
    private TextView contactUsernameTextView;
    private ChatPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        presenter = new ChatPresenter(this, intent.getStringExtra("chatID"));

        Toolbar toolbar = findViewById(R.id.toolbar_chat_activity);
        setSupportActionBar(toolbar);
        messageList = findViewById(R.id.message_list);
        chatAdapter = new ChatAdapter();
        presenter.updateAdapter();
        messageList.setAdapter(chatAdapter);
        messageList.setLayoutManager(new LinearLayoutManager(this));
        contactUsernameTextView = findViewById(R.id.appbar_username);
        messageTextEdit = findViewById(R.id.message_input);
        sendMessageButton = findViewById(R.id.send_button);
        sendMessageButton.setOnClickListener((v) -> {
            presenter.sendMessage(messageTextEdit.getText().toString());
        });
        contactUsernameTextView.setText(intent.getStringExtra("contactUsername"));


    }

    @Override
    public void updateMessageList(MessageEntity message) {
        runOnUiThread(() -> {
            chatAdapter.addMessage(message);
            chatAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void updateMessageList(List<MessageEntity> messages) {
        runOnUiThread(() -> {
            chatAdapter.addMessages(messages);
            chatAdapter.notifyDataSetChanged();
        });
    }
}
