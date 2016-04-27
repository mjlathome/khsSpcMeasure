package com.khs.spcmeasure.helper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.khs.spcmeasure.entity.Product;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product>{

	private static final String TAG = "ProductAdapter";
	
	private final LayoutInflater mInflater;
    private int mResource;
	private ProductFilter mProductFilter;
    private List<Product> mProductList;
	
	// constructor
	public ProductAdapter(Context context, int resource) {
		super(context, resource);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mResource = resource;		
		// TODO shoudn't resource be kept for use later on
	}

	// resets adapter data to provided data.  called from ProductLoader
    public void setData(List<Product> data) {
        clear();
        mProductList = null;
        if (data != null) {
            addAll(data);
            mProductList = data;
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

	// custom filter to support wildcard product search
	// see:
	// http://www.survivingwithandroid.com/2012/10/android-listview-custom-filter-and.html
	// http://stackoverflow.com/questions/794381/how-to-find-files-that-match-a-wildcard-string-in-java
	@Override
	public Filter getFilter() {
        Log.d(TAG, "getFilter");

		// return super.getFilter();
		if (mProductFilter == null)
			mProductFilter = new ProductFilter();

		return mProductFilter;
	}

	private class ProductFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                Log.d(TAG, "FilterResults: null constraint");

                // No filter implemented we return all the list
                results.values = mProductList;
                results.count = mProductList.size();
            }
            else {
                Log.d(TAG, "FilterResults: constraint = " + constraint.toString());

                // We perform filtering operation
                String regex = constraint.toString().toUpperCase().replace("?", ".?").replace("*", ".*?");

                List<Product> filterList = new ArrayList<Product>();

                for (Product p : mProductList) {
                    if (p.getName().toUpperCase().matches(regex)) {
                        filterList.add(p);
                    }
                }

                results.values = filterList;
                results.count = filterList.size();
            }
            return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
            Log.d(TAG, "publishResults: count = " + results.count);

            // Now we have to inform the adapter about the new list filtered
            // setData((List<Product>) results.values);

            clear();
            if (results.count == 0) {
                // notifyDataSetInvalidated();
            } else {
                addAll((List<Product>) results.values);
                // mProductList = (List<Product>) results.values;
                // notifyDataSetChanged();
            }

		}
	}
}
