package com.khs.spcmeasure.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.khs.spcmeasure.R;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.helper.DBAdapter;
import com.khs.spcmeasure.library.SecurityUtils;

/**
 * An activity representing a single Piece detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link PieceListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link PieceListFragment}.
 */
public class PieceListActivity extends Activity implements
        PieceListFragment.OnFragmentInteractionListener, PieceDialogFragment.OnNewPieceListener{

	private static final String TAG = "PieceListActivity";

    private Long mProdId = null;
    private CollectStatus mCollStat = CollectStatus.OPEN;

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

            // launch feature measurement screen
            Intent featIntent = new Intent(this, FeatureReviewActivity.class);    /* was: FeatureActivity.class */
            featIntent.putExtra(DBAdapter.KEY_PIECE_ID, pieceId);
            startActivity(featIntent);
        }
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_piece_list);

		// show the Up button in the action bar.
        // TODO is this required?
		getActionBar().setDisplayHomeAsUpEnabled(true);

        // extract intent arguments, if any
        getArguments(getIntent().getExtras());

        // extract saved instance state arguments, if any
        getArguments(savedInstanceState);

        // verify arguments
        if (!chkArguments()) {
            Log.d(TAG, "args are BAD");
            AlertUtils.errorDialogShow(this, getString(R.string.text_mess_arguments_invalid));
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

//        Log.d(TAG, "OnStart: 1 Lock = " + SecurityUtils.getLockStatus(this) + "; App = " + SecurityUtils.getInAppStatus(this));
//        if (SecurityUtils.getLockStatus(this)) {
//            // show lock screen
//            Intent intentLogin = new Intent(this, LoginActivity.class);
//            startActivity(intentLogin);
//        } else {
//            // not locked
//            SecurityUtils.setInAppStatus(this, true);
//        }
//        Log.d(TAG, "OnStart: 2 Lock = " + SecurityUtils.getLockStatus(this) + "; App = " + SecurityUtils.getInAppStatus(this));
    }

    @Override
    protected void onStop() {
        super.onStop();

//        Log.d(TAG, "OnStop: 1 Lock = " + SecurityUtils.getLockStatus(this) + "; App = " + SecurityUtils.getInAppStatus(this));
//        if (!SecurityUtils.getInAppStatus(this)) {
//            SecurityUtils.setLockStatus(this, true);    // lock the app
//        } else {
//            SecurityUtils.setLockStatus(this, false);   // not locked
//        }
//        Log.d(TAG, "OnStop: 2 Lock = " + SecurityUtils.getLockStatus(this) + "; App = " + SecurityUtils.getInAppStatus(this));
    }

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
            case R.id.action_login:
                Log.d(TAG, "Menu: Login");
                // show login screen
                Intent intentLogin = new Intent(this, LoginActivity.class);
                startActivity(intentLogin);
                return true;
            case R.id.action_logout:
                Log.d(TAG, "Menu: Logout");
                // set logged out
                SecurityUtils.setIsLoggedIn(this, false);
                return true;
            case R.id.action_settings:
                Log.d(TAG, "Menu: Settings");
                // change preferences
                Intent intentPrefs = new Intent(this, SettingsActivity.class);
                startActivity(intentPrefs);
                return true;
            case R.id.mnuNewPiece:
                createPiece();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}

    // save the state prior to exit
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSaveInstanceState - prodId = " + mProdId);

        // save state
        outState.putLong(DBAdapter.KEY_PROD_ID, mProdId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d(TAG, "onRestoreInstanceState");

        // extract saved instance state arguments, if any
        getArguments(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d(TAG, "onNewIntent");

        // extract intent arguments, if any
        getArguments(intent.getExtras());
    }

    // extracts arguments from provided Bundle
    private void getArguments(Bundle args) {
        if (args != null) {
            if (args.containsKey(DBAdapter.KEY_PROD_ID)) {
                mProdId = args.getLong(DBAdapter.KEY_PROD_ID);
                Log.d(TAG, "prod id = " + mProdId);
            }
        } else {
            Log.d(TAG, "getArguments - NULL");
        }
    }

    // checks arguments
    private boolean chkArguments() {
        return (mProdId != null);
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
