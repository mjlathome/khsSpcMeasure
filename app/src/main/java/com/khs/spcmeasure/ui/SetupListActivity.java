package com.khs.spcmeasure.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.khs.spcmeasure.Globals;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.helper.DBAdapter;
import com.khs.spcmeasure.library.SecurityUtils;
import com.khs.spcmeasure.receiver.VersionReceiver;
import com.khs.spcmeasure.service.PieceService;
import com.khs.spcmeasure.service.SetupService;
import com.khs.spcmeasure.service.SylvacBleService;
import com.khs.spcmeasure.tasks.DeleteSetupTask;

public class SetupListActivity extends Activity implements SetupListFragment.OnSetupListListener, DeleteSetupTask.OnDeleteSetupListener {

    private static final String TAG = "SetupListActivity";

    // local broadcast constants
    public static final String SPC_MEASURE_CLOSE = "SpcMeasureClose";

    // BLE member variables
    private SylvacBleService mSylvacBleSrvc = null;

    // Activity result codes
    private static final int RESULT_IMPORT = 1;
    private static final int RESULT_CHECK_UPDATE = 2;

    // ensure that User Login is only asked once when app initially starts
    private boolean mAskLogin = true;
    // TODO remove later as even though onCreate is re-run when activity is re-started
    // TODO the static var was set to true so no login shown
    // private static boolean askLogin = true;

    private SetupListFragment mSetupListFrag;
    private Long mProdId;

    // TODO - remove dynamic broadcast receiver later on
//    // version receiver members
//    private BroadcastReceiver mVersionReceiver;
//    private IntentFilter mVersionFilter;
//    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // initialize shared preference to the default values - executed once only
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // start the BLE service
        startBleService();

        // TODO - remove dynamic broadcast receiver later on
//        // instantiate version receiver and filter
//        mVersionReceiver = new VersionReceiver(this);
//        mVersionFilter = new IntentFilter(VersionReceiver.ACTION_VERSION_NOT_OK);

