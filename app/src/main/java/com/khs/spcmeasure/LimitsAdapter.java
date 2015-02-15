package com.khs.spcmeasure;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class LimitsAdapter extends ResourceCursorAdapter {

	// constructor
	public LimitsAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);		
	}

	// bind the cursor fields to the view
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		// extract text views 
		TextView txtLimType = (TextView) view.findViewById(R.id.colLimType);
		TextView txtLimUpper = (TextView) view.findViewById(R.id.colLimUpper);
		TextView txtLimLower = (TextView) view.findViewById(R.id.colLimLower);
		
		// display data
		txtLimType.setText(cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_LIMIT_TYPE)));
		txtLimUpper.setText(Double.toString(cursor.getDouble(cursor.getColumnIndex(DBAdapter.KEY_UPPER))));
		txtLimLower.setText(Double.toString(cursor.getDouble(cursor.getColumnIndex(DBAdapter.KEY_LOWER))));
		
		return;
	}

}
