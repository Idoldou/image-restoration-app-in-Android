package com.addict.simplecamera;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.hardware.Camera;
import android.preference.ListPreference;
import java.util.ArrayList;
import java.util.List;



public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}