package com.khs.spcmeasure.ui;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.khs.spcmeasure.R;
import com.khs.spcmeasure.helper.DBAdapter;
import com.khs.spcmeasure.library.AlertUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class AboutActivity extends Activity {

    private static final String TAG = "AboutActivity";

    // counts number of times version name clicked on
    private static final int DEBUG_COUNT = 5;
    private int mVersionCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // display version
        displayVersion();
    }

    // handle click of versionName
    public void onClickVersionName(View v) {
        mVersionCount++;
        if (mVersionCount >= DEBUG_COUNT) {
            mVersionCount = 0;
            copyAppDbToDownloadFolder();
        }
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

    // copy SQLite db to download folder for debug purposes
    // see: http://stackoverflow.com/questions/9066682/android-copy-database-from-internal-to-external-storage
    public boolean copyAppDbToDownloadFolder() {
        try {
            File backupDB = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), DBAdapter.DATABASE_NAME); // for example "my_data_backup.db"
            File currentDB = getApplicationContext().getDatabasePath(DBAdapter.DATABASE_NAME); //databaseName=your current application database name, for example "my_data.db"
            if (currentDB.exists()) {
                FileInputStream fis = new FileInputStream(currentDB);
                FileOutputStream fos = new FileOutputStream(backupDB);
                fos.getChannel().transferFrom(fis.getChannel(), 0, fis.getChannel().size());
                // or fis.getChannel().transferTo(0, fis.getChannel().size(), fos.getChannel());
                fis.close();
                fos.close();
                Log.i(TAG, "db copied to " + backupDB.getPath());
                // display message
                String message = getString(R.string.text_pathname, backupDB.getPath());
                AlertUtils.alertDialogShow(this, getString(R.string.text_db_written), message);
                return true;
            } else {
                Log.i(TAG, "db not found");
            }
        } catch (IOException e) {
            Log.d(TAG, "db copy failed");
            e.printStackTrace();
        }

        return false;
    }

}