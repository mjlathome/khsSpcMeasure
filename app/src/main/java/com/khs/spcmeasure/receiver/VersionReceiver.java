package com.khs.spcmeasure.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.khs.spcmeasure.R;
import com.khs.spcmeasure.library.SecurityUtils;

public class VersionReceiver extends BroadcastReceiver {
    public static final String ACTION_VERSION_NOT_OK = "com.khs.spcmeasure.VERSION_NOT_OK";

    // TODO remove later - constructor context arg not required
//    private Context mContext;

    public VersionReceiver() {
    }

    // TODO remove later - constructor context arg not required
//    public VersionReceiver(Context context) {
//        mContext = context;
//    }


    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        String action = intent.getAction();
        if (action.equalsIgnoreCase(ACTION_VERSION_NOT_OK)) {
            // force logout and inform user
            SecurityUtils.setIsLoggedIn(context, false);
            Toast.makeText(context, context.getString(R.string.text_version_logout_app_old), Toast.LENGTH_LONG).show();
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        // TODO - remove later cannot display dialog inside a static broadcast receiver - toast is ok though
        // AlertUtils.alertDialogShow(mContext, "TEST", "MJL");

        // start activity
//        Intent i = new Intent(context, LoginActivity.class);
//        i.putExtra(ErrorDialogActivity.EXTRA_MESSAGE, context.getString(R.string.text_version_contact));
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(i);
    }

    // sends the version not ok broadcast
    public static void sendBroadcast(Context context) {
        Intent intent = new Intent();
        intent.setAction(VersionReceiver.ACTION_VERSION_NOT_OK);
        context.sendBroadcast(intent);
    }
}
