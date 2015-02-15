package com.khs.spcmeasure;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.khs.spcmeasure.dummy.DummyContent;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.library.DateTimeUtils;

/**
 * A fragment representing a single Piece detail screen. This fragment is either
 * contained in a {@link PieceListActivity} in two-pane mode (on tablets) or a
 * {@link PieceDetailActivity} on handsets.
 */
public class PieceDetailFragment extends ListFragment {
	
	private static final String TAG = "PieceDetailFragment";
	
	/**
	 * The dummy content this fragment is presenting.
	 */
	private Long mProdId;
	
	private long mPieceId = ListView.INVALID_POSITION;
	private CollectStatus mCollStat;

//	private TextView mHeader;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PieceDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		
		/* extract arguments */
		Bundle args = getArguments();		
		if (args != null) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mProdId = args.getLong(DBAdapter.KEY_PROD_ID);
		}		
		Log.d(TAG, "mProdId = " + mProdId);	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//		View rootView = inflater.inflate(R.layout.fragment_piece_detail,
//				container, false);

//		// Show the dummy content as text in a TextView.
//		if (mProdId != null) {
//			((TextView) rootView.findViewById(R.id.piece_detail))
//					.setText(Double.toString(mProdId));
//		}		
		
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
//		return rootView;
	}
	
	

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		this.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);	
		
		// add header to listview - has problems when updated in refreshList
//		setListAdapter(null);
//		mHeader = new TextView(getActivity());
//		mHeader.setText("Prod = " + mProdId);  
//		getListView().addHeaderView(mHeader);		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		OnItemLongClickListener listener = new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				// start the measurement activity
				startMeasActivity(id);
				
				// indicate that event was handled
				return true;
			}
        };
 
        getListView().setOnItemLongClickListener(listener);		
		
		// empty list text
		setEmptyText("No Pieces");
		
		// populate list with all pieces for the product		
		DBAdapter db = new DBAdapter(getActivity());
		db.open();
		Cursor c = db.getAllPieces(mProdId);		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), 
				android.R.layout.simple_list_item_activated_2, c, 
				new String[] {DBAdapter.KEY_COLLECT_DATETIME, DBAdapter.KEY_OPERATOR}, 
				new int[] {android.R.id.text1, android.R.id.text2}, 0);
		// associate adapter with list view
		setListAdapter(adapter);		
		db.close();				
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		Log.d(TAG, "OnListItemClick Id = " + id);
		// save current selected Piece Id
		mPieceId = id;
		
		// inform the Activity of the new Setup
		// mListener.onSetupSelected(id);
	}	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.frag_piece_dtl, menu);
		
		// find menu item for Collect Status
		MenuItem mnuCollStat = (MenuItem) menu.findItem(R.id.mnuCollectStatus);
		Log.d(TAG, "MenuI = " + mnuCollStat);
		
		// find the Collect Status spinner
		// Spinner miSollStat = (Spinner) mnuCollStat.findItem(R.id.spnCollectStatus);		
		// Log.d(TAG, "Spin = " + collStat);
		
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		Log.d(TAG, "menu = " + item.getTitle());
		
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.mnuCollectStatus) {
			Log.d(TAG, "id = mnuCollectStatus");
			return false;
		} else if (id == R.id.mnuPieceMeas) {
			startMeasActivity(mPieceId);
			return true;
		} else if (id == R.id.mnuPieceDelete) {		
			deletePiece(mPieceId);
			return true;
 		} 
					
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}	

	// refresh piece list based upon provided collect status 
	public void refeshList(CollectStatus collStat) {
		
		Log.d(TAG, "refreshList = " + collStat);
		
		// save current Collect Status
		mCollStat = collStat;
		
//		getListView().removeHeaderView(mHeader);
		
		// clear current selected Piece
		mPieceId = ListView.INVALID_POSITION;
		
		// extract all Pieces for the prodId and collect status
        DBAdapter db = new DBAdapter(getActivity());
        db.open();
		// Cursor c = db.getAllPieces(mProdId, collStat);
        Cursor c = db.getAllPieces(mProdId, collStat);
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListView().getAdapter();
		
		adapter.changeCursor(c);
		adapter.notifyDataSetChanged();
			
		db.close();					
	}	
	
	// starts the measurement activity for the provided piece
	private void startMeasActivity(long pieceId) {
		
		// launch the measurement list (i.e. master) activity
		if (pieceId == ListView.INVALID_POSITION) {
			AlertUtils.errorDialogShow(getActivity(), "No Piece is selected");
		} else {			
			Intent measIntent = new Intent(getActivity(), MeasurementListActivity.class);
			measIntent.putExtra(DBAdapter.KEY_PIECE_ID, pieceId);
			startActivity(measIntent);
		}
		
        return;
	}
	
	// handles piece deletion
	private void deletePiece(final long pieceId) {
		// TODO show extra product/piece info?
		String message = "Are you sure you wish to delete this Piece?";
						
		AlertDialog.Builder dlgAlert = AlertUtils.createAlert(getActivity(), "Warning", message);
		dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {							
				// TODO use DAO?
				DBAdapter db = new DBAdapter(getActivity());
								
				// delete the piece
				try {
					db.open();
					db.deletePiece(pieceId);
					Toast.makeText(getActivity(), "Piece has been Deleted", Toast.LENGTH_LONG).show();
					// TODO need to ensure no further readings can take place
					refeshList(mCollStat);
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					db.close();
				}
			}
		}); 		
		dlgAlert.setNegativeButton("Cancel", null); 	
		dlgAlert.show();
		
		return;
	}
	
}
