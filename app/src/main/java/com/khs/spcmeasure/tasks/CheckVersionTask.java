package com.khs.spcmeasure.tasks;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.khs.spcmeasure.R;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.JSONParser;
import com.khs.spcmeasure.library.SecurityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class CheckVersionTask extends AsyncTask<Void, Void, JSONObject>{
    private static final String TAG = "CheckVersionTask";

	private Context mContext;

	// constants
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

		// handle null
		if (json == null) {
			SecurityUtils.doLogout(mContext);
			return;
		}

		try {
			// extract latest version info
			int latestCode = json.getInt(TAG_CODE);
			String latestName = json.getString(TAG_NAME);

			// extract installed version  info
			PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			String installName = pInfo.versionName;
			int installCode = pInfo.versionCode;

			// DEBUG remove later
			installName = "0.1.3";
			installCode = 0;

			if (installCode == latestCode) {
				// nothing as installed version is the latest
			} else {
				// extract major, minor and patch version info
				String[] installRev = installName.split("\\.");
				String[] latestRev = latestName.split("\\.");

				Log.d(TAG, "installRev = " + Arrays.toString(installRev));
				Log.d(TAG, "latestRev = " + Arrays.toString(latestRev));

				// build confirmation message
				StringBuffer message = new StringBuffer(mContext.getString(R.string.text_version_contact) + "\n");
				message.append(mContext.getString(R.string.text_version_install, installName, installCode) + "\n");
				message.append(mContext.getString(R.string.text_version_latest, latestName, latestCode));

				// check for major revision change
				if (installRev == null || latestRev == null || !installRev[0].equals(latestRev[0]) ) {
					// force logout as too old
					SecurityUtils.setIsLoggedIn(mContext, false);
					message.insert(0, mContext.getString(R.string.text_version_logout_too_old) + "\n");
				}

				Log.d(TAG, "message = " + message.toString());

				// display version dialog
				// TODO need to send a broadcast so that version failure an be checked upon
				// AlertUtils.alertDialogShow(mContext, mContext.getString(R.string.text_version_title), message.toString());
				createAlert
			}

			// Toast.makeText(mContext, "version name = " + versionName, Toast.LENGTH_LONG).show();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
}
