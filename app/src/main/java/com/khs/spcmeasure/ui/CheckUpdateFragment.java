package com.khs.spcmeasure.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.khs.spcmeasure.Globals;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.library.VersionUtils;
import com.khs.spcmeasure.service.SimpleCodeService;
import com.khs.spcmeasure.tasks.CheckVersionTask;

/**
 * A simple {@link Fragment} subclass.
 * handles version check and update action
 * see the following for how to handle asynctaks and configuration changes:
 * https://androidresearch.wordpress.com/2013/05/10/dealing-with-asynctask-and-screen-orientation/
 */
public class CheckUpdateFragment extends Fragment implements CheckVersionTask.OnCheckVersionListener{

    private final String TAG = "CheckUpdateFragment";

    private Context mAppContext;
    private ProgressDialog mProgressDialog;
    private boolean mIsTaskRunning = false;
    private CheckVersionTask mCheckVersionTask;

    // views
    private TextView mTxtInstallVersion;
    private TextView mTxtLatestVersion;
    private TextView mTxtVersionInfo;
    private Button mBtnInstallUpdate;

    // exit if version is ok
    private boolean mExitIfOk = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain fragment instance on configuration changes
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAppContext = getActivity().getApplicationContext();

        // store layout views
        View rootView = getView();
        mTxtInstallVersion = (TextView) rootView.findViewById(R.id.txtInstallVersion);
        mTxtLatestVersion = (TextView) rootView.findViewById(R.id.txtLatestVersion);
        mTxtVersionInfo = (TextView) rootView.findViewById(R.id.txtVersionInfo);
        mBtnInstallUpdate = (Button) rootView.findViewById(R.id.btnInstallUpdate);

        // display fields
        displayFields();

        // If we are returning here from a screen orientation
        // and the AsyncTask is still working, re-create and display the
        // progress dialog.
        if (mIsTaskRunning) {
            // TODO use String constants
            mProgressDialog = ProgressDialog.show(getActivity(), getString(R.string.text_checking_version), getString(R.string.text_please_wait));
        } else {
            mCheckVersionTask = new CheckVersionTask(mAppContext, this);
            mCheckVersionTask.execute();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // extract bundle arguments
        // see:
        // http://stackoverflow.com/questions/12739909/send-data-from-activity-to-fragment-in-android
        mExitIfOk = getArguments().getBoolean(CheckUpdateActivity.KEY_EXIT_IF_OK, false);

        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_check_update, container, false);
    }

    @Override
    public void onDetach() {
        // All dialogs should be closed before leaving the activity in order to avoid
        // the: Activity has leaked window com.android.internal.policy... exception
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        super.onDetach();
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

    @Override
    public void onCheckVersionStarted() {
        mIsTaskRunning = true;
        // TODO use String constants
        mProgressDialog = ProgressDialog.show(getActivity(), getString(R.string.text_checking_version), getString(R.string.text_please_wait));
    }

    @Override
    public void onCheckVersionFinished() {

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mIsTaskRunning = false;

        // get globals for version info
        Globals g = Globals.getInstance();

        if (g.isVersionOk()) {
            // import Action Cause Simple Codes
            SimpleCodeService.startActionImport(mAppContext, SimpleCodeService.TYPE_ACTION_CAUSE);

            // import Gauge Audit Simple Codes
            SimpleCodeService.startActionImport(mAppContext, SimpleCodeService.TYPE_GAUGE_AUDIT);

            // exit if required
            if (mExitIfOk) {
                if (getActivity() != null) {
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }
            }
        }

        // TODO display latest version and version message
        String latestVersion = g.getLatestName() + " (" + g.getLatestCode() + ")";
        mTxtLatestVersion.setText(latestVersion);

        // check if latest version is not installed
        if (VersionUtils.isVersionCodeChanged(mAppContext, g.getLatestCode())) {
            // latest version not installed
            mTxtVersionInfo.setText(R.string.text_version_newer_available);
            mBtnInstallUpdate.setVisibility(View.VISIBLE);
        } else {
            // latest version is installed
            mTxtVersionInfo.setText(R.string.text_version_latest_installed);
            mBtnInstallUpdate.setVisibility(View.INVISIBLE);
        }

    }
}
