package com.khs.spcmeasure.ui;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.khs.spcmeasure.R;

public class AboutActivity extends Activity {

    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // display version
        displayVersion();
    }

    // handle on click of button OK
    public void onClickBtnOk(View view) {
        Log.d(TAG, "onClickBtnOk");
        finish();
    }

    // display version
    private void displayVersion() {
        String versionName = null;

        // extract version
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch(Exception e) {
            e.printStackTrace();
        }

        // display version
        if (versionName != null) {
            TextView txtVersionName = (TextView) findViewById(R.id.txtVersionName);
            txtVersionName.setText(versionName);
        }
    }
}
