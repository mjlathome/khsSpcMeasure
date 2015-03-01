package com.khs.spcmeasure.old;

import java.util.ArrayList;
import java.util.HashMap;

import com.khs.spcmeasure.R;
import com.khs.spcmeasure.library.JSONParser;
import com.khs.spcmeasure.tasks.ImportSetupTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ImportSetupActivity extends Activity {
	ListView list;
	TextView marker;
	TextView prodName;
	ListAdapter adapter;
	ArrayList<HashMap<String, String>> setupList = new ArrayList<HashMap<String,String>>();
	
	// URL to get JSON Array
	// TODO why hardcoded IP?
	private static String url = "http://192.168.0.111/karmax/spc/getAllProducts.php";
	
	//JSON Node Names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PRODUCT = "product";
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
	private static final String TAG_PROD_NAME = "prodName";
	
	JSONArray android = null;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup_import);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		new JSONParse().execute();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_import_setup, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {


		
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_setup_import,
					container, false);			
			return rootView;
		}
	}
	
	private class JSONParse extends AsyncTask<String, String, JSONObject> {
		private ProgressDialog pDialog;		
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			// extract layout fields
			marker = (TextView)findViewById(R.id.marker);
			prodName = (TextView)findViewById(R.id.prodName);
			
			// inform user it's started
			pDialog = new ProgressDialog(ImportSetupActivity.this);
			pDialog.setMessage("Getting Data ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			JSONParser jParser = new JSONParser();
			
			// get JSON from URL
			JSONObject json = jParser.getJSONFromUrl(url);
			
			Log.d("JSON", json.toString());
			
			return json;
		}

		@Override
		protected void onPostExecute(JSONObject json) {			
			super.onPostExecute(json);
			
			Log.d("JSONa", json.toString());
			
			pDialog.dismiss();
			try {
				// get JSON Array from URL
				android = json.getJSONArray(TAG_PRODUCT);
				
				for (int i = 0; i < android.length(); i++) {
					JSONObject c = android.getJSONObject(i);
					
					// store JSON items as variables
					String id = c.getString(TAG_ID);
					String name = c.getString(TAG_NAME);
					
					// create HashMap key => value
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(TAG_ID, id);
					map.put(TAG_PROD_NAME, name);
															
					setupList.add(map);					
				}
				
				Log.d("Debug len setupList = ", String.valueOf(setupList.size()));
				
				// associate ListView with the HashMap
				list = (ListView)findViewById(R.id.lstSetup);				
				adapter = new SimpleAdapter(ImportSetupActivity.this, setupList, 
						R.layout.list_setup, 
						new String[] {TAG_ID, TAG_PROD_NAME}, 
						new int[] {R.id.marker, R.id.prodName});
				
				Log.d("Debug len setupList 1= ", String.valueOf(setupList.size()));
				
				list.setAdapter(adapter);
				
				Log.d("Debug len setupList 2= ", String.valueOf(setupList.size()));
				
				list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO was:
						//Toast.makeText(SetupImportActivity.this,
							//	"You Clicked at " + setupList.get(+position).get(TAG_PROD_NAME) + 
							//	" id = " + setupList.get(+position).get(TAG_PROD_ID), Toast.LENGTH_SHORT).show();
						//		
						final long prodId = Long.valueOf(setupList.get(+position).get(TAG_ID));
						
						AlertDialog.Builder aBuilder = new AlertDialog.Builder(ImportSetupActivity.this);					
						aBuilder.setTitle("Import Setup");
						aBuilder.setMessage("Do you want to import:\n" + setupList.get(+position).get(TAG_PROD_NAME));
						// TODO aDialog.setIcon
						aBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Log.d("DEBUG ADialog id = ", String.valueOf(prodId));	
								new ImportSetupTask(ImportSetupActivity.this).execute(prodId);
								dialog.cancel();									
							}
						});
						aBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();									
							}
						});	
						
						AlertDialog aDialog = aBuilder.create();
						aDialog.show();
					}
					
				});
				
												
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}		
	}	
}
