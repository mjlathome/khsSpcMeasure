package com.khs.spcmeasure;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.khs.spcmeasure.entity.Product;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product>{

	private static final String TAG = "ProductLoader";
	
	private final LayoutInflater mInflater;
    private int mResource;
	
	// constructor
	public ProductAdapter(Context context, int resource) {
		super(context, resource);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mResource = resource;		
		// TODO shoudn't resource be kept for use later on
	}

	// resets adapter data to provided data
	// TODO is this required?
    public void setData(List<Product> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
    }	
	
    // populate new items in the list.
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		
		if (convertView == null) {
			// new view
			view = mInflater.inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
			// TODO remove: view = mInflater.inflate(mResource, parent, false);
		} else {
			// re-use view
			// TODO check correct type?
			view = convertView;
		}
		
		// populate view with data
		Product prod = getItem(position);
		((TextView) view.findViewById(android.R.id.text1)).setText(prod.getName());
		
		return view;
	}
	
}
