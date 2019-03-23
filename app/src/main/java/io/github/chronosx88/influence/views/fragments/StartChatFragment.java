package io.github.chronosx88.influence.views.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.contracts.startchat.StartChatViewContract;
import io.github.chronosx88.influence.presenters.StartChatPresenter;

public class StartChatFragment extends Fragment implements StartChatViewContract {
    private TextInputLayout textInputPeerID;
    private ProgressDialog progressDialog;
    private Button createChatButton;
    private StartChatPresenter presenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.start_chat_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new StartChatPresenter(this);
        textInputPeerID = view.findViewById(R.id.textInputPeerID);
        progressDialog = new ProgressDialog(getActivity(), R.style.AlertDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        createChatButton = view.findViewById(R.id.create_chat_button);
        createChatButton.setOnClickListener((v) -> {
            presenter.startChatWithPeer(textInputPeerID.getEditText().getText().toString());
        });
    }

    @Override
    public void showMessage(String message) {
        requireActivity().runOnUiThread(() -> {
            Snackbar.make(getView().findViewById(R.id.start_chat_coordinator), message, Snackbar.LENGTH_SHORT).show();
        });
    }

    @Override
    public void showProgressDialog(boolean enabled) {
        if(enabled) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    // TODO: clear text input
}
