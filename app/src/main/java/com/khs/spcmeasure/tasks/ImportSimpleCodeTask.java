package com.khs.spcmeasure.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.khs.spcmeasure.helper.DBAdapter;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.entity.SimpleCode;
import com.khs.spcmeasure.library.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImportSimpleCodeTask extends AsyncTask<String, String, JSONObject>{
    private static final String TAG = "ImportSetupTask";

	private Context mContext;

    private static String url = "http://thor.kmx.cosma.com/spc/get_simple_code.php?type=";

	//JSON Node Names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_SIMPLE_CODE = "simpleCode";

	private static final String TAG_ID = "id";
	private static final String TAG_TYPE = "type";
    private static final String TAG_CODE = "code";
    private static final String TAG_DESC = "desc";
    private static final String TAG_INT_CODE = "intCode";
	private static final String TAG_ACTIVE = "active";

	// constructor
	public ImportSimpleCodeTask(Context context) {
		mContext = context;
	}

    public String mType = null;

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		// String type = params[0];
        mType = params[0];

        Log.d(TAG, "ImportSimpleCode = " + mType);

		JSONParser jParser = new JSONParser();
		
		// get JSON from URL
		JSONObject json = jParser.getJSONFromUrl(url + mType);
		
		return json;
	}

	@Override
	protected void onPostExecute(JSONObject json) {
		// TODO Auto-generated method stub
		super.onPostExecute(json);
		
		Log.d(TAG, "onPostExecute: json = " + json.toString());
		
		try {

            // TODO check for success within all json operations
			// get JSON Array from URL
			// JSONObject jSuccess = json.getJSONObject(TAG_SUCCESS);

			// open the DB
			DBAdapter db = new DBAdapter(mContext);
			db.open();

			// create SimpleCodes
            JSONArray jSimpleCodeArr = json.getJSONArray(TAG_SIMPLE_CODE);
			for(int i = 0; i < jSimpleCodeArr.length(); i++) {
				
				JSONObject jSimpleCode = jSimpleCodeArr.getJSONObject(i);
				
				// extract Simple Code fields from json data
				long id = Long.valueOf(jSimpleCode.getString(TAG_ID));
				String type = jSimpleCode.getString(TAG_TYPE);
                String code = jSimpleCode.getString(TAG_CODE);
                String desc = jSimpleCode.getString(TAG_DESC);
                String intCode = jSimpleCode.getString(TAG_INT_CODE);
				boolean active = Boolean.valueOf(jSimpleCode.getString(TAG_ACTIVE));

				// create the SimpleCode object
				SimpleCode simpleCode = new SimpleCode(id, type, code, desc, intCode, active);
				Log.d(TAG, "onPostExecute id = " + id);

				// update or insert SimpleCode into the DB
				if (db.updateSimpleCode(simpleCode) == false) {
					db.createSimpleCode(simpleCode);
				}

			}  // create SimpleCodes
			
			// close the DB			
			db.close();
			
			Toast.makeText(mContext, mContext.getString(R.string.text_simple_code_import, mType), Toast.LENGTH_LONG).show();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}	
}
