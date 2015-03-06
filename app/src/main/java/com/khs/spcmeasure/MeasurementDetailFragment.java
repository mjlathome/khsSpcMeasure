package com.khs.spcmeasure;

import java.util.Date;

import android.R.bool;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.khs.spcmeasure.dummy.DummyContent;
import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Limits;
import com.khs.spcmeasure.entity.Measurement;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.library.DateTimeUtils;
import com.khs.spcmeasure.library.LimitType;

/**
 * A fragment representing a single Measurement detail screen. This fragment is
 * either contained in a {@link MeasurementListActivity} in two-pane mode (on
 * tablets) or a {@link MeasurementDetailActivity} on handsets.
 */
public class MeasurementDetailFragment extends Fragment {
	
	private static final String TAG = "MeasurementDetailFragment";
	
	// id's
	private Long mPieceId;
	private Long mProdId;
	private Long mFeatId;

	// data
	private Product mProduct;
	private Piece mPiece;	
	private Feature mFeature;
	private Measurement mMeasurement;
	private Limits mLimitCl;
	private Limits mLimitEng;

	// views
	private TextView mTxtProdName;
	private TextView mTxtFeatName;
	private TextView mTxtCollDt;
	private TextView mTxtCollSt;
	private TextView mTxtMeasValue;
	private TextView mTxtInControl;
	private TextView mTxtNoLimits;
	private ListView mLstLimits;
	
	// services
	private SylvacBleService mBleService;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MeasurementDetailFragment() {
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		// move to the first feature
		// MeasurementListActivity measListActivity = (MeasurementListActivity) activity;
		// measListActivity.getFirst();		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// indicate fragment has action bar menu
		setHasOptionsMenu(true);
		
		// TODO create Toast utils?
		// error message constants
		String errPrefix = "ERROR: ";
		String errSuffix = ".  Contact administrator (" + TAG + ")";
		
		// extract arguments
		Bundle args = getArguments();		
		if (args != null) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mPieceId = args.getLong(DBAdapter.KEY_PIECE_ID);
			mFeatId = args.getLong(DBAdapter.KEY_FEAT_ID);
		} else {
			Toast.makeText(getActivity(), errPrefix + "no Arguments" + errSuffix, Toast.LENGTH_LONG).show();
			getActivity().finish();			
		}
				
		Log.d(TAG, "mPieceId = " + mPieceId);	
		Log.d(TAG, "mFeatId = " + mFeatId);	
		
		// verify arguments
		if (mPieceId == null ) {
			Toast.makeText(getActivity(), errPrefix + "Piece Id invalid" + errSuffix, Toast.LENGTH_LONG).show();
			getActivity().finish();						
		}
		
		if (mFeatId == null ) {
			Toast.makeText(getActivity(), errPrefix + "Feature Id invalid" + errSuffix, Toast.LENGTH_LONG).show();
			getActivity().finish();						
		}
				
		return;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_measurement_detail,
				container, false);

		// TODO remove later
