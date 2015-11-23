package com.khs.spcmeasure.tasks;

import android.content.Context;
import android.util.Log;

import com.khs.spcmeasure.dao.FeatureDao;
import com.khs.spcmeasure.dao.LimitsDao;
import com.khs.spcmeasure.dao.MeasurementDao;
import com.khs.spcmeasure.dao.PieceDao;
import com.khs.spcmeasure.dao.ProductDao;
import com.khs.spcmeasure.dao.SimpleCodeDao;
import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Limits;
import com.khs.spcmeasure.entity.Measurement;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.entity.SimpleCode;
import com.khs.spcmeasure.library.LimitType;
import com.khs.spcmeasure.library.SecurityUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by Mark on 05/04/2015.
 * Measurement Business Task
 */
public class MeasurementTask {

    private static final String TAG = "MeasurementTask";

    // member variables
    private Context mContext;

    // declare Dao's - cannot initialize yet as require Activity context
    private PieceDao mPieceDao;
    private ProductDao mProdDao;
    private FeatureDao mFeatDao;
    private LimitsDao mLimDao;
    private MeasurementDao mMeasDao;
    private SimpleCodeDao mSimpleCodeDao;

    // constructor
    public MeasurementTask(Context mContext) {
        this.mContext = mContext;

        // instantiate Dao's
        mPieceDao = new PieceDao(mContext);
        mProdDao = new ProductDao(mContext);
        mFeatDao = new FeatureDao(mContext);
        mLimDao = new LimitsDao(mContext);
        mMeasDao = new MeasurementDao(mContext);
        mSimpleCodeDao = new SimpleCodeDao(mContext);
    }

    // create Measurement object
    public Measurement createMeasurement(long pieceId, long featId, Double value) {
        Measurement meas = null;

        try {
            // extract data
            Piece piece     = mPieceDao.getPiece(pieceId);
            Feature feature = mFeatDao.getFeature(piece.getProdId(), featId);
            Limits limitCl  = mLimDao.getLimit(feature.getProdId(), feature.getFeatId(), feature.getLimitRev(), LimitType.CONTROL);
            Limits limitEng = mLimDao.getLimit(feature.getProdId(), feature.getFeatId(), feature.getLimitRev(), LimitType.ENGINEERING);

            // determine in control and cause
            boolean inCtrl = isInLimit(limitCl, value);
            Long cause = getCause(inCtrl);
            // Long cause = (inCtrl? null : mSpnMeasCause.getAdapter().getItemId(0));   // use first entry in spinner if out-of-control

            Log.d(TAG, "createMeas: prodId = " + piece.getProdId());

            meas = new Measurement(piece.getId(), feature.getProdId(), feature.getFeatId(),
                    new Date(), // TODO is this required? was: mPiece.getCollectDt()
                    SecurityUtils.getUsername(mContext), // was: piece.getOperator() TODO needs to be current user, not the one who created the piece?
                    value,
                    calcRange(feature.getProdId(), feature.getFeatId(), piece.getCollectDt(), value),
                    cause,
                    feature.getLimitRev(),
                    inCtrl,
                    isInLimit(limitEng, value));
        } catch(Exception e) {
            e.printStackTrace();
        }


        return meas;
    }

    // update Measurement object
    public boolean updateMeasurement(Measurement meas, Double value) {
        boolean success = false;

        try {
            // extract data
            Piece piece     = mPieceDao.getPiece(meas.getPieceId());
            Feature feature = mFeatDao.getFeature(piece.getProdId(), meas.getFeatId());
            Limits limitCl  = mLimDao.getLimit(meas.getProdId(), meas.getFeatId(), feature.getLimitRev(), LimitType.CONTROL);
            Limits limitEng = mLimDao.getLimit(meas.getProdId(), meas.getFeatId(), feature.getLimitRev(), LimitType.ENGINEERING);

            // calculate cause, use first entry in spinner if out-of-control
            boolean inCtrl = isInLimit(limitCl, value);
            Long cause = getCause(inCtrl);

            // Long cause = (inCtrl ? null : mSpnMeasCause.getAdapter().getItemId(0));   // use first entry in spinner if out-of-control

            meas.setCollectDt(new Date());    // TODO is actual dt collected for measurement required?
            meas.setOperator(piece.getOperator());    // TODO should this be from the actual logged in user
            meas.setValue(value);
            meas.setRange(calcRange(piece.getProdId(), feature.getFeatId(), piece.getCollectDt(), value));
            meas.setCause(cause);
            meas.setLimitRev(feature.getLimitRev());
            meas.setInControl(inCtrl);
            meas.setInEngLim(isInLimit(limitEng, value));

            success = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return success;
    }

    // extracts cause Id given in-control state
    private Long getCause(boolean inCtrl) {
        Long cause = null;

        try {
            if (inCtrl == false) {
                SimpleCode defCause = mSimpleCodeDao.getSimpleCodeByTypeIntCode(SimpleCode.TYPE_ACTION_CAUSE, SimpleCode.INTERNAL_CODE_NO_CAUSE);
                cause = defCause.getId();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return cause;
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

    // calculate the range for Feature between the current and previous value
    private double calcRange(long prodId, long featId, Date collDate, double value) {
        double range = 0.0;

        try {
            // extract previous list of Pieces, if any
            // order is already descending from the db query
            List<Piece> pieceList = mPieceDao.getPrevPieces(prodId, collDate);
            Log.d(TAG, "calcRange: pieceList size = " + pieceList.size());

            // calculate range using the previous Measurement
            for (Piece piece : pieceList) {
                Log.d(TAG, "calcRange: Prev Piece Date = " + piece.getCollectDt());
                Measurement prevMeas = mMeasDao.getMeasurement(piece.getId(), piece.getProdId(), featId);
                if (prevMeas != null) {
                    Log.d(TAG, "calcRange: value = " + value + "; Prev Meas = " + prevMeas.getValue());

                    range = value - prevMeas.getValue();
                    break;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return range;
    }

}
