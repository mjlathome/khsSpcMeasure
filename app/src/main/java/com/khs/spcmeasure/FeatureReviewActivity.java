package com.khs.spcmeasure;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.khs.spcmeasure.dao.PieceDao;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.CollectStatus;

// TODO handle Action Bar menu Up button - see Stack Overflow

public class FeatureReviewActivity extends Activity implements
        FeatureReviewFragment.OnFragmentInteractionListener {

    private static final String TAG = "FeatureReviewActivity";

    private Long mPieceId = null;

    @Override
    public void onFragmentInteraction(long featId) {
        Log.d(TAG, "featId = " + featId);
        selectFeature(featId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_review);

        // show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // extract piece id from intent; exit if not found
        Bundle args = getIntent().getExtras();
        if (args != null && args.containsKey(DBAdapter.KEY_PIECE_ID)) {
            mPieceId = args.getLong(DBAdapter.KEY_PIECE_ID);
        } else {
            AlertUtils.errorDialogShow(this, getString(R.string.text_mess_piece_id_invalid));
            finish();
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            FeatureReviewFragment fragment = FeatureReviewFragment.newInstance(mPieceId);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feature_review, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                Log.d(TAG, "Home");
                return true;
            case R.id.mnuClosePiece:
                closePiece();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // TODO move into a task so it can be called elsewhere
    // close the Piece
    private boolean closePiece() {
        boolean success = false;

        int numFeat = 0;
        int numMeas = 0;

        // extract the piece
        PieceDao pieceDao = new PieceDao(this);
        final Piece piece = pieceDao.getPiece(mPieceId);

        Log.d(TAG, "Close Piece St = " + piece.getStatus());

        // exit if Piece is already closed
        if (piece.getStatus() == CollectStatus.CLOSED) {
            AlertUtils.alertDialogShow(this, getString(R.string.text_information), getString(R.string.text_piece_already_closed));
            return true;
        }

        // TODO move to another layer
        // extract features and measurements for the product/piece
        DBAdapter db = new DBAdapter(this);
        db.open();
        Cursor cFeat = db.getAllFeatures(piece.getProdId());
        Cursor cMeas = db.getAllMeasurements(piece.getId());
        numFeat = cFeat.getCount();
        numMeas = cMeas.getCount();
        cFeat.close();
        cMeas.close();
        db.close();

        // build dialog message
        // TODO use strings constants
        String message = "Are you sure you wish to Close this piece?\n";
        if (numFeat == numMeas) {
            message += "All Features have been measured.";
        } else {
            message += numMeas + " out of " + numFeat + " features have been measured.";
        }

        // display dialog
        AlertDialog.Builder dlgAlert = AlertUtils.createAlert(this, getString(R.string.text_warning), message);
        dlgAlert.setPositiveButton(getString(R.string.text_okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "Piece Close - Okay");

                // TODO use DAO?
                DBAdapter db = new DBAdapter(FeatureReviewActivity.this);

                // mark Piece as closed, update db and unbind Ble service as not required
                try {
                    piece.setStatus(CollectStatus.CLOSED);
                    db.open();
                    db.updatePiece(piece);
                    Toast.makeText(FeatureReviewActivity.this, getString(R.string.text_piece_now_closed), Toast.LENGTH_LONG).show();
                    // unbindBleService();
                    // TODO need to ensure no further readings can take place
                    // TODO need to refresh on-screen Piece Status via Fragment call
                    // TODO need to refresh Piece List screen upon return
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    db.close();
                }
            }
        });
        dlgAlert.setNegativeButton(getString(R.string.text_cancel), null);
        dlgAlert.show();

        return success;
    }

    // starts the Feature activity
    private void selectFeature(long featId) {

        // launch activity
        if (featId == ListView.INVALID_POSITION) {
            AlertUtils.errorDialogShow(this, getString(R.string.text_mess_feature_not_selected));
        } else {
            Intent featIntent = new Intent(this, FeatureActivity.class);
            featIntent.putExtra(DBAdapter.KEY_PIECE_ID, mPieceId);
            featIntent.putExtra(DBAdapter.KEY_FEAT_ID, featId);
            startActivity(featIntent);
        }

        return;
    }

}
