package com.khs.spcmeasure;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.khs.spcmeasure.service.PieceService;
import com.khs.spcmeasure.service.SetupService;
import com.khs.spcmeasure.service.SimpleCodeService;
import com.khs.spcmeasure.tasks.DeleteSetupTask;

public class SetupListActivity extends Activity implements SetupListFragment.OnSetupListListener, DeleteSetupTask.OnDeleteSetupListener {

    private static final String TAG = "SetupListActivity";

    // Activity result codes
    private static int RESULT_IMPORT = 1;

    // ensure Action Cause list is only imported once
    private static boolean importActionCause = true;

    private SetupListFragment mSetupListFrag;
    private Long mProdId;


    // TODO remove later - now in Setup Import
    // handle Setup import
//    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            Log.d(TAG, "mMessageReceiver Action = " + action);
//
//            if (SetupService.ACTION_IMPORT.equals(action)) {
//                // get extras
//                final long prodId = intent.getLongExtra(SetupService.EXTRA_PROD_ID, -1);
//                final ActionStatus actStat = (ActionStatus) intent.getSerializableExtra(SetupService.EXTRA_STATUS);
//
//                Log.d(TAG, "mMessageReceiver: prodId = " + prodId + "; actStat = " + actStat + "; mSetupListFrag = " + mSetupListFrag);
//
//                if (mSetupListFrag != null) {
//                    switch(actStat) {
//                        case STARTING:
//                            Log.d(TAG, "mMessageReceiver: starting; prodId = " + prodId);
//                            break;
//                        case COMPLETE:
//                            Log.d(TAG, "mMessageReceiver: complete; prodId = " + prodId);
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mSetupListFrag.refreshList(prodId);
//                                }
//                            });
//                            break;
//                        case FAILED:
//                            Log.d(TAG, "mMessageReceiver: failed; prodId = " + prodId);
//                            mSetupListFrag.refreshList();
//                            break;
//                    }
//                }
//            }
//            return;
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // import Action Cause Simple Codes
        if (importActionCause == true) {
            importActionCause = false;
            // new ImportSimpleCodeTask(this).execute(ImportSimpleCodeTask.TYPE_ACTION_CAUSE);
            SimpleCodeService.startActionImport(this, SimpleCodeService.TYPE_ACTION_CAUSE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();



        // TODO remove later - now in Setup Import
        // Log.d(TAG, "OnResume");
        // register local broadcast receiver for Setup import
        // IntentFilter setupFilter = new IntentFilter();
        // setupFilter.addAction(SetupService.ACTION_IMPORT);
        // LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, setupFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // TODO remove later - now in Setup Import
        // unregister local broadcast receiver
        // LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
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

        // handle Activity results
        if (requestCode == RESULT_IMPORT && resultCode == RESULT_OK) {
            long prodId = data.getLongExtra(SetupImportActivity.RESULT_PROD_ID, -1);
            Log.d(TAG, "onActivityResult: prodId = " + prodId);
            if (mSetupListFrag != null) {
                mSetupListFrag.refreshList(prodId);
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
            case R.id.mnuImport:
                Log.d(TAG, "Menu: Import");
                // run the Import Setup Activity
                Intent intent = new Intent(this, SetupImportActivity.class);
                startActivityForResult(intent, RESULT_IMPORT);
                return true;
            case R.id.action_settings:
                Log.d(TAG, "Menu: Settings");
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
}
