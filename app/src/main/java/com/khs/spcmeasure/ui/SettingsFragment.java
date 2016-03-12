package com.khs.spcmeasure.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.khs.spcmeasure.R;

/**
 * A simple {@link Fragment} subclass.
 * used to define system preferences
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}
