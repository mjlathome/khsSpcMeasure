package com.khs.spcmeasure.widget;

import com.khs.spcmeasure.R;
import com.khs.spcmeasure.library.CollectStatus;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class CollectStatusActionProvider2 extends ActionProvider{

	private static final String TAG = "CollectStatusActionProvider";
	
	// Context for accessing resources
	private final Context mContext;
	
	// constructor
	private CollectStatusActionProvider2(Context context) {				
		super(context);
		this.mContext = context;
		Log.d(TAG, "CollectStatusActionProvider");
	}

	// create new action view
	@Override
	public View onCreateActionView() {
		
		Log.d(TAG, "onCreateActionView");
		
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.search_layout,null);
        return view;
		
		/*
		// View view = new CollectStatusView(mContext); 
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.collect_status,null);
		
		Spinner spinner = (Spinner) view.findViewById(R.id.spnCollectStatus);
		ArrayAdapter<CollectStatus> adapter = new ArrayAdapter<CollectStatus>(mContext, android.R.layout.simple_list_item_1, CollectStatus.values());
		spinner.setAdapter(adapter);
		
		return view;
		*/
	}

	private class CollectStatusView extends LinearLayout {

		public CollectStatusView(Context context) {
			super(context);
						
			// set layout properties 
			setOrientation(LinearLayout.VERTICAL);
			setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
							
			// create spinner
			Spinner spinner = new Spinner(context, Spinner.MODE_DIALOG);
			ArrayAdapter<CollectStatus> adapter = new ArrayAdapter<CollectStatus>(context, android.R.layout.simple_list_item_1, CollectStatus.values());
			spinner.setAdapter(adapter);
			
			// add spinner to layout
			addView(spinner);
			
			Log.d(TAG, "CollectStatusView");
			
		}
		
	}
	

}
