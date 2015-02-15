package com.khs.spcmeasure.library;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * @author Mark
 *
 */
public class ProgressUtils {
	
	// generate alert dialog with the provided title and message
	public static ProgressDialog progressDialogCreate(Context context, String message)
    {
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setMessage(message);
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);		
		return dialog;
    }	
}
