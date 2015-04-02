package com.khs.spcmeasure.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.SetupListActivity;
import com.khs.spcmeasure.entity.SimpleCode;
import com.khs.spcmeasure.library.ActionStatus;
import com.khs.spcmeasure.library.JSONParser;
import com.khs.spcmeasure.library.NotificationId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SimpleCodeService extends IntentService {
    private static final String TAG = "SimpleCodeService";

    // supported actions
    private static final String ACTION_IMPORT = "com.khs.spcmeasure.service.action.IMPORT";

    // supported parameters
    private static final String EXTRA_TYPE = "com.khs.spcmeasure.service.extra.TYPE";

    // Simple Code types supported
    public static final String TYPE_ACTION_CAUSE = "actionCause";

    // url address
    private static String url = "http://thor/spc/get_simple_code.php?type=";

    // JSON node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_SIMPLE_CODE = "simpleCode";

    // JSON tags
    private static final String TAG_ID = "id";
    private static final String TAG_TYPE = "type";
    private static final String TAG_CODE = "code";
    private static final String TAG_DESC = "desc";
    private static final String TAG_INT_CODE = "intCode";
    private static final String TAG_ACTIVE = "active";

    // notification members
    private NotificationManager mNotificationManager;
    private PendingIntent mSetupListIntent;
    private int mNotifyId = 1;

    /**
     * Starts this service to perform action IMPORT with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionImport(Context context, String codeType) {
        Intent intent = new Intent(context, SimpleCodeService.class);
        intent.setAction(ACTION_IMPORT);
        intent.putExtra(EXTRA_TYPE, codeType);
        context.startService(intent);
    }

    // constructor
    public SimpleCodeService() {
        super("SimpleCodeService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // extract notification manager
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // build pending intent for Setup List
        Intent intent = new Intent(SimpleCodeService.this, SetupListActivity.class);
        mSetupListIntent = PendingIntent.getActivity(SimpleCodeService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_IMPORT.equals(action)) {
                final String type = intent.getStringExtra(EXTRA_TYPE);
                handleActionImport(type);
            }
        }
    }

    /**
     * Handle action IMPORT in the provided background thread with the provided
     * parameters.
     */
    private void handleActionImport(String codeType) {

        // assume failure
        ActionStatus actStat = ActionStatus.FAILED;
        String notifyText = codeType;

        try {
            JSONParser jParser = new JSONParser();

            // get JSON from URL
            JSONObject json = jParser.getJSONFromUrl(url + codeType);

            // verify json was successful
            if (json == null || json.getBoolean(TAG_SUCCESS) != true) {
                // notify user - failure
                actStat = ActionStatus.FAILED;
            } else {
                // open the DB
                DBAdapter db = new DBAdapter(this);
                db.open();

                // create SimpleCodes
                JSONArray jSimpleCodeArr = json.getJSONArray(TAG_SIMPLE_CODE);
                for (int i = 0; i < jSimpleCodeArr.length(); i++) {
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

                // notify user - success
                actStat = ActionStatus.COMPLETE;            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // notify user
        updateNotification(ACTION_IMPORT, actStat, notifyText);
    }

    // create service notification
    private Notification getNotification(String title, String text) {
        Notification.Builder nb = new Notification.Builder(this);
        nb.setSmallIcon(R.drawable.ic_launcher);
        nb.setContentTitle(title);
        nb.setContentText(text);
        nb.setContentIntent(mSetupListIntent);
        nb.setAutoCancel(true);
        return nb.build();
    }

    // update service notification - uses text string provided
    private void updateNotification(String action, ActionStatus actStat, String text) {
        String title = this.getString(R.string.text_unknown);
        if (action.equals(ACTION_IMPORT)) {
            title = this.getString(R.string.text_simple_code_import, actStat);
        }

        mNotificationManager.notify(NotificationId.getId(), getNotification(title, text));
        return;
    }

    // remove service notification
    private void removeNotification() {
        mNotificationManager.cancel(mNotifyId);
        return;
    }
}
