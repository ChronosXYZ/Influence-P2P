package io.github.chronosx88.influence.views.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.presenters.SettingsPresenter;

public class SettingsFragment extends PreferenceFragmentCompat implements CoreContracts.ISettingsView {
    private ProgressDialog progressDialog;
    private SettingsPresenter presenter;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        progressDialog = new ProgressDialog(getContext(), R.style.AlertDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        presenter = new SettingsPresenter(this);
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.main_settings);
        /*getPreferenceScreen().getPreference(0).setSummary(AppHelper.getPeerID());
        getPreferenceScreen().getPreference(0).setOnPreferenceClickListener((preference -> {
            ClipboardManager clipboard = (ClipboardManager) AppHelper.getActivityContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", AppHelper.getPeerID());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(AppHelper.getActivityContext(), "Скопировано в буфер обмена!", Toast.LENGTH_SHORT).show();
            return false;
        }));
        getPreferenceScreen().getPreference(1).setSummary(AppHelper.getUsername());
        getPreferenceScreen().getPreference(1).setOnPreferenceClickListener((v) -> {
            setupUsernameEditDialog().show();
            return true;
        });
        getPreferenceScreen().getPreference(1).setOnPreferenceChangeListener((p, nV) -> {
            getPreferenceScreen().getPreference(1).setSummary((String) nV);
            return true;
        });*/
    }

    @Override
    public void loadingScreen(boolean state) {
        if(state)
            progressDialog.show();
        else
            progressDialog.dismiss();
    }

    @Override
    public void showMessage(@NotNull String message) {
        Toast.makeText(AppHelper.getContext(), message, Toast.LENGTH_LONG).show();
    }

    private AlertDialog.Builder setupUsernameEditDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle(getContext().getString(R.string.username_settings));

        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setSingleLine();
        input.setLayoutParams(lp);
        input.setText(AppHelper.getPreferences().getString("username", null));

        alertDialog.setView(input);
        alertDialog.setPositiveButton(getContext().getString(R.string.ok), (dialog, which) -> presenter.updateUsername(input.getText().toString()));
        alertDialog.setNegativeButton(getContext().getString(R.string.cancel), (dialog, which) -> dialog.cancel());

        return alertDialog;
    }

    @Override
    public void refreshScreen() {
        getPreferenceScreen().getPreference(1).callChangeListener(AppHelper.getPreferences().getString("username", null));
    }

}
