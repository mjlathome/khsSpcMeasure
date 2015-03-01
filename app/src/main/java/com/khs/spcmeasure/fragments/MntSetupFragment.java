/**
 * 
 */
package com.khs.spcmeasure.fragments;

import java.util.ArrayList;
import java.util.List;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.old.ImportSetupActivity;
import com.khs.spcmeasure.tasks.DeleteSetupTask;
import com.khs.spcmeasure.tasks.ImportSetupTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * @author Mark
 *
 */
public class MntSetupFragment extends ListFragment {
	
	private static ListView listV;
	OnSetupSelectedListener mListener;
	
	// container activity must implement this interface
	public interface OnSetupSelectedListener {
		public void onSetupSelected(Long prodId);
	}
		
	@Override
	public void onAttach(Activity activity) {		
		super.onAttach(activity);
		
		// ensure host Activity implements the OnSetupSelectedListener interface
		try {
			mListener = (OnSetupSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnSetupSelectedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		setEmptyText("No Data");
		
		// extract all current Products into the listview
        DBAdapter db = new DBAdapter(getActivity());
        db.open();
		Cursor c = db.getAllProducts();
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), 
				android.R.layout.simple_list_item_multiple_choice, c, 
				new String[] {DBAdapter.KEY_NAME}, 
				new int[] {android.R.id.text1}, 0);
		// associate adapter with list view
		setListAdapter(adapter);
		
		listV = getListView();		
		listV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listV.setTextFilterEnabled(true);		
		
		db.close();	
		
		// TODO may have to use a custom adapter view to ensure that the selected setup persists?
		// see: 
		// http://stackoverflow.com/questions/10023904/maintaining-highlight-for-selected-item-in-fragment
		// http://www.michenux.net/android-listview-highlight-selected-item-387.html
		this.getListView().setSelector(R.drawable.list_selector);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.mnt_setup, menu);		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.importSetup) {
			Log.d("DEBUG = ", "Action Item Import");
			importSetup();
		} else if (id == R.id.deleteSetup) {
			Log.d("DEBUG = ", "Action Item Delete");
			deleteSetup();
		}
		
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		Log.d("Debug OnListItemClick Id = ", Long.toString(id));
				
		// inform the Activity of the new Setup
		mListener.onSetupSelected(id);
	}

	public void importSetup() {			
		
		int count = 0;
		SparseBooleanArray checked = listV.getCheckedItemPositions();
		Long[] importSetup = new Long[listV.getCheckedItemCount()];
				
		Log.d("DEBUG, number = ", Integer.toString(listV.getCheckedItemCount()));
		for (int i = 0; i < listV.getCount(); i++) {
			if (checked.get(i)) {				
				Cursor c = (Cursor) listV.getItemAtPosition(i);
				Product p = new DBAdapter(getActivity()).cursorToProduct(c);
				Log.d("DEBUG, selected = ", p.getName());
				importSetup[count++] = p.getId();
				new ImportSetupTask(getActivity()).execute(p.getId());
			}
		}
										
		return;
	}	
	
	public void deleteSetup() {			
		
		// confirm with user via dialog
		AlertDialog.Builder aBuilder = new AlertDialog.Builder(getActivity());					
		aBuilder.setTitle("Delete Setup(s)");
		aBuilder.setMessage("Are you sure you want the " + listV.getCheckedItemCount() + " selected setup(s)?");
		
		// delete action upon Yes
		aBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int count = 0;
				SparseBooleanArray checked = listV.getCheckedItemPositions();
				Long[] deleteSetup = new Long[listV.getCheckedItemCount()];
						
				Log.d("DEBUG, number = ", Integer.toString(listV.getCheckedItemCount()));
				for (int i = 0; i < listV.getCount(); i++) {
					if (checked.get(i)) {				
						Cursor c = (Cursor) listV.getItemAtPosition(i);
						Product p = new DBAdapter(getActivity()).cursorToProduct(c);
						Log.d("DEBUG, selected = ", p.getName());
						deleteSetup[count++] = p.getId();
					}
				}
			    /*
				new DeleteSetupTask(getActivity(), MntSetupFragment.this).execute(deleteSetup);
				*/
				dialog.cancel();									
			}
		});
		
		// cancel action upon No
		aBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();									
			}
		});	
		
		// show the dialog
		AlertDialog aDialog = aBuilder.create();
		aDialog.show();		
		
		return;
	}

	public void refeshList() {
		
		// extract all current Products into the listview
        DBAdapter db = new DBAdapter(getActivity());
        db.open();
		Cursor c = db.getAllProducts();
		
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) listV.getAdapter();
		
		adapter.changeCursor(c);
		adapter.notifyDataSetChanged();

		/*
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), 
				android.R.layout.simple_list_item_multiple_choice, c, 
				new String[] {DBAdapter.KEY_NAME}, 
				new int[] {android.R.id.text1}, 0);
		// associate adapter with list view
		setListAdapter(adapter);
		*/
		
		// listV.invalidateViews();
			
		db.close();					
	}
	
}
