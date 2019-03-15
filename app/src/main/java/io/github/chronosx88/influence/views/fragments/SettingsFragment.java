package io.github.chronosx88.influence.views.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;
import io.github.chronosx88.influence.R;
import io.github.chronosx88.influence.helpers.AppHelper;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.main_settings);
        getPreferenceScreen().getPreference(0).setSummary(AppHelper.getPeerID());
        getPreferenceScreen().getPreference(0).setOnPreferenceClickListener((preference -> {
            ClipboardManager clipboard = (ClipboardManager) AppHelper.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", AppHelper.getPeerID());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(AppHelper.getContext(), "Скопировано в буфер обмена!", Toast.LENGTH_SHORT).show();
            return false;
        }));
    }
}
