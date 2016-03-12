package com.khs.spcmeasure.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.khs.spcmeasure.helper.DBAdapter;
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

    // save provided Measurement into the db
    public boolean saveMeasurement(Measurement meas) {
        Log.d(TAG, "saveMeasurement: InCtrl = " + meas.isInControl());

        boolean success = false;

        // update db
        try {
            db.open();

            // update or insert Measurement into the db
            if (db.updateMeasurement(meas) == false) {
                long rowId = db.createMeasurement(meas);
                if (rowId >= 0) {
                    meas.setId(rowId);
                    success = true;
                }
            } else {
                success = true;
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return success;
    }

    // delete provided Measurement from the db
    public boolean deleteMeasurement(Measurement meas) {
        boolean success = false;

        // delete Measurement if it exists
        if (meas != null && meas.getId() != null) {
            try {
                db.open();
                success = db.deleteMeasurement(meas.getId());
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                db.close();
            }
        }

        return success;
    }

}
