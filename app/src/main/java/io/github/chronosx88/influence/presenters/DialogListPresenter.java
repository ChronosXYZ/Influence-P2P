package io.github.chronosx88.influence.presenters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.appcompat.app.AlertDialog;

import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.XMPPConnectionService;
import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.contracts.observer.IObserver;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.LocalDBWrapper;
import io.github.chronosx88.influence.helpers.ObservableActions;
import io.github.chronosx88.influence.logic.ChatListLogic;
import io.github.chronosx88.influence.models.GenericDialog;
import io.github.chronosx88.influence.views.ChatActivity;
import java8.util.stream.StreamSupport;

public class DialogListPresenter implements CoreContracts.IDialogListPresenterContract, IObserver {
    private CoreContracts.IChatListViewContract view;
    private CoreContracts.IDialogListLogicContract logic;
    private DialogsListAdapter<GenericDialog> dialogListAdapter = new DialogsListAdapter<>((imageView, url, payload) -> {
        imageView.setImageResource(R.mipmap.ic_launcher); // FIXME
    });
    private BroadcastReceiver incomingMessagesReceiver;

    public DialogListPresenter(CoreContracts.IChatListViewContract view) {
        this.view = view;
        dialogListAdapter.setOnDialogClickListener(dialog -> openChat(dialog.getId()));
        dialogListAdapter.setOnDialogLongClickListener(dialog -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getActivityContext());
            builder.setPositiveButton(R.string.ok, (dialog1, id) -> {
                dialogListAdapter.deleteById(dialog.getId());
                AppHelper.getChatDB().chatDao().deleteChat(dialog.getId());
                AppHelper.getChatDB().messageDao().deleteMessagesByChatID(dialog.getId());
            });
            builder.setNegativeButton(R.string.cancel, (dialog2, which) -> {
                //
            });
            builder.setMessage("Remove chat?");
            builder.create().show();
        });
        this.logic = new ChatListLogic();
        this.view.setDialogAdapter(dialogListAdapter);
        ArrayList<GenericDialog> dialogs = new ArrayList<>();
        StreamSupport.stream(logic.loadAllChats())
                .forEach(chatEntity -> dialogs.add(new GenericDialog(chatEntity)));
        dialogListAdapter.setItems(dialogs);
        setupIncomingMessagesReceiver();
        AppHelper.getObservable().register(this);
    }

    @Override
    public void openChat(String chatID) {
        Intent intent = new Intent(AppHelper.getContext(), ChatActivity.class);
        intent.putExtra("chatID", chatID);
        intent.putExtra("chatName", LocalDBWrapper.getChatByChatID(chatID).chatName);
        view.startActivity(intent);
    }

    private void setupIncomingMessagesReceiver() {
        incomingMessagesReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String chatID = intent.getStringExtra(XMPPConnectionService.MESSAGE_CHATID);
                GenericDialog dialog = dialogListAdapter.getItemById(chatID);
                if(dialog == null) {
                    dialogListAdapter.addItem(new GenericDialog(LocalDBWrapper.getChatByChatID(chatID)));
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(XMPPConnectionService.INTENT_NEW_MESSAGE);
        AppHelper.getContext().registerReceiver(incomingMessagesReceiver, filter);
    }

    @Override
    public void handleEvent(JSONObject object) throws JSONException {
        switch (object.getInt("action")) {
            case ObservableActions.NEW_CHAT_CREATED: {
                dialogListAdapter.addItem(new GenericDialog(LocalDBWrapper.getChatByChatID(object.getJSONArray("additional").optString(0))));
                break;
            }
        }
    }
}
