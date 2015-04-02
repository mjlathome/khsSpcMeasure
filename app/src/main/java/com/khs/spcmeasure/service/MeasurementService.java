package com.khs.spcmeasure.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.FeatureReviewActivity;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.dao.PieceDao;
import com.khs.spcmeasure.dao.ProductDao;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.library.ActionStatus;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.library.DateTimeUtils;
import com.khs.spcmeasure.library.JSONParser;
import com.khs.spcmeasure.library.NotificationId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link android.app.IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MeasurementService extends IntentService {
    private static final String TAG = "MeasurementService";

    // supported actions
    public static final String ACTION_EXPORT = "com.khs.spcmeasure.service.action.EXPORT";

    // supported parameters
    public static final String EXTRA_STATUS = "com.khs.spcmeasure.service.extra.STATUS";

    // cancelled Product ids.  see:
    // http://stackoverflow.com/questions/7318666/android-intentservice-how-abort-or-skip-a-task-in-the-handleintent-queue
    private static List<Long> canceledExport = new ArrayList<Long>();

    // url address
    private static String url = "http://thor.kmx.cosma.com/spc/save_measurements.php";

    // JSON node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_DATA    = "data";

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
    private int mNotifyId = 1;

    private PieceDao mPieceDao = new PieceDao(this);
    private ProductDao mProductDao = new ProductDao(this);

    /**
     * Starts this service to perform action IMPORT with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see android.app.IntentService
     */
    public static void startActionExport(Context context, Long pieceId) {
        Intent intent = new Intent(context, MeasurementService.class);
        intent.setAction(ACTION_EXPORT);
        intent.putExtra(DBAdapter.KEY_PIECE_ID, pieceId);
        context.startService(intent);
    }

    // constructor
    public MeasurementService() {
        super("MeasurementService");

        // TODO - I think this is the default so remove later
        // don't redeliver Intents if process dies
        // setIntentRedelivery(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // extract notification manager
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_EXPORT.equals(action)) {
                final Long pieceId = intent.getLongExtra(DBAdapter.KEY_PIECE_ID, -1);

                if(canceledExport.contains(pieceId)) {
                    // remove cancellation to ensure it's re-tried in the future
                    canceledExport.remove(pieceId);
                } else {
                    // import Product
                    handleActionExport(pieceId);
                }
            }
        }
    }

    /**
     * Handle action EXPORT in the provided background thread with the provided
     * parameters.
     */
    private void handleActionExport(Long pieceId) {
        // notify user - starting
        broadcastUpdate(ACTION_EXPORT, pieceId, ActionStatus.WORKING);

        // assume failure
        ActionStatus actStat = ActionStatus.FAILED;
        String notifyText = pieceId.toString();

        try {
            // extract data
            Piece piece = mPieceDao.getPiece(pieceId);
            Product product = mProductDao.getProduct(piece.getProdId());
            notifyText = product.getName() + " - " + DateTimeUtils.getDateTimeStr(piece.getCollectDt());

            if (piece.getStatus() == CollectStatus.CLOSED) {

                // build json for the piece/measurements
                JSONObject jResults = getJsonResults(pieceId);

                if (jResults == null) {
                    // notify user - failure
                    actStat = ActionStatus.FAILED;
                } else {
                    Log.d(TAG, "results of " + pieceId + " = " + jResults.toString());

                    // post json request
                    JSONParser jParser = new JSONParser();
                    JSONObject json = jParser.getJSONFromUrl(url, jResults.toString());

                    // process json response
                    if (processResponse(json) == true) {
                        // notify user - success
                        actStat = ActionStatus.COMPLETE;
                    } else {
                        // notify user - failure
                        actStat = ActionStatus.FAILED;
                    }
                }
            } else {
                // notify user - skipped
                actStat = ActionStatus.SKIPPED;
            }

        } catch (Exception e) {
            e.printStackTrace();

            // cancel import to stop re-try
            cancelExport(pieceId);

            // notify user - failure
            actStat = ActionStatus.FAILED;
        }

        // notify user
        broadcastUpdate(ACTION_EXPORT, pieceId, actStat);
        updateNotification(ACTION_EXPORT, actStat, notifyText, pieceId);
    }

    // builds JSON piece/measurement data for the given piece Id
    private JSONObject getJsonResults(Long rowId) {
        JSONObject jResults = null;

        try {
            // open the DB
            DBAdapter db = new DBAdapter(this);
            db.open();

            // extract Piece
            Cursor cPiece = db.getPiece(rowId);
            Log.d(TAG, "cPiece count = " + cPiece.getCount());

            // verify Piece is available and CLOSED
            if (cPiece.moveToFirst() && CollectStatus.fromValue(cPiece.getString(cPiece.getColumnIndex(DBAdapter.KEY_COLLECT_STATUS))) == CollectStatus.CLOSED) {
                // initialize json results
                jResults = new JSONObject();

                // build json for the Piece
                JSONObject jPiece = new JSONObject();
                jPiece.put(DBAdapter.KEY_ROWID, cPiece.getLong(cPiece.getColumnIndex(DBAdapter.KEY_ROWID)));
                jPiece.put(DBAdapter.KEY_PROD_ID, cPiece.getLong(cPiece.getColumnIndex(DBAdapter.KEY_PROD_ID)));
                jPiece.put(DBAdapter.KEY_SUB_GRP_ID, cPiece.getLong(cPiece.getColumnIndex(DBAdapter.KEY_SUB_GRP_ID)));
                jPiece.put(DBAdapter.KEY_PIECE_NUM, cPiece.getLong(cPiece.getColumnIndex(DBAdapter.KEY_PIECE_NUM)));
                jPiece.put(DBAdapter.KEY_COLLECT_DATETIME, cPiece.getString(cPiece.getColumnIndex(DBAdapter.KEY_COLLECT_DATETIME)));
                jPiece.put(DBAdapter.KEY_OPERATOR, cPiece.getString(cPiece.getColumnIndex(DBAdapter.KEY_OPERATOR)));
                jPiece.put(DBAdapter.KEY_LOT, cPiece.getString(cPiece.getColumnIndex(DBAdapter.KEY_LOT)));

                // build Measurement json array
                JSONArray jMeasArr = new JSONArray();

                // extract Measurements
                Cursor cMeas = db.getAllMeasurements(rowId);
                Log.d(TAG, "cMeas count = " + cMeas.getCount());
                if (cMeas.moveToFirst()) {
                    do {
                        // build json for the Measurement
                        JSONObject jMeas = new JSONObject();
                        jMeas.put(DBAdapter.KEY_FEAT_ID, cMeas.getLong(cMeas.getColumnIndex(DBAdapter.KEY_FEAT_ID)));
                        jMeas.put(DBAdapter.KEY_VALUE, cMeas.getDouble(cMeas.getColumnIndex(DBAdapter.KEY_VALUE)));
                        jMeas.put(DBAdapter.KEY_LIMIT_REV, cMeas.getLong(cMeas.getColumnIndex(DBAdapter.KEY_LIMIT_REV)));
                        jMeas.put(DBAdapter.KEY_IN_CONTROL, DBAdapter.intToBool(cMeas.getInt(cMeas.getColumnIndex(DBAdapter.KEY_IN_CONTROL))));
                        jMeas.put(DBAdapter.KEY_IN_ENG_LIM, DBAdapter.intToBool(cMeas.getInt(cMeas.getColumnIndex(DBAdapter.KEY_IN_ENG_LIM))));

                        // add json Measurement data to json array
                        jMeasArr.put(jMeas);

                    } while(cMeas.moveToNext());
                }
                cMeas.close();

                // add json Measurement array to json Piece object
                jPiece.put(DBAdapter.TABLE_MEASUREMENT, jMeasArr);

                // build json results
                jResults.put(TAG_SUCCESS, true);
                jResults.put(DBAdapter.TABLE_PIECE, jPiece);
            }

            // close the DB
            cPiece.close();
            db.close();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jResults;
    }

    // process json response
    private boolean processResponse(JSONObject json) {
        boolean success = false;

        Log.d(TAG, "processResponse: json = " + json.toString());

        try {
            // unpack success flag and message
            success = Boolean.valueOf(json.getBoolean(TAG_SUCCESS));
            String  message = String.valueOf(json.getString(TAG_MESSAGE));

            if (success == true) {

                // unpack Piece data from json response
                JSONObject jPiece = json.getJSONObject(DBAdapter.TABLE_PIECE);
                long rowId = Long.valueOf(jPiece.getLong(DBAdapter.KEY_ROWID));
                long sgId = Long.valueOf(jPiece.getLong(DBAdapter.KEY_SUB_GRP_ID));

                Log.d(TAG, "processResponse: rowId = " + rowId + " ; sgId = " + sgId);

                // open the DB
                DBAdapter db = new DBAdapter(this);
                db.open();

                // extract Piece
                Cursor cPiece = db.getPiece(rowId);
                Piece piece = db.cursorToPiece(cPiece);
                cPiece.close();

                // update Piece
                piece.setSgId(sgId);
                piece.setStatus(CollectStatus.HISTORY);

                // save Piece
                db.updatePiece(piece);

                // close the DB
                db.close();

                Log.d(TAG, "processResponse: success");

            } else {
                // TODO handle error
                // Toast.makeText(mContext, "ERROR: " + message, Toast.LENGTH_LONG).show();
                success = false;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }


    // allows export cancel for provided Piece Id
    public static void cancelExport(Long pieceId) {
        canceledExport.add(pieceId);
    }

    // create service notification
    private Notification getNotification(String title, String text, Long pieceId) {
        // build pending intent for Feature Review
        Intent intent = new Intent(MeasurementService.this, FeatureReviewActivity.class);
        intent.putExtra(DBAdapter.KEY_PIECE_ID, pieceId);
        PendingIntent notifyIntent = PendingIntent.getActivity(MeasurementService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // build Notification
        Notification.Builder nb = new Notification.Builder(this);
        nb.setSmallIcon(R.drawable.ic_launcher);
        nb.setContentTitle(title);
        nb.setContentText(text);
        nb.setContentIntent(notifyIntent);
        nb.setAutoCancel(true);
        nb.setShowWhen(true);
        return nb.build();
    }

    // update service notification - uses text string provided
    private void updateNotification(String action, ActionStatus actStat, String text, Long pieceId) {
        String title = this.getString(R.string.text_unknown);
        if (action.equals(ACTION_EXPORT)) {
            title = this.getString(R.string.text_meas_export, actStat);
        }

        mNotificationManager.notify(NotificationId.getId(), getNotification(title, text, pieceId));
        return;
    }

    // remove service notification
    private void removeNotification() {
        mNotificationManager.cancel(mNotifyId);
        return;
    }

    // broadcast action
    private void broadcastUpdate(final String action, final long pieceId, final ActionStatus actStat) {
        Log.d(TAG, "broadcastUpdate: action = " + action + "; pieceId = " + pieceId + "; actStat = " + actStat);

        Intent intent = new Intent(action);
        // intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(DBAdapter.KEY_PIECE_ID, pieceId);
        intent.putExtra(EXTRA_STATUS, actStat);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
