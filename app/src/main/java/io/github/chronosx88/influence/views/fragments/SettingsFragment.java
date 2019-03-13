package io.github.chronosx88.influence.views.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import io.github.chronosx88.influence.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.main_settings, s);
    }
}
