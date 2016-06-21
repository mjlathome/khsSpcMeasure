package com.khs.spcmeasure.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.khs.spcmeasure.library.NetworkUtils;

public class WaitWiFiTask extends AsyncTask<Void, Void, Boolean>{
    private static final String TAG = "WaitWiFiTask";

	private Context mContext;
	private OnWaitWiFiListener mListener;

	// Wait WiFi constants
	private static final int WAIT_WIFI_TRIES = 15;
	private static final int WAIT_WIFI_SLEEP = 2000;

	// constructor
	public WaitWiFiTask(Context context, OnWaitWiFiListener listener) {
		mContext = context;
		mListener = listener;
	}

	@Override
	protected void onPreExecute() {
		mListener.onWaitWiFiStarted();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
        Log.d(TAG, "start");

		Boolean isWiFiConn = false;
		int count = 0;

		// see:
		// http://stackoverflow.com/questions/8678362/wait-until-wifi-connected-on-android
		// http://stackoverflow.com/questions/6122812/using-wait-in-asynctask
		try {
			// check if wifi is connected
			while (!NetworkUtils.isWiFi(mContext) && count < WAIT_WIFI_TRIES) {
                count++;
				// wait to connect
				Thread.sleep(WAIT_WIFI_SLEEP);
			}

			isWiFiConn = NetworkUtils.isWiFi(mContext);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isWiFiConn;
	}

	@Override
	protected void onPostExecute(Boolean isWiFiConn) {
		Log.d(TAG, "wifi = " + isWiFiConn);

		mListener.onWaitWiFiFinished(isWiFiConn);
	}

	// generate task
	public static WaitWiFiTask newInstance(Context context, OnWaitWiFiListener listener) {
		WaitWiFiTask asyncTask = new WaitWiFiTask(context, listener);
		return asyncTask;
	}

	// communication interface
	public interface OnWaitWiFiListener {
		public void onWaitWiFiStarted();

		// TODO: Update argument type and name
		public void onWaitWiFiFinished(boolean isWiFiConn);
	}
}
