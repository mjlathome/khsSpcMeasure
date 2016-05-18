package com.khs.spcmeasure.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.khs.spcmeasure.R;

/**
 * A simple {@link Fragment} subclass.
 * handles version check and update action
 */
public class CheckUpdateFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_check_update, container, false);
    }
}
