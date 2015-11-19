package com.khs.spcmeasure.library;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Mark on 04/10/2015.
 */
public class NetworkUtils {

    // returns boolean indicating whether device is connected by wifi or not
    // see:
    // http://stackoverflow.com/questions/14268407/how-to-programmatically-determine-if-android-is-connected-to-wifi
    public static Boolean isWiFi(Context con) {

        // extract current connection
        ConnectivityManager connManager =
                (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo current = connManager.getActiveNetworkInfo();

        // verify wifi connection
        if (current != null && current.isConnected() && current.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else {
            return false;
        }
    }
}
