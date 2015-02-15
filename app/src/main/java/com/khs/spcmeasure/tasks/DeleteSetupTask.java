/**
 * 
 */
package com.khs.spcmeasure.tasks;

import java.io.ObjectInputStream.GetField;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.fragments.MntSetupFragment;
import com.khs.spcmeasure.old.ImportSetupActivity;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

/**
 * @author Mark
 *
 */
public class DeleteSetupTask extends AsyncTask <Long, Void, Void>{
	private Context mContext;
	private MntSetupFragment mMntSetupFrag;
	
	private ProgressDialog pDialog;	
	
	// constructor
	public DeleteSetupTask(Context context, MntSetupFragment mntSetupfrag) {
		super();
		this.mContext = context;
		this.mMntSetupFrag = mntSetupfrag;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		
		// inform user it's started
		pDialog = new ProgressDialog(mContext);
		pDialog.setMessage("Deleting Setup(s) ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(true);
		pDialog.show();
		
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}

	@Override
	protected Void doInBackground(Long... params) {
				
		// TODO Auto-generated method stub
		for (Long id: params) {				
			Log.d("DELETE Task = ", Long.toString(id));
		
			// open the DB
			DBAdapter db = new DBAdapter(mContext);
			db.open();
			
			db.deleteProduct(id);
			
			// close the DB			
			db.close();
			
		}
		
		Log.d("DELETE B4 CLOSE = ", "Hi");
		
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		
		pDialog.dismiss();	
		
		mMntSetupFrag.refeshList();				
	}	
	
}
