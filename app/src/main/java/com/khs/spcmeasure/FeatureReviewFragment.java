package com.khs.spcmeasure;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.khs.spcmeasure.dao.PieceDao;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.library.DateTimeUtils;
import com.khs.spcmeasure.library.SecurityUtils;
import com.khs.spcmeasure.service.MeasurementService;

/**
 * Measurement Review fragment
 * allows the user to examine all values at once and Close the Piece if required
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FeatureReviewFragment extends ListFragment {

    private static final String TAG = "FeatureReviewFragment";
    private static ListView mListView;

    // declare Dao's
    private PieceDao mPieceDao;

    // Piece members
    private Long mPieceId;
    private Piece mPiece;

    private long mFeatureId = ListView.INVALID_POSITION;

    private OnFragmentInteractionListener mListener;

    // close Piece members
    private ClosePieceConfirmTask mClosePieceConfirmTask;
    private ClosePieceTask mClosePieceTask;

    // close Piece confirmation nested class -  ensures work is done off the UI thread to prevent ANR
    private class ClosePieceConfirmTask extends AsyncTask<Void, Void, Boolean> {
        // feature count members
        private int mNumFeat = 0;
        private int mNumMeas = 0;

        @Override
        protected Boolean doInBackground(Void... args) {
            boolean success = false;

            try {
                // extract the piece
                Log.d(TAG, "Close Piece St = " + mPiece.getStatus());

                // exit if Piece is not Open
                if (mPiece.getStatus() == CollectStatus.OPEN) {
                    // extract features and measurements for the product/piece
                    DBAdapter db = new DBAdapter(getActivity());
                    db.open();

                    // TODO remove later - was extracting all features
                    // Cursor cFeat = db.getAllFeatures(mPiece.getProdId());

                    // get active features for the product
                    Cursor cFeat = db.getFeaturesByProdIdActive(mPiece.getProdId(), true);
                    Cursor cMeas = db.getAllMeasurements(mPiece.getId());
                    mNumFeat = cFeat.getCount();
                    mNumMeas = cMeas.getCount();
                    cFeat.close();
                    cMeas.close();
                    db.close();
                }

                success = true;
            } catch(Exception e) {
                e.printStackTrace();
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            // skip UI update if task was cancelled
            if (isCancelled()) {
                return;
            }

            // exit upon failure
            if (result == false) {
                AlertUtils.errorDialogShow(getActivity(), getString(R.string.text_piece_close_failed));
                return;
            }

            // ensure piece is open
            if (mPiece.getStatus() != CollectStatus.OPEN) {
                AlertUtils.alertDialogShow(getActivity(), getString(R.string.text_information), getString(R.string.text_piece_not_open));
                return;
            }

            // ensure that there was at least one measurement taken
            if (mNumMeas == 0) {
                AlertUtils.alertDialogShow(getActivity(), getString(R.string.text_information), getString(R.string.text_piece_feature_measured_none));
                return;
            }

            // build confirmation message
            String message = getString(R.string.text_piece_close_question) + "\n";
            if (mNumFeat == mNumMeas) {
                message += getString(R.string.text_piece_feature_measured_all);
            } else {
                message += getString(R.string.text_piece_feature_measured_some, mNumMeas, mNumFeat);
            }

            // display confirmation dialog
            AlertDialog.Builder dlgAlert = AlertUtils.createAlert(getActivity(), getString(R.string.text_warning), message);
            dlgAlert.setPositiveButton(getString(R.string.text_okay), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "Piece Close - Okay");

                    // close the Piece
                    mClosePieceTask = new ClosePieceTask();
                    mClosePieceTask.execute();
                }
            });
            dlgAlert.setNegativeButton(getString(R.string.text_cancel), null);
            dlgAlert.show();
        }
    }

    // close Piece task nested class -  ensures work is done off the UI thread to prevent ANR
    private class ClosePieceTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... args) {
            boolean success = false;

            try {
                Log.d(TAG, "Close Piece St = " + mPiece.getStatus());

                // exit if Piece is not Open
                if (mPiece.getStatus() == CollectStatus.OPEN) {
                    mPiece.setStatus(CollectStatus.CLOSED);
                    success = mPieceDao.savePiece(mPiece);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            // skip UI update if task was cancelled
            if (isCancelled()) {
                return;
            }

            // exit upon failure
            if (result == false) {
                AlertUtils.errorDialogShow(getActivity(), getString(R.string.text_piece_close_failed));
                return;
            }

            // export Piece to server
            MeasurementService.startActionExport(getActivity(), mPiece.getId());

            // update the screen
            displayView();

            // inform user
            Toast.makeText(getActivity(), getString(R.string.text_piece_now_closed), Toast.LENGTH_LONG).show();
                        // TODO need to ensure no further readings can take place
                        // TODO need to refresh Piece List screen upon return
        }
    }

    // TODO: use newInstance method throughout
    public static FeatureReviewFragment newInstance(Long pieceId) {
        FeatureReviewFragment fragment = new FeatureReviewFragment();
        Bundle args = new Bundle();
        args.putLong(DBAdapter.KEY_PIECE_ID, pieceId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FeatureReviewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // indicate Fragment menu option support required
        setHasOptionsMenu(true);

		/* extract arguments */
        Bundle args = getArguments();
        if (args != null) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mPieceId = args.getLong(DBAdapter.KEY_PIECE_ID);
        }
        Log.d(TAG, "mPieceId = " + mPieceId);

        // extract the Piece
        DBAdapter db = new DBAdapter(getActivity());
        db.open();
        Cursor c = db.getPiece(mPieceId);
        mPiece = db.cursorToPiece(c);
        c.close();
        db.close();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_feature_review, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle action bar item clicks here
        int id = item.getItemId();
        switch(id) {
            case R.id.mnuClosePiece:
                closePiece();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // return super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_feature_review,
                container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated");

        // initialize Dao's
        mPieceDao = new PieceDao(getActivity());

        // displayView();
        // refreshList();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        displayView();
        refreshList();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(id);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(long featId);
    }

    // display on-screen Views
    public void displayView() {

        // TODO error trap when required data not available

        // display Piece Views
        if (mPiece != null) {
            ((TextView) getView().findViewById(R.id.txtCollectDt))
                    .setText(DateTimeUtils.getDateTimeStr(mPiece.getCollectDt()));
            ((TextView) getView().findViewById(R.id.txtCollStatus))
                    .setText(mPiece.getStatus().toString());

            // show the Product Name in the TextView
            DBAdapter db = new DBAdapter(getActivity());
            db.open();
            Cursor c = db.getProduct(mPiece.getProdId());
            ((TextView) getView().findViewById(R.id.txtProdName))
                    .setText(c.getString(c.getColumnIndex(DBAdapter.KEY_NAME)));
            c.close();
            db.close();
        }
    }
    
    // refresh Feature List
    public void refreshList() {

        Log.d(TAG, "refreshList = " + mPieceId);

        // setEmptyText(getString(R.string.text_no_data));

        // start out with a progress indicator.
        // setListShown(false);

        // clear current selected Feature
        mFeatureId = ListView.INVALID_POSITION;

        // extract features for the product
        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        // extract cursor to features based upon piece collect state
        Cursor c = null;
        if (mPiece.getStatus() == CollectStatus.OPEN) {
            // get active features for the product
            c = db.getFeaturesByProdIdActive(mPiece.getProdId(), true);
        } else {
            // get features measured on the piece
            c = db.getFeaturesByPieceId(mPieceId);
        }

        // TODO was always return all Features
        // Cursor c = db.getAllFeatures(mPiece.getProdId());

        // SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                // android.R.layout.simple_list_item_activated_1, c,
        FeatureReviewAdapter adapter = new FeatureReviewAdapter(getActivity(), c, mPieceId);

        // associate adapter with list view
        setListAdapter(adapter);
        db.close();

        // remove progress indicator.
        // setListShown(true);

        /****
         *
         *
         mListView = getListView();
         mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         mListView.setTextFilterEnabled(true);
         *
         */
    }

    // close the Piece
    private void closePiece() {

        // check security
        if (SecurityUtils.checkSecurity(getActivity(), true)) {
            // handle Close Piece confirmation
            mClosePieceConfirmTask = new ClosePieceConfirmTask();
            mClosePieceConfirmTask.execute();
        }
    }

}
