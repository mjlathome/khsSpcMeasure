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
    private static final String TAG = "VersionUtils";
    public static final String TAG_VERSION_OK = "versionOk";
    public static final String TAG_INSTALL_CODE = "installCode";
    public static final String TAG_INSTALL_NAME = "installName";


    public static final int VERSION_CODE_UNKNOWN = -1;
    public static final String VERSION_NAME_UNKNOWN = "";

    // extract version code
    public static int getVersionCode(Context context) {

        // initialize
        int installCode = VERSION_CODE_UNKNOWN;

        try {
            // extract installed version code
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            installCode = pInfo.versionCode;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return installCode;
    }

    // extract version name
    public static String getVersionName(Context context) {

        // initialize
        String installName = VERSION_NAME_UNKNOWN;

        try {
            // extract installed version name
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            installName = pInfo.versionName;
        } catch(Exception e) {
            e.printStackTrace();
        }

        // Toast.makeText(context, "version name = " + versionName, Toast.LENGTH_LONG).show();

        return installName;
    }

    // returns URL query string for installed version
    public static String getUrlQuery(Context context) {
        return TAG_INSTALL_CODE + "=" + String.valueOf(getVersionCode(context)) + "&" + TAG_INSTALL_NAME + "=" + getVersionName(context);
    }

    // check version code to see if changed
    public static boolean isVersionCodeChanged(Context context, int latestCode) {

        // initialize
        boolean changed = false;

        // get installed version code
        int installCode = getVersionCode(context);

        Log.d(TAG, "isVersionCodeChanged: latestCode = " + latestCode + "; installCode = " + installCode);

        // compare versions
        if (Integer.compare(installCode, latestCode) != 0) {
            changed = true;
        }

        return changed;
    }

    // check version names to see if major version has changed
    public static boolean isMajorVersionNameChanged(Context context, String latestName) {

        // initialize
        boolean changed = false;

        try {
            // get installed version name
            String installName = getVersionName(context);

            // extract major, minor and patch version info
            String[] installRev = installName.split("\\.");
            String[] latestRev = latestName.split("\\.");

            // check for major revision change
            if (installRev == null || installRev.equals("") ||
                    latestRev == null || latestRev.equals("") ||
                    !installRev[0].equals(latestRev[0])) {
                changed = true;
            }
        } catch(Exception e) {
            e.printStackTrace();
            changed = true;
        }

        return changed;
    }
}
