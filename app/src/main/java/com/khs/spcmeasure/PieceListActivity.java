package com.khs.spcmeasure;

import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.fragments.MntSetupFragment;
import com.khs.spcmeasure.fragments.PieceDialogFragment;
import com.khs.spcmeasure.fragments.PieceDialogFragment.OnNewPieceListener;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.ImportSetupActivity;
import com.khs.spcmeasure.tasks.DeleteSetupTask;
import com.khs.spcmeasure.tasks.DeleteSetupTask2;
import com.khs.spcmeasure.tasks.ExportMeasTask;
import com.khs.spcmeasure.widget.CollectStatusActionProvider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;

/**
 * An activity representing a list of Pieces. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link PieceDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PieceListFragment} and the item details (if present) is a
 * {@link PieceDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link PieceListFragment.Callbacks} interface to listen for item selections.
 */
public class PieceListActivity extends Activity implements
		PieceListFragment.Callbacks, OnNewPieceListener {

	private static final String TAG = "PieceListActivity";
	
	// intent response code
	public static final int REQUEST_IMPORT_SETUP = 1;
	
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	private Long mProdId = null;
	private CollectStatus mCollStat = CollectStatus.OPEN;

	// TODO see exportMeas(); cannot define internal due to final requirement?
	private Long[] rowIds = null;
	
	// handle broadcast intents from collect status action provider
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
	        String action = intent.getAction();
	        Log.d(TAG, "mMessageReceiver Action = " + action);		
	        
	        // check for collect status item selected 
	        if (action.equals(CollectStatusActionProvider.ACTION_ITEM_SELECTED)) {
	        	if (intent.hasExtra(CollectStatusActionProvider.EXTRA_DATA_ID)) {	        		
	        		// extract id for collect status
	        		// TODO was: CollectStatus collStat = (CollectStatus) intent.getSerializableExtra(CollectStatusActionProvider.EXTRA_DATA_ID);
	        		mCollStat = (CollectStatus) intent.getSerializableExtra(CollectStatusActionProvider.EXTRA_DATA_ID);
	        		Log.d(TAG, "intent collStat = " + mCollStat);
	        		// CollectStatus collStat = CollectStatus.fromValue(id);
	        		refreshPieceList(mCollStat);
	        	}
	        }
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_piece_list);

		if (findViewById(R.id.piece_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((PieceListFragment) getFragmentManager().findFragmentById(
					R.id.piece_list)).setActivateOnItemClick(true);
		}

		// TODO: If exposing deep links into your app, handle intents here.
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// register local broadcast receiver - filter multiple intent actions
		IntentFilter collStatFilter = new IntentFilter();
		collStatFilter.addAction(CollectStatusActionProvider.ACTION_ITEM_SELECTED);
		collStatFilter.addAction(CollectStatusActionProvider.ACTION_NOTHING_SELECTED);
		  
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, collStatFilter);    				
	}



	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		// unregister local broadcast receiver
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inflate menu
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.act_piece_lst, menu);
		
		return super.onCreateOptionsMenu(menu);
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Log.d(TAG, "menu = " + item.getTitle());
			
		// handle action bar clicks
		switch(id) {
		case R.id.mnuImportSetup:
			importSetup();			
			return true;
		case R.id.mnuExportMeas:
			exportMeas();
			return true;
		case R.id.mnuDeleteSetup:
			deleteSetup();
			return true;
		case R.id.mnuNewPiece:
			createPiece();
			return true;			
		default:
			return super.onOptionsItemSelected(item);	
		}		
				
	}	

	// handle response from callee
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// check request code
		switch(requestCode) {
		case REQUEST_IMPORT_SETUP:
			// check for success
			if (resultCode == RESULT_OK) {
				// refresh product list
				refreshProductList();
			}
			break;
			
		default:
			AlertUtils.errorDialogShow(this, "Unhandled request code " + requestCode + ".  Contact administrator.");
			break;
		}
		
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}	
	
	/**
	 * Callback method from {@link PieceListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(long id) {
		mProdId = id;
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putLong(DBAdapter.KEY_PROD_ID, id);
			PieceDetailFragment fragment = new PieceDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.replace(R.id.piece_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, PieceDetailActivity.class);
			detailIntent.putExtra(DBAdapter.KEY_PROD_ID, id);
			startActivity(detailIntent);
		}
	}

	@Override
	public void onNewPieceCreated(Long pieceId) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
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
		
	// refresh the list of Products
	private void refreshProductList() {
		try {
			// update Measurement value
			PieceListFragment pieceListFrag = (PieceListFragment) getFragmentManager().findFragmentById(R.id.piece_list);
			if (pieceListFrag != null) {
				pieceListFrag.refreshList();
				refreshPieceList(mCollStat);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return;		
	}
	
	// refresh the list of pieces for the new collect status
	private void refreshPieceList(CollectStatus collStat) {
		
		try {
			// update Measurement value
			PieceDetailFragment pieceDetailFrag = (PieceDetailFragment) getFragmentManager().findFragmentById(R.id.piece_detail_container);
			if (pieceDetailFrag != null) {
				pieceDetailFrag.refeshList(collStat);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return;
	}

	// import Setup(s)
	public void importSetup() {
		
    	// run the Import Setup Activity
    	Intent intent = new Intent(this, ImportSetupActivity.class);
    	startActivityForResult(intent, REQUEST_IMPORT_SETUP);			
		
    	return;
	}
	
	// export piece/measurements
	public void exportMeas() {
		int count = 0;
		
		// verify Product is selected
		if (isProductSelected() == false) {
			return;
		}		
				
		// build rowId list
		// open the DB
		DBAdapter db = new DBAdapter(this);
		db.open();
												
		// extract closed Pieces
		Cursor cPiece = db.getAllPieces(mProdId, CollectStatus.CLOSED);
		
		count = cPiece.getCount();
		if (count > 0) {
			// add rowId to list
			rowIds = new Long[count];
			int i = 0;
			if (cPiece.moveToFirst()) {
				do {
					rowIds[i++] = cPiece.getLong(cPiece.getColumnIndex(DBAdapter.KEY_ROWID));
				} while(cPiece.moveToNext());
			}	
		}
						
		// close the DB			
		db.close();			
				
		if (count <= 0) {
			AlertUtils.errorDialogShow(this, "Export failed.  There are no CLOSED Pieces.");
		} else {
			
			// confirm with user via dialog
			AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);					
			aBuilder.setTitle("Warning");
			aBuilder.setMessage("Are you sure you want to export the " + count + " CLOSED Pieces?");
			
			// delete action upon Yes
			aBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new ExportMeasTask(PieceListActivity.this).execute(rowIds);									
					dialog.cancel();									
				}
			});	
			
			// cancel action upon No
			aBuilder.setNegativeButton("No", null);	
			
			// show the dialog
			AlertDialog aDialog = aBuilder.create();
			aDialog.show();	
		}
				
    	return;
	}
	
	// create new Piece
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
	
	// delete Product setup
	public void deleteSetup() {			
		
		// verify Product is selected
		if (isProductSelected() == false) {
			return;
		}
		
		// confirm with user via dialog
		AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);					
		aBuilder.setTitle("Warning");
		aBuilder.setMessage("Are you sure you want to delete this Setup?");
		
		// delete action upon Yes
		aBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// delete the Setup
				PieceListFragment pieceListFrag = (PieceListFragment) getFragmentManager().findFragmentById(R.id.piece_list);
				new DeleteSetupTask2(PieceListActivity.this, pieceListFrag).execute(mProdId);									
				dialog.cancel();									
			}
		});
		
		// cancel action upon No
		aBuilder.setNegativeButton("No", null);	
		
		// show the dialog
		AlertDialog aDialog = aBuilder.create();
		aDialog.show();		
		
		return;
	}
	
	// verify Product is selected
	private boolean isProductSelected() {
		// TODO probably needs to handle refresh of the list better!
		if (mProdId == null) {
			AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);				
			dlgAlert.setTitle("Error");
			dlgAlert.setMessage("Product Id is unknown.  Please try again.");
			dlgAlert.setCancelable(false);
			dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
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
}
