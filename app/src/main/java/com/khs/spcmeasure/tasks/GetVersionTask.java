package com.khs.spcmeasure.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.khs.spcmeasure.library.JSONParser;
import com.khs.spcmeasure.library.VersionUtils;

import org.json.JSONObject;

public class GetVersionTask extends AsyncTask<Void, Void, JSONObject>{
    private static final String TAG = "GetVersionTask";

	private OnGetVersionListener mListener;

	// constants
	private static final String url = "http://thor.kmx.cosma.com/spc/get_version.php";

	//JSON Node Names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_CODE = "code";
	private static final String TAG_NAME = "name";

	// constructor
	public GetVersionTask(OnGetVersionListener listener) {
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

		// initialize
		boolean success = false;
		int latestCode = VersionUtils.VERSION_CODE_UNKNOWN;
		String latestName = VersionUtils.VERSION_NAME_UNKNOWN;

		try {
			// handle null
			if (json != null) {
				// extract latest version info
				latestCode = json.getInt(TAG_CODE);
				latestName = json.getString(TAG_NAME);
				success = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		mListener.onGetVersionPostExecute(success, latestCode, latestName);
	}

	// generate task
	public static GetVersionTask newInstance(OnGetVersionListener listener) {
		GetVersionTask asyncTask = new GetVersionTask(listener);
		return asyncTask;
	}

	// communication interface
	public interface OnGetVersionListener {
		// TODO: Update argument type and name
		public void onGetVersionPostExecute(boolean success, int latestCode, String latestName);
	}
}
