package com.khs.spcmeasure.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.khs.spcmeasure.Globals;
import com.khs.spcmeasure.helper.DBAdapter;
import com.khs.spcmeasure.library.NetworkUtils;
import com.khs.spcmeasure.library.VersionUtils;
import com.khs.spcmeasure.receiver.VersionReceiver;
import com.khs.spcmeasure.ui.PieceListActivity;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.library.ActionStatus;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.library.JSONParser;
import com.khs.spcmeasure.library.NotificationId;
import com.khs.spcmeasure.ui.SettingsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class HistoryService extends IntentService {
    private static final String TAG = "HistoryService";

    // supported actions
    public static final String ACTION_MEAS_HIST = "com.khs.spcmeasure.service.action.MEAS_HIST";
    public static final String ACTION_DELETE = "com.khs.spcmeasure.service.action.DELETE";

    // supported parameters
    public static final String EXTRA_PROD_ID = "com.khs.spcmeasure.service.extra.PROD_ID";
    public static final String EXTRA_MAX_SG = "com.khs.spcmeasure.service.extra.MAX_SG";
    public static final String EXTRA_STATUS = "com.khs.spcmeasure.service.extra.STATUS";

    // cancelled Product ids.  see:
    // http://stackoverflow.com/questions/7318666/android-intentservice-how-abort-or-skip-a-task-in-the-handleintent-queue
    private static List<Long> canceledMeasHistProdId = new ArrayList<Long>();

    // url address
    private static String url = "http://thor.magna.global/spc/get_meas_hist.php?";
    private static String querySep = "&";
    private static String queryProdId = "prodId=";
    private static String queryMaxSg = "maxSg=";

    // JSON node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_SUB_GROUP = "sg";
    private static final String TAG_SETUP = "setup";
    private static final String TAG_PRODUCT = "product";
    private static final String TAG_FEATURE = "feature";
    private static final String TAG_LIMIT = "limit";

    // JSON tags
    private static final String TAG_PROD_ID = "prodId";
    private static final String TAG_SG_ID = "sgId";
    private static final String TAG_COLLECT_DT = "collectDt";
    private static final String TAG_OPERATOR = "operator";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_ACTIVE = "active";
    private static final String TAG_CUSTOMER = "customer";
    private static final String TAG_PROGRAM = "program";
    private static final String TAG_CP = "cp";
    private static final String TAG_CPK = "cpk";
    private static final String TAG_LIMIT_REV = "limitRev";
    private static final String TAG_LIMIT_TYPE = "type";
    private static final String TAG_UPPER = "upper";
    private static final String TAG_LOWER = "lower";

    // notification members
    private NotificationManager mNotificationManager;
    private PendingIntent mPieceListIntent;
    private int mNotifyId = 1;

    public HistoryService() {
        super("HistoryService");
    }

    /**
     * Starts this service to perform action for measurement history with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionMeasHist(Context context, Long prodId) {
        Intent intent = new Intent(context, HistoryService.class);
        intent.setAction(ACTION_MEAS_HIST);
        intent.putExtra(EXTRA_PROD_ID, prodId);
        // for app constants info see:
        // http://stackoverflow.com/questions/9761386/android-best-way-to-provide-app-specific-constants-in-a-library-project
        intent.putExtra(EXTRA_MAX_SG, context.getResources().getInteger(R.integer.maxSg));
        context.startService(intent);
    }

    /**
     * Starts this service to perform action for history delete with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDelete(Context context, Long prodId) {
        Intent intent = new Intent(context, HistoryService.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(EXTRA_PROD_ID, prodId);
        intent.putExtra(EXTRA_MAX_SG, context.getResources().getInteger(R.integer.maxSg));
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        // extract notification manager
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // build pending intent for Setup List
        // TODO use this if import is unsuccessful?

        Intent intent = new Intent(HistoryService.this, PieceListActivity.class);
        mPieceListIntent = PendingIntent.getActivity(HistoryService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MEAS_HIST.equals(action)) {
                final Long prodId = intent.getLongExtra(EXTRA_PROD_ID, -1);
                final Integer maxSg = intent.getIntExtra(EXTRA_MAX_SG, getResources().getInteger(R.integer.maxSg));

                if(canceledMeasHistProdId.contains(prodId)) {
                    // remove cancellation to ensure it's re-tried in the future
                    canceledMeasHistProdId.remove(prodId);
                } else {
                    // import measurement history
                    handleActionMeasHist(prodId, maxSg);
                }
            } else if (ACTION_DELETE.equals(action)) {
                final Long prodId = intent.getLongExtra(EXTRA_PROD_ID, -1);
                final Integer maxSg = intent.getIntExtra(EXTRA_MAX_SG, getResources().getInteger(R.integer.maxSg));

                // delete measurement history
                handleActionDelete(prodId, maxSg);
            }
        }
    }

    /**
     * Handle action measurement history in the provided background thread with the provided
     * parameters.
     */
    private void handleActionMeasHist(Long prodId, Integer maxSg) {

        Log.d(TAG, "handleActionMeasHist");

        // assume failure
        ActionStatus actStat = ActionStatus.FAILED;

        int numFound = 0;
        int numImport = 0;

        // get global vars
        Globals g = Globals.getInstance();

        // check version and wifi.  skip if in error
        if (g.isVersionOk() && NetworkUtils.isWiFi(this)) {
            try {
                JSONParser jParser = new JSONParser();

                // get JSON from URL
                JSONObject json = jParser.getJSONFromUrl(url + VersionUtils.getUrlQuery(this) + querySep + queryProdId + String.valueOf(prodId) + querySep + queryMaxSg + String.valueOf(maxSg));

                Log.d(TAG, "json - " + json);

                // verify json was returned
                if (json == null) {
                    // notify user - failure
                    actStat = ActionStatus.FAILED;
                } else {
                    // extract success, version and product id
                    boolean success = json.getBoolean(TAG_SUCCESS);
                    boolean versionOk = json.getBoolean(VersionUtils.TAG_VERSION_OK);
                    long resProdId = json.getLong(TAG_PROD_ID);

                    // update version global
                    g.setVersionOk(versionOk);

                    // verify success and version
                    if (!success || !versionOk || resProdId != prodId.longValue()) {
                        // notify user - failure
                        actStat = ActionStatus.FAILED;

                        // handle version failure
                        if (!versionOk) {
                            Log.d(TAG, "version not ok");
                            // broadcast version failure
                            VersionReceiver.sendBroadcast(HistoryService.this);
                        }
                    } else {
                        // open the DB
                        DBAdapter db = new DBAdapter(this);
                        db.open();

                        // get JSON sub-group array
                        JSONArray jSgArr = json.getJSONArray(TAG_SUB_GROUP);
                        numFound = jSgArr.length();

                        // loop sub-groups
                        for (int i = 0; i < jSgArr.length(); i++) {
                            JSONObject jSubGrp = jSgArr.getJSONObject(i);

                            long sgId = Long.valueOf(jSubGrp.getString(TAG_SG_ID));
                            String collDt = jSubGrp.getString(TAG_COLLECT_DT);
                            String operator = jSubGrp.getString(TAG_OPERATOR);

                            // extract Piece
                            Cursor cPiece = db.getPiece(prodId, sgId, 1);
                            // Log.d(TAG, "sgId = " + sgId + "; collDt = " + collDt + "; operator = " + operator + "; cPiece = " + db.isCursorEmpty(cPiece));

                            // import Piece if not found on device
                            // TODO need to check last modified date in the future
                            if (db.isCursorEmpty(cPiece)) {
                                numImport++;
                                MeasurementService.startActionImport(this, prodId, sgId, collDt);
                            }
                        }

                        // close the DB
                        db.close();

                        // notify user - success
                        actStat = ActionStatus.COMPLETE;

                        // TODO want to do history delete now
                        HistoryService.startActionDelete(this, prodId);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();

                // cancel import to stop re-try
                cancelMeasHist(prodId);

                // notify user - failure
                actStat = ActionStatus.FAILED;
            }
        }

        // notify user
        String notifyText = this.getString(R.string.text_meas_hist_text, numImport, numFound);
        updateNotification(ACTION_MEAS_HIST, actStat, notifyText, prodId);
    }

    /**
     * Handle action history delete in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDelete(Long prodId, int maxSg) {
        Log.d(TAG, "handleActionDelete");

        DBAdapter db = new DBAdapter(this);

        // assume failure
        ActionStatus actStat = ActionStatus.FAILED;

        // initialize counts
        int numFound = 0;
        int numDelete = 0;

        try {
            // open the DB
            db.open();

            // get cursor to all history pieces for product on device
            Cursor cPiece = db.getAllPieces(prodId, CollectStatus.HISTORY);

            // TODO need to check last modified date in the future
            if (!db.isCursorEmpty(cPiece)) {
                numFound = cPiece.getCount();

                if (numFound > maxSg) {
                    // start transaction
                    db.beginTransaction();

                    // iterate the results
                    if (cPiece.moveToFirst()) {
                        int numCount = 0;
                        do {
                            numCount++;
                            // check if max sub-group limit met
                            if (numCount > maxSg) {
                                numDelete++;
                                // delete the Piece
                                db.deletePiece(cPiece.getInt(cPiece.getColumnIndex(db.KEY_ROWID)));
                            }
                        } while(cPiece.moveToNext());
                    }

                    if (db.inTransaction()) {
                        // set transaction successful
                        db.setTransactionSuccessful();
                    }
                }
            }

            // notify user - success
            actStat = ActionStatus.COMPLETE;
        } catch (Exception e) {
            e.printStackTrace();

            // notify user - failure
            actStat = ActionStatus.FAILED;
        } finally {
            if (db.inTransaction()) {
                // commit
                db.endTransaction();
            }

            // close the DB
            db.close();
        }

        // notify user
        String notifyText = this.getString(R.string.text_history_delete_text, numDelete, numFound);
        updateNotification(ACTION_DELETE, actStat, notifyText, prodId);
    }

    // allows measurement history cancel for provided Product Id
    public static void cancelMeasHist(Long prodId) {
        canceledMeasHistProdId.add(prodId);
    }

    // create service notification
    private Notification getNotification(String title, String text, Long prodId) {
        // build pending intent for Piece List
        Intent intent = new Intent(HistoryService.this, PieceListActivity.class);
        intent.putExtra(DBAdapter.KEY_PROD_ID, prodId);
        PendingIntent notifyIntent = PendingIntent.getActivity(HistoryService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder nb = new Notification.Builder(this);
        nb.setSmallIcon(R.drawable.ic_launcher);
        nb.setContentTitle(title);
        nb.setContentText(text);
        nb.setContentIntent(notifyIntent);
        nb.setAutoCancel(true);
        return nb.build();
    }

    // update service notification - uses text string provided
    private void updateNotification(String action, ActionStatus actStat, String text, Long prodId) {
        // extract shared preferences and exit if show notifications is not required
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sharedPref.getBoolean(SettingsActivity.KEY_PREF_SHOW_NOTIFICATIONS, false)) {return;}

        String title = this.getString(R.string.text_unknown);
        if (action.equals(ACTION_MEAS_HIST)) {
            title = this.getString(R.string.text_meas_hist_title, actStat);
        } else if (action.equals(ACTION_DELETE)) {
            title = this.getString(R.string.text_history_delete_title, actStat);
        }

        mNotificationManager.notify(NotificationId.getId(), getNotification(title, text, prodId));
        return;
    }
}
