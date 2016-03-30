package com.khs.spcmeasure.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.khs.spcmeasure.helper.DBAdapter;
import com.khs.spcmeasure.ui.PieceListActivity;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.ui.SettingsActivity;
import com.khs.spcmeasure.ui.SetupListActivity;
import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Limits;
import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.library.ActionStatus;
import com.khs.spcmeasure.library.JSONParser;
import com.khs.spcmeasure.library.LimitType;
import com.khs.spcmeasure.library.NotificationId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * used to import the setup for a Product as json into the Android Sqlite db.
 */
public class SetupService extends IntentService {
    private static final String TAG = "SetupService";

    // supported actions
    public static final String ACTION_IMPORT = "com.khs.spcmeasure.service.action.IMPORT";

    // supported parameters
    public static final String EXTRA_PROD_ID = "com.khs.spcmeasure.service.extra.PROD_ID";
    public static final String EXTRA_STATUS = "com.khs.spcmeasure.service.extra.STATUS";

    // cancelled Product ids.  see:
    // http://stackoverflow.com/questions/7318666/android-intentservice-how-abort-or-skip-a-task-in-the-handleintent-queue
    private static List<Long> canceledImportProdId = new ArrayList<Long>();

    // url address
    private static String url = "http://thor.kmx.cosma.com/spc/get_setup.php?prodId=";

    // JSON node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_SETUP = "setup";
    private static final String TAG_PRODUCT = "product";
    private static final String TAG_FEATURE = "feature";
    private static final String TAG_LIMIT = "limit";

    // JSON tags
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
    private PendingIntent mSetupListIntent;
    private int mNotifyId = 1;

