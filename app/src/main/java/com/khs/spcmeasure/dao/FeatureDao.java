package com.khs.spcmeasure.dao;

import android.content.Context;
import android.database.Cursor;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.entity.Feature;

import java.util.ArrayList;
import java.util.List;

public class FeatureDao {

	private static final String TAG = "FeatureDao";

	private Context mContext;
	private DBAdapter db;

	// constructor
	public FeatureDao(Context context) {
		super();
		this.mContext = context;
		
		// instantiate db helper 
		db = new DBAdapter(mContext);
	}

    // find Feature
    public Feature getFeature(long prodId, long featId) {
        Feature feat = null;

        // DBAdapter db = new DBAdapter(getActivity());
        try {
            db.open();

            // get feature
            Cursor cFeat = db.getFeature(prodId, featId);
            feat = db.cursorToFeature(cFeat);

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return feat;
    }

    // list All Features for provided Product Id
    public List<Feature> getAllFeatures(long prodId) {
        List featList = new ArrayList();

        // DBAdapter db = new DBAdapter(getActivity());
        try {
            db.open();

            // query the database
            Cursor cFeat = db.getAllFeatures(prodId);

            // iterate the results
            if (cFeat.moveToFirst()) {
                do {
                    // add feature to the list
                    featList.add(db.cursorToFeature(cFeat));
                } while(cFeat.moveToNext());
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return featList;
    }

}
