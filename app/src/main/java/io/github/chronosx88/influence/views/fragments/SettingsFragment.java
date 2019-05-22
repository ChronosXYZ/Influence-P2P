package io.github.chronosx88.influence.views.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;

import org.jetbrains.annotations.NotNull;

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
        addPreferencesFromResource(R.xml.main_settings);
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
}
