package com.khs.spcmeasure.ui;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.khs.spcmeasure.R;
import com.khs.spcmeasure.dao.FeatureDao;
import com.khs.spcmeasure.dao.PieceDao;
import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.helper.DBAdapter;
import com.khs.spcmeasure.library.SecurityUtils;
import com.khs.spcmeasure.library.SylvacGattAttributes;
import com.khs.spcmeasure.service.SylvacBleService;

import java.util.List;


public class FeatureActivity extends FragmentActivity implements ActionBar.OnNavigationListener, ViewPager.OnPageChangeListener {

    final static String TAG = "FeatureActivity";

    // activity result codes
    private static int RESULT_LOGIN = 1;

    /// tab constants
    private static final int TAB_POS_MEASUREMENT = 0;
    private static final int TAB_POS_CHART_XBAR  = 1;
    private static final int TAB_POS_CHART_RANGE = 2;
    private static final int TAB_POS_INFORMATION = 3;

    // message constants
    private static final int MESSAGE_MOVE_NEXT = 0;

    private boolean mShowSecMess = true;
    private Long mPieceId = null;
    private Long mFeatId  = null;
    private PieceDao mPieceDao = new PieceDao(this);
    private FeatureDao mFeatDao = new FeatureDao(this);
    private Piece mPiece;

    public List<Feature> mFeatList;

    FeaturePagerAdapter mAdapter;

    ViewPager mPager;

    // BLE member variables
    private SylvacBleService mSylvacBleSrvc = null;
    boolean mBound = false;

    // tab member variables
    public int mTabPos = TAB_POS_MEASUREMENT;

    // handler for delayed move to next feature
    private Handler mHandler = new Handler();

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    // handle BLE service connection status change
    private ServiceConnection mBleSrvcConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");

            SylvacBleService.MyLocalBinder binder = (SylvacBleService.MyLocalBinder) service;
            mSylvacBleSrvc = binder.getService();

            mBound = true;

            // connect ble device, if necessary
            enableMeasurement();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");

            // TODO Auto-generated method stub

