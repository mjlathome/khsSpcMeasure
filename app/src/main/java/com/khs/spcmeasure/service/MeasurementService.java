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
import com.khs.spcmeasure.entity.Measurement;
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
import java.util.Date;
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
    public static final String ACTION_IMPORT = "com.khs.spcmeasure.service.action.IMPORT";

    // supported parameters
    public static final String EXTRA_STATUS = "com.khs.spcmeasure.service.extra.STATUS";

    // cancelled Product ids.  see:
    // http://stackoverflow.com/questions/7318666/android-intentservice-how-abort-or-skip-a-task-in-the-handleintent-queue
    private static List<Long> canceledExport = new ArrayList<Long>();

    // url address
    private static String urlExport = "http://thor.kmx.cosma.com/spc/save_measurements.php";
    private static String urlImport = "http://thor.kmx.cosma.com/spc/get_measurements.php?";
    private static String querySep = "&";
    private static String queryProdId = "prodId=";
    private static String querySgId = "sgId=";

    // JSON node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_DATA    = "data";
    private static final String TAG_SUB_GROUP = "sg";
    private static final String TAG_MEAS = "meas";

    // JSON tags
    private static final String TAG_PROD_ID = "prodId";
    private static final String TAG_SG_ID = "sgId";
    private static final String TAG_COLLECT_DT = "collectDt";
    private static final String TAG_OPERATOR = "operator";
    private static final String TAG_LOT = "lot";
    private static final String TAG_FEAT_ID = "featId";
    private static final String TAG_VALUE = "value";
    private static final String TAG_RANGE = "range";
    private static final String TAG_CAUSE = "cause";
    private static final String TAG_LIMIT_REV = "limitRev";
    private static final String TAG_IN_CONTROL = "inControl";
    private static final String TAG_IN_ENG_LIM = "inEngLim";



    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_ACTIVE = "active";
    private static final String TAG_CUSTOMER = "customer";
    private static final String TAG_PROGRAM = "program";
    private static final String TAG_LIMIT_TYPE = "type";
    private static final String TAG_UPPER = "upper";
    private static final String TAG_LOWER = "lower";

    // notification members
    private NotificationManager mNotificationManager;
    private int mNotifyId = 1;

    private PieceDao mPieceDao = new PieceDao(this);
    private ProductDao mProductDao = new ProductDao(this);

    /**
     * Starts this service to perform action EXPORT with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see android.app.IntentService
     */
    // generate intent and start export of a closed piece from the device to the server
    public static void startActionExport(Context context, Long pieceId) {
        Intent intent = new Intent(context, MeasurementService.class);
        intent.setAction(ACTION_EXPORT);
        intent.putExtra(DBAdapter.KEY_PIECE_ID, pieceId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action IMPORT with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see android.app.IntentService
     */
    // generate intent and start import of history data from the server to device
    public static void startActionImport(Context context, Long prodId, Long sgId, String collDt) {
        Intent intent = new Intent(context, MeasurementService.class);
        intent.setAction(ACTION_IMPORT);
        intent.putExtra(DBAdapter.KEY_PROD_ID, prodId);
        intent.putExtra(DBAdapter.KEY_SUB_GRP_ID, sgId);
        intent.putExtra(DBAdapter.KEY_COLLECT_DATETIME, collDt);
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

    // handle service intents
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_EXPORT.equals(action)) {
                // handle export action
                final Long pieceId = intent.getLongExtra(DBAdapter.KEY_PIECE_ID, -1);

                if(canceledExport.contains(pieceId)) {
                    // remove cancellation to ensure it's re-tried in the future
                    canceledExport.remove(pieceId);
                } else {
                    // export piece
                    handleActionExport(pieceId);
                }
            } else if (ACTION_IMPORT.equals(action)) {
                // handle import action
                final Long prodId = intent.getLongExtra(DBAdapter.KEY_PROD_ID, -1);
                final Long sgId = intent.getLongExtra(DBAdapter.KEY_SUB_GRP_ID, -1);
                final String collDt = intent.getStringExtra(DBAdapter.KEY_COLLECT_DATETIME);

                // import piece
                handleActionImport(prodId, sgId, collDt);
            }
        }
    }

    /**
     * Handle action EXPORT in the provided background thread with the provided
     * parameters.
     */
    // export closed piece from device to server
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
                JSONObject jResults = getJsonResultsExport(pieceId);

                if (jResults == null) {
                    // notify user - failure
                    actStat = ActionStatus.FAILED;
                } else {
                    Log.d(TAG, "results of " + pieceId + " = " + jResults.toString());

                    // post json request
                    JSONParser jParser = new JSONParser();
                    JSONObject json = jParser.getJSONFromUrl(urlExport, jResults.toString());

                    // process json response
                    if (processResponseExport(json) == true) {
                        // notify user - success
                        actStat = ActionStatus.COMPLETE;

                        // TODO want to do history delete now
                        HistoryService.startActionDelete(this, piece.getProdId());
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

    // builds JSON piece/measurement data of the given piece Id for export from device to server
    private JSONObject getJsonResultsExport(Long rowId) {
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

                        // TODO export range too... don't have to use it, but it's then consistent with the Measurement Import logic
                        jMeas.put(DBAdapter.KEY_RANGE, cMeas.getDouble(cMeas.getColumnIndex(DBAdapter.KEY_RANGE)));

                        // TODO handle null cause if not out-of-control
                        Long cause = cMeas.getLong(cMeas.getColumnIndex(DBAdapter.KEY_CAUSE));
                        jMeas.put(DBAdapter.KEY_CAUSE, cause);

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

    // process json response for export
    private boolean processResponseExport(JSONObject json) {
        boolean success = false;

        Log.d(TAG, "processResponseExport: json = " + json.toString());

        try {
            // unpack success flag and message
            success = Boolean.valueOf(json.getBoolean(TAG_SUCCESS));
            String  message = String.valueOf(json.getString(TAG_MESSAGE));

            if (success == true) {

                // unpack Piece data from json response
                JSONObject jPiece = json.getJSONObject(DBAdapter.TABLE_PIECE);
                long rowId = Long.valueOf(jPiece.getLong(DBAdapter.KEY_ROWID));
                long sgId = Long.valueOf(jPiece.getLong(DBAdapter.KEY_SUB_GRP_ID));

                Log.d(TAG, "processResponseExport: rowId = " + rowId + " ; sgId = " + sgId);

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

                Log.d(TAG, "processResponseExport: success");

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

    /**
     * Handle action IMPORT in the provided background thread with the provided
     * parameters.
     */
    // import historic piece data from server to device
    private void handleActionImport(Long prodId, Long sgId, String collDt) {

        // assume failure
        ActionStatus actStat = ActionStatus.FAILED;
        String notifyText = prodId.toString();

        try {
            // extract data
            Product product = mProductDao.getProduct(prodId);
            // TODO work out better notify text - maybe use CollectDate as Piece is not on device yet
            notifyText = product.getName() + " - " + collDt; // + " - " + DateTimeUtils.getDateTimeStr(piece.getCollectDt());

            // build url
            String url = urlImport + queryProdId + prodId.toString() + querySep + querySgId + sgId.toString();

            // get json request
            JSONParser jParser = new JSONParser();
            JSONObject json = jParser.getJSONFromUrl(url);

            // process json response
            if (processResponseImport(json, prodId, sgId) == true) {
                // notify user - success
                actStat = ActionStatus.COMPLETE;
            } else {
                // notify user - failure
                actStat = ActionStatus.FAILED;
            }
        } catch (Exception e) {
            e.printStackTrace();

            // cancel import to stop re-try
            // TODO work out how to stop import when we have prodId and sgId
            // cancelExport(pieceId);

            // notify user - failure
            actStat = ActionStatus.FAILED;
        }

        // notify user
        // broadcastUpdate(ACTION_IMPORT, pieceId, actStat);
        updateNotification(ACTION_IMPORT, actStat, notifyText, prodId);
    }

    // process json response for history import
    private boolean processResponseImport(JSONObject json, Long prodId, Long sgId) {
        boolean success = false;
        DBAdapter db = new DBAdapter(this);
        PieceDao pieceDao = new PieceDao(this);
        long pieceNum = 1;  // TODO FUTURE allow multiple measurements per sub-group

        Log.d(TAG, "processResponseImport: json = " + json.toString());

        try {
            // open the DB
            db.open();

            // unpack success flag
            success = Boolean.valueOf(json.getBoolean(TAG_SUCCESS));

            // TODO verify prodId and sgId too
            if (success == true) {

                // verify product id
                if (json.getLong(TAG_PROD_ID) != prodId) {
                    throw new JSONException("prodId does not match: " + prodId + " != " + json.getLong(TAG_PROD_ID));
                }

                // get JSON sub-group array
                JSONArray jSgArr = json.getJSONArray(TAG_SUB_GROUP);

                if (jSgArr.length() != 1) {
                    throw new JSONException("sub-group count not 1: " + jSgArr.length());
                }

                // loop sub-groups - there should only be one
                for (int i = 0; i < jSgArr.length(); i++) {
                    JSONObject jSubGrp = jSgArr.getJSONObject(i);

                    // verify sub-group id
                    if (jSubGrp.getLong(TAG_SG_ID) != sgId) {
                        throw new JSONException("sgId does not match: " + sgId);
                    }

                    // extract Piece fields from json data
                    Date collDt = DateTimeUtils.getDate(jSubGrp.getString(TAG_COLLECT_DT));
                    String operator = jSubGrp.getString(TAG_OPERATOR);
                    String lot = jSubGrp.getString(TAG_LOT);

                    // attempt to find Piece on device
                    // TODO investigate how Piece can be updated, but handle situation.  Without db cursor rowIdPiece is null
                    // TODO problem caused after Setup is imported where the user quickly clicks on the Product and the History is pulled again.
                    // TODO cause duplicate IntentService requests which lead to the same sg being created and subsequently updated.
                    Piece piece = pieceDao.getPiece(prodId, sgId, pieceNum);
                    if (piece != null) {
                        // update Piece
                        piece.setCollectDt(collDt);
                        piece.setOperator(operator);
                        piece.setLot(lot);
                        piece.setStatus(CollectStatus.HISTORY);
                    } else {
                        // create Piece
                        piece = new Piece(prodId, sgId, pieceNum, collDt, operator, lot, CollectStatus.HISTORY);
                    }

                    // start transaction
                    db.beginTransaction();

                    // update or insert Piece into the DB
                    Long rowIdPiece = piece.getId();
                    if (db.updatePiece(piece) == false) {
                        rowIdPiece = db.createPiece(piece);
                    } else {
                        // TODO investigate how Piece can be updated, but handle situation.  Without db cursor rowIdPiece is null
                        Log.d(TAG, "Update successful: id = " + piece.getId() + "; prodId = " + piece.getProdId() + "; sgId = " + piece.getSgId() + "; pieceNum = " + piece.getPieceNum() );
                    }

                    // get JSON measurement array
                    JSONArray jMeasArr = jSubGrp.getJSONArray(TAG_MEAS);

                    // loop measurements
                    for (int j = 0; j < jMeasArr.length(); j++) {
                        JSONObject jMeas = jMeasArr.getJSONObject(j);

                        // extract Measurement field from json data
                        Long featId = jMeas.getLong(TAG_FEAT_ID);
                        Double value = jMeas.getDouble(TAG_VALUE);
                        Double range = jMeas.getDouble(TAG_RANGE);
                        Long cause = jMeas.getLong(TAG_CAUSE);
                        Long limitRev = jMeas.getLong(TAG_LIMIT_REV);
                        Boolean inControl = db.intToBool(jMeas.getInt(TAG_IN_CONTROL));
                        Boolean inEngLim = db.intToBool(jMeas.getInt(TAG_IN_ENG_LIM));

                        Log.d(TAG, "Measurement: featId = " + featId + "; value = " + value + "; rowIdPiece = " + rowIdPiece + "; piece.getProdId() = " + piece.getProdId());

                        // create Measurement object
                        // TODO use independent collect date and operator as will differ from piece
                        Measurement meas = new Measurement(rowIdPiece, piece.getProdId(), featId,
                                piece.getCollectDt(), piece.getOperator(), value, range, cause, limitRev, inControl, inEngLim);

                        // update or insert Measurement into the DB
                        if (db.updateMeasurement(meas) == false) {
                            db.createMeasurement(meas);
                        }
                    }

                }

                if (db.inTransaction()) {
                    // set transaction successful
                    db.setTransactionSuccessful();
                }

                Log.d(TAG, "processResponse: success");

            } else {
                // TODO handle error
                // Toast.makeText(mContext, "ERROR: " + message, Toast.LENGTH_LONG).show();
                success = false;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (db.inTransaction()) {
                // commit
                db.endTransaction();
            }

            // close the DB
            db.close();
        }

        return success;
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
        } else if (action.equals(ACTION_IMPORT)) {
            title = this.getString(R.string.text_meas_import, actStat);
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