        setContentView(R.layout.activity_setup_list);
        if (savedInstanceState == null) {
            mSetupListFrag = SetupListFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mSetupListFrag)
                    .commit();
        }

        // change title
        this.setTitle(getString(R.string.title_activity_setup_list));

        // start Piece Service
        startService(new Intent(getBaseContext(), PieceService.class));

        // get globals for version info
        Globals g = Globals.getInstance();

        if (!g.isVersionOk()) {
            // version not ok yet so force check version
            Intent intentChkUpd = new Intent(this, CheckUpdateActivity.class);
            intentChkUpd.putExtra(CheckUpdateActivity.KEY_EXIT_IF_OK, true);
            startActivityForResult(intentChkUpd, RESULT_CHECK_UPDATE);
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: mAskLogin = " + mAskLogin);
        super.onStart();

        // get globals for version info
        Globals g = Globals.getInstance();

        if (g.isVersionOk()) {
            // check if user should be asked to login
            if (mAskLogin && !SecurityUtils.getIsLoggedIn(this)) {

                mAskLogin = false;

                // attempt login
                SecurityUtils.doLogin(this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO - remove dynamic broadcast receiver later on
        // register version receiver
        // registerReceiver(mVersionReceiver, mVersionFilter);

        // TODO - remove dynamic broadcast receiver later on
        // TODO debug receiver - remove later
//        mHandler = new Handler();
//        mHandler.postDelayed(new Runnable(){
//            @Override
//            public void run() {
//                // TODO remove later
//                VersionReceiver.sendBroadcast(SetupListActivity.this);
//            }
//        }, 5000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO - remove dynamic broadcast receiver later on
//        // unregister version receiver
//        unregisterReceiver(mVersionReceiver);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();

//        Log.d(TAG, "OnStop: 1 Lock = " + SecurityUtils.getLockStatus(this) + "; App = " + SecurityUtils.getInAppStatus(this));
//        if (!SecurityUtils.getInAppStatus(this)) {
//            // lock the app
//            SecurityUtils.setLockStatus(this, true);
//        } else {
//            // not locked
//            SecurityUtils.setLockStatus(this, false);
//        }
//        Log.d(TAG, "OnStop: 2 Lock = " + SecurityUtils.getLockStatus(this) + "; App = " + SecurityUtils.getInAppStatus(this));
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopBleService();
        broadcastUpdate(SPC_MEASURE_CLOSE);
        super.onDestroy();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        // keep track of fragment upon orientation change
        mSetupListFrag = (SetupListFragment) fragment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode = " + requestCode + "; resultCode = " + resultCode);

        if (resultCode == RESULT_OK) {
            // handle Activity results
            switch (requestCode) {
                case RESULT_IMPORT:
                    long prodId = data.getLongExtra(SetupImportActivity.RESULT_PROD_ID, -1);
                    Log.d(TAG, "onActivityResult: prodId = " + prodId);
                    if (mSetupListFrag != null) {
                        mSetupListFrag.refreshList(prodId);
                    }
                    break;
                // TODO remove later no handled in onStart
//                case RESULT_CHECK_UPDATE:
//                    // get globals for version info
//                    Globals g = Globals.getInstance();
//
//                    if (g.isVersionOk()) {
//                        // version ok, launch login if required
//                        // check if user should be asked to login
//                        if (!SecurityUtils.getIsLoggedIn(this)) {
//
//                            // initialize as logged out
//                            SecurityUtils.setIsLoggedIn(this, false);
//
//                            // show login screen
//                            Intent intentLogin = new Intent(this, LoginActivity.class);
//                            startActivity(intentLogin);
//                        }
//                    }
//                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_setup_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.mnuExit:
                // exit the app
                Log.d(TAG, "Menu: Exit");
                // set logged out
                SecurityUtils.setIsLoggedIn(this, false);
                // close activity
                finish();
                // System.exit(0);
                return true;
            case R.id.mnuImport:
                Log.d(TAG, "Menu: Import");
                // run the Import Setup Activity
                Intent intent = new Intent(this, SetupImportActivity.class);
                startActivityForResult(intent, RESULT_IMPORT);
                return true;
            case R.id.action_login:
                Log.d(TAG, "Menu: Login");
                // attempt login
                SecurityUtils.doLogin(this);

                // TODO remove later once as done in SecurityUtils now
                // show login screen
                // Intent intentLogin = new Intent(this, LoginActivity.class);
                // startActivity(intentLogin);
                return true;
            case R.id.action_logout:
                Log.d(TAG, "Menu: Logout");
                // set logged out
                SecurityUtils.doLogout(this);
                return true;
            case R.id.action_settings:
                Log.d(TAG, "Menu: Settings");
                // change preferences
                Intent intentPrefs = new Intent(this, SettingsActivity.class);
                startActivity(intentPrefs);
                return true;
            case R.id.action_check_update:
                // about activity
                Log.d(TAG, "Menu: Check for Update");
                Intent intentCheckUpdate = new Intent(this, CheckUpdateActivity.class);
                startActivity(intentCheckUpdate);
                return true;
            case R.id.action_about:
                // about activity
                Log.d(TAG, "Menu: About");
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSetupSelected(Long prodId) {
        // store the Product Id for use later
        this.mProdId = prodId;
        Log.d(TAG, "prodId = " + Long.toString(prodId));

        // download the latest setup for the Product
        SetupService.startActionImport(this, prodId);

        // launch Piece List Activity for Product
        Intent intent = new Intent(this, PieceListActivity.class);
        intent.putExtra(DBAdapter.KEY_PROD_ID, prodId);
        startActivity(intent);
    }

    @Override
    public void onDeleteSetupPostExecute() {
        Log.d(TAG, "onDeleteSetupPostExecute");
        if (mSetupListFrag != null) {
            mSetupListFrag.refreshList();
        }
    }

    // method to start the BLE service
    // see:
    // http://www.tutorialspoint.com/android/android_services.htm
    public void startBleService() {
        Log.d(TAG, "startBleService");
        startService(new Intent(getBaseContext(), SylvacBleService.class));
    }

    // method to stop the BLE service
    public void stopBleService() {
        Log.d(TAG, "stopBleService");
        stopService(new Intent(getBaseContext(), SylvacBleService.class));
    }

    // broadcast action - no extras
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
