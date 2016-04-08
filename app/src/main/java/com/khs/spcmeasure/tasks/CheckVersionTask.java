package com.khs.spcmeasure.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.khs.spcmeasure.Globals;
import com.khs.spcmeasure.library.JSONParser;
import com.khs.spcmeasure.library.VersionUtils;

import org.json.JSONObject;

public class CheckVersionTask extends AsyncTask<Void, Void, JSONObject>{
    private static final String TAG = "CheckVersionTask";

	private OnCheckVersionListener mListener;

	// constants
	private static final String url = "http://thor.kmx.cosma.com/spc/check_version.php?";

	//JSON Node Names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_LATEST_CODE = "latestCode";
	private static final String TAG_LATEST_NAME = "latestName";

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
		JSONObject json = jParser.getJSONFromUrl(url + VersionUtils.getUrlQuery((Context) mListener));
		
		return json;
	}

	@Override
	protected void onPostExecute(JSONObject json) {
		super.onPostExecute(json);

		Log.d(TAG, "json = " + json.toString());

		// initialize
		boolean success = false;
		boolean versionOk = false;
		int latestCode = VersionUtils.VERSION_CODE_UNKNOWN;
		String latestName = VersionUtils.VERSION_NAME_UNKNOWN;

		try {
			// handle null
			if (json != null) {
				// extract latest version info
				versionOk = json.getBoolean(VersionUtils.TAG_VERSION_OK);
				latestCode = json.getInt(TAG_LATEST_CODE);
				latestName = json.getString(TAG_LATEST_NAME);
				success = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// update version globals
		Globals g = Globals.getInstance();
		g.setVersionOk(versionOk);
		g.setLatestCode(latestCode);
		g.setLatestName(latestName);

		mListener.onCheckVersionPostExecute(versionOk);
	}

	// generate task
	public static CheckVersionTask newInstance(OnCheckVersionListener listener) {
		CheckVersionTask asyncTask = new CheckVersionTask(listener);
		return asyncTask;
	}

	// communication interface
	public interface OnCheckVersionListener {
		// TODO: Update argument type and name
		public void onCheckVersionPostExecute(boolean versionOk);
	}
}
