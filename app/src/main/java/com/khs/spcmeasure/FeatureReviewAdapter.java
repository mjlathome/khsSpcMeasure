package com.khs.spcmeasure;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.khs.spcmeasure.dao.MeasurementDao;
import com.khs.spcmeasure.dao.PieceDao;
import com.khs.spcmeasure.entity.Measurement;
import com.khs.spcmeasure.entity.Piece;

/**
 * Created by Mark on 23/02/2015.
 */
public class FeatureReviewAdapter extends CursorAdapter {

    private static final String TAG = "FeatureReviewCurAdapt";

    private Context mContext;
    private OnFeatureReviewAdapter mFeatRevAdapt;
    private PieceDao mPieceDao;
    private MeasurementDao mMeasurementDao;

    // constructor
    public FeatureReviewAdapter(Context context, Cursor c) {
        super(context, c, 0);

        mContext = context;
        mPieceDao = new PieceDao(context);
        mMeasurementDao = new MeasurementDao(context);

        try {
            mFeatRevAdapt = (OnFeatureReviewAdapter) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(mFeatRevAdapt.toString()
                    + " must implement OnFeatureReviewAdapter");
        }
    }

    // inflate the view
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_row_feature_review, viewGroup, false);
    }

    // bind data to the view provided
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // find fields to populate from inflated template
        TextView tvFeatName = (TextView) view.findViewById(R.id.txtFeatName);
        ImageView ivInControl = (ImageView) view.findViewById(R.id.imgInControl);

        // extract properties from cursor
        Long featId = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_FEAT_ID));
        String featName = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_NAME));

        // populate fields with extracted properties
        tvFeatName.setText(featName);

        // extract Measurement
        Long pieceId = mFeatRevAdapt.onGetPieceId();
        Piece piece = mPieceDao.getPiece(pieceId);
        Measurement meas = mMeasurementDao.getMeasurement(pieceId, piece.getProdId(), featId);

        // populate ListView image and set row colour according to Measurement in-control state
        if (meas != null) {
            Log.d(TAG, "meas - isInCtrl = " + meas.isInControl());
            ivInControl.setImageResource(meas.isInControl()? R.drawable.ic_meas_in_control : R.drawable.ic_meas_out_control);
            view.setBackgroundColor(mContext.getResources().getColor(meas.isInControl()? R.color.measInControl : R.color.measOutControl));
        } else {
            Log.d(TAG, "meas - null");
            ivInControl.setImageResource(R.drawable.ic_meas_unknown);
            view.setBackgroundColor(mContext.getResources().getColor(android.R.color.background_light));
        }
    }

    // this Interface which must be defined by anybody that uses this Adapter
    public interface OnFeatureReviewAdapter {
        // extract the Piece Id
        public Long onGetPieceId();
    }

}
