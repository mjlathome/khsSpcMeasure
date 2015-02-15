package com.khs.spcmeasure;

import com.khs.spcmeasure.library.AlertUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class ErrorDialogActivity extends Activity{

	public static final String EXTRA_MESSAGE = "MESSAGE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		String message = "An error occurred.  Contact administrator.";
		
		// extract error message from starting intent
		Intent intent = getIntent();
		if (intent.hasExtra(EXTRA_MESSAGE)) {
			message = intent.getStringExtra(EXTRA_MESSAGE);
		}
		
		AlertDialog.Builder dlgAlert = AlertUtils.createAlert(this, message);
		dlgAlert.setCancelable(false);
		
		dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// dismiss the dialog
				try {
					ErrorDialogActivity.this.finalize();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}); 				
		
		return;
	}
	
}
