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
    static final String IS_LOGGED_IN = "is_logged_in";
    static final String CAN_MEASURE = "can_meassure";
    static final String USERNAME = "username";

    public static void setIsLoggedIn(Context context, boolean status) {
        context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).edit().putBoolean(IS_LOGGED_IN, status).commit();
    }

    public static boolean getIsLoggedIn(Context context) {
        return context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).getBoolean(IS_LOGGED_IN, true);
    }

    public static void setCanMeasure(Context context, boolean status) {
        context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).edit().putBoolean(CAN_MEASURE, status).commit();
    }

    public static boolean getCanMeasure(Context context) {
        return context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).getBoolean(CAN_MEASURE, false);
    }

    public static void setUsername(Context context, String username) {
        context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).edit().putString(USERNAME, username).commit();
    }

    public static String getUsername(Context context) {
        return context.getApplicationContext().getSharedPreferences(SECURITY, Context.MODE_PRIVATE).getString(USERNAME, "");
    }
}
