package com.khs.spcmeasure;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.widget.ResourceCursorAdapter;
import com.khs.spcmeasure.library.AlertUtils;

/**
 * A fragment representing a list of Limits.
 * <p/>
 * <p/>
 */
public class LimitsFragment extends ListFragment {

    final static String TAG = "LimitsFragment";

    private Long mProdId = null;
    private Long mFeatId  = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LimitsFragment() {
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        refreshList();
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
        setEmptyText(getString(R.string.text_no_data));

        // start out with a progress indicator.
        setListShown(false);

        // extract all current Products into the listview
        DBAdapter db = new DBAdapter(getActivity());
        db.open();
        Cursor c = db.getAllLimits(mProdId, mFeatId);

        ResourceCursorAdapter adapter = new LimitsAdapter(getActivity(),
                R.layout.list_row_limits, c, 0);

        // associate adapter with list view
        setListAdapter(adapter);

        db.close();

        // remove progress indicator.
        setListShown(true);
    }
}
