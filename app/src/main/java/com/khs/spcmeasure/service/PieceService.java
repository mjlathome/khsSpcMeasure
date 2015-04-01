package com.khs.spcmeasure.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.SetupListActivity;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.library.NotificationId;

import java.util.Timer;
import java.util.TimerTask;

public class PieceService extends Service {
    private static final String TAG = "PieceService";

    private static final int MIN_5_MILLI_SEC = 5 * 60 * 60 * 1000;

    // timer members
    private Timer mTimer = new Timer();

    // notification members
    private NotificationManager mNotificationManager;
    private PendingIntent mSetupListIntent;
    private int mNotifyId;

    public PieceService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // service does not allow binding
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // extract notification manager
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // build pending intent for Spc Measure
        Intent intent = new Intent(PieceService.this, SetupListActivity.class);
        mSetupListIntent = PendingIntent.getActivity(PieceService.this, 0, intent, 0);

        // get Notification id
        mNotifyId = NotificationId.getId();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // check for CLOSED Pieces and export them to the server
        checkPieceTask();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // cancel timer tasks
        if (mTimer != null){
            mTimer.cancel();
        }

        removeNotification();
    }

    // reschedule task
    private void checkPieceTask() {
        mTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Log.d(TAG, "checkPieceTask");

                exportClosedPiece();
                return;
            }
        }, 0, MIN_5_MILLI_SEC);
    }

    // export CLOSED Pieces to the server
    private void exportClosedPiece() {
        Log.d(TAG, "exportClosedPiece");

        int found = 0;
        int export = 0;

        try {
            // open the DB
            DBAdapter db = new DBAdapter(this);
            db.open();

            // extract all closed pieces
            Cursor cPiece = db.getAllPieces(CollectStatus.CLOSED);
            found = cPiece.getCount();
            Log.d(TAG, "cPiece count = " + cPiece.getCount());
            if (cPiece.moveToFirst()) {
                do {
                    // export Measurements for Piece
                    MeasurementService.startActionExport(this, cPiece.getLong(cPiece.getColumnIndex(DBAdapter.KEY_ROWID)));
                    export++;
                } while(cPiece.moveToNext());
            }
            cPiece.close();

            // close the DB
            db.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        // notify user
        String alertText = this.getString(R.string.text_piece_serv_text, export, found);
        updateNotification(alertText, export);
    }

    // create service notification
    private Notification getNotification(String text, int export) {
        Notification.Builder nb = new Notification.Builder(this);
        nb.setSmallIcon(R.drawable.ic_launcher);
        nb.setContentTitle("Spc Measure - Piece Service");
        nb.setContentText(text);
        nb.setContentIntent(mSetupListIntent);
        nb.setNumber(export);
        nb.setAutoCancel(true);
        nb.setShowWhen(true);
        return nb.build();
    }

    // update service notification - uses text string provided
    private void updateNotification(String text, int export) {
        mNotificationManager.notify(mNotifyId, getNotification(text, export));
        return;
    }

    // remove service notification
    private void removeNotification() {
        mNotificationManager.cancel(mNotifyId);
        return;
    }
}
