package com.khs.spcmeasure;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.library.DateTimeUtils;
import com.khs.spcmeasure.library.SecurityUtils;

/**
 * A fragment representing a single Piece detail screen. This fragment is either
 * contained in a {@link com.khs.spcmeasure.PieceListActivity} in two-pane mode (on tablets) or a
 * {@link PieceListActivity} on handsets.
 */
public class PieceListFragment extends ListFragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "PieceListFragment";
    private static ListView mListView;

	private Long mProdId;
	
	private long mPieceId = ListView.INVALID_POSITION;
	private CollectStatus mCollStat = CollectStatus.OPEN;

    private OnFragmentInteractionListener mListener;

    // spinner interface calls
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        CollectStatus collStat = (CollectStatus) adapterView.getSelectedItem();
        refreshList(collStat);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    //	private TextView mHeader;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PieceListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

        // TODO need to trap when Product Id is not set
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
		View rootView = inflater.inflate(R.layout.fragment_piece_list,
				container, false);

		// TODO Auto-generated method stub
//		return super.onCreateView(inflater, container, savedInstanceState);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        // TODO remove as no longer required
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		// this.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		// add header to listview - has problems when updated in refreshList
//		setListAdapter(null);
//		mHeader = new TextView(getActivity());
//		mHeader.setText("Prod = " + mProdId);  
//		getListView().addHeaderView(mHeader);		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        mListView = getListView();

        // register the listview for a context menu
        registerForContextMenu(mListView);

        // show the Product Name in the TextView
        if (mProdId != null) {
            // extract the Product
            DBAdapter db = new DBAdapter(getActivity());
            db.open();
            Cursor c = db.getProduct(mProdId);
            ((TextView) getView().findViewById(R.id.txtProdName))
                    .setText(c.getString(c.getColumnIndex(DBAdapter.KEY_NAME)));
            c.close();
            db.close();
        }

        // populate spinner for Collect Status and setup handler
        Spinner spinner = (Spinner) getView().findViewById(R.id.spnCollStatus);
        ArrayAdapter<CollectStatus> adapter = new ArrayAdapter<CollectStatus>(getActivity(), android.R.layout.simple_list_item_1, CollectStatus.values());
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // refresh piece list
        // refreshList(mCollStat);
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // refresh the list prior
    @Override
    public void onResume() {
        super.onResume();

        refreshList(mCollStat);
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		Log.d(TAG, "OnListItemClick Id = " + id);
		// save current selected Piece Id
		mPieceId = id;
		
		// inform the Activity of the selected Piece
		mListener.onFragmentInteraction(id);
	}

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(long pieceId);
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);

        // TODO remove later as Fragment no longer adds any menu options
        // TODO probably should add new piece through
//		// Inflate the menu; this adds items to the action bar if it is present.
//		inflater.inflate(R.menu.fragment_piece_list, menu);
//
//		// find menu item for Collect Status
//		MenuItem mnuCollStat = (MenuItem) menu.findItem(R.id.mnuCollectStatus);
//		Log.d(TAG, "MenuI = " + mnuCollStat);
		
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

        switch(id) {
            case R.id.action_settings:
                return true;
            // TODO remove later as Action Spinner not used
//            case R.id.mnuCollectStatus:
//                return false;
            default:
                return super.onOptionsItemSelected(item);
        }

        // TODO old action menu - remove later
//		if (id == R.id.action_settings) {
//			return true;
//		} else if (id == R.id.mnuCollectStatus) {
//			Log.d(TAG, "id = mnuCollectStatus");
//			return false;
//		} else if (id == R.id.mnuPieceMeas) {
//			selectPiece(mPieceId);
//			return true;
//		} else if (id == R.id.mnuPieceDelete) {
//			deletePiece(mPieceId);
//			return true;
// 		}
//
//		// TODO Auto-generated method stub
//		return super.onOptionsItemSelected(item);
	}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_piece_list, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        // get info for item selected
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        // handle menu option selected
        int id = item.getItemId();
        switch(id) {
            case R.id.mnuOpen:
                mListener.onFragmentInteraction(info.id);
                // selectPiece(info.id);
                return true;
            case R.id.mnuDelete:
                deletePiece(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // refresh piece list based upon provided collect status
	public void refreshList(CollectStatus collStat) {
		Log.d(TAG, "refreshList = " + collStat);

        // setEmptyText(getString(R.string.text_no_data));

        // start out with a progress indicator.
        // setListShown(false);

		// save current Collect Status
		mCollStat = collStat;
		
		// clear current selected Piece
		mPieceId = ListView.INVALID_POSITION;
		
		// extract all Pieces for the prodId and collect status
        DBAdapter db = new DBAdapter(getActivity());
        db.open();
        Cursor c = db.getAllPieces(mProdId, collStat);


//		SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListView().getAdapter();

        // create adapter
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_activated_2, c,
                new String[] {DBAdapter.KEY_COLLECT_DATETIME, DBAdapter.KEY_OPERATOR},
                new int[] {android.R.id.text1, android.R.id.text2}, 0);

        // associate adapter with list view
        setListAdapter(adapter);
        db.close();


//        adapter.changeCursor(c);
//		  adapter.notifyDataSetChanged();
			
		db.close();

        // remove progress indicator.
        // setListShown(true);

        /****
         *
         *
         mListView = getListView();
         mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         mListView.setTextFilterEnabled(true);
         *
         */
	}	

	// handles piece deletion
	private void deletePiece(int pos) {
        // check security
        if (!SecurityUtils.checkSecurity(getActivity(), true)) {
            return;
        }

        Cursor c = (Cursor) mListView.getItemAtPosition(pos);
        final Piece p = new DBAdapter(getActivity()).cursorToPiece(c);
        String message;

        // TODO block delete or context menu option when Piece is not OPEN
        if (!p.getStatus().equals(CollectStatus.OPEN)) {
            message = String.format(getString(R.string.text_mess_delete_piece_not_open), DateTimeUtils.getDateTimeStr(p.getCollectDt()));
            AlertUtils.errorDialogShow(getActivity(), message);
            return;
        }

        // confirm with user via dialog
        message = String.format(getString(R.string.text_mess_delete_piece), DateTimeUtils.getDateTimeStr(p.getCollectDt()));

		AlertDialog.Builder dlgAlert = AlertUtils.createAlert(getActivity(), getString(R.string.text_warning), message);
		dlgAlert.setPositiveButton(R.string.text_okay, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {							
				// TODO use DAO?
				DBAdapter db = new DBAdapter(getActivity());

                // TODO use a task to perform the delete instead
				// delete the piece
				try {
					db.open();
					db.deletePiece(p.getId());
					Toast.makeText(getActivity(), getString(R.string.text_mess_piece_deleted), Toast.LENGTH_LONG).show();
					// TODO need to ensure no further readings can take place
					refreshList(mCollStat);
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					db.close();
				}
			}
		}); 		
		dlgAlert.setNegativeButton(getString(R.string.text_cancel), null);
		dlgAlert.show();
		
		return;
	}
	
}
