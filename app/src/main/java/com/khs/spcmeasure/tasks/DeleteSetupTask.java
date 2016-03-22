/**
 * 
 */
package com.khs.spcmeasure.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.khs.spcmeasure.helper.DBAdapter;
import com.khs.spcmeasure.R;

/**
 * @author Mark
 *
 */
public class DeleteSetupTask extends AsyncTask <Long, Void, Void>{
    private final static String TAG = "DeleteSetupTask";

	private Context mContext;

	private ProgressDialog pDialog;

    // generate task
    public static DeleteSetupTask newInstance(Context context) {
        DeleteSetupTask asyncTask = new DeleteSetupTask(context);
        return asyncTask;
    }

	// constructor
	public DeleteSetupTask(Context context) {
		super();
		this.mContext = context;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		
		// inform user it's started
		pDialog = new ProgressDialog(mContext);
		pDialog.setMessage(mContext.getString(R.string.text_deleting_setups));
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
			Log.d(TAG, "id = " + Long.toString(id));
		
			// open the DB
			DBAdapter db = new DBAdapter(mContext);
			db.open();
			
			db.deleteProduct(id);
			
			// close the DB			
			db.close();
		}
		
		Log.d(TAG, "Before close");

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		
		pDialog.dismiss();	

        if (mContext instanceof OnDeleteSetupListener) {
            OnDeleteSetupListener listener = (OnDeleteSetupListener) mContext;
            listener.onDeleteSetupPostExecute();
        }
	}

    // communication interface
    public interface OnDeleteSetupListener {
        // TODO: Update argument type and name
        public void onDeleteSetupPostExecute();
    }

}
