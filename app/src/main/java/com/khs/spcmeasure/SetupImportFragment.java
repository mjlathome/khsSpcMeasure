/**
 * 
 */
package com.khs.spcmeasure;

import java.util.List;

import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.service.SetupService;
import com.khs.spcmeasure.tasks.ImportSetupTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
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
public class SetupImportFragment extends ListFragment
	implements OnQueryTextListener, OnCloseListener, LoaderManager.LoaderCallbacks<List<Product>>{
	
	private static final String TAG = "SetupImportFragment";
	
    // This is the Adapter being used to display the list's data.
    ProductAdapter mAdapter;

    // The SearchView for doing filtering.
    SearchView mSearchView;

    // If non-null, this is the current filter the user has provided.
    String mCurFilter;
			
	@Override
	public void onAttach(Activity activity) {		
		super.onAttach(activity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// TODO can this be done here instead?
		// setHasOptionsMenu(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText(getString(R.string.text_no_data));

        // we have a menu item to show in action bar.
        setHasOptionsMenu(true);
        
        // create an empty adapter we will use to display the loaded data.
        mAdapter = new ProductAdapter(getActivity(), android.R.layout.simple_list_item_multiple_choice);    
        
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        // associate adapter with list view
     	setListAdapter(mAdapter);
        
     	// start out with a progress indicator.
        setListShown(false);
        
        // prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(0, null, this).forceLoad();
     	
        return;
	}
	
    public static class MySearchView extends SearchView {
        public MySearchView(Context context) {
            super(context);
        }

        // The normal SearchView doesn't clear its search text when
        // collapsed, so we will do this for it.
        @Override
        public void onActionViewCollapsed() {
            setQuery("", false);
            super.onActionViewCollapsed();
        }
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_import_setup, menu);
		
        // place an action bar item for searching.
        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        mSearchView = new MySearchView(getActivity());
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setIconifiedByDefault(true);

        // make the search view field readable
        int searchPlateId = mSearchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = mSearchView.findViewById(searchPlateId);
        if (searchPlate!=null) {
            searchPlate.setBackgroundColor(Color.DKGRAY);
            int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
            if (searchText!=null) {
	            searchText.setTextColor(Color.WHITE);
	            searchText.setHintTextColor(Color.WHITE);
            }
        }        
        
        item.setActionView(mSearchView);	
        
        return;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Since this
        // is a simple array adapter, we can just have it do the filtering.
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        mAdapter.getFilter().filter(mCurFilter);
		return true;
	}
		
	@Override
	public boolean onQueryTextSubmit(String query) {
        // Don't care about this.
        return true;	
	}

	@Override
	public boolean onClose() {
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        return true;
	}
	
	@Override
	public Loader<List<Product>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new ProductLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<Product>> loader, List<Product> data) {
        // Set the new data in the adapter.
        mAdapter.setData(data);

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }			
	}

	@Override
	public void onLoaderReset(Loader<List<Product>> loader) {
        // Clear the data in the adapter.
        mAdapter.setData(null);		
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
		} else if (id == R.id.mnuImportSetup) {		
			importSetup();
            return true;
		} 

		return super.onOptionsItemSelected(item);		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Log.d("Debug OnListItemClick Id = ", Long.toString(id));	
	}

	// import the selected setups 
	// TODO make importSetup task accept multiple id's with Progress bar
	public void importSetup() {			
		// confirm with user
		String message = String.format(getString(R.string.text_mess_import_setup,
            getListView().getCheckedItemCount()));
		AlertDialog.Builder dlgAlert = AlertUtils.createAlert(getActivity(), getString(R.string.text_warning), message);
		dlgAlert.setPositiveButton(getString(R.string.text_okay), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				// build selection list
				ListView listV = getListView();							
				int count = 0;
				SparseBooleanArray checked = listV.getCheckedItemPositions();
				Long[] importSetup = new Long[listV.getCheckedItemCount()];
				
				// import products
				// TODO update task to handle multiple and use progress bar
				for (int i = 0; i < listV.getCount(); i++) {
					if (checked.get(i)) {				
						Product p = (Product) listV.getItemAtPosition(i);				
						Log.d(TAG, "importSetup selected = " + p.getName());
						importSetup[count++] = p.getId();
						// new ImportSetupTask(getActivity()).execute(p.getId());
                        SetupService.startActionImport(getActivity(), p.getId());
					}
				}
				
				// inform callee and close
				Intent intent = new Intent();
				getActivity().setResult(Activity.RESULT_OK, intent);
				getActivity().finish();
				
			}
		}); 		
		dlgAlert.setNegativeButton(getString(R.string.text_cancel), null);
		dlgAlert.show();		
												
		return;
	}	
		
}