//		// store layout views
//		mTxtProdName = (TextView) rootView.findViewById(R.id.txtProdName);
//		mTxtFeatName = (TextView) rootView.findViewById(R.id.txtFeatName);
//		mTxtCollDt = (TextView) rootView.findViewById(R.id.txtCollDt);
//		mTxtCollSt = (TextView) rootView.findViewById(R.id.txtCollSt);
//		mTxtMeasValue = (TextView) rootView.findViewById(R.id.txtMeasValue);
//		mTxtInControl = (TextView) rootView.findViewById(R.id.txtInControl);
//		mTxtNoLimits = (TextView) rootView.findViewById(R.id.txtNoLimits);
//		mLstLimits = (ListView) rootView.findViewById(R.id.lstLimits);
		
		return rootView;
	}
	
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.frag_meas_dtl, menu);		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		Log.d(TAG, "menu = " + item.getTitle());
		
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.mnuScanBle) {
			mBleService.scanLeDevice(true);
			return true;
		} else if (id == R.id.mnuSetUomMm) {			
			mBleService.writeCharacteristic(SylvacBleService.COMMAND_SET_MEASUREMENT_UOM_MM);
			return true;			
		} else if (id == R.id.mnuSetZero) {
			mBleService.writeCharacteristic(SylvacBleService.COMMAND_SET_ZERO_RESET);
			return true;
 		} else if (id == R.id.mnuGetValue) {
 			mBleService.writeCharacteristic(SylvacBleService.COMMAND_GET_CURRENT_VALUE);
			return true;
 		} else if (id == R.id.mnuClearValue) {
 			setValue(null); 			 	
			return true;			
		} else if (id == R.id.mnuGetBattery) {
 			mBleService.writeCharacteristic(SylvacBleService.COMMAND_GET_BATTERY_STATUS);
			return true;			
		}
			
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		// store layout views
		View rootView = getView();
		mTxtProdName = (TextView) rootView.findViewById(R.id.txtProdName);
		mTxtFeatName = (TextView) rootView.findViewById(R.id.txtFeatName);
		mTxtCollDt = (TextView) rootView.findViewById(R.id.txtCollDt);
		mTxtCollSt = (TextView) rootView.findViewById(R.id.txtCollSt);
		mTxtMeasValue = (TextView) rootView.findViewById(R.id.txtMeasValue);
		mTxtInControl = (TextView) rootView.findViewById(R.id.txtInControl);
		mTxtNoLimits = (TextView) rootView.findViewById(R.id.txtNoLimits);
		mLstLimits = (ListView) rootView.findViewById(R.id.lstLimits);		
		
		try {
			// TODO implement interface for this?
			// TODO ensure call works when not 2 pane
			// extract Ble service for direct communication
			MeasurementListActivity measListAct = (MeasurementListActivity) getActivity();
			mBleService = measListAct.getBleService();		
					
			// extract on-screen data
			mPiece = findPiece(mPieceId);
			mProduct = findProduct(mPiece.getProdId());
			mFeature = findFeature(mPiece.getProdId(), mFeatId);
			mLimitCl  = findLimit(mFeature.getProdId(), mFeature.getFeatId(), mFeature.getLimitRev(), LimitType.CONTROL);
			mLimitEng = findLimit(mFeature.getProdId(), mFeature.getFeatId(), mFeature.getLimitRev(), LimitType.ENGINEERING);
			mMeasurement = findMeasurement(mPieceId, mPiece.getProdId(), mFeatId);
		
			// display layout views
			displayAll();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
			
		return;
	}

	// sets the on-screen value
	public boolean setValue(Double value) {
		// TODO should probably need to create/update/delete the measurement here instead of externally 
		boolean success = false;			
		boolean inControl = false;
		
		Log.d(TAG, "setValue: " + value);
				
		if (value != null) {
			// determine whether value is in-control
			inControl = isInLimit(mLimitCl, value);
			
			// update or create Measurement object
			if (mMeasurement != null) {
				if (updateMeasurement(value) == false) {
					return false;			
				}
			} else {
				mMeasurement = createMeasurement(value);
			}
			
			// save Measurement db record
			success = saveMeasurement(mMeasurement);
		} else {
			// delete db record
			success = deleteMeasurement(mMeasurement);
			mMeasurement = null;
		}
		
		// update display
		displayMeasurement();
		
		if (inControl == true) {
			// TODO implement interface for this?
			// TODO ensure call works when not 2 pane
			// navigate to the next feature automatically as reading was good
			MeasurementListActivity measListAct = (MeasurementListActivity) getActivity();
			measListAct.getNext();
		}
		
		return success;
	}
	
	// gets the on-screen value
	public Double getValue() {
		Double d = null;
		
		try {
			if (mTxtMeasValue.getText().length() != 0) {
				d = Double.parseDouble(mTxtMeasValue.getText().toString());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return d;	
	}
		
	public void setBleService(SylvacBleService serv) {
		mBleService = serv;
	}
	
	// extracts the piece
	private Piece findPiece(long pieceId) {
		Piece piece = null;

		DBAdapter db = new DBAdapter(getActivity());
		try {		
			db.open();
			
			// get piece
			Cursor cPiece = db.getPiece(mPieceId);
			piece = db.cursorToPiece(cPiece);
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
				
		return piece;
	}

	// extracts the product
	private Product findProduct(long prodId) {
		Product prod = null;

		DBAdapter db = new DBAdapter(getActivity());
		try {		
			db.open();
			
			// get product
			Cursor cProd = db.getProduct(prodId);
			prod = db.cursorToProduct(cProd);
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
				
		return prod;
	}
	
	// extracts the feature
	private Feature findFeature(long prodId, long featId) {
		Feature feat = null;

		DBAdapter db = new DBAdapter(getActivity());
		try {		
			db.open();
			
			// get feature
			Cursor cFeat = db.getFeature(prodId, featId);
			feat = db.cursorToFeature(cFeat);
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
				
		return feat;
	}
	
	// extracts the limit
	private Limits findLimit(long prodId, long featId, long rev, LimitType limType) {
		Limits limit = null;

		DBAdapter db = new DBAdapter(getActivity());
		try {		
			db.open();
			
			// get limit
			Cursor cLimit = db.getLimit(prodId, featId, rev, limType);
			limit = db.cursorToLimit(cLimit);
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
				
		return limit;
	}	
	
	// extracts the measurement
	private Measurement findMeasurement(long pieceId, long prodId, long featId) {
		Measurement meas = null;

		DBAdapter db = new DBAdapter(getActivity());
		try {		
			db.open();
			
			// get measurement
			Cursor cMeas = db.getMeasurement(pieceId, prodId, featId);
			if(cMeas != null && cMeas.getCount() > 0) {
				meas = db.cursorToMeasurement(cMeas);				
			}
				
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
				
		return meas;
	}

	// save provided Measurement object into the db
	private boolean saveMeasurement(Measurement meas) {
		Log.d(TAG, "saveMeas: Inctrl = " +  meas.isInControl());
		boolean success = false;
		long rowId;
						
		// update db
		DBAdapter db = new DBAdapter(getActivity());
		try {		
			db.open();
					
			// update or insert Measurement into the db
			if (db.updateMeasurement(meas) == false) {
				rowId = db.createMeasurement(meas);
				if (rowId >= 0) {
					meas.setId(rowId);
					success = true;
				}
			} else {
				success = true;
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		
		return success;
	}	
	
	// delete provided Measurement object from the db
	private boolean deleteMeasurement(Measurement meas) {
		boolean success = true;
		
		// delete Measurement if it exists
		if (meas != null && meas.getId() != null) {					
			DBAdapter db = new DBAdapter(getActivity());
			try {		
				db.open();
				success = db.deleteMeasurement(meas.getId());				
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				db.close();
			}
		}
		
		return success;
	}

	// display all layout views
	private void displayAll() {		
		Log.d(TAG, "displayAll");
		displayProduct();
		displayPiece();
		displayFeature();
		displayMeasurement();
		displayLimits();
		return;
	}	

	// display Product layout views 
	private void displayProduct() {
		mTxtProdName.setText(mProduct.getName());
		return;
	}	
	
	// display Piece layout views 
	private void displayPiece() {
		mTxtCollDt.setText(mPiece.getCollectDt().toString());
		mTxtCollSt.setText(mPiece.getStatus().name());
		return;
	}	

	// display Feature layout views 
	private void displayFeature() {
		mTxtFeatName.setText(mFeature.getName());
		return;
	}	
		
	// display Measurement layout views 
	private void displayMeasurement() {
		if (mMeasurement != null) {
			Log.d(TAG, "dispMeas - isInCtrl = " + mMeasurement.isInControl());
			
			mTxtMeasValue.setText(Double.toString(mMeasurement.getValue()));
			mTxtInControl.setText(mMeasurement.isInControl()? "YES" : "NO");
		} else {
			mTxtMeasValue.setText("");	
			mTxtInControl.setText("");
		}			
		
		if (mTxtMeasValue != null) {
		}
		return;
	}	
	
	// display Limits 
	private void displayLimits() {
		// set empty limits text
		mLstLimits.setEmptyView(mTxtNoLimits);
		
		// populate limits list 		
		DBAdapter db = new DBAdapter(getActivity());
		db.open();
		Cursor c = db.getAllLimits(mFeature.getProdId(), mFeature.getFeatId());		
		ResourceCursorAdapter adapter = new LimitsAdapter(getActivity(), 
				R.layout.list_row_limits, c, 0);
		// associate adapter with list view
		mLstLimits.setAdapter(adapter);		
		db.close();				
		
		return;
	}	
		
	// create Measurement object
	private Measurement createMeasurement(Double value) {		
		Measurement meas = new Measurement(mPieceId, mPiece.getProdId(), mFeatId, 
				new Date(), // TODO is this required? was: mPiece.getCollectDt() 
				mPiece.getOperator(),	// TODO needs to be current user, not the one who created the piece?  
				value, 0.0, 0, mFeature.getLimitRev(),
				isInLimit(mLimitCl, value), isInLimit(mLimitEng, value));  
		
		return meas;
	}
	
	// update Measurement object
	private boolean updateMeasurement(Double value) {
		boolean success = false;
		
		if (mMeasurement != null) {
			mMeasurement.setCollectDt(new Date());	// TODO is actual dt collected for measurement required?
			mMeasurement.setOperator(mPiece.getOperator());	// TODO should this be from the actual logged in user
			mMeasurement.setValue(value);
            mMeasurement.setRange(0.0);
            mMeasurement.setCause(0);
			mMeasurement.setInControl(isInLimit(mLimitCl, value));
			mMeasurement.setInEngLim(isInLimit(mLimitEng, value));
			
			success = true;
		}
		
		return success;
	}	
	
	// returns whether the provided value is within the Limits
	private boolean isInLimit(Limits limit, Double value) {
		
		if (limit != null) {
			Log.d(TAG, "isInLimit: U: " + limit.getUpper() + "; L: " + limit.getLower() + "; V: " + value);	
		}
				
		boolean inLim = false;	
		if (limit != null && value >= limit.getLower() && value <= limit.getUpper()) {
			inLim = true;
		}
		return inLim;		
	}
		
}
