package com.khs.spcmeasure;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.widget.CollectStatusActionProvider;

/**
 * An activity representing a single Piece detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link com.khs.spcmeasure.PieceListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link PieceListFragment}.
 */
public class PieceListActivity extends Activity implements
        PieceListFragment.OnFragmentInteractionListener, PieceDialogFragment.OnNewPieceListener{

	private static final String TAG = "PieceListActivity";

    private Long mProdId = null;
    private CollectStatus mCollStat = CollectStatus.OPEN;

    // TODO action bar spinner no longer required
//    // handle broadcast intents from collect status action provider
//    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // TODO Auto-generated method stub
//            String action = intent.getAction();
//            Log.d(TAG, "mMessageReceiver Action = " + action);
//
//            // check for collect status item selected
//            if (action.equals(CollectStatusActionProvider.ACTION_ITEM_SELECTED)) {
//                if (intent.hasExtra(CollectStatusActionProvider.EXTRA_DATA_ID)) {
//                    // extract id for collect status
//                    // TODO was: CollectStatus collStat = (CollectStatus) intent.getSerializableExtra(CollectStatusActionProvider.EXTRA_DATA_ID);
//                    mCollStat = (CollectStatus) intent.getSerializableExtra(CollectStatusActionProvider.EXTRA_DATA_ID);
//                    Log.d(TAG, "intent collStat = " + mCollStat);
//                    // CollectStatus collStat = CollectStatus.fromValue(id);
//                    refreshPieceList(mCollStat);
//                }
//            }
//        }
//    };


    @Override
    public void onFragmentInteraction(long pieceId) {
        selectPiece(pieceId);
    }

    // interface method: handle new Piece created
    @Override
    public void onNewPieceCreated(Long pieceId) {
        // TODO Auto-generated method stub
        // TODO remove later
        Toast.makeText(this, "New Piece Id = " + pieceId.toString(), Toast.LENGTH_LONG).show();

        if (pieceId != null) {
            // TODO figure out how to automatically reset spinner to OPEN
            // mCollStat = CollectStatus.OPEN;
            if (mCollStat == CollectStatus.OPEN) {
                refreshPieceList(mCollStat);
            }

            // launch measurement screen
            Intent measIntent = new Intent(this, MeasurementListActivity.class);
            measIntent.putExtra(DBAdapter.KEY_PIECE_ID, pieceId);
            startActivity(measIntent);
        }
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_piece_list);

		// Show the Up button in the action bar.
        // TODO is this required?
		getActionBar().setDisplayHomeAsUpEnabled(true);

        // extract piece id from intent; exit if not found
        Bundle args = getIntent().getExtras();
        if (args != null && args.containsKey(DBAdapter.KEY_PROD_ID)) {
            mProdId = args.getLong(DBAdapter.KEY_PROD_ID);

            Log.d(TAG, "prodId = " + Long.toString(mProdId));

            // TODO remove how to get the actual record later
//            // extract the piece
////			mPiece = mPieceDao.findPiece(mPieceId);
//            DBAdapter db = new DBAdapter(this);
//            db.open();
//            Cursor c = db.findPiece(mPieceId);
//            Log.d(TAG, "Cursor count = " + c.getCount());
//            mPiece = db.cursorToPiece(c);
//            db.close();
//            Log.d(TAG, "OnCreate Piece St = " + mPiece.getStatus());

        } else {
            AlertUtils.errorDialogShow(this, getString(R.string.text_mess_prod_id_invalid));
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
			Bundle arguments = new Bundle();
			arguments.putLong(DBAdapter.KEY_PROD_ID, mProdId);
			PieceListFragment fragment = new PieceListFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.add(R.id.piece_list_container, fragment).commit();
		}
	}

    // TODO no longer required
//    // register for Collect Status changed broadcast
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        // register local broadcast receiver - filter multiple intent actions
//        IntentFilter collStatFilter = new IntentFilter();
//        collStatFilter.addAction(CollectStatusActionProvider.ACTION_ITEM_SELECTED);
//        collStatFilter.addAction(CollectStatusActionProvider.ACTION_NOTHING_SELECTED);
//
//        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, collStatFilter);
//    }

    // inflate Action Bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // inflate menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_piece_list, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // handle Action Bar menu selection
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

        switch(id) {
            case R.id.mnuNewPiece:
                createPiece();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

        // TODO remove later
//		if (id == android.R.id.home) {
//			// This ID represents the Home or Up button. In the case of this
//			// activity, the Up button is shown. For
//			// more details, see the Navigation pattern on Android Design:
//			//
//			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
//			//
//
//			// navigateUpTo(new Intent(this, com.khs.spcmeasure.PieceListActivity.class));
//			return true;
//		}
	}

    // helper method: verify Product is selected
    private boolean isProductSelected() {
        // TODO probably needs to handle refresh of the list better!
        if (mProdId == null) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setTitle(getString(R.string.text_error));
            dlgAlert.setMessage(getString(R.string.text_mess_prod_id_invalid));
            dlgAlert.setCancelable(false);
            dlgAlert.setPositiveButton(getString(R.string.text_okay), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // dismiss the dialog
                }
            });
            dlgAlert.create().show();
            return false;
        } else {
            return true;
        }
    }

    // helper method: refresh the list of pieces for the new collect status
    private void refreshPieceList(CollectStatus collStat) {

        try {
            // refresh list
            PieceListFragment pieceListFrag = (PieceListFragment) getFragmentManager().findFragmentById(R.id.piece_list_container);
            if (pieceListFrag != null) {
                pieceListFrag.refreshList(collStat);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return;
    }

    // handle Action Bar menu option: New Piece
    public void createPiece() {

        // verify Product is selected
        if (isProductSelected() == false) {
            return;
        }

        // create dialog
        PieceDialogFragment myDialog = new PieceDialogFragment();

        // attach arguments
        Bundle args = new Bundle();
        args.putLong(DBAdapter.KEY_PROD_ID, mProdId);
        myDialog.setArguments(args);

        // show dialog
        FragmentManager fragMgr = getFragmentManager();
        myDialog.show(fragMgr, "newPiece");

        return;
    }

    // starts the measurement activity for the provided piece
    private void selectPiece(long pieceId) {

        // launch the measurement list (i.e. master) activity
        if (pieceId == ListView.INVALID_POSITION) {
            AlertUtils.errorDialogShow(this, getString(R.string.text_mess_piece_not_selected));
        } else {
            Intent featRevIntent = new Intent(this, FeatureReviewActivity.class);
            featRevIntent.putExtra(DBAdapter.KEY_PIECE_ID, pieceId);
            startActivity(featRevIntent);
        }

        return;
    }
}
