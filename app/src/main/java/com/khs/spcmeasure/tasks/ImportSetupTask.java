package com.khs.spcmeasure.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.khs.spcmeasure.helper.DBAdapter;
import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Limits;
import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.library.JSONParser;
import com.khs.spcmeasure.library.LimitType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImportSetupTask extends AsyncTask<Long, String, JSONObject>{
    private static final String TAG = "ImportSetupTask";

	private Context mContext;
	
	// private static String url = "http://192.168.0.111/karmax/spc/getSetupByProdId.php?prodId=";
	// private static String url = "http://thor.kmx.cosma.com/spc/getSetupByProdId.php?prodId=";
	private static String url = "http://thor.kmx.cosma.com/spc/get_setup.php?prodId=";
    // private static String url = "http://10.35.33.58/spc/get_setup.php?prodId=";

	//JSON Node Names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_SETUP = "setup";
	private static final String TAG_PRODUCT = "product";
	private static final String TAG_FEATURE = "feature";
	private static final String TAG_LIMIT = "limit";
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
	private static final String TAG_ACTIVE = "active";
	private static final String TAG_CUSTOMER = "customer";
	private static final String TAG_PROGRAM = "program";
    private static final String TAG_LIMIT_REV = "limitRev";
    private static final String TAG_LIMIT_TYPE = "type";
    private static final String TAG_UPPER = "upper";
    private static final String TAG_LOWER = "lower";	
		
	JSONArray android = null;
		
	// private long fakeId;	// TODO remove later
	
	// constructor
	public ImportSetupTask(Context context) {
		mContext = context;
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

	@Override
	protected JSONObject doInBackground(Long... params) {
		// TODO Auto-generated method stub
		long prodId = params[0]; 
		// fakeId = prodId;  // TODO remove later

        Log.d(TAG, "DEBUG ImportSetup = " + String.valueOf(prodId));

		JSONParser jParser = new JSONParser();
		
		// get JSON from URL
		JSONObject json = jParser.getJSONFromUrl(url + String.valueOf(prodId));
		
		return json;
	}

	@Override
	protected void onPostExecute(JSONObject json) {
		// TODO Auto-generated method stub
		super.onPostExecute(json);
		
		Log.d(TAG, "DEBUG JSON3 = " + json.toString());
		
		try {
			
			// get JSON Array from URL
			JSONObject jSetup = json.getJSONObject(TAG_SETUP);
			JSONObject jProduct = jSetup.getJSONObject(TAG_PRODUCT);

			// open the DB
			DBAdapter db = new DBAdapter(mContext);
			db.open();
			
			// android = json.getJSONArray(TAG_SETUP);
			// JSONObject c = android.getJSONObject(0);	
			
			// TODO write helper for json to array of products???
			// store JSON items as variables
			
			// extract Product fields from json data
			long prodId = Long.valueOf(jProduct.getString(TAG_ID));
			String name = jProduct.getString(TAG_NAME);
			
			// TODO remove fake stuff later
			// prodId = fakeId;
			// name = "Product_ID_" + Long.toString(fakeId);
			
			boolean active = Boolean.valueOf(jProduct.getString(TAG_ACTIVE));
			String customer = jProduct.getString(TAG_CUSTOMER);
			String program = jProduct.getString(TAG_PROGRAM);
			
			Log.d("DEBUG id = ", Long.toString(prodId));

			// TODO remove all existing data for Product first??? and then re-import or have to check differences and remove???
			// create Product
			Product product = new Product(prodId, name, active, customer, program);

			// update or insert Product into the DB
			if (db.updateProduct(product) == false) {
				db.createProduct(product);
			}
						
			// create Features
			JSONArray jFeatureArr = jProduct.getJSONArray(TAG_FEATURE);
			for(int i = 0; i < jFeatureArr.length(); i++) {
				
				JSONObject jFeature = jFeatureArr.getJSONObject(i);
				
				// extract Feature fields from json data
				long featId = Long.valueOf(jFeature.getString(TAG_ID));
				name = jFeature.getString(TAG_NAME);
				active = Boolean.valueOf(jFeature.getString(TAG_ACTIVE));
				long limitRev = Long.valueOf(jFeature.getString(TAG_LIMIT_REV));
				
				// create the Feature object
				Feature feature = new Feature(product.getId(), featId, name, active, limitRev, 0.0, 0.0);
				Log.d("DEBUG feat id;name = ", Long.toString(featId) + "; " + name);

				// update or insert Feature into the DB
				if (db.updateFeature(feature) == false) {
					db.createFeature(feature);
				}
				
				// create Limits
				JSONArray jLimitArr = jFeature.getJSONArray(TAG_LIMIT);
				for(int j = 0; j < jLimitArr.length(); j++) {
					
					JSONObject jLimit = jLimitArr.getJSONObject(j);
					
					// extract Limit fields from json data
					String limitType = jLimit.getString(TAG_LIMIT_TYPE);
					double upper 	 = jLimit.getDouble(TAG_UPPER);
					double lower     = jLimit.getDouble(TAG_LOWER);
					
					Log.d("DEBUG limit type; upper; lower; LimitType = ", 
							limitType + "; " + upper + "; " + lower + "; " + LimitType.fromValue(limitType).getValue() );
										
					// create the Limit object
					Limits limit = new Limits(product.getId(), feature.getFeatId(), limitRev, LimitType.fromValue(limitType), upper, lower);
					Log.d("DEBUG limit type; upper; lower = ", limitType + "; " + upper + "; " + lower);

					// update or insert Limit into the DB					
					if (db.updateLimit(limit) == false) {
						db.createLimit(limit);
					}																	
					
				}  // create Limits
				
			}  // create Features					
			
			// close the DB			
			db.close();
			
			Toast.makeText(mContext, product.getName() + " import complete", Toast.LENGTH_LONG).show();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
	}	
}
