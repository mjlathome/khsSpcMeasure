package com.khs.spcmeasure;

import java.util.ArrayList;
import java.util.List;

import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.fragments.ImportSetupFragment;
import com.khs.spcmeasure.fragments.MntSetupFragment;
import com.khs.spcmeasure.fragments.MntSetupFragment.OnSetupSelectedListener;
import com.khs.spcmeasure.fragments.PieceDialogFragment;
import com.khs.spcmeasure.fragments.PieceDialogFragment.OnNewPieceListener;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.os.Build;

public class ImportSetupActivity extends Activity {

	private static final String TAG = "ImportSetupActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mnt_setup);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new ImportSetupFragment()).commit();
		}				
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inflate action bar menu items
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.import_setup, menu);
		
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
