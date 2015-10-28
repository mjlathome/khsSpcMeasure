package com.khs.spcmeasure.library;

import android.content.Context;

/**
 * Created by Mark on 08/10/2015.
 * see:
 * http://stackoverflow.com/questions/20969848/passcode-on-resume-from-background
 */
public class SecurityUtils {

    private static final String TAG = "SecurityUtils";

    static final String SECURITY = "security";
    static final String LOCK = "lock";
    static final String IN_APP = "in_app";
    static final String USERNAME = "username";

    public static void setLockStatus(Context context, boolean status) {
        context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).edit().putBoolean(LOCK, status).commit();
    }

    public static boolean getLockStatus(Context context) {
        return context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).getBoolean(LOCK, true);
    }

    public static void setInAppStatus(Context context, boolean status) {
        context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).edit().putBoolean(IN_APP, status).commit();
    }

    public static boolean getInAppStatus(Context context) {
        return context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).getBoolean(IN_APP, false);
    }

    public static void setUsername(Context context, String username) {
        context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).edit().putString(USERNAME, username).commit();
    }

    public static String getUsername(Context context) {
        return context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).getString(USERNAME, "");
    }
}
