package com.khs.spcmeasure.widget;

import com.khs.spcmeasure.R;
import com.khs.spcmeasure.library.CollectStatus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

// TODO needs comments

public class CollectStatusActionProvider extends ActionProvider implements OnItemSelectedListener{
	
	private static final String TAG = "CollectStatusActionProvider";
	
    // local broadcast intent actions
    public final static String ACTION_PREFIX = "com.khs.spcmeasure.widget.CollectStatusActionProvider_";
    public final static String ACTION_ITEM_SELECTED = ACTION_PREFIX + "ACTION_ITEM_SELECTED";
    public final static String ACTION_NOTHING_SELECTED = ACTION_PREFIX + "ACTION_NOTHING_SELECTED";	
	
    // local broadcast intent data
    public final static String EXTRA_DATA_ID = "ID";
    
    Context mContext;
    public CollectStatusActionProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {
    	
		// inflate layout
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.collect_status,null);

        // save spinner view
		Spinner spinner = (Spinner) view.findViewById(R.id.spnCollectStatus);
        
        // populate spinner for collect status and setup handler
		ArrayAdapter<CollectStatus> adapter = new ArrayAdapter<CollectStatus>(mContext, android.R.layout.simple_list_item_1, CollectStatus.values());
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);
		
		return view;    	
    }

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Log.d(TAG, "Selected" + id);
		
		// broadcast message - item selected 
    	final Intent intent = new Intent(ACTION_ITEM_SELECTED);
    	CollectStatus collStat = (CollectStatus) parent.getSelectedItem();
    	intent.putExtra(EXTRA_DATA_ID, collStat);
    	LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "Not Selected");
		
		// broadcast message - nothing selected
    	final Intent intent = new Intent(ACTION_NOTHING_SELECTED);
    	LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);			
	}

}
