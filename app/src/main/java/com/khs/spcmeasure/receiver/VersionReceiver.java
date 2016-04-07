package com.khs.spcmeasure.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.khs.spcmeasure.R;
import com.khs.spcmeasure.ui.ErrorDialogActivity;
import com.khs.spcmeasure.ui.LoginActivity;

public class VersionReceiver extends BroadcastReceiver {
    public static final String ACTION_VERSION_NOT_OK = "com.khs.spcmeasure.VERSION_NOT_OK";

    public VersionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Toast.makeText(context, context.getString(R.string.text_version_too_old) + "  " + context.getString(R.string.text_version_contact), Toast.LENGTH_LONG).show();
        // throw new UnsupportedOperationException("Not yet implemented");

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
