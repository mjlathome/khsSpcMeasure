package com.khs.spcmeasure;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.library.DateTimeUtils;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FeatureReviewFragment extends ListFragment {

    private static final String TAG = "FeatureReviewFragment";
    private static ListView mListView;

    private Long mPieceId;
    private Piece mPiece;

    private long mFeatureId = ListView.INVALID_POSITION;

    private OnFragmentInteractionListener mListener;

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
        db.close();
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

        displayView();
        refreshList();
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
        Cursor c = db.getAllFeatures(mPiece.getProdId());
        // SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                // android.R.layout.simple_list_item_activated_1, c,
        FeatureReviewAdapter adapter = new FeatureReviewAdapter(getActivity(),
                R.layout.list_row_feature_review, c,
                new String[] {DBAdapter.KEY_NAME, DBAdapter.KEY_FEAT_ID},
                new int[] {R.id.txtFeatName, R.id.imgInControl}, 0);

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

}
