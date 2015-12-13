package com.khs.spcmeasure;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.khs.spcmeasure.dao.FeatureDao;
import com.khs.spcmeasure.dao.LimitsDao;
import com.khs.spcmeasure.dao.MeasurementDao;
import com.khs.spcmeasure.dao.PieceDao;
import com.khs.spcmeasure.dao.ProductDao;
import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Limits;
import com.khs.spcmeasure.entity.Measurement;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.entity.SimpleCode;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.library.CursorAdapterUtils;
import com.khs.spcmeasure.library.DateTimeUtils;
import com.khs.spcmeasure.library.LimitType;
import com.khs.spcmeasure.library.SecurityUtils;
import com.khs.spcmeasure.tasks.MeasurementTask;

import java.text.DecimalFormat;

/**
 * A fragment representing a single Measurement detail screen.
 */
public class MeasurementFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "MeasurementFragment";

    // id's
    private Long mPieceId;
    private Long mFeatId;

    // declare Dao's - cannot initialize yet as require Activity context
    private PieceDao mPieceDao;
    private ProductDao mProdDao;
    private FeatureDao mFeatDao;
    private LimitsDao mLimDao;
    private MeasurementDao mMeasDao;

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
    private TextView mEdtMeasValue;
    private TextView mTxtMeasRange;
    private Spinner mSpnMeasCause;
    private ImageView mImgInControl;
    private Button mBtnGetValue;
    private Button mBtnClearValue;

    // set measurement value task member
    private SetMeasValueTask mSetMeasValueTask;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MeasurementFragment() {
    }

    // set measurement value nested class -  ensures work is done off the UI thread to prevent ANR
    private class SetMeasValueTask extends AsyncTask<Double, Void, Boolean> {

        // business logic members
        private MeasurementTask mMeasTask;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d(TAG, "onPreExecute");

            // business logic load
            mMeasTask = new MeasurementTask(getActivity());
        }

        @Override
        protected Boolean doInBackground(Double... args) {
            Log.d(TAG, "doInBackground");

            Boolean success = false;

            try {
                // extract value
                Double value = args[0];

                Log.d(TAG, "setValue: " + value);

                if (value != null) {
                    // update or create Measurement object
                    if (mMeasurement != null) {
                        if (mMeasTask.updateMeasurement(mMeasurement, value) == false) {
                            return false;
                        }
                    } else {
                        mMeasurement = mMeasTask.createMeasurement(mPieceId, mFeatId, value);
                    }

                    // save Measurement db record
                    success = mMeasDao.saveMeasurement(mMeasurement);
                } else {
                    // delete db record
                    success = mMeasDao.deleteMeasurement(mMeasurement);
                    mMeasurement = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            Log.d(TAG, "onPostExecute");

            // skip UI update if task was cancelled
            if (isCancelled()) {
                return;
            }

            // handle success/failure
            if (result == false) {
                AlertUtils.errorDialogShow(MeasurementFragment.this.getActivity(), getString(R.string.text_meas_save_failed));
                return;
            } else {
                // update display
                displayMeasurement();

                if (mMeasurement != null) {
                    if (mMeasurement.isInControl()) {
                        // navigate to next measurement if in control
                        // TODO implement interface for this?
                        FeatureActivity featAct = (FeatureActivity) getActivity();
                        featAct.moveNext();
                    } else {
                        // prompt for cause
                        // AlertUtils.alertDialogShow(getActivity(), getString(R.string.text_warning), getString(R.string.text_out_control_choose_cause));
                        mSpnMeasCause.performClick();
                    }
                }
            }

            return;
        }
    }

    //region spinner interface calls

    // handle cause selection
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        Log.d(TAG, "onItemSelected: pos = " + pos + "; id = " + id);

        if (mMeasurement != null && mMeasurement.getCause() != id) {
            mMeasurement.setCause(id);
            mMeasDao.saveMeasurement(mMeasurement);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    //endregion

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);

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
        if (mPieceId == null) {
            Toast.makeText(getActivity(), errPrefix + "Piece Id invalid" + errSuffix, Toast.LENGTH_LONG).show();
            getActivity().finish();
        }

        if (mFeatId == null) {
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
        Log.d(TAG, "onActivityCreated");

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
        mEdtMeasValue = (TextView) rootView.findViewById(R.id.edtMeasValue);
        mTxtMeasRange = (TextView) rootView.findViewById(R.id.txtMeasRange);
        mSpnMeasCause = (Spinner) rootView.findViewById(R.id.spnMeasCause);
        mImgInControl = (ImageView) rootView.findViewById(R.id.imgInControl);
        mBtnGetValue = (Button) rootView.findViewById(R.id.btnGetValue);
        mBtnClearValue = (Button) rootView.findViewById(R.id.btnClearValue);

        try {
            // instantiate Dao's
            mPieceDao = new PieceDao(getActivity());
            mProdDao = new ProductDao(getActivity());
            mFeatDao = new FeatureDao(getActivity());
            mLimDao = new LimitsDao(getActivity());
            mMeasDao = new MeasurementDao(getActivity());

            // extract on-screen data
            mPiece = mPieceDao.getPiece(mPieceId);
            mProduct = mProdDao.getProduct(mPiece.getProdId());
            mFeature = mFeatDao.getFeature(mPiece.getProdId(), mFeatId);
            mLimitCl = mLimDao.getLimit(mFeature.getProdId(), mFeature.getFeatId(), mFeature.getLimitRev(), LimitType.CONTROL);
            mLimitEng = mLimDao.getLimit(mFeature.getProdId(), mFeature.getFeatId(), mFeature.getLimitRev(), LimitType.ENGINEERING);
            mMeasurement = mMeasDao.getMeasurement(mPieceId, mPiece.getProdId(), mFeatId);

            // TODO set value format
            DecimalFormat threeZeros = new DecimalFormat("#0.000");

            // for gap checks, setup a listener for Done on the value field
            mEdtMeasValue.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        Log.d(TAG, "Meas Value: DONE");

                        // get double and set value if not null
                        Double value = null;
                        try {
                            value = Double.parseDouble(mEdtMeasValue.getText().toString());
                        } catch (final NumberFormatException e) {
                            value = null;
                        }
                        setValue(value);
                    }
                    return false;
                }
            });

            // TODO remove later
            /*
            if (isGapCheck()) {
                mEdtMeasValue.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    // handle text changed
                    @Override
                    public void afterTextChanged(Editable s) {
                        Double value = null;
                        // get double and set value if not null
                        if (s.length() > 0) {
                            try {
                                value = Double.parseDouble(mEdtMeasValue.getText().toString());
                            } catch (final NumberFormatException e) {
                                value = null;
                            }
                        }

                        setValue(value);
                    }
                });
            }
            */

            // show the Action Cause list in the Spinner
            DBAdapter db = new DBAdapter(getActivity());
            db.open();

            Cursor c = db.getAllSimpleCode(SimpleCode.TYPE_ACTION_CAUSE);

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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return;
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            // display layout views
            displayAll();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");

        // cancel active Set Value Measurement task, if any
        if (mSetMeasValueTask != null) {
            Log.d(TAG, "onPause - task cancel");

            mSetMeasValueTask.cancel(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    // sets the on-screen value
    public void setValue(Double value) {
        Log.d(TAG, "setValue: " + value);

        if (!isGapCheck() && value != null) {
            // ignore new measurement if there is already one set
            if (mMeasurement != null) {
                AlertUtils.alertDialogShow(getActivity(), getString(R.string.text_information), getString(R.string.text_meas_not_cleared));
                return;
            }
        }

        // set the measurement value via a background task
        mSetMeasValueTask = new SetMeasValueTask();
        mSetMeasValueTask.execute(value);

        return;
    }

    // gets the on-screen value
    public Double getValue() {
        Double d = null;

        try {
            if (mEdtMeasValue.getText().length() != 0) {
                d = Double.parseDouble(mEdtMeasValue.getText().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return d;
    }

    // display all layout views
    public void displayAll() {
        Log.d(TAG, "displayAll");
        displayProduct();
        displayFeature();
        displayPiece();
        displayLimits();
        displayMeasurement();
        enableFields();
        return;
    }

    // display Product layout views
    private void displayProduct() {
        Log.d(TAG, "displayProduct: prod = " + mProduct);

        mTxtProdName.setText(mProduct.getName());
        return;
    }

    // display Piece layout views
    private void displayPiece() {
        mTxtCollDt.setText(DateTimeUtils.getDateTimeStr(mPiece.getCollectDt()));
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
        DecimalFormat df = new DecimalFormat("#.000");
        mTxtLimUpper.setText(df.format(mLimitCl.getUpper()));
        mTxtLimLower.setText(df.format(mLimitCl.getLower()));
        return;
    }

    // display Measurement layout views
    private void displayMeasurement() {
        DecimalFormat df = new DecimalFormat("#.000");
        Integer pos = 0;

        if (mMeasurement != null) {
            Log.d(TAG, "dispMeas - isInCtrl = " + mMeasurement.isInControl());

            getView().setBackgroundColor(getResources().getColor(mMeasurement.isInControl() ? R.color.measInControl : R.color.measOutControl));

            mEdtMeasValue.setText(df.format(mMeasurement.getValue()));
            mTxtMeasRange.setText(df.format(mMeasurement.getRange()));
            mImgInControl.setImageResource(mMeasurement.isInControl() ? R.drawable.ic_meas_in_control : R.drawable.ic_meas_out_control);

            // display cause
            if (mMeasurement.getCause() != null) {
                pos = CursorAdapterUtils.getPosForId((CursorAdapter) mSpnMeasCause.getAdapter(), mMeasurement.getCause());
//                SimpleCursorAdapter adapter = (SimpleCursorAdapter) mSpnMeasCause.getAdapter();
//                for (int pos = 0; pos < adapter.getCount(); pos++) {
//                    if (adapter.getItemId(pos) == mMeasurement.getCause()) {
//                        mSpnMeasCause.setSelection(pos);
//                    }
//                }
            }

            Log.d(TAG, "Spinner: count = " + mSpnMeasCause.getCount() + "; pos = " + pos + "; cause = " + mMeasurement.getCause());
            if (pos == null || pos < 0 || pos >= mSpnMeasCause.getCount()) {
                pos = 0;
            }
            mSpnMeasCause.setSelection(pos);
            mSpnMeasCause.setVisibility(mMeasurement.isInControl() ? View.INVISIBLE : View.VISIBLE);

        } else {
            getView().setBackgroundColor(getResources().getColor(android.R.color.background_light));
            mEdtMeasValue.setText("");
            mTxtMeasRange.setText("");
            mImgInControl.setImageResource(R.drawable.ic_meas_unknown);
            mSpnMeasCause.setSelection(0);
            mSpnMeasCause.setVisibility(View.INVISIBLE);
        }

        return;
    }

    // enable or disable on-screen fields
    public void enableFields() {
        // disable/hide/show views based upon Piece collect status
        // check security too
        // TODO needs to be called upon login/logout
        if (mPiece.getStatus() == CollectStatus.OPEN && SecurityUtils.checkSecurity(getActivity(), false)) {
            // TODO Gap test
            Log.d(TAG, "Gap Test - Feature: " + mTxtFeatName.getText().toString());
            if (isGapCheck()) {
                enableValue();
                mBtnGetValue.setVisibility(View.INVISIBLE);
                Log.d(TAG, "Gap Test - Enabled");
            } else {
                disableValue();
                mBtnGetValue.setVisibility(View.VISIBLE);
                Log.d(TAG, "Gap Test - Disabled");
            }

            mSpnMeasCause.setEnabled(true);
            // mBtnGetValue.setVisibility(View.VISIBLE);
            mBtnClearValue.setVisibility(View.VISIBLE);
        } else {
            disableValue();
            mSpnMeasCause.setEnabled(false);
            mBtnGetValue.setVisibility(View.INVISIBLE);
            mBtnClearValue.setVisibility(View.INVISIBLE);
        }
    }

    // enable measurement value field
    private void enableValue() {
        mEdtMeasValue.setEnabled(true);
        mEdtMeasValue.setFocusable(true);
        mEdtMeasValue.setFocusableInTouchMode(true);
        mEdtMeasValue.setKeyListener(DigitsKeyListener.getInstance("01234567890."));
        mEdtMeasValue.setBackgroundResource(android.R.drawable.edit_text);
        mEdtMeasValue.setHint(R.string.prompt_gap_value);
    }

    // disable measurement value field
    private void disableValue() {
        mEdtMeasValue.setEnabled(false);
        mEdtMeasValue.setFocusable(false);
        mEdtMeasValue.setFocusableInTouchMode(false);
        mEdtMeasValue.setKeyListener(DigitsKeyListener.getInstance("01234567890.-"));
        mEdtMeasValue.setBackgroundResource(android.R.color.transparent);
        mEdtMeasValue.setHint("");
        mEdtMeasValue.setTextColor(getResources().getColor(android.R.color.black));
    }

    // returns whether feature is a gap check
    public Boolean isGapCheck() {
        if (mFeature != null) {
            return (mFeature.isGapCheck());
        } else {
            return null;
        }
    }

    // hide keyboard input
    // see: http://stackoverflow.com/questions/24335223/edittext-swipes-out-of-visible-view-but-keyboard-remains
    public void hideInput() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEdtMeasValue.getWindowToken(), 0);
    }
}
