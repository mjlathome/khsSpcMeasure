package com.khs.spcmeasure.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.khs.spcmeasure.tasks.UpdateApp;

/**
 * Created by Mark on 05/16/2015.
 * handles version check and update action
 */
public class CheckUpdateActivity extends Activity {
    private final String TAG = "CheckUpdateActivity";

    // url for spcMeasure apk
    // private static final String url = "http://thor.kmx.cosma.com/spc/apk/spcMeasure/app-release.apk";
    private static final String url = "http://thor.kmx.cosma.com/spc/apk/spcMeasure/app-debug.apk";

    // preference keys
    public static final String KEY_PREF_SHOW_NOTIFICATIONS      = "key_pref_show_notifications";
    public static final String KEY_PREF_IN_CONTROL_AUTO_MOVE    = "key_pref_in_control_auto_move";
    public static final String KEY_PREF_IN_CONTROL_DELAY        = "key_pref_in_control_delay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new CheckUpdateFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                Log.d(TAG, "Home");
                finish();
                // NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // handle on click of button Install Update
    public void onClickBtnInstallUpdate(View view) {
        Log.d(TAG, "onClickBtnInstallUpdate");

        UpdateApp updateApp = new UpdateApp();
        updateApp.setContext(getApplicationContext());
        updateApp.execute(url);

        return;
    }
}
