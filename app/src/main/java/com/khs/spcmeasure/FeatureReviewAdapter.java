package com.khs.spcmeasure;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

import com.khs.spcmeasure.dao.MeasurementDao;
import com.khs.spcmeasure.dao.PieceDao;
import com.khs.spcmeasure.entity.Measurement;
import com.khs.spcmeasure.entity.Piece;

/**
 * Created by Mark on 23/02/2015.
 */
public class FeatureReviewAdapter extends SimpleCursorAdapter {

    private static final String TAG = "FeatureReviewCurAdapt";
    private Context mContext;
    private OnFeatureReviewAdapter mFeatRevAdapt;
    private PieceDao mPieceDao;
    private MeasurementDao mMeasurementDao;

    // constructor
    public FeatureReviewAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);

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

    // sets ListView image
    @Override
    public void setViewImage(ImageView v, String value) {
        // super.setViewImage(v, value);

        // extract Measurement
        Long featId = Long.parseLong(value);
        Long pieceId = mFeatRevAdapt.onGetPieceId();
        Piece piece = mPieceDao.getPiece(pieceId);
        Measurement meas = mMeasurementDao.getMeasurement(pieceId, piece.getProdId(), featId);

        // set ListView image according to Measurement in-control state
        if (meas != null) {
            Log.d(TAG, "meas - isInCtrl = " + meas.isInControl());
            v.setImageResource(meas.isInControl()? R.drawable.ic_meas_in_control : R.drawable.ic_meas_out_control);
        } else {
            Log.d(TAG, "meas - null");
            v.setImageResource(R.drawable.ic_meas_unknown);
        }

    }

    // this Interface which must be defined by anybody that uses this Adapter
    public interface OnFeatureReviewAdapter {
        // extract the Piece Id
        public Long onGetPieceId();
    }

}
