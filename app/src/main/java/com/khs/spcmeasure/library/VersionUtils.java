package com.khs.spcmeasure.library;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.khs.spcmeasure.R;

import java.util.Arrays;

/**
 * Created by Mark on 3/21/2016.
 */
public class VersionUtils {

    // constants
    public static final String TAG = "VersionUtils";
    public static final int VERSION_CODE_UNKNOWN = -1;
    public static final String VERSION_NAME_UNKNOWN = "";

    // extract version code
    public static int getVersionCode(Context context) {

        try {
            // extract installed version  info
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return VERSION_CODE_UNKNOWN;
    }

    // extract version name
    public static String getVersionName(Context context) {

        try {
            // extract installed version  info
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return VERSION_NAME_UNKNOWN;
    }

    // check version names
    public boolean checkVersionName(String install, String latest) {

        // extract major, minor and patch version info
        String[] installRev = installName.split("\\.");
        String[] latestRev = latestName.split("\\.");

        // check for major revision change
        if (installRev == null || latestRev == null || !installRev[0].equals(latestRev[0]) ) {
            // force logout as too old
            SecurityUtils.setIsLoggedIn(mContext, false);
            message.insert(0, mContext.getString(R.string.text_version_logout_too_old) + "\n");
        }


    }

    // message.append(getBasegetString(R.string.text_version_logout_not_found));


    // extract latest version info
    int latestCode = json.getInt(TAG_CODE);
    String latestName = json.getString(TAG_NAME);

    // extract installed version  info
    PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
    String installName = pInfo.versionName;
    int installCode = pInfo.versionCode;

    // DEBUG remove later
    installName = "0.1.3";
    installCode = 0;

    if (installCode == latestCode) {
        // nothing as installed version is the latest
    } else {
        // extract major, minor and patch version info
        String[] installRev = installName.split("\\.");
        String[] latestRev = latestName.split("\\.");

        Log.d(TAG, "installRev = " + Arrays.toString(installRev));
        Log.d(TAG, "latestRev = " + Arrays.toString(latestRev));

        // build confirmation message
        StringBuffer message = new StringBuffer(mContext.getString(R.string.text_version_contact) + "\n");
        message.append(mContext.getString(R.string.text_version_install, installName, installCode) + "\n");
        message.append(mContext.getString(R.string.text_version_latest, latestName, latestCode));

        // check for major revision change
        if (installRev == null || latestRev == null || !installRev[0].equals(latestRev[0]) ) {
            // force logout as too old
            SecurityUtils.setIsLoggedIn(mContext, false);
            message.insert(0, mContext.getString(R.string.text_version_logout_too_old) + "\n");
        }

        Log.d(TAG, "message = " + message.toString());

        // display version dialog
        // TODO need to send a broadcast so that version failure an be checked upon
        // AlertUtils.alertDialogShow(mContext, mContext.getString(R.string.text_version_title), message.toString());

        // handle failure in caller
        mContext.onCheckVersionPostExecute(false, message);
    }

    // Toast.makeText(mContext, "version name = " + versionName, Toast.LENGTH_LONG).show();

}