            mSylvacBleSrvc = null;
            mBound = false;
        }

    };

    // handle BLE service communication
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "mMessageReceiver Action = " + action);

            //  ... react to local broadcast message
            if (SylvacBleService.ACTION_CHARACTERISTIC_CHANGED.equals(action)) {
                if (intent.hasExtra(SylvacBleService.EXTRA_DATA_CHAR_UUID)) {

                    // extract characteristic UUID as a string
                    String charUuid = intent.getStringExtra(SylvacBleService.EXTRA_DATA_CHAR_UUID);

                    if (charUuid.equals(SylvacGattAttributes.ANSWER_TO_REQUEST_OR_CMD_FROM_INSTRUMENT)) {

                        // extract the last request/command written
                        String lastWrite = intent.getStringExtra(SylvacBleService.EXTRA_DATA_CHAR_LAST_WRITE);

                        if (lastWrite == SylvacBleService.COMMAND_GET_CURRENT_VALUE) {
                            // handle the measurement
                            setMeasurement(intent.getByteArrayExtra(SylvacBleService.EXTRA_DATA_CHAR_DATA));
                        } else if (lastWrite == SylvacBleService.COMMAND_GET_BATTERY_STATUS) {
                            // handle battery status
                            Log.d(TAG, "BattState intent: " + SylvacBleService.byteArrayToString(intent.getByteArrayExtra(SylvacBleService.EXTRA_DATA_CHAR_DATA)));
                            String battState = new String(intent.getByteArrayExtra(SylvacBleService.EXTRA_DATA_CHAR_DATA));

                            if (battState.equals(SylvacBleService.BATTERY_OK)) {
                                Toast.makeText(FeatureActivity.this, getString(R.string.text_ble_battery_okay, battState.trim()), Toast.LENGTH_LONG).show();
                            } else if (battState.equals(SylvacBleService.BATTERY_LOW)) {
                                AlertUtils.alertDialogShow(FeatureActivity.this, getString(R.string.text_warning), getString(R.string.text_ble_battery_low, battState.trim()));
                            } else {
                                AlertUtils.errorDialogShow(FeatureActivity.this, getString(R.string.text_ble_battery_unknown, battState.trim()));
                            }
                        } else if (lastWrite == SylvacBleService.COMMAND_GET_INSTRUMENT_ID_CODE) {
                            String id = new String(intent.getByteArrayExtra(SylvacBleService.EXTRA_DATA_CHAR_DATA));
                            Log.d(TAG, "Instrument Id: " + id);
                            AlertUtils.alertDialogShow(FeatureActivity.this, getString(R.string.text_information), getString(R.string.text_ble_instrument_id, id));
                        } else if (lastWrite == SylvacBleService.COMMAND_SET_MEASUREMENT_UOM_MM) {
                            Log.d(TAG, "UOM is now MM");
                            Toast.makeText(FeatureActivity.this, getString(R.string.text_ble_uom_now_mm), Toast.LENGTH_LONG).show();
                        } else if (lastWrite == SylvacBleService.COMMAND_SET_ZERO_RESET) {
                            Log.d(TAG, "Zero was reset");
                            Toast.makeText(FeatureActivity.this, getString(R.string.text_ble_zero_reset), Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(TAG, "Unknown Cmd/Req: " + lastWrite);
                            AlertUtils.errorDialogShow(FeatureActivity.this, getString(R.string.text_ble_battery_low, lastWrite));
                        }
                    } else if (charUuid.equals(SylvacGattAttributes.DATA_RECEIVED_FROM_INSTRUMENT)) {
                        // handle the measurement
                        setMeasurement(intent.getByteArrayExtra(SylvacBleService.EXTRA_DATA_CHAR_DATA));
                    }
                }
            }

            return;
        }
    };


    //region implement ViewPager OnPageChangeListener interface
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // extract Feature at the pager position
        Feature feat = mFeatList.get(position);

        // set feature member variable
        mFeatId = feat.getId();

        Log.d(TAG, "onPageScrolled: mFeatId = " + mFeatId);

        // cancel pending move next
        cancelMoveNext();
    }

    @Override
    public void onPageSelected(int position) {
        // extract Feature at the pager position
        Feature feat = mFeatList.get(position);

        // set feature member variable
        mFeatId = feat.getId();

        Log.d(TAG, "onPageSelected: mFeatId = " + mFeatId);

        // cancel pending move next
        cancelMoveNext();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.d(TAG, "onPageScrollStateChanged");

        // hide keyboard entry
        if (mTabPos == FeatureActivity.TAB_POS_MEASUREMENT) {
            // communicate measurement to fragment
            MeasurementFragment measFrag = (MeasurementFragment) mAdapter.getCurrentFragment();
            measFrag.hideInput();
        }
    }
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature);

        // extract intent arguments, if any
        getArguments(getIntent().getExtras());

        // extract saved instance state arguments, if any
        getArguments(savedInstanceState);

        // verify arguments
        if (!chkArguments()) {
            AlertUtils.errorDialogShow(this, getString(R.string.text_mess_arguments_invalid));
            finish();
        }

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        // Show the Up button in the action bar.
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[]{
                                getString(R.string.tab_title_measurement),
                                getString(R.string.tab_title_chart_xbar),
                                getString(R.string.tab_title_chart_range),
                                getString(R.string.tab_title_information),
                        }),
                this);

        // extract data
        mPiece = mPieceDao.getPiece(mPieceId);

        // TODO remove later - was always extracting all features
        // mFeatList = mFeatDao.getAllFeatures(mPiece.getProdId());

        // extract list of features based upon piece collect state
        mFeatList = null;
        if (mPiece.getStatus() == CollectStatus.OPEN) {
            // get active features for the product
            mFeatList = mFeatDao.getFeaturesByProdIdActive(mPiece.getProdId(), true);
        } else {
            // get features measured on the piece
            mFeatList = mFeatDao.getFeaturesByPieceId(mPieceId);
        }

        // display views
        displayView();

        // configure ViewPager
        mAdapter = new FeaturePagerAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(this);

        final PagerTabStrip pagerTitle = (PagerTabStrip) findViewById(R.id.pagerTabStrip);
        pagerTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        pagerTitle.setTextColor(Color.WHITE);
        pagerTitle.setNonPrimaryAlpha(0.64f);
        pagerTitle.setTextSpacing(4);
        pagerTitle.setBackgroundColor(Color.DKGRAY);
        pagerTitle.setTabIndicatorColor(Color.CYAN);
        pagerTitle.setPadding(0, 10, 0, 0);

        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.goto_first);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(0);
            }
        });

        button = (Button)findViewById(R.id.goto_last);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(mFeatList.size() - 1);
            }
        });

        // display the required feature
        mPager.setCurrentItem(getFeaturePos(mFeatId));
    }

    // handle Activity destroy
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        // TODO may need to close down the GATT etc here first, if already connected
        // close down BLE service, if finishing
        // if (isFinishing()) {
        // unbindBleService();
        // }

        // Intent intent = new Intent(this, SylvacBleService.class);
        // stopService(intent);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        // FUTURE move all Ble checking to the service.  Can this be done due to UI interaction.
        // TODO at minimum move into private method
        // Maybe via a callback.

        // TODO use isMeasurementEnabled within MeasurementFragment too
        if (isMeasurementEnabled()) {
            // Check for Bluetooth LE Support.  In production, the manifest entry will keep this
            // from installing on these devices, but this will allow test devices or other
            // sideloads to report whether or not the feature exists.
            // NOTE: Not really needed as included in Manifest.
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, getString(R.string.text_ble_not_supported), Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // TODO shouldn't ForResult be used to ensure that the user enabled BlueTooth?
                // startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                startActivity(enableBtIntent);
                finish();
                return;
            }
        }

        // register local broadcast receiver - filter multiple intent actions
        IntentFilter bleFilter = new IntentFilter();
        // bleFilter.addAction(SylvacBleService.ACTION_BLE_NOT_SUPPORTED);
        // bleFilter.addAction(SylvacBleService.ACTION_BLUETOOTH_NOT_ENABLED);
        bleFilter.addAction(SylvacBleService.ACTION_CHARACTERISTIC_CHANGED);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, bleFilter);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        // unregister local broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        // TODO should this be in onResume instead?
        // set bound state

        // bind to BLE service
        bindBleService();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");

        // TODO should this be in onPause instead?

        // TODO remove later
        // unbind from Ble service
        // unbindBleService();

        // close down BLE service
        unbindBleService();


        // disconnect ble device
        // disconnectBleDevice();

        super.onStop();

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    // save the current Activity state
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstState: FeatId = " + mFeatId);

        // serialize the Piece
        outState.putLong(DBAdapter.KEY_PIECE_ID, mPieceId);

        // serialize the Feature
        outState.putLong(DBAdapter.KEY_FEAT_ID, mFeatId);

        // serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }

    // TODO remove later - enableMeasurement called when service is bound from OnStart call to bindBleService()
