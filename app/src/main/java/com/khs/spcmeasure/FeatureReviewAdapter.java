package com.khs.spcmeasure;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

    // set row colour
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    // sets ListView image and row color
    // TODO change to CursorAdpater instead; see:
    // http://www.gustekdev.com/2013/05/custom-cursoradapter-and-why-not-use.html
    @Override
    public void setViewImage(ImageView v, String value) {
        // super.setViewImage(v, value);

        // extract parent view
        ViewGroup row = (ViewGroup) v.getParent();

        // extract Measurement
        Long featId = Long.parseLong(value);
        Long pieceId = mFeatRevAdapt.onGetPieceId();
        Piece piece = mPieceDao.getPiece(pieceId);
        Measurement meas = mMeasurementDao.getMeasurement(pieceId, piece.getProdId(), featId);

        // set ListView image according to Measurement in-control state
        if (meas != null) {
            Log.d(TAG, "meas - isInCtrl = " + meas.isInControl());
            v.setImageResource(meas.isInControl()? R.drawable.ic_meas_in_control : R.drawable.ic_meas_out_control);
            row.setBackgroundColor(mContext.getResources().getColor(meas.isInControl()? R.color.measInControl : R.color.measOutControl));
        } else {
            Log.d(TAG, "meas - null");
            v.setImageResource(R.drawable.ic_meas_unknown);
            row.setBackgroundColor(mContext.getResources().getColor(android.R.color.background_light));
        }

    }

    // this Interface which must be defined by anybody that uses this Adapter
    public interface OnFeatureReviewAdapter {
        // extract the Piece Id
        public Long onGetPieceId();
    }

}
