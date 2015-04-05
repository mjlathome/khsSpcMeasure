package com.khs.spcmeasure.dao;

import android.content.Context;
import android.database.Cursor;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.entity.SimpleCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 05/04/2015.
 * Simple Code Data Access Object
 */
public class SimpleCodeDao {

    private static final String TAG = "SimpleCodeDao";

    private Context mContext;
    private DBAdapter db;

    // constructor
    public SimpleCodeDao(Context context) {
        super();
        this.mContext = context;

        // instantiate db helper
        db = new DBAdapter(mContext);
    }

    // find Simple Code by Type and Internal Code
    public SimpleCode getSimpleCodeByTypeIntCode(String type, String intCode) {
        SimpleCode simpleCode = null;
        Cursor cSimpleCode = null;

        try {
            db.open();

            // get feature
            cSimpleCode = db.getSimpleCodeByTypeIntCode(type, intCode);
            simpleCode = db.cursorToSimpleCode(cSimpleCode);

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (cSimpleCode != null) {
                cSimpleCode.close();
            }
            db.close();
        }

        return simpleCode;
    }

    // list All Simple Codes by Type
    public List<SimpleCode> getAllSimpleCodesByType(String type) {
        List simpleCodeList = new ArrayList();
        Cursor cSimpleCode = null;

        try {
            db.open();

            // query the database
            cSimpleCode = db.getAllSimpleCode(type);

            // iterate the results
            if (cSimpleCode.moveToFirst()) {
                do {
                    // add feature to the list
                    simpleCodeList.add(db.cursorToSimpleCode(cSimpleCode));
                } while(cSimpleCode.moveToNext());
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (cSimpleCode != null) {
                cSimpleCode.close();
            }
            db.close();
        }

        return simpleCodeList;
    }

}
