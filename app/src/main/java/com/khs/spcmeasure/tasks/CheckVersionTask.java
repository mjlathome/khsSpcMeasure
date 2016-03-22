package com.khs.spcmeasure.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.khs.spcmeasure.library.JSONParser;

import org.json.JSONObject;

public class CheckVersionTask extends AsyncTask<Void, Void, JSONObject>{
    private static final String TAG = "CheckVersionTask";

	private OnCheckVersionListener mListener;

	// constants
	private static String url = "http://thor.kmx.cosma.com/spc/get_version.php";

	//JSON Node Names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_CODE = "code";
	private static final String TAG_NAME = "name";

	// constructor
	public CheckVersionTask(OnCheckVersionListener listener) {
		mListener = listener;
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

	@Override
	protected JSONObject doInBackground(Void... params) {
        Log.d(TAG, "start");

		JSONParser jParser = new JSONParser();
		
		// get JSON from URL
		JSONObject json = jParser.getJSONFromUrl(url);
		
		return json;
	}

	@Override
	protected void onPostExecute(JSONObject json) {
		super.onPostExecute(json);

		Log.d(TAG, "json = " + json.toString());

		boolean versionOk = false;
		StringBuffer message = new StringBuffer("");

		try {
			// handle null
			if (json == null) {
				mListener.onCheckVersionPostExecute(-1, "");
			} else {
				// extract latest version info
				int latestCode = json.getInt(TAG_CODE);
				String latestName = json.getString(TAG_NAME);
				mListener.onCheckVersionPostExecute(latestCode, latestName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// generate task
	public static CheckVersionTask newInstance(OnCheckVersionListener listener) {
		CheckVersionTask asyncTask = new CheckVersionTask(listener);
		return asyncTask;
	}

	// communication interface
	public interface OnCheckVersionListener {
		// TODO: Update argument type and name
		public void onCheckVersionPostExecute(int latestCode, String latestName);
	}
}
