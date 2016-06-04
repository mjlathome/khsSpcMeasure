package com.khs.spcmeasure.library;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.khs.spcmeasure.Globals;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.ui.LoginActivity;

/**
 * Created by Mark on 08/10/2015.
 * see:
 * http://stackoverflow.com/questions/20969848/passcode-on-resume-from-background
 */
public class SecurityUtils {

    private static final String TAG = "SecurityUtils";

    static final String SECURITY = "security";
    static final String IS_LOGGED_IN = "is_logged_in";
    static final String CAN_MEASURE = "can_meassure";
    static final String USERNAME = "username";

    // handle log in
    public static void doLogin(Context context) {
        Log.d(TAG, "doLogin: getIsLoggedIn(context) = " + getIsLoggedIn(context));

        // get globals for version info
        Globals g = Globals.getInstance();

        if (g.isVersionOk()) {
            // initialize as logged out
            SecurityUtils.setIsLoggedIn(context, false);

            // show login screen
            Intent intentLogin = new Intent(context, LoginActivity.class);
            context.startActivity(intentLogin);
        } else {
            // version not ok, tell user cannot login
            AlertUtils.alertDialogShow(context, context.getString(R.string.text_cannot_login), context.getString(R.string.text_version_too_old));
        }
    }

    // handle log out
    public static void doLogout(Context context) {
        Log.d(TAG, "doLogout: getIsLoggedIn(context) = " + getIsLoggedIn(context));

        // inform user
        AlertUtils.alertDialogShow(context, context.getString(R.string.text_information), context.getString(R.string.sec_now_logged_out));

        // set logged out
        setIsLoggedIn(context, false);
    }

    public static void setIsLoggedIn(Context context, boolean status) {
        // get globals
        Globals g = Globals.getInstance();
        g.setLoggedIn(status);

        // was:
        // context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).edit().putBoolean(IS_LOGGED_IN, status).commit();
    }

    public static boolean getIsLoggedIn(Context context) {
        // get globals
        Globals g = Globals.getInstance();
        return g.isLoggedIn();
        // was:
        // return context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).getBoolean(IS_LOGGED_IN, true);
    }

    public static void setCanMeasure(Context context, boolean status) {
        // get globals
        Globals g = Globals.getInstance();
        g.setCanMeasure(status);
        // was:
        // context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).edit().putBoolean(CAN_MEASURE, status).commit();
    }

    public static boolean getCanMeasure(Context context) {
        // get globals
        Globals g = Globals.getInstance();
        return g.isCanMeasure();
        // was:
        // return context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).getBoolean(CAN_MEASURE, false);
    }

    public static void setUsername(Context context, String username) {
        // get globals
        Globals g = Globals.getInstance();
        g.setUsername(username);
        // was:
        // context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).edit().putString(USERNAME, username).commit();
    }

    public static String getUsername(Context context) {
        // get globals
        Globals g = Globals.getInstance();
        return g.getUsername();
        // was:
        // return context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).getString(USERNAME, "");
    }

    // checks whether security is okay
    public static boolean isSecurityOk(Context context, boolean showMess) {
        boolean securityOkay = true;

        if (!SecurityUtils.getIsLoggedIn(context)) {
            // ensure user is logged in
            if (showMess) {
                AlertUtils.errorDialogShow(context, context.getString(R.string.sec_not_logged_in));
            }
            securityOkay = false;
        } else if (!SecurityUtils.getCanMeasure(context)) {
            // ensure user has access rights
            if (showMess) {
                AlertUtils.errorDialogShow(context, context.getString(R.string.sec_cannot_measure));
            }
            securityOkay = false;
        }

        return securityOkay;
    }

    // returns URL query string for username
    public static String getUrlQuery(Context context) {
        return USERNAME + "=" + getUsername(context);
    }
}
