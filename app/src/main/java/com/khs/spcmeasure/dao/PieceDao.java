package com.khs.spcmeasure.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.entity.Piece;

public class PieceDao {
	
	private static final String TAG = "PieceDao";
	
	private Context mContext;
	private DBAdapter db;
	
	// constructor
	public PieceDao(Context mContext) {
		super();
		this.mContext = mContext;
		
		// instantiate db helper 
		db = new DBAdapter(mContext);
	}

	// find Piece from id
	public Piece getPiece(long id) {
		Log.d(TAG, "getPeice id = " + id);
		
        db.open();
		Cursor c = db.getPiece(id);
		Piece piece = db.cursorToPiece(c);
		db.close();
		
		Log.d(TAG, "getPeice Piece St = " + piece.getStatus());
		
		return piece;
	}
}
