package com.khs.spcmeasure;

import com.khs.spcmeasure.fragments.MntSetupFragment;
import com.khs.spcmeasure.fragments.MntSetupFragment.OnSetupSelectedListener;
import com.khs.spcmeasure.PieceDialogFragment.OnNewPieceListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MntSetupActivity extends Activity implements OnSetupSelectedListener, OnNewPieceListener {

	private Long mProdId;
	
	@Override
	public void onSetupSelected(Long prodId) {
		// store the Product Id for use later
		this.mProdId = prodId;
		Log.d("DEBUG MntSetupActivity prodId = ", Long.toString(prodId));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mnt_setup);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new MntSetupFragment()).commit();
		}				
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inflate action bar menu items
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mnt_setup_activity, menu);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle action bar menu items
		switch (item.getItemId()) {
		case R.id.newPiece:
						
			// verify Product is selected
			if (mProdId == null) {
				AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);				
				dlgAlert.setTitle("Error");
				dlgAlert.setMessage("Product Id is unknown.  Please try again.");
				dlgAlert.setCancelable(false);
				dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// dismiss the dialog						
					}
				}); 				
				dlgAlert.create().show();
				return true;
			}
						
			// create dialog
			PieceDialogFragment myDialog = new PieceDialogFragment();
						
			// attach arguments
			Bundle args = new Bundle();
			args.putLong(DBAdapter.KEY_PROD_ID, mProdId);
			myDialog.setArguments(args);
			
			// show dialog
			FragmentManager fragMgr = getFragmentManager();
			myDialog.show(fragMgr, "newPiece");
			
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onNewPieceCreated(Long pieceId) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "New Piece Id = " + pieceId.toString(), Toast.LENGTH_LONG).show();
		
		if (pieceId != null) {
			Intent measIntent = new Intent(this, MeasurementListActivity.class);
			measIntent.putExtra(DBAdapter.KEY_PIECE_ID, pieceId);
			startActivity(measIntent);
		}
	}	
		
}
