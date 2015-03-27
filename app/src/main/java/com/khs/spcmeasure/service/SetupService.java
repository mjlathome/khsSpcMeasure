package com.khs.spcmeasure.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.SetupListActivity;
import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Limits;
import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.entity.SimpleCode;
import com.khs.spcmeasure.library.JSONParser;
import com.khs.spcmeasure.library.LimitType;
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
public class SetupService extends IntentService {
    private static final String TAG = "SetupService";

    // supported actions
    private static final String ACTION_IMPORT = "com.khs.spcmeasure.service.action.IMPORT";

    // supported parameters
    private static final String EXTRA_PROD_ID = "com.khs.spcmeasure.service.extra.PROD_ID";

    // url address
    private static String url = "http://thor/spc/get_setup.php?prodId=";

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
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // extract notification manager
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // build pending intent for Setup List
        Intent intent = new Intent(SetupService.this, SetupListActivity.class);
        mSetupListIntent = PendingIntent.getActivity(SetupService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_IMPORT.equals(action)) {
                final Long prodId = intent.getLongExtra(EXTRA_PROD_ID, -1);
                handleActionImport(prodId);
            }
        }
    }

    /**
     * Handle action IMPORT in the provided background thread with the provided
     * parameters.
     */
    private void handleActionImport(Long setupId) {

        try {
            JSONParser jParser = new JSONParser();

            // get JSON from URL
            JSONObject json = jParser.getJSONFromUrl(url + String.valueOf(setupId));

            // verify json was successful
            if (json == null || json.getBoolean(TAG_SUCCESS) != true) {
                String alertText = this.getString(R.string.text_setup_imp_fail, setupId);
                updateNotification(alertText);
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

                boolean active = Boolean.valueOf(jProduct.getString(TAG_ACTIVE));
                String customer = jProduct.getString(TAG_CUSTOMER);
                String program = jProduct.getString(TAG_PROGRAM);

                Log.d(TAG, "id = " + Long.toString(prodId));

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
                    long limitRev = Long.valueOf(jFeature.getString(TAG_LIMIT_REV));

                    // FUTURE:
                    // double cp 	 = jFeature.getDouble(TAG_CP);
                    // double cpk     = jFeature.getDouble(TAG_CPK);
                    // TODO remove later
                    double cp = 0.1;
                    double cpk = 0.2;

                    // create the Feature object
                    Feature feature = new Feature(product.getId(), featId, name, active, limitRev, cp, cpk);
                    Log.d(TAG, "feat id;name = " + Long.toString(featId) + "; " + name);

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

                        Log.d(TAG, "DEBUG limit type; upper; lower; LimitType = " +
                                limitType + "; " + upper + "; " + lower + "; " + LimitType.fromValue(limitType).getValue() );

                        // create the Limit object
                        Limits limit = new Limits(product.getId(), feature.getFeatId(), limitRev, LimitType.fromValue(limitType), upper, lower);
                        Log.d(TAG, "DEBUG limit type; upper; lower = " + limitType + "; " + upper + "; " + lower);

                        // update or insert Limit into the DB
                        if (db.updateLimit(limit) == false) {
                            db.createLimit(limit);
                        }

                    }  // create Limits

                }  // create Features

                // close the DB
                db.close();

                // notify user
                String alertText = this.getString(R.string.text_setup_imp_comp, product.getName());
                updateNotification(alertText);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // create service notification
    private Notification getNotification(String text) {
        Notification.Builder nb = new Notification.Builder(this);
        nb.setSmallIcon(R.drawable.ic_launcher);
        nb.setContentTitle("Spc Measure - Setup Service");      // TODO string
        nb.setContentText(text);
        nb.setContentIntent(mSetupListIntent);
        nb.setAutoCancel(true);
        return nb.build();
    }

    // update service notification - uses text string provided
    private void updateNotification(String text) {
        mNotificationManager.notify(NotificationId.getId(), getNotification(text));
        return;
    }

    // remove service notification
    private void removeNotification() {
        mNotificationManager.cancel(mNotifyId);
        return;
    }
}
