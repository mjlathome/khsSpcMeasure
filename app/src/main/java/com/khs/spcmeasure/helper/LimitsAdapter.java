package com.khs.spcmeasure.helper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.khs.spcmeasure.R;
import com.khs.spcmeasure.helper.DBAdapter;

import java.text.DecimalFormat;

public class LimitsAdapter extends ResourceCursorAdapter {

	// constructor
	public LimitsAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);		
	}

	// bind the cursor fields to the view
	@Override
	public void bindView(View view, Context context, Cursor cursor) {

        // format to 3dp
        DecimalFormat df = new DecimalFormat("#.000");

		// extract text views
		TextView txtLimType = (TextView) view.findViewById(R.id.txtLimType);
		TextView txtLimUpper = (TextView) view.findViewById(R.id.txtLimUpper);
		TextView txtLimLower = (TextView) view.findViewById(R.id.txtLimLower);

        // use normal typeface for rows
        txtLimType.setTypeface(null, Typeface.NORMAL);
        txtLimUpper.setTypeface(null, Typeface.NORMAL);
        txtLimLower.setTypeface(null, Typeface.NORMAL);

		// display data
		txtLimType.setText(cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_LIMIT_TYPE)));
		txtLimUpper.setText(df.format(cursor.getDouble(cursor.getColumnIndex(DBAdapter.KEY_UPPER))));
		txtLimLower.setText(df.format(cursor.getDouble(cursor.getColumnIndex(DBAdapter.KEY_LOWER))));
		
		return;
	}

}
