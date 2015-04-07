package com.khs.spcmeasure;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.khs.spcmeasure.dao.FeatureDao;
import com.khs.spcmeasure.dao.ProductDao;
import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.library.AlertUtils;

import java.text.DecimalFormat;

/**
 * A fragment representing a list of Limits.
 * <p/>
 * <p/>
 */
public class FeatureInfoFragment extends ListFragment {

    final static String TAG = "FeatureInfoFragment";

    // argument members
    private Long mProdId = null;
    private Long mFeatId  = null;

    // declare Dao members
    private ProductDao mProductDao;
    private FeatureDao mFeatureDao;

    // declare data members
    private Product mProduct;
    private Feature mFeature;

    // declare view members
    private TextView mTxtProdName;
    private TextView mTxtFeatName;
    private TextView mTxtCp;
    private TextView mTxtCpk;
    private View mHeaderView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FeatureInfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // extract intent arguments, if any
        getArguments(getArguments());

        // extract saved instance state arguments, if any
        getArguments(savedInstanceState);

        // verify arguments
        if (!chkArguments()) {
            AlertUtils.errorDialogShow(getActivity(), getString(R.string.text_mess_arguments_invalid));
            getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // return super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_feature_info,
                container, false);

        // inflate list header - to be added later in the lifecycle during onActivityCreated()
        mHeaderView = inflater.inflate(R.layout.list_row_limits, null);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            // instantiate Dao's
            mProductDao = new ProductDao(getActivity());
            mFeatureDao = new FeatureDao(getActivity());

            // extract data
            mProduct = mProductDao.getProduct(mProdId);
            mFeature = mFeatureDao.getFeature(mProdId, mFeatId);

            // extract views
            View rootView = getView();
            mTxtProdName    = (TextView) rootView.findViewById(R.id.txtProdName);
            mTxtFeatName    = (TextView) rootView.findViewById(R.id.txtFeatName);
            mTxtCp          = (TextView) rootView.findViewById(R.id.txtCp);
            mTxtCpk         = (TextView) rootView.findViewById(R.id.txtCpk);

            // add list header view
            this.getListView().addHeaderView(mHeaderView);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // display layout views
        displayAll();

        // set view data
//        txtProdName.setText(mProduct.getName());
//        txtFeatName.setText(mFeature.getName());
//        txtCp.setTe

        // refresh list view
        refreshList();
    }

    // display all layout views
    private void displayAll() {
        Log.d(TAG, "displayAll");

        // format to 3dp
        DecimalFormat df = new DecimalFormat("#.000");

        try {
            mTxtProdName.setText(mProduct.getName());
            mTxtFeatName.setText(mFeature.getName());

            Log.d(TAG, "displayAll: cp = " + mFeature.getCp() + "; cpk = " + mFeature.getCpk());

            if (mFeature.getCp() != null) {
                mTxtCp.setText(df.format(mFeature.getCp()));
            } else {
                mTxtCp.setText("");
            }

            if (mFeature.getCpk() != null) {
                mTxtCpk.setText(df.format(mFeature.getCpk()));
            } else {
                mTxtCpk.setText("");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // extracts arguments from provided Bundle
    private void getArguments(Bundle args) {
        // extract piece id
        if (args != null) {
            if (args.containsKey(DBAdapter.KEY_PROD_ID)) {
                mProdId = args.getLong(DBAdapter.KEY_PROD_ID);
            }
            if (args.containsKey(DBAdapter.KEY_FEAT_ID)) {
                mFeatId = args.getLong(DBAdapter.KEY_FEAT_ID);
            }
        }
    }

    // checks arguments
    private boolean chkArguments() {
        // verify arguments
        return (mProdId != null && mFeatId != null);
    }

    // refresh listview
    public void refreshList() {
        // setEmptyText(getString(R.string.text_no_data));

        // start out with a progress indicator.
        // setListShown(false);

        // extract all current Products into the listview
        DBAdapter db = new DBAdapter(getActivity());
        db.open();
        Cursor c = db.getAllLimits(mProdId, mFeatId, mFeature.getLimitRev());

        ResourceCursorAdapter adapter = new LimitsAdapter(getActivity(),
                R.layout.list_row_limits, c, 0);

        // associate adapter with list view
        setListAdapter(adapter);

        db.close();

        // remove progress indicator.
        // setListShown(true);
    }
}
