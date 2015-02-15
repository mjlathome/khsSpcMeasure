package com.khs.spcmeasure;

import com.khs.spcmeasure.SylvacBleService.MyLocalBinder;
import com.khs.spcmeasure.entity.Measurement;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.dao.PieceDao;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;

/**
 * An activity representing a list of Measurements. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link MeasurementDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MeasurementListFragment} and the item details (if present) is a
 * {@link MeasurementDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link MeasurementListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class MeasurementListActivity extends Activity implements
		MeasurementListFragment.Callbacks {

	private static final String TAG = "MeasurementListActivity";
	
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	
	private Long mPieceId;
	private Piece mPiece;
	
	SylvacBleService mBleService;
	boolean mBound = false;
	
	PieceDao mPieceDao;
	
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			MyLocalBinder binder = (MyLocalBinder) service;
			mBleService = binder.getService();
			
			mBound = true;
			
			MeasurementDetailFragment measDetailFrag = (MeasurementDetailFragment) getFragmentManager().findFragmentById(R.id.measurement_detail_container);
			measDetailFrag.setBleService(mBleService);
						
		}
				
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub				
			mBound = false;			
			
			MeasurementDetailFragment measDetailFrag = (MeasurementDetailFragment) getFragmentManager().findFragmentById(R.id.measurement_detail_container);
			measDetailFrag.setBleService(null);			
		}
		
	};
		
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
	        					Toast.makeText(MeasurementListActivity.this, "Battery Level Okay (" + battState.trim() + ")", Toast.LENGTH_LONG).show();	        					
	        				} else if (battState.equals(SylvacBleService.BATTERY_LOW)) {
	        					AlertUtils.alertDialogShow(MeasurementListActivity.this, "Warning", "Battery Level Low (" + battState.trim() + ")");
	        				} else {
	        					AlertUtils.errorDialogShow(MeasurementListActivity.this, "Battery Level Unknown (" + battState + ")");
	        				}	        						        					        		
	        			} else if (lastWrite == SylvacBleService.COMMAND_GET_INSTRUMENT_ID_CODE) {
	        				String id = new String(intent.getByteArrayExtra(SylvacBleService.EXTRA_DATA_CHAR_DATA));	        				
	        				Log.d(TAG, "Instrument Id: " + id);
	        				AlertUtils.alertDialogShow(MeasurementListActivity.this, "Information", "Instrument ID = " + id);
	        			} else if (lastWrite == SylvacBleService.COMMAND_SET_MEASUREMENT_UOM_MM) {
	        				Log.d(TAG, "UOM is now MM");
        					Toast.makeText(MeasurementListActivity.this, "Unit of Measuresure now mm", Toast.LENGTH_LONG).show();
	        			} else if (lastWrite == SylvacBleService.COMMAND_SET_ZERO_RESET) {
	        				Log.d(TAG, "Zero was reset");
        					Toast.makeText(MeasurementListActivity.this, "Zero was reset", Toast.LENGTH_LONG).show();	        						        				
	        			} else {
	        				Log.e(TAG, "Unknown Cmd/Req: " + lastWrite);
	        				AlertUtils.errorDialogShow(MeasurementListActivity.this, "Unknown Command/Request.  Contact administrator. (" + lastWrite + ")");	        				
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate - layout before");
		setContentView(R.layout.activity_measurement_list);
		Log.d(TAG, "onCreate - layout after");
		
		// enable up on the action bar icon
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if (findViewById(R.id.measurement_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((MeasurementListFragment) getFragmentManager().findFragmentById(
					R.id.measurement_list)).setActivateOnItemClick(true);
		}

		// create dao
		mPieceDao = new PieceDao(this);
		
		// TODO: If exposing deep links into your app, handle intents here.
		// extract piece id from intent; exit if not found
		Bundle args = getIntent().getExtras();
		if (args != null && args.containsKey(DBAdapter.KEY_PIECE_ID)) {
			mPieceId = args.getLong(DBAdapter.KEY_PIECE_ID);
			
			// extract the piece
//			mPiece = mPieceDao.getPiece(mPieceId);
	        DBAdapter db = new DBAdapter(this);
	        db.open();
			Cursor c = db.getPiece(mPieceId);
			Log.d(TAG, "Cursor count = " + c.getCount());
			mPiece = db.cursorToPiece(c);
			db.close();
			Log.d(TAG, "OnCreate Piece St = " + mPiece.getStatus());
			
		} else {		
			AlertUtils.errorDialogShow(this, "Piece Id is invalid");
			finish();			
		}			
	}
	
	@Override
	protected void onResume() {		
		super.onResume();
		
		// FUTURE move all Ble checking to the service.  Can this be done due to UI interaction.
		// Maybe via a callback. 
		
        // Check for Bluetooth LE Support.  In production, the manifest entry will keep this
        // from installing on these devices, but this will allow test devices or other
        // sideloads to report whether or not the feature exists.
        // NOTE: Not really needed as included in Manifest.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No Bluetooth LE Support.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }		
      
		// register local broadcast receiver - filter multiple intent actions
		IntentFilter bleFilter = new IntentFilter();
		// bleFilter.addAction(SylvacBleService.ACTION_BLE_NOT_SUPPORTED);
		// bleFilter.addAction(SylvacBleService.ACTION_BLUETOOTH_NOT_ENABLED);
		bleFilter.addAction(SylvacBleService.ACTION_CHARACTERISTIC_CHANGED);
		  
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, bleFilter);    		
		
		// Initializes Bluetooth adapter.
		final BluetoothManager bluetoothManager =
		        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
		
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            startActivity(enableBtIntent);
            finish();
            return;
        }		
	}
	
	
		
    @Override
	protected void onPause() {
		super.onPause();
		
		// unregister local broadcast receiver
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		if (mPiece.getStatus() == CollectStatus.OPEN) {
			// bind to Ble service
			bindBleService();
		}
	
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
        // unbind from Ble service
		unbindBleService();
	}	
	
	/**
	 * Callback method from {@link MeasurementListFragment.Callbacks} indicating
	 * that the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(Long rowId) {
		
		// extract the Feature Id
        DBAdapter db = new DBAdapter(this);
        db.open();
		Cursor cFeat = db.getFeature(rowId);
		int featId = cFeat.getInt(cFeat.getColumnIndex(DBAdapter.KEY_FEAT_ID));
		db.close();
		
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putLong(DBAdapter.KEY_PIECE_ID, mPieceId);
			arguments.putLong(DBAdapter.KEY_FEAT_ID, featId);
			MeasurementDetailFragment fragment = new MeasurementDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.replace(R.id.measurement_detail_container, fragment)
					.commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this,
					MeasurementDetailActivity.class);
			detailIntent.putExtra(DBAdapter.KEY_PIECE_ID, mPieceId);
			detailIntent.putExtra(DBAdapter.KEY_FEAT_ID, featId);
			startActivity(detailIntent);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inflate menu
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.act_meas_lst, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Log.d(TAG, "menu = " + item.getTitle());
			
		// handle action bar clicks
		switch(id) {
		case R.id.mnuFirst:
			getFirst();			
			return true;			
		case R.id.mnuPrev:
			getPrev();			
			return true;			
		case R.id.mnuNext:
			getNext();			
			return true;			
		case R.id.mnuLast:
			getLast();			
			return true;					
		case R.id.mnuClosePiece:
			closePiece();			
			return true;
		default:
			return super.onOptionsItemSelected(item);	
		}				
	}		
	
    // Check for Bluetooth LE Support.  In production, the manifest entry will keep this
    // from installing on these devices, but this will allow test devices or other
    // sideloads to report whether or not the feature exists.
    // NOTE: Not really needed as included in Manifest.
//    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//        Toast.makeText(this, "No Bluetooth LE Support.", Toast.LENGTH_LONG).show();
//        finish();
//        return;
//    }		
	
	private boolean closePiece() {
		boolean success = false;
		
		int numFeat = 0;
		int numMeas = 0;
		
		// extract the piece
		// mPiece = mPieceDao.getPiece(mPieceId);
        DBAdapter db = new DBAdapter(this);
        db.open();
		Cursor c = db.getPiece(mPieceId);
		mPiece = db.cursorToPiece(c);
		db.close();
		
		Log.d(TAG, "Close Piece St = " + mPiece.getStatus());
		
		// exit if Piece is already closed
		if (mPiece.getStatus() == CollectStatus.CLOSED) {
			AlertUtils.alertDialogShow(this, "Inforamtion", "Piece is already closed.");
			return true;
		}
				
		// TODO move to another layer
		// extract features and measurements for the product/piece		
		// DBAdapter db = new DBAdapter(this);
		db.open();
		Cursor cFeat = db.getAllFeatures(mPiece.getProdId());
		Cursor cMeas = db.getAllMeasurements(mPiece.getId());
		numFeat = cFeat.getCount();
		numMeas = cMeas.getCount();
		db.close();			
		
		// build dialog message
		String message = "Are you sure you wish to Close this piece?\n";
		if (numFeat == numMeas) {
			message += "All Features have been measured.";
		} else {
			message += numMeas + " out of " + numFeat + " features have been measured."; 
		}
				
		// display dialog
		AlertDialog.Builder dlgAlert = AlertUtils.createAlert(MeasurementListActivity.this, "Warning", message);
		dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {							
				Log.d(TAG, "Piece Close - Okay");
				
				// TODO use DAO?
				DBAdapter db = new DBAdapter(MeasurementListActivity.this);
								
				// mark Piece as closed, update db and unbind Ble service as not required
				try {
					mPiece.setStatus(CollectStatus.CLOSED);
					db.open();
					db.updatePiece(mPiece);
					Toast.makeText(MeasurementListActivity.this, "Piece is now Closed", Toast.LENGTH_LONG).show();
					unbindBleService();
					// TODO need to ensure no further readings can take place
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					db.close();
				}
			}
		}); 		
		dlgAlert.setNegativeButton("Cancel", null); 				
		dlgAlert.show();
		
		return success;
	}

	// bind to Ble service
	private void bindBleService() {
	    Intent intent = new Intent(this, SylvacBleService.class);
	    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);		
	    return;
	}
	
	// unbind from Ble service
	private void unbindBleService() {
	    if (mBound) {
	        unbindService(mConnection);
	        mBound = false;
	    }
	    return;
	}
		
	public SylvacBleService getBleService() {
		return mBleService;
	}
	
	private boolean setMeasurement(byte[] value) {
		boolean success = false;
		
		// convert characteristic byte array data to a double
		Double myDouble = Double.parseDouble(new String(value));
		
		// update Measurement value
		if (myDouble != null) {
			MeasurementDetailFragment measDetailFrag = (MeasurementDetailFragment) getFragmentManager().findFragmentById(R.id.measurement_detail_container);
			success = measDetailFrag.setValue(myDouble);
		}		
		
		return success;
	}
	
	public ListView getListView() {
		MeasurementListFragment measListFrag = (MeasurementListFragment) getFragmentManager().findFragmentById(R.id.measurement_list);
		return measListFrag.getListView();
	}
	
	public void getFirst() {		
		MeasurementListFragment measListFrag = (MeasurementListFragment) getFragmentManager().findFragmentById(R.id.measurement_list);
		long featId = measListFrag.getFirst();
		onItemSelected(featId);
	}

	public void getPrev() {
		MeasurementListFragment measListFrag = (MeasurementListFragment) getFragmentManager().findFragmentById(R.id.measurement_list);
		long featId = measListFrag.getPrev();
		onItemSelected(featId);
	}
	
	public void getNext() {
		MeasurementListFragment measListFrag = (MeasurementListFragment) getFragmentManager().findFragmentById(R.id.measurement_list);
		long featId = measListFrag.getNext();
		onItemSelected(featId);
	}
	
	public void getLast() {
		MeasurementListFragment measListFrag = (MeasurementListFragment) getFragmentManager().findFragmentById(R.id.measurement_list);
		long featId = measListFrag.getLast();
		onItemSelected(featId);
	}
		
}
