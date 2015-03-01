package com.khs.spcmeasure.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.entity.Piece;

import java.util.ArrayList;
import java.util.List;

public class PieceDao {
	
	private static final String TAG = "PieceDao";
	
	private Context mContext;
	private DBAdapter db;
	
	// constructor
	public PieceDao(Context context) {
		super();
		this.mContext = context;
		
		// instantiate db helper 
		db = new DBAdapter(mContext);
	}

	// find Piece from id
	public Piece getPiece(long pieceId) {
		Log.d(TAG, "getPiece id = " + pieceId);

        Piece piece = null;

        // DBAdapter db = new DBAdapter(getActivity());
        try {
            db.open();

            // get piece
            Cursor cPiece = db.getPiece(pieceId);
            piece = db.cursorToPiece(cPiece);

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return piece;
	}

    // list All Pieces for provided Product
    public List<Piece> getAllPieces(long prodId) {
        List pieceList = new ArrayList();

        // DBAdapter db = new DBAdapter(getActivity());
        try {
            db.open();

            // query the database
            Cursor cPiece = db.getAllPieces(prodId);

            // iterate the results
            if (cPiece.moveToFirst()) {
                do {
                    // add feature to the list
                    pieceList.add(db.cursorToPiece(cPiece));
                } while(cPiece.moveToNext());
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return pieceList;
    }
}
