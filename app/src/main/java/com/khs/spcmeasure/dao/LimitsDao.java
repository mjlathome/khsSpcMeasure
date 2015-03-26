package com.khs.spcmeasure.dao;

import android.content.Context;
import android.database.Cursor;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.entity.Limits;
import com.khs.spcmeasure.library.LimitType;

import java.util.ArrayList;
import java.util.List;

public class LimitsDao {

	private static final String TAG = "LimitsDao";

	private Context mContext;
	private DBAdapter db;

	// constructor
	public LimitsDao(Context context) {
		super();
		this.mContext = context;
		
		// instantiate db helper 
		db = new DBAdapter(mContext);
	}

    // find Limits
    public Limits getLimit(long prodId, long featId, long rev, LimitType limitType) {
        Limits limit = null;
        Cursor cLimit = null;

        // DBAdapter db = new DBAdapter(getActivity());
        try {
            db.open();

            // get limit
            cLimit = db.getLimit(prodId, featId, rev, limitType);
            limit = db.cursorToLimit(cLimit);

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (cLimit != null) {
                cLimit.close();
            }
            db.close();
        }

        return limit;
    }

    // list All Limits for provided Product and Feature
    public List<Limits> getAllLimits(long prodId, long featId) {
        List limitList = new ArrayList();
        Cursor cLimit = null;

        // DBAdapter db = new DBAdapter(getActivity());
        try {
            db.open();

            // query the database
            cLimit = db.getAllLimits(prodId, featId);

            // iterate the results
            if (cLimit.moveToFirst()) {
                do {
                    // add feature to the list
                    limitList.add(db.cursorToLimit(cLimit));
                } while(cLimit.moveToNext());
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (cLimit != null) {
                cLimit.close();
            }
            db.close();
        }

        return limitList;
    }

}
