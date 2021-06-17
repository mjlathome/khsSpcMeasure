package com.khs.spcmeasure.tasks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.khs.spcmeasure.helper.DBAdapter;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.library.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* task to export piece/measurement data to the server.
** updates the Piece with the response sub-group id.
** marks the Piece as history.
*/
public class ExportMeasTask extends AsyncTask<Long, JSONObject, Integer>{
	private static final String TAG = "ExportMeasTask";

	// private static String url = "http://10.35.33.58/spc/save_measurement.php";
	private static String url = "http://thor.magna.global/spc/save_measurements.php";
	// json node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_DATA    = "data";
		
	private Context mContext;
	private int mTotal   = 0;
	private int mCount   = 0;
	private int mSuccess = 0;
	
	// constructor
	public ExportMeasTask(Context context) {
		mContext = context;
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

	// perform task
	@Override
	protected Integer doInBackground(Long... params) {
		
		mTotal = params.length;
		
		// loop Piece rowId's
		for (int i = 0; i < mTotal; i++) {
			long rowId = params[i];
			
			try {
				// build json for the piece/measurements
				JSONObject jResults = getJsonResults(rowId);
				
				Log.d(TAG, "results of " + rowId + " = " + jResults.toString());
								
				// post json request
				JSONParser jParser = new JSONParser();
				JSONObject json = jParser.getJSONFromUrl(url, jResults.toString());
				
				// process json response
				publishProgress(json);
				
			} catch(Exception e) {
				e.printStackTrace();
			}
			/* TODO need to handle returned json 
			publishProgress((int) ((i / (float) count) * 100));
			*/
			
			// escape early if cancel() is called
            if (isCancelled()) break;                      
		}
		
		return mSuccess;
	}

	// check json for success, display error or update the database 
	@Override
	protected void onProgressUpdate(JSONObject... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		
		// extract json object from array
		JSONObject json = values[0];
		
		Log.d(TAG, "onProgressUpdate: json = " + json.toString());
		
		mCount++;
		
		try {
			// unpack success flag and message
			boolean success = Boolean.valueOf(json.getBoolean(TAG_SUCCESS));
			String  message = String.valueOf(json.getString(TAG_MESSAGE));
			
			if (success == true) {
								
				// unpack Piece data from json response
				JSONObject jPiece = json.getJSONObject(DBAdapter.TABLE_PIECE);
				long rowId = Long.valueOf(jPiece.getLong(DBAdapter.KEY_ROWID));
				long sgId = Long.valueOf(jPiece.getLong(DBAdapter.KEY_SUB_GRP_ID));
				
				Log.d(TAG, "onProgressUpdate: rowId = " + rowId + " ; sgId = " + sgId);
				
				// open the DB
				DBAdapter db = new DBAdapter(mContext);
				db.open();
				
				// extract Piece
				Cursor cPiece = db.getPiece(rowId);
				Piece piece = db.cursorToPiece(cPiece);
							
				// update Piece				
				piece.setSgId(sgId);
				piece.setStatus(CollectStatus.HISTORY);
				
				// save Piece
				db.updatePiece(piece);
			
				// close the DB			
				db.close();				
				
				Log.d(TAG, "onProgressUpdate: success");
				mSuccess++;
				
			} else {
				Toast.makeText(mContext, "ERROR: " + message, Toast.LENGTH_LONG).show();
			}
					
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return;		
	}
	
	// when all the Piece/Measurement data has been exported
	@Override
	protected void onPostExecute(Integer result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		
		if (result == mTotal) {			
			Toast.makeText(mContext, "All " + " Pieces were exported successfully.", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mContext, result + " out of " + mTotal + " pieces were exported successfully.", Toast.LENGTH_LONG).show();	
		}
					
		return;
	}	
	
	// builds JSON piece/measurement data for the given piece Id 
	private JSONObject getJsonResults(Long rowId) {
		JSONObject jResults = null;
				
		try {
			// open the DB
			DBAdapter db = new DBAdapter(mContext);
			db.open();
													
			// extract Piece
			Cursor cPiece = db.getPiece(rowId);
			Log.d(TAG, "cPiece count = " + cPiece.getCount());
			
			if (cPiece.moveToFirst()) {
				// initialize json results		
			    jResults = new JSONObject();
			    
			    // build json for the Piece
			    JSONObject jPiece = new JSONObject();
			    jPiece.put(DBAdapter.KEY_ROWID, cPiece.getLong(cPiece.getColumnIndex(DBAdapter.KEY_ROWID)));
			    jPiece.put(DBAdapter.KEY_PROD_ID, cPiece.getLong(cPiece.getColumnIndex(DBAdapter.KEY_PROD_ID)));
			    jPiece.put(DBAdapter.KEY_SUB_GRP_ID, cPiece.getLong(cPiece.getColumnIndex(DBAdapter.KEY_SUB_GRP_ID)));
			    jPiece.put(DBAdapter.KEY_PIECE_NUM, cPiece.getLong(cPiece.getColumnIndex(DBAdapter.KEY_PIECE_NUM)));
			    jPiece.put(DBAdapter.KEY_COLLECT_DATETIME, cPiece.getString(cPiece.getColumnIndex(DBAdapter.KEY_COLLECT_DATETIME)));
			    jPiece.put(DBAdapter.KEY_OPERATOR, cPiece.getString(cPiece.getColumnIndex(DBAdapter.KEY_OPERATOR)));
			    jPiece.put(DBAdapter.KEY_LOT, cPiece.getString(cPiece.getColumnIndex(DBAdapter.KEY_LOT)));
			    	   
			    // build Measurement json array
			    JSONArray jMeasArr = new JSONArray();
			    
			    // extract Measurements
			    Cursor cMeas = db.getAllMeasurements(rowId);			    
			    Log.d(TAG, "cMeas count = " + cMeas.getCount());			   
				if (cMeas.moveToFirst()) {
					do {				
						// build json for the Measurement
						JSONObject jMeas = new JSONObject();
						jMeas.put(DBAdapter.KEY_FEAT_ID, cMeas.getLong(cMeas.getColumnIndex(DBAdapter.KEY_FEAT_ID)));
						jMeas.put(DBAdapter.KEY_VALUE, cMeas.getDouble(cMeas.getColumnIndex(DBAdapter.KEY_VALUE)));
						jMeas.put(DBAdapter.KEY_LIMIT_REV, cMeas.getLong(cMeas.getColumnIndex(DBAdapter.KEY_LIMIT_REV)));
						jMeas.put(DBAdapter.KEY_IN_CONTROL, DBAdapter.intToBool(cMeas.getInt(cMeas.getColumnIndex(DBAdapter.KEY_IN_CONTROL))));
						jMeas.put(DBAdapter.KEY_IN_ENG_LIM, DBAdapter.intToBool(cMeas.getInt(cMeas.getColumnIndex(DBAdapter.KEY_IN_ENG_LIM))));
						
						// add json Measurement data to json array
						jMeasArr.put(jMeas);
						
					} while(cMeas.moveToNext());
				}		    
	    
				// add json Measurement array to json Piece object
				jPiece.put(DBAdapter.TABLE_MEASUREMENT, jMeasArr);
			
				// build json results
				jResults.put(TAG_SUCCESS, true);
				jResults.put(DBAdapter.TABLE_PIECE, jPiece);
			}
			
			// close the DB			
			db.close();
						
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jResults;
	}
}