    /**
     * Starts this service to perform action IMPORT with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionImport(Context context, Long prodId) {
        Intent intent = new Intent(context, SetupService.class);
        intent.setAction(ACTION_IMPORT);
        intent.putExtra(EXTRA_PROD_ID, prodId);
        context.startService(intent);
    }

    // constructor
    public SetupService() {
        super("SetupService");

        // don't redeliver Intents if process dies
        // setIntentRedelivery(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // extract notification manager
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // build pending intent for Setup List
        // TODO use this if import is unsuccessful?
        Intent intent = new Intent(SetupService.this, SetupListActivity.class);
        mSetupListIntent = PendingIntent.getActivity(SetupService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_IMPORT.equals(action)) {
                final Long prodId = intent.getLongExtra(EXTRA_PROD_ID, -1);

                if(canceledImportProdId.contains(prodId)) {
                    // remove cancellation to ensure it's re-tried in the future
                    canceledImportProdId.remove(prodId);
                } else {
                    // import Product
                    handleActionImport(prodId);
                }
            }
        }
    }

    /**
     * Handle action IMPORT in the provided background thread with the provided
     * parameters.
     */
    private void handleActionImport(Long setupId) {
        // notify user - starting
        broadcastUpdate(ACTION_IMPORT, setupId, ActionStatus.WORKING);

        // assume failure
        ActionStatus actStat = ActionStatus.FAILED;
        String notifyText = setupId.toString();

        try {
            JSONParser jParser = new JSONParser();

            // get JSON from URL
            JSONObject json = jParser.getJSONFromUrl(url + String.valueOf(setupId));

            // verify json was successful
            if (json == null || json.getBoolean(TAG_SUCCESS) != true) {
                // notify user - failure
                actStat = ActionStatus.FAILED;
            } else {
                // get JSON Array from URL
                JSONObject jSetup = json.getJSONObject(TAG_SETUP);
                JSONObject jProduct = jSetup.getJSONObject(TAG_PRODUCT);

                // open the DB
                DBAdapter db = new DBAdapter(this);
                db.open();

                // TODO write helper for json to array of products???
                // store JSON items as variables

                // extract Product fields from json data
                long prodId = Long.valueOf(jProduct.getString(TAG_ID));
                String name = jProduct.getString(TAG_NAME);
                notifyText = name;

                boolean active = Boolean.valueOf(jProduct.getString(TAG_ACTIVE));
                String customer = jProduct.getString(TAG_CUSTOMER);
                String program = jProduct.getString(TAG_PROGRAM);

                // Log.d(TAG, "id = " + Long.toString(prodId));

                // create Product
                Product product = new Product(prodId, name, active, customer, program);

                // update or insert Product into the DB
                if (db.updateProduct(product) == false) {
                    db.createProduct(product);
                }

                // create Features
                JSONArray jFeatureArr = jProduct.getJSONArray(TAG_FEATURE);
                for(int i = 0; i < jFeatureArr.length(); i++) {

                    JSONObject jFeature = jFeatureArr.getJSONObject(i);

                    // extract Feature fields from json data
                    long featId = Long.valueOf(jFeature.getString(TAG_ID));
                    name = jFeature.getString(TAG_NAME);
                    active = Boolean.valueOf(jFeature.getString(TAG_ACTIVE));
                    Double cp 	 = jFeature.getDouble(TAG_CP);
                    Double cpk     = jFeature.getDouble(TAG_CPK);
                    long limitRev = Long.valueOf(jFeature.getString(TAG_LIMIT_REV));

                    // create the Feature object
                    Feature feature = new Feature(product.getId(), featId, name, active, limitRev, cp, cpk);
                    // Log.d(TAG, "feat id;name = " + Long.toString(featId) + "; " + name);

                    // update or insert Feature into the DB
                    if (db.updateFeature(feature) == false) {
                        db.createFeature(feature);
                    }

                    // create Limits
                    JSONArray jLimitArr = jFeature.getJSONArray(TAG_LIMIT);
                    for(int j = 0; j < jLimitArr.length(); j++) {

                        JSONObject jLimit = jLimitArr.getJSONObject(j);

                        // extract Limit fields from json data
                        String limitType = jLimit.getString(TAG_LIMIT_TYPE);
                        double upper 	 = jLimit.getDouble(TAG_UPPER);
                        double lower     = jLimit.getDouble(TAG_LOWER);

                        // Log.d(TAG, "DEBUG limit type; upper; lower; LimitType = " +
                        //        limitType + "; " + upper + "; " + lower + "; " + LimitType.fromValue(limitType).getValue() );

                        // create the Limit object
                        Limits limit = new Limits(product.getId(), feature.getFeatId(), limitRev, LimitType.fromValue(limitType), upper, lower);
                        // Log.d(TAG, "DEBUG limit type; upper; lower = " + limitType + "; " + upper + "; " + lower);

                        // update or insert Limit into the DB
                        if (db.updateLimit(limit) == false) {
                            db.createLimit(limit);
                        }

                    }  // create Limits

                }  // create Features

                // close the DB
                db.close();

                // notify user - success
                actStat = ActionStatus.COMPLETE;

                // TODO want to do history import now
                HistoryService.startActionMeasHist(this, prodId);
            }

        } catch (JSONException e) {
            e.printStackTrace();

            // cancel import to stop re-try
            cancelImport(setupId);

            // notify user - failure
            actStat = ActionStatus.FAILED;
        }

        // notify user
        broadcastUpdate(ACTION_IMPORT, setupId, actStat);
        updateNotification(ACTION_IMPORT, actStat, notifyText, setupId);
    }

    // allows import cancel for provided Product Id
    public static void cancelImport(Long prodId) {
        canceledImportProdId.add(prodId);
    }

    // create service notification
    private Notification getNotification(String title, String text, Long prodId) {
        // build pending intent for Feature Review
        Intent intent = new Intent(SetupService.this, PieceListActivity.class);
        intent.putExtra(DBAdapter.KEY_PROD_ID, prodId);
        PendingIntent notifyIntent = PendingIntent.getActivity(SetupService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
        if (action.equals(ACTION_IMPORT)) {
            title = this.getString(R.string.text_setup_import, actStat);
        }

        mNotificationManager.notify(NotificationId.getId(), getNotification(title, text, prodId));
        return;
    }

    // remove service notification
    private void removeNotification() {
        mNotificationManager.cancel(mNotifyId);
        return;
    }

    // broadcast action
    private void broadcastUpdate(final String action, final long prodId, final ActionStatus actStat) {
        // Log.d(TAG, "broadcastUpdate: action = " + action + "; prodId = " + prodId + "; actStat = " + actStat);

        Intent intent = new Intent(action);
        // intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(EXTRA_PROD_ID, prodId);
        intent.putExtra(EXTRA_STATUS, actStat);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
