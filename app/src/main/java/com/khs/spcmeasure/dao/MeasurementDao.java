package com.khs.spcmeasure.dao;

import android.content.Context;
import android.database.Cursor;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.entity.Measurement;

public class MeasurementDao {

	private static final String TAG = "MeasurementDao";

	private Context mContext;
	private DBAdapter db;

	// constructor
	public MeasurementDao(Context context) {
		super();
		this.mContext = context;
		
		// instantiate db helper 
		db = new DBAdapter(mContext);
	}

    // find Measurement
    public Measurement getMeasurement(long pieceId, long prodId, long featId) {
        Measurement meas = null;
        Cursor cMeas = null;

        // DBAdapter db = new DBAdapter(getActivity());
        try {
            db.open();

            // get measurement
            cMeas = db.getMeasurement(pieceId, prodId, featId);
            if(cMeas != null && cMeas.getCount() > 0) {
                meas = db.cursorToMeasurement(cMeas);
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (cMeas != null) {
                cMeas.close();
            }
            db.close();
        }

        return meas;
    }

}
