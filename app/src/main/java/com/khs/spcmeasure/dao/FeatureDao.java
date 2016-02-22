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
        Cursor cFeat = null;

        // DBAdapter db = new DBAdapter(getActivity());
        try {
            db.open();

            // get feature
            cFeat = db.getFeature(prodId, featId);
            feat = db.cursorToFeature(cFeat);

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (cFeat != null) {
                cFeat.close();
            }
            db.close();
        }

        return feat;
    }

    // list All Features for provided Product Id
    public List<Feature> getAllFeatures(long prodId) {
        List featList = new ArrayList();
        Cursor cFeat = null;

        // DBAdapter db = new DBAdapter(getActivity());
        try {
            db.open();

            // query the database
            cFeat = db.getAllFeatures(prodId);

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
            if (cFeat != null) {
                cFeat.close();
            }
            db.close();
        }

        return featList;
    }

    // list Features by prodId and active
    public List<Feature> getFeaturesByProdIdActive(long prodId, boolean active) {
        List featList = new ArrayList();
        Cursor cFeat = null;

        try {
            db.open();

            // query the database
            cFeat = db.getFeaturesByProdIdActive(prodId, active);

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
            if (cFeat != null) {
                cFeat.close();
            }
            db.close();
        }

        return featList;
    }

    // list Features by pieceId
    public List<Feature> getFeaturesByPieceId(long pieceId) {
        List featList = new ArrayList();
        Cursor cFeat = null;

        try {
            db.open();

            // query the database
            cFeat = db.getFeaturesByPieceId(pieceId);

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
            if (cFeat != null) {
                cFeat.close();
            }
            db.close();
        }

        return featList;
    }

}
