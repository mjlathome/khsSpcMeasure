package com.khs.spcmeasure.widget;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.khs.spcmeasure.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginActivityFragmentOld extends Fragment {

    public LoginActivityFragmentOld() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_old, container, false);
    }
}
