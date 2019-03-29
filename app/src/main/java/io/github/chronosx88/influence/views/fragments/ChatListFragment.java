package io.github.chronosx88.influence.views.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.contracts.chatlist.IChatListPresenterContract;
import io.github.chronosx88.influence.contracts.chatlist.IChatListViewContract;
import io.github.chronosx88.influence.contracts.observer.IObserver;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.ChatListAdapter;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;
import io.github.chronosx88.influence.presenters.ChatListPresenter;

public class ChatListFragment extends Fragment implements IChatListViewContract, IObserver {
    private IChatListPresenterContract presenter;
    private RecyclerView chatList;
    private Handler mainThreadHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppHelper.getObservable().register(this);
        this.mainThreadHandler = new Handler(getContext().getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chatlist_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatList = view.findViewById(R.id.chatlist_container);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));
        presenter = new ChatListPresenter(this);
        presenter.updateChatList();
        registerForContextMenu(chatList);
    }

    @Override
    public void setRecycleAdapter(ChatListAdapter adapter) {
        chatList.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.updateChatList();
    }

    @Override
    public void handleEvent(JsonObject object) {
        switch (object.get("action").getAsInt()) {
            case UIActions.SUCCESSFUL_CREATE_CHAT:
            case UIActions.NEW_CHAT: {
                presenter.updateChatList();
                break;
            }
        }
    }

    @Override
    public void updateChatList(ChatListAdapter adapter, List<ChatEntity> chats) {
        mainThreadHandler.post(() -> {
            adapter.setChatList(chats);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        presenter.onContextItemSelected(item);
        return super.onContextItemSelected(item);
    }
}
