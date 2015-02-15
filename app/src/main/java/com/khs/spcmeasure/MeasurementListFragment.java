package com.khs.spcmeasure;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.khs.spcmeasure.dummy.DummyContent;
import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.CollectStatus;

/**
 * A list fragment representing a list of Measurements. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link MeasurementDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class MeasurementListFragment extends ListFragment {
	
	private static final String TAG = "MeasurementListFragment";
	
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	private ListView mListV; 
	
	private long mPieceId;
	private Piece mPiece;
	
	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(Long id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(Long id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MeasurementListFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate - start");
		
		// TODO remove as menu now in activity
		// indicate fragment has action bar menu
//		setHasOptionsMenu(true);
		
		/* extract piece id from the activities intent */
		Bundle args = getActivity().getIntent().getExtras();		
		if (args != null) {
			mPieceId = args.getLong(DBAdapter.KEY_PIECE_ID);
		}		
		Log.d(TAG, "mPieceId = " + mPieceId);		
		
		// extract the piece
        DBAdapter db = new DBAdapter(getActivity());
        db.open();
		Cursor c = db.getPiece(mPieceId);
		mPiece = db.cursorToPiece(c);
		db.close();
		
		Log.d(TAG, "onCreate - end");
		
		// TODO original remove later
		// TODO: replace with a real list adapter.
//		setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
//				android.R.layout.simple_list_item_activated_1,
//				android.R.id.text1, DummyContent.ITEMS));
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		Log.d(TAG, "onViewCreated - start");
		
//		// Restore the previously serialized activated item position.
//		if (savedInstanceState != null
//				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
//			mActivatedPosition = savedInstanceState
//					.getInt(STATE_ACTIVATED_POSITION);
//			setActivatedPosition(savedInstanceState
//					.getInt(STATE_ACTIVATED_POSITION));
//		}	
		
		Log.d(TAG, "onViewCreated - getLV before");
		mListV = getListView();
		Log.d(TAG, "onViewCreated - getLV after");
		
		Log.d(TAG, "onViewCreated - end");
	}			
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
				
		Log.d(TAG, "onActivityCreated - start");
		
		setEmptyText("No Features");
		
		// extract features for the product		
		DBAdapter db = new DBAdapter(getActivity());
		db.open();
		Cursor c = db.getAllFeatures(mPiece.getProdId());		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), 
				android.R.layout.simple_list_item_activated_1, c, 
				new String[] {DBAdapter.KEY_NAME}, 
				new int[] {android.R.id.text1}, 0);
		// associate adapter with list view
		setListAdapter(adapter);		
		db.close();	

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			mActivatedPosition = savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION);
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}	

		Log.d(TAG, "onActivityCreated - end");
	}
		
	@Override
	public void onStart() {
		super.onStart();
		
		// initially set activate first item in the list
		if (mActivatedPosition == ListView.INVALID_POSITION) {
			// onListItemClick(getListView(), getView(), 1, 0);		
			// getFirst();
			long rowId = performItemClick(0);			
		}
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		mActivatedPosition = position;
		
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(id);
		// TODO remove original later on 
		// mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);		
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}
		
	// TODO remove as menu now in activity
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		// TODO Auto-generated method stub
//		super.onCreateOptionsMenu(menu, inflater);
//		
//		// Inflate the menu; this adds items to the action bar if it is present.
//		inflater.inflate(R.menu.frag_meas_lst, menu);		
//	}	

// TODO remove as menu now in activity	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		int id = item.getItemId();
//		Log.d(TAG, "menu = " + item.getTitle());
//		
//		
//		// handle action bar clicks
//		if (id == R.id.mntPieceClose) {
//			pieceClose();
//			return true;
//		}
//		
//		return super.onOptionsItemSelected(item);
//	}	
	
	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);		
	}

	private long setActivatedPosition(int position) {
		Log.d(TAG, "setActivatedPosition - start: " + position);
		
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
		Log.d(TAG, "setActivatedPosition - end: " + position);
		return getListAdapter().getItemId(position);
	}
	
	private boolean pieceClose() {
		boolean success = false;
		
		int numFeat = 0;
		int numMeas = 0;
		
		// extract features and measurements for the product/piece		
		DBAdapter db = new DBAdapter(getActivity());
		db.open();
		Cursor cFeat = db.getAllFeatures(mPiece.getProdId());
		Cursor cMeas = db.getAllMeasurements(mPiece.getId());
		numFeat = cFeat.getCount();
		numMeas = cMeas.getCount();
		db.close();			
		
		// build dialog message
		String message = "Are you sure you wish to Close this piece?\n";
		if (numFeat == numMeas) {
			message += "All Features have been measured.";
		} else {
			message += numMeas + " out of " + numFeat + " features have been measured."; 
		}
				
		// display dialog
		AlertDialog.Builder dlgAlert = AlertUtils.createAlert(getActivity(), "Warning", message);
		dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {							
				Log.d(TAG, "Piece Close - Okay");
				
				DBAdapter db = new DBAdapter(getActivity());
				
				// mark Piece as closed and update the db
				try {
					mPiece.setStatus(CollectStatus.CLOSED);
					db.open();
					db.updatePiece(mPiece);
					Toast.makeText(getActivity(), "Piece is now Closed", Toast.LENGTH_LONG).show();
					// TODO need to ensure no further readings can take place
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					db.close();
				}
			}
		}); 		
		dlgAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// dismiss the dialog
				Log.d(TAG, "Piece Close - Cancel");				
			}
		}); 		
		
		dlgAlert.show();
		
		return success;
	}
	
	private long performItemClick(int position) {
		long rowId = getListView().getItemIdAtPosition(position);
		getListView().performItemClick(getListView(), position, rowId);
		return rowId;
	}
	
	public long getFirst() {		
		// return setActivatedPosition(0);
		// getListView().performItemClick(getListView(), 0, getListView().getItemIdAtPosition(0));
		// return getListAdapter().getItemId(mActivatedPosition);
		return performItemClick(0);
	}
	
	public long getPrev() {
		int newPos = mActivatedPosition;
		
		Log.d(TAG, "getPrev pos = " + newPos);
		
		if (newPos > 0) {
			newPos--;
		}
		
		// return setActivatedPosition(newPos);
		return performItemClick(newPos);
	}

	public long getNext() {
		int newPos = mActivatedPosition;
		
		Log.d(TAG, "getNext pos = " + newPos);
		
		if (newPos < (getListAdapter().getCount() - 1)) {
			newPos++;
		}
		
		// return setActivatedPosition(newPos);
		return performItemClick(newPos);
	}
	
	public long getLast() {
		// return setActivatedPosition(getListAdapter().getCount() - 1);
		return performItemClick(getListAdapter().getCount() - 1);
	}
	
}
