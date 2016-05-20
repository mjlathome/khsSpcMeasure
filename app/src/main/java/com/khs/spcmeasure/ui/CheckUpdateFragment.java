package com.khs.spcmeasure.ui;

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.khs.spcmeasure.R;

/**
 * A simple {@link Fragment} subclass.
 * handles version check and update action
 */
public class CheckUpdateFragment extends Fragment {

    // views
    private TextView mTxtInstallVersion;
    private TextView mTxtLatestVersion;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_check_update, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // store layout views
        View rootView = getView();
        mTxtInstallVersion = (TextView) rootView.findViewById(R.id.txtInstallVersion);
        mTxtLatestVersion = (TextView) rootView.findViewById(R.id.txtLatestVersion);

        // display fields
        displayFields();
    }

    // display version
    private void displayFields() {
        String installVersion = null;

        // extract version
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            installVersion = pInfo.versionName + " (" + pInfo.versionCode + ")";
        } catch(Exception e) {
            e.printStackTrace();
        }

        // display version
        if (installVersion != null) {
            mTxtInstallVersion.setText(installVersion);
        }

    }
}
