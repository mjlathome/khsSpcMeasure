package com.khs.spcmeasure;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SetupImportActivity extends Activity {

	private static final String TAG = "SetupImportActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup_import);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SetupImportFragment()).commit();
		}				
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inflate action bar menu items
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_import_setup, menu);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle action bar menu items
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}
		
}
