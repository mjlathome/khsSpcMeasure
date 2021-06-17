package com.khs.spcmeasure.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.khs.spcmeasure.Globals;
import com.khs.spcmeasure.library.DeviceUtils;
import com.khs.spcmeasure.library.JSONParser;
import com.khs.spcmeasure.library.NetworkUtils;
import com.khs.spcmeasure.library.SecurityUtils;
import com.khs.spcmeasure.library.VersionUtils;
import com.khs.spcmeasure.receiver.VersionReceiver;

import org.json.JSONObject;

import java.net.URLEncoder;

public class CheckVersionTask extends AsyncTask<Void, Void, JSONObject>{
    private static final String TAG = "CheckVersionTask";

	private Context mContext;
	private OnCheckVersionListener mListener;

	// constants
	private static final String url = "http://thor.magna.global/spc/check_version.php?";
    private static final String querySep = "&";

	//JSON Node Names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_LATEST_CODE = "latestCode";
	private static final String TAG_LATEST_NAME = "latestName";

	// constructor
	public CheckVersionTask(Context context, OnCheckVersionListener listener) {
		mContext = context;
		mListener = listener;
	}

	@Override
	protected void onPreExecute() {
		mListener.onCheckVersionStarted();
	}

	@Override
	protected JSONObject doInBackground(Void... params) {
        Log.d(TAG, "start");

		// delete app apk if it exists on the device
		UpdateApp.deleteFullPathname(UpdateApp.getApkFullPathname());

		JSONObject json = null;

        try {
            // verify wifi connection
            if (NetworkUtils.isWiFi(mContext)) {
                JSONParser jParser = new JSONParser();

                // get JSON from URL
				String chkVerUrl = url + VersionUtils.getUrlQuery(mContext) + querySep + DeviceUtils.getUrlQuery() + querySep + SecurityUtils.getUrlQuery(mContext);
				Log.d(TAG, "url: " + chkVerUrl);
                json = jParser.getJSONFromUrl(chkVerUrl);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

		return json;
	}

	@Override
	protected void onPostExecute(JSONObject json) {
		Log.d(TAG, "json = " + json);

		// initialize
		boolean success = false;
		boolean versionOk = false;
		int latestCode = VersionUtils.VERSION_CODE_UNKNOWN;
		String latestName = VersionUtils.VERSION_NAME_UNKNOWN;

		try {
			// handle null
			if (json != null) {
				// extract success flag
				success = json.getBoolean(TAG_SUCCESS);

				// extract latest version info
				versionOk = json.getBoolean(VersionUtils.TAG_VERSION_OK);

                // handle version failure
                if (!versionOk) {
					// broadcast version failure
					VersionReceiver.sendBroadcast(mContext);
				}

				// upon success, extract the latest version info
                if(success) {
                    latestCode = json.getInt(TAG_LATEST_CODE);
                    latestName = json.getString(TAG_LATEST_NAME);
                }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// update version globals
		Globals g = Globals.getInstance();
		g.setVersionOk(versionOk);
		g.setLatestCode(latestCode);
		g.setLatestName(latestName);

		mListener.onCheckVersionFinished();
	}

	// generate task
	public static CheckVersionTask newInstance(Context context, OnCheckVersionListener listener) {
		CheckVersionTask asyncTask = new CheckVersionTask(context, listener);
		return asyncTask;
	}

	// communication interface
	public interface OnCheckVersionListener {
		public void onCheckVersionStarted();

		// TODO: Update argument type and name
		public void onCheckVersionFinished();
	}
}
