package io.github.chronosx88.influence.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.contracts.chatlist.ChatListPresenterContract;
import io.github.chronosx88.influence.contracts.chatlist.ChatListViewContract;
import io.github.chronosx88.influence.contracts.observer.Observer;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.ChatListAdapter;
import io.github.chronosx88.influence.helpers.UIActions;
import io.github.chronosx88.influence.observable.MainObservable;
import io.github.chronosx88.influence.presenters.ChatListPresenter;

public class ChatListFragment extends Fragment implements ChatListViewContract, Observer {
    private ChatListPresenterContract presenter;
    private RecyclerView chatList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new ChatListPresenter(this);
        AppHelper.getObservable().register(this, MainObservable.UI_ACTIONS_CHANNEL);
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
        presenter.updateChatList();
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
    public void handleEvent(JSONObject object) {
        try {
            switch (object.getInt("action")) {
                case UIActions.NEW_CHAT: {
                    presenter.updateChatList();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