//    // handle activity result intent callback
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // super.onActivityResult(requestCode, resultCode, data);
//
//        Log.d(TAG, "onActivityResult: requestCode = " + requestCode + "; resultCode = " + resultCode);
//
//        // handle activity results
//        if (requestCode == RESULT_LOGIN) {
//            enableMeasurement();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_feature, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d(TAG, "menu = " + item.getTitle());

        // handle menu item
        switch(id) {
            case R.id.action_login:
                Log.d(TAG, "Menu: Login");
                mShowSecMess = true;
                // attempt login
                SecurityUtils.doLogin(this);
                // TODO remove later once happy with doLogin call
//                // show login screen
//                Intent intentLogin = new Intent(this, LoginActivity.class);
//                startActivityForResult(intentLogin, RESULT_LOGIN);
                return true;
            case R.id.action_logout:
                Log.d(TAG, "Menu: Logout");
                mShowSecMess = true;
                // set logged out
                SecurityUtils.doLogout(this);
                enableMeasurement();
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
            case R.id.mnuScanBle:
                // TODO remove later - now calls connectDevice
                // mSylvacBleSrvc.scanLeDevice(true);
                if (mBound) {
                    mSylvacBleSrvc.connectDevice();
                }
                return true;
            case R.id.mnuSetUomMm:
                if (mBound) {
                    mSylvacBleSrvc.writeCharacteristic(SylvacBleService.COMMAND_SET_MEASUREMENT_UOM_MM);
                }
                return true;
            case R.id.mnuSetZero:
                if (mBound) {
                    mSylvacBleSrvc.writeCharacteristic(SylvacBleService.COMMAND_SET_ZERO_RESET);
                }
                return true;
            case R.id.mnuGetBattery:
                if (mBound) {
                    mSylvacBleSrvc.writeCharacteristic(SylvacBleService.COMMAND_GET_BATTERY_STATUS);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {

        // hide keyboard entry
        if (mTabPos == FeatureActivity.TAB_POS_MEASUREMENT) {
            // communicate measurement to fragment
            MeasurementFragment measFrag = (MeasurementFragment) mAdapter.getCurrentFragment();
            measFrag.hideInput();
        }

        // handle tab change
        Log.d(TAG, "pos = " + position + "; id = " + id);
        mTabPos = position;
        mAdapter.notifyDataSetChanged();

        return true;
    }

    // extracts arguments from provided Bundle
    private void getArguments(Bundle args) {
        // extract piece id
        if (args != null) {
            if (args.containsKey(DBAdapter.KEY_PIECE_ID)) {
                mPieceId = args.getLong(DBAdapter.KEY_PIECE_ID);
            }
            if (args.containsKey(DBAdapter.KEY_FEAT_ID)) {
                mFeatId = args.getLong(DBAdapter.KEY_FEAT_ID);
                Log.d(TAG, "getArguments: FeatId = " + mFeatId);
            }

//            if (args.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
//                getActionBar().setSelectedNavigationItem(
//                        args.getInt(STATE_SELECTED_NAVIGATION_ITEM));
//            }
        }
    }

    // checks arguments
    private boolean chkArguments() {
        // verify arguments
        // mFeatId is no longer mandatory due to launch from New Piece Dialog
        // return (mPieceId != null && mFeatId != null);
        return (mPieceId != null);
    }

    // set bind state
    private void enableMeasurement() {
        Log.d(TAG, "enableMeasurement");

        // for Open Pieces only to save battery power
        // check security too
        if (mPiece.getStatus() == CollectStatus.OPEN) {
            if (SecurityUtils.isSecurityOk(this, mShowSecMess)) {
                connectBleDevice();
            } else {
                mShowSecMess = false;
                disconnectBleDevice();
            }
        } else {
            disconnectBleDevice();
        }

        // update Measurement tab fields
        if (mTabPos == FeatureActivity.TAB_POS_MEASUREMENT) {
            mAdapter.notifyDataSetChanged();
        }
    }

    // bind to Ble service
    private void bindBleService() {
        Intent intent = new Intent(this, SylvacBleService.class);
        bindService(intent, mBleSrvcConn, Context.BIND_AUTO_CREATE);
    }

    // unbind from Ble service
    private void unbindBleService() {
        if (mBound == true && mBleSrvcConn != null) {

            // disconnect device
            disconnectBleDevice();

            unbindService(mBleSrvcConn);
            mBound = false;
        }
    }

    // connect Ble device
    private void connectBleDevice() {
        Log.d(TAG, "connectBleDevice: " + mSylvacBleSrvc);
        if (mSylvacBleSrvc != null) {
            mSylvacBleSrvc.connectDevice();
        }
    }

    // disconnect Ble device
    private void disconnectBleDevice() {
        Log.d(TAG, "disconnectBleDevice: " + mSylvacBleSrvc);
        if (mSylvacBleSrvc != null) {
            mSylvacBleSrvc.disconnectDevice();
        }
    }

    // getter for Ble service
    public SylvacBleService getBleService() {
        return mSylvacBleSrvc;
    }

    // extracts Feature pager position for the feature Id provided
    // returns 0 if feature Id is unknown
    public int getFeaturePos(Long featId) {
        Log.d(TAG, "getFeaturePos: featId = " + featId);
        int pos = 0;

        if (featId != null) {
            // start off at required feature
            for (Feature f : mFeatList) {
                Log.d(TAG, "getFeaturePos: featId = " + featId + "; f.id = " + f.getId());
                if (f.getId().equals(mFeatId)) {
                    pos = mFeatList.indexOf(f);
                    break;
                }
            }
        }

        Log.d(TAG, "getFeaturePos: featId = " + featId + " ; pos = " + pos);

        return pos;
    }

    // navigate to first Feature
    public void getFirst() {
        int pos = mPager.getCurrentItem();
        if (pos != 0) {
            mPager.setCurrentItem(0);
        }
    }

    // navigate to previous Feature
    public void getPrev() {
        int pos = mPager.getCurrentItem();
        if (pos != 0) {
            mPager.setCurrentItem(pos - 1);
        }
    }

    // navigate to next Feature
    public void getNext() {
        int pos = mPager.getCurrentItem();
        if (pos != (mFeatList.size() - 1)) {
            mPager.setCurrentItem(pos + 1);
        }
    }

    // get next Position
    public int getNextPos() {
        int pos = mPager.getCurrentItem();
        if (pos != (mFeatList.size() - 1)) {
            pos += 1;
        }
        return pos;
    }

    // navigate to last Feature
    public void getLast() {
        int pos = mPager.getCurrentItem();
        if (pos != (mFeatList.size() - 1)) {
            mPager.setCurrentItem((mFeatList.size() - 1));
        }
    }

    // sets the View Pager position
    public void setPagerPos(int pos) {
        mPager.setCurrentItem(pos);
    }

    // moves to the next feature, if any
    public void moveNext() {

        // extract shared preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // extract auto move from preferences
        boolean autoMove = sharedPref.getBoolean(SettingsActivity.KEY_PREF_IN_CONTROL_AUTO_MOVE, true);
        Log.d(TAG, "moveNext: auto = " + autoMove);

        if (autoMove == true) {
            // extract current and next View Pager positions
            final int currPos = mPager.getCurrentItem();
            final int nextPos = getNextPos();

            // if there's a next, then post delayed move
            if (currPos != nextPos) {

                // extract delay from preferences
                String delayStr = sharedPref.getString(SettingsActivity.KEY_PREF_IN_CONTROL_DELAY, "3000");
                int delay = Integer.parseInt(delayStr);
                Log.d(TAG, "moveNext: delay = " + delay);

                // create runnable
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        // clear move next message
                        mHandler.removeMessages(MESSAGE_MOVE_NEXT);

                        // ensure that user has not updated the displayed feature
                        if (mPager.getCurrentItem() == currPos) {
                            setPagerPos(nextPos);
                        }
                    }
                };

                // post message and, if successful, delayed runnable
                if (mHandler.sendEmptyMessage(MESSAGE_MOVE_NEXT)) {
                    mHandler.postDelayed(run, delay);
                }
            }
        }
    }

    // display on-screen Views
    public void displayView() {

        // TODO error trap when required data not available

//        // display Piece views
//        if (mPiece != null) {
//            // extract views
//            TextView txtProdName = (TextView)findViewById(R.id.txtProdName);
//            TextView txtCollectDt = (TextView)findViewById(R.id.txtCollectDt);
//            TextView txtCollStatus = (TextView)findViewById(R.id.txtCollStatus);
//
//            // configure as decorations
//            ((ViewPager.LayoutParams)txtProdName.getLayoutParams()).isDecor = true;
//            ((ViewPager.LayoutParams)txtCollectDt.getLayoutParams()).isDecor = true;
//            ((ViewPager.LayoutParams)txtCollStatus.getLayoutParams()).isDecor = true;
//
//            // set view data
//            txtCollectDt.setText(DateTimeUtils.getDateTimeStr(mPiece.getCollectDt()));
//            txtCollStatus.setText(mPiece.getStatus().toString());
//
//            // show the Product Name in the TextView
//            DBAdapter db = new DBAdapter(this);
//            db.open();
//            Cursor c = db.getProduct(mPiece.getProdId());
//            txtProdName.setText(c.getString(c.getColumnIndex(DBAdapter.KEY_NAME)));
//            db.close();
//        }
    }

    // cancel move to the next feature.
    public void cancelMoveNext() {
        // remove all runnables and messages
        mHandler.removeCallbacksAndMessages(null);
    }

    // set measured value
    private void setMeasurement(byte[] value) {

        // convert characteristic byte array data to a double
        Double myDouble = Double.parseDouble(new String(value));

        // update Measurement value if okay
        if (myDouble != null && mTabPos == FeatureActivity.TAB_POS_MEASUREMENT && !mHandler.hasMessages(MESSAGE_MOVE_NEXT)) {
            // communicate measurement to fragment
            MeasurementFragment measFrag = (MeasurementFragment) mAdapter.getCurrentFragment();

            // ensure not feature is not a gap check
            if (!measFrag.isGapCheck()) {
                measFrag.setValue(myDouble);
            }
        } else {
            // ignore as not on the Measurement fragment
            return;
        }
    }

    // clear measured value
    private void clearMeasurement() {
        // clear Measurement value
        if (mTabPos == FeatureActivity.TAB_POS_MEASUREMENT) {
            // communicate measurement to fragment
            MeasurementFragment measFrag = (MeasurementFragment) mAdapter.getCurrentFragment();
            measFrag.setValue(null);
        } else {
            // ignore as not on the Measurement fragment
            return;
        }
    }

    // glue between on-screen View Pager and the Fragments it contains
    public class FeaturePagerAdapter extends FragmentStatePagerAdapter {

        // store current fragment.  see:
        // http://stackoverflow.com/questions/18609261/getting-the-current-fragment-instance-in-the-viewpager
        private Fragment mCurrentFragment = null;

        public FeaturePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // extract feature list count
        @Override
        public int getCount() {
            return mFeatList.size();
        }

        // extract feature name for page title
        @Override
        public CharSequence getPageTitle(int position) {
            // return super.getPageTitle(position);
            return mFeatList.get(position).getName();
        }

        // create fragment appropriate to on-screen tab selected
        @Override
        public Fragment getItem(int position) {

            // extract Feature at the pager position
            Feature feat = mFeatList.get(position);
            // mFeatId = feat.getId();

            Bundle args;

            switch(mTabPos) {
                case TAB_POS_MEASUREMENT:
                    // create the measurement fragment
                    args = new Bundle();
                    args.putLong(DBAdapter.KEY_PIECE_ID, mPieceId);
                    args.putLong(DBAdapter.KEY_FEAT_ID, feat.getFeatId());
                    MeasurementFragment measFrag = new MeasurementFragment();
                    measFrag.setArguments(args);
                    return measFrag;
                case TAB_POS_CHART_XBAR:
                case TAB_POS_CHART_RANGE:
                    // create the chart fragment
                    args = new Bundle();
                    if (mTabPos == TAB_POS_CHART_XBAR) {
                        args.putInt(ChartFragment.CHART_TYPE, ChartFragment.CHART_TYPE_XBAR);
                    } else {
                        args.putInt(ChartFragment.CHART_TYPE, ChartFragment.CHART_TYPE_RANGE);
                    }
                    args.putLong(DBAdapter.KEY_PROD_ID, feat.getProdId());
                    args.putLong(DBAdapter.KEY_FEAT_ID, feat.getFeatId());
                    ChartFragment chartFrag = new ChartFragment();
                    chartFrag.setArguments(args);
                    return chartFrag;
                case TAB_POS_INFORMATION:
                    // create the information fragment
                    args = new Bundle();
                    args.putLong(DBAdapter.KEY_PROD_ID, feat.getProdId());
                    args.putLong(DBAdapter.KEY_FEAT_ID, feat.getFeatId());
                    FeatureInfoFragment limFrag = new FeatureInfoFragment();
                    limFrag.setArguments(args);
                    return limFrag;
                default:
                    // TODO handle invalid tab position
                    return null;
            }
        }

        // override item position change in order to refresh fragments
        @Override
        public int getItemPosition(Object object) {
            // return super.getItemPosition(object);
            Log.d(TAG, "getItemPosition = " + object.toString());
            return PagerAdapter.POSITION_NONE;  // changed
        }

        // store current fragment. see:
        // http://stackoverflow.com/questions/18609261/getting-the-current-fragment-instance-in-the-viewpager
        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (getCurrentFragment() != object) {
                mCurrentFragment = ((Fragment) object);
            }
            super.setPrimaryItem(container, position, object);
        }

        // extracts current fragment
        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }
    }

    // handle on click of button Get Value
    public void onClickBtnGetValue(View view) {
        Log.d(TAG, "onClickBtnGetValue");

        mSylvacBleSrvc.writeCharacteristic(SylvacBleService.COMMAND_GET_CURRENT_VALUE);
        return;
    }

    // handle on click of button Clear Value
    public void onClickBtnClearValue(View view) {
        Log.d(TAG, "onClickBtnClearValue");

        clearMeasurement();
        return;
    }

    // checks whether the app
    public boolean isMeasurementEnabled() {
        return (mPiece.getStatus() == CollectStatus.OPEN && SecurityUtils.isSecurityOk(this, false));
    }
}
