package com.khs.spcmeasure.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.khs.spcmeasure.Globals;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.VersionUtils;
import com.khs.spcmeasure.service.SimpleCodeService;
import com.khs.spcmeasure.tasks.CheckVersionTask;
import com.khs.spcmeasure.tasks.UpdateApp;
import com.khs.spcmeasure.tasks.WaitWiFiTask;

/**
 * A simple {@link Fragment} subclass.
 * handles version check and update action
 * see the following for how to handle asynctaks and configuration changes:
 * https://androidresearch.wordpress.com/2013/05/10/dealing-with-asynctask-and-screen-orientation/
 */
public class CheckUpdateFragment extends Fragment implements View.OnClickListener, WaitWiFiTask.OnWaitWiFiListener, CheckVersionTask.OnCheckVersionListener, UpdateApp.OnUpdateAppListener {

    private final String TAG = "CheckUpdateFragment";

    // url for spcMeasure apk
    private static final String url = "http://thor.magna.global/spc/apk/spcMeasure/app-release.apk";
    // private static final String url = "http://thor.magna.global/spc/apk/spcMeasure/app-debug.apk";

    private Context mAppContext;
    private ProgressDialog mProgressDialog;
    private ProgressDialog mProgressDialogUpdateApp;
    private ProgressDialog mProgressDialogWaitWiFi;

    private boolean mIsTaskRunning = false;
    private boolean mIsTaskRunningUpdateApp = false;
    private boolean mIsTaskRunningWaitWiFi = false;

    // views
    private TextView mTxtInstallVersion;
    private TextView mTxtLatestVersion;
    private TextView mTxtVersionInfo;
    private Button mBtnUpdateApp;

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
        mBtnUpdateApp = (Button) rootView.findViewById(R.id.btnUpdateApp);

        // display fields
        displayFields();

        // If we are returning here from a screen orientation
        // and the AsyncTask is still working, re-create and display the
        // progress dialog.
        if (mIsTaskRunning) {
            mProgressDialog = ProgressDialog.show(getActivity(), getString(R.string.text_checking_version), getString(R.string.text_please_wait));
        } else if (mIsTaskRunningUpdateApp) {
            mProgressDialogUpdateApp = ProgressDialog.show(getActivity(), getString(R.string.text_updating_app), getString(R.string.text_please_wait));
        } else if (mIsTaskRunningWaitWiFi) {
            mProgressDialogWaitWiFi = ProgressDialog.show(getActivity(), getString(R.string.text_checking_wifi_conn), getString(R.string.text_please_wait));
        } else {
            WaitWiFiTask waitWiFiTask = new WaitWiFiTask(mAppContext, this);
            waitWiFiTask.execute();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // extract bundle arguments
        // see:
        // http://stackoverflow.com/questions/12739909/send-data-from-activity-to-fragment-in-android
        mExitIfOk = getArguments().getBoolean(CheckUpdateActivity.KEY_EXIT_IF_OK, false);

        // create listener for update app button
        View v = inflater.inflate(R.layout.fragment_check_update, container, false);

        // see
        // http://stackoverflow.com/questions/6091194/how-to-handle-button-clicks-using-the-xml-onclick-within-fragments/6271637#6271637
        Button bUpdateApp = (Button) v.findViewById(R.id.btnUpdateApp);
        bUpdateApp.setOnClickListener(this);

        // inflate the layout for this fragment
        return v;
    }

    @Override
    public void onDetach() {
        // All dialogs should be closed before leaving the activity in order to avoid
        // the: Activity has leaked window com.android.internal.policy... exception
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        if (mProgressDialogUpdateApp != null && mProgressDialogUpdateApp.isShowing()) {
            mProgressDialogUpdateApp.dismiss();
        }

        if (mProgressDialogWaitWiFi != null && mProgressDialogWaitWiFi.isShowing()) {
            mProgressDialogWaitWiFi.dismiss();
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

    // handle update app button click
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnUpdateApp:
                Log.d(TAG, "onClick - btnUpdateApp");
                UpdateApp updateApp = new UpdateApp(mAppContext, this);
                updateApp.execute(url);
                break;
        }
    }

    @Override
    public void onWaitWiFiStarted() {
        mIsTaskRunningWaitWiFi = true;
        mProgressDialogWaitWiFi = ProgressDialog.show(getActivity(), getString(R.string.text_checking_wifi_conn), getString(R.string.text_please_wait));
    }

    @Override
    public void onWaitWiFiFinished(boolean isWiFiConn) {
        // TODO handle when wifi not connected
        if (mProgressDialogWaitWiFi != null) {
            mProgressDialogWaitWiFi.dismiss();
        }
        mIsTaskRunningWaitWiFi = false;

        // run the check version task
        CheckVersionTask checkVersionTask = new CheckVersionTask(mAppContext, this);
        checkVersionTask.execute();
    }

    @Override
    public void onCheckVersionStarted() {
        mIsTaskRunning = true;
        mProgressDialog = ProgressDialog.show(getActivity(), getString(R.string.text_checking_version), getString(R.string.text_please_wait));
    }

    @Override
    public void onCheckVersionFinished() {

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mIsTaskRunning = false;

        // version code changed
        boolean verCodeChanged = false;

        // get globals for version info
        Globals g = Globals.getInstance();

        if (g.isVersionOk()) {
            // calculate whether version code has changed
            verCodeChanged = VersionUtils.isVersionCodeChanged(mAppContext);

            // import Action Cause Simple Codes
            SimpleCodeService.startActionImport(mAppContext, SimpleCodeService.TYPE_ACTION_CAUSE);

            // import Gauge Audit Simple Codes
            SimpleCodeService.startActionImport(mAppContext, SimpleCodeService.TYPE_GAUGE_AUDIT);

            // exit if required
            if (!verCodeChanged && mExitIfOk) {
                if (getActivity() != null) {
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }
            }

            // check if latest version is not installed
            if (verCodeChanged) {
                // latest version not installed
                mTxtVersionInfo.setText(R.string.text_version_newer_available);
                mBtnUpdateApp.setVisibility(View.VISIBLE);
            } else {
                // latest version is installed
                mTxtVersionInfo.setText(R.string.text_version_latest_installed);
                mBtnUpdateApp.setVisibility(View.INVISIBLE);
            }
        } else {
            // latest version could not be checked
            mTxtVersionInfo.setText(R.string.text_version_check_failed);
            mBtnUpdateApp.setVisibility(View.INVISIBLE);
            if (getActivity() != null) {
                AlertUtils.errorDialogShow(getActivity(), getString(R.string.text_version_check_failed) + "  " + getString(R.string.text_contact_admin));
            }
        }

        // display latest version and version message
        mTxtLatestVersion.setText(VersionUtils.getLatestVersion());
    }

    @Override
    public void onUpdateAppStarted() {
        mIsTaskRunningUpdateApp = true;
        mProgressDialogUpdateApp = ProgressDialog.show(getActivity(), getString(R.string.text_updating_app), getString(R.string.text_please_wait));
    }

    @Override
    public void onUpdateAppFinished() {
        if (mProgressDialogUpdateApp != null) {
            mProgressDialogUpdateApp.dismiss();
        }
        mIsTaskRunningUpdateApp = false;
    }
}
