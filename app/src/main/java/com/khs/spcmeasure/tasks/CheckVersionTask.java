package com.khs.spcmeasure.tasks;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.khs.spcmeasure.R;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

public class CheckVersionTask extends AsyncTask<Void, Void, JSONObject>{
    private static final String TAG = "CheckVersionTask";

	private Context mContext;

	private static String url = "http://thor.kmx.cosma.com/spc/get_version.php";

	//JSON Node Names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_CODE = "code";
	private static final String TAG_NAME = "name";

	JSONArray android = null;

	// constructor
	public CheckVersionTask(Context context) {
		mContext = context;
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
		
		try {
			int latestCode = json.getInt(TAG_CODE);
			String latestName = json.getString(TAG_NAME);

			String versionName = null;
			int versionCode = -1;

			// extract version
			PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			versionName = pInfo.versionName;
			versionCode = pInfo.versionCode;

			if (versionCode != latestCode  || !versionName.equals(latestName)) {
				// build confirmation message
				String message = mContext.getString(R.string.text_version_contact) + "\n";
				message += mContext.getString(R.string.text_version_install, versionName, versionCode) + "\n";
				message += mContext.getString(R.string.text_version_latest, latestName, latestCode);

				// display version dialog
				AlertUtils.alertDialogShow(mContext, mContext.getString(R.string.text_version_title), message);
			}

			// Toast.makeText(mContext, "version name = " + versionName, Toast.LENGTH_LONG).show();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
}
