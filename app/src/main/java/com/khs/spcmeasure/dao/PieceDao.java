package com.khs.spcmeasure.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.entity.Piece;

import java.util.ArrayList;
import java.util.Date;
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
        Cursor cPiece = null;

        try {
            db.open();

            // get piece
            cPiece = db.getPiece(pieceId);
            piece = db.cursorToPiece(cPiece);

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (cPiece != null) {
                cPiece.close();
            }
            db.close();
        }

        Log.d(TAG, "getPiece: " + piece);

        return piece;
	}

    // find Piece by product id, sub-group id and piece number
    public Piece getPiece(long prodId, long sgId, long pieceNum) {
        Piece piece = null;
        Cursor cPiece = null;

        try {
            db.open();

            // get Piece
            cPiece = db.getPiece(prodId, sgId, pieceNum);
            if(!db.isCursorEmpty(cPiece)) {
                piece = db.cursorToPiece(cPiece);
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (cPiece != null) {
                cPiece.close();
            }
            db.close();
        }

        return piece;
    }

    // list All Pieces for provided Product
    public List<Piece> getAllPieces(long prodId) {
        List pieceList = new ArrayList();
        Cursor cPiece = null;

        // DBAdapter db = new DBAdapter(getActivity());
        try {
            db.open();

            // query the database
            cPiece = db.getAllPieces(prodId);

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
            if (cPiece != null) {
                cPiece.close();
            }
            db.close();
        }

        return pieceList;
    }

    // list previous Pieces for provided product id and collect date
    public List<Piece> getPrevPieces(long prodId, Date collDate) {
        List pieceList = new ArrayList();
        Cursor cPiece = null;

        // DBAdapter db = new DBAdapter(getActivity());
        try {
            db.open();

            // query the database
            cPiece = db.getPrevPieces(prodId, collDate);

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
            if (cPiece != null) {
                cPiece.close();
            }
            db.close();
        }

        return pieceList;
    }

    // save provided Piece into the db
    public boolean savePiece(Piece piece) {
        Log.d(TAG, "savePiece: id = " + piece.getId());

        boolean success = false;

        // update db
        try {
            db.open();

            // update or insert Piece into the db
            if (db.updatePiece(piece) == false) {
                long rowId = db.createPiece(piece);
                if (rowId >= 0) {
                    piece.setId(rowId);
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
}
