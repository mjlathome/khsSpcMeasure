package com.khs.spcmeasure.widget;

import com.khs.spcmeasure.R;

import android.content.Context;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;

public class SearchActionProvider extends ActionProvider {
	
    Context mContext;
    public SearchActionProvider(Context context) {
        super(context);
        mContext = context;
    }
 
    @Override
    public View onCreateActionView() {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.search_layout,null);
        return view;
    }
    
}
