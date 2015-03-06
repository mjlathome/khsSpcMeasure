package com.khs.spcmeasure;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Limits;
import com.khs.spcmeasure.entity.Measurement;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.LimitType;
import com.khs.spcmeasure.tasks.ImportSimpleCodeTask;

import java.util.Date;

/**
 * A fragment representing a single Measurement detail screen. This fragment is
 * either contained in a {@link com.khs.spcmeasure.MeasurementListActivity} in two-pane mode (on
 * tablets) or a {@link com.khs.spcmeasure.MeasurementDetailActivity} on handsets.
 */
public class MeasurementFragment extends Fragment implements AdapterView.OnItemSelectedListener{

	private static final String TAG = "MeasurementFragment";

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
    private TextView mTxtLimUpper;
    private TextView mTxtLimLower;
	private TextView mTxtMeasValue;
    private TextView mTxtMeasRange;
    private Spinner mSpnMeasCause;
    private ImageView mImgInControl;

	// services
//	private SylvacBleService mBleService;

    // spinner interface calls
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "onItemSelected: i = " + i + "; l = " + l);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MeasurementFragment() {
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
		// setHasOptionsMenu(true);
		
		// TODO create Toast utils?
		// error message constants
		String errPrefix = "ERROR: ";
		String errSuffix = ".  Contact administrator (" + TAG + ")";

        // TODO use separate extract and check methods
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
		View rootView = inflater.inflate(R.layout.fragment_measurement,
				container, false);

		return rootView;
	}
	
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);

        // TODO old and can be removed?
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

        // TODO - can remove as No longer called?
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.mnuScanBle) {
//			mBleService.scanLeDevice(true);
			return true;
		} else if (id == R.id.mnuSetUomMm) {			
//			mBleService.writeCharacteristic(SylvacBleService.COMMAND_SET_MEASUREMENT_UOM_MM);
			return true;			
		} else if (id == R.id.mnuSetZero) {
//			mBleService.writeCharacteristic(SylvacBleService.COMMAND_SET_ZERO_RESET);
			return true;
 		} else if (id == R.id.mnuGetValue) {
// 			mBleService.writeCharacteristic(SylvacBleService.COMMAND_GET_CURRENT_VALUE);
			return true;
 		} else if (id == R.id.mnuClearValue) {
 			setValue(null); 			 	
			return true;			
		} else if (id == R.id.mnuGetBattery) {
// 			mBleService.writeCharacteristic(SylvacBleService.COMMAND_GET_BATTERY_STATUS);
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
        mTxtLimUpper = (TextView) rootView.findViewById(R.id.txtLimUpper);
        mTxtLimLower = (TextView) rootView.findViewById(R.id.txtLimLower);
		mTxtMeasValue = (TextView) rootView.findViewById(R.id.txtMeasValue);
        mTxtMeasRange = (TextView) rootView.findViewById(R.id.txtMeasRange);
        mSpnMeasCause = (Spinner) rootView.findViewById(R.id.spnMeasCause);
		mImgInControl = (ImageView) rootView.findViewById(R.id.imgInControl);

        try {
            // TODO implement interface for this?
            // TODO ensure call works when not 2 pane
            // extract Ble service for direct communication
//			MeasurementListActivity measListAct = (MeasurementListActivity) getActivity();
//			mBleService = measListAct.getBleService();

            // extract on-screen data
            mPiece = findPiece(mPieceId);
            mProduct = findProduct(mPiece.getProdId());
            mFeature = findFeature(mPiece.getProdId(), mFeatId);
            mLimitCl  = findLimit(mFeature.getProdId(), mFeature.getFeatId(), mFeature.getLimitRev(), LimitType.CONTROL);
            mLimitEng = findLimit(mFeature.getProdId(), mFeature.getFeatId(), mFeature.getLimitRev(), LimitType.ENGINEERING);
            mMeasurement = findMeasurement(mPieceId, mPiece.getProdId(), mFeatId);

        } catch(Exception e) {
            e.printStackTrace();
        }

        // show the Action Cause list in the Spinner
        DBAdapter db = new DBAdapter(getActivity());
        db.open();
        // TODO move types const to SimpleCode
        Cursor c = db.getAllSimpleCode(ImportSimpleCodeTask.TYPE_ACTION_CAUSE);

        // populate spinner for Collect Status and setup handler
        if (c.getCount() > 0) {
            String[] from = new String[]{DBAdapter.KEY_DESCRIPTION};
            // create an array of the display item we want to bind our data to
            int[] to = new int[]{android.R.id.text1};
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, c, from, to, 0);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpnMeasCause.setAdapter(adapter);
            mSpnMeasCause.setOnItemSelectedListener(this);
        }

        db.close();

		return;
	}

    @Override
    public void onResume() {
        super.onResume();

        try {
            // display layout views
            displayAll();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // sets the on-screen value
	public boolean setValue(Double value) {
        // TODO if measurement already set then ignore.
		// TODO should probably need to create/update/delete the measurement here instead of externally 
		boolean success = false;			
		boolean inControl = false;
		
		Log.d(TAG, "setValue: " + value);
				
		if (value != null) {
            // ignore new measurement if there is already one set
            if (mMeasurement != null) {
                AlertUtils.alertDialogShow(getActivity(), getString(R.string.text_warning), getString(R.string.text_meas_not_cleared));
                return false;
            }

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

        // navigate to next measurement if in control
		if (inControl == true) {
			// TODO implement interface for this?
			// navigate to the next feature automatically as reading was good
            FeatureActivity featAct = (FeatureActivity) getActivity();
            featAct.getNext();
		} else {
            AlertUtils.alertDialogShow(getActivity(), getString(R.string.text_warning), getString(R.string.text_out_control_choose_cause));
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
		
//	public void setBleService(SylvacBleService serv) {
//		mBleService = serv;
//	}
	
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
        displayLimits();
		displayMeasurement();
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

    // display Limit layout views
    private void displayLimits() {
        mTxtLimUpper.setText(Double.toString(mLimitCl.getUpper()));
        mTxtLimLower.setText(Double.toString(mLimitCl.getLower()));
        return;
    }

    // display Measurement layout views
	private void displayMeasurement() {
		if (mMeasurement != null) {
			Log.d(TAG, "dispMeas - isInCtrl = " + mMeasurement.isInControl());
			
			mTxtMeasValue.setText(Double.toString(mMeasurement.getValue()));
			mImgInControl.setImageResource(mMeasurement.isInControl() ? R.drawable.ic_meas_in_control : R.drawable.ic_meas_out_control);
            mSpnMeasCause.setVisibility(mMeasurement.isInControl() ? View.INVISIBLE : View.VISIBLE);
		} else {
			mTxtMeasValue.setText("");
            mImgInControl.setImageResource(R.drawable.ic_meas_unknown);
            mSpnMeasCause.setVisibility(View.INVISIBLE);
		}			

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
