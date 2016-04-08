package com.khs.spcmeasure.helper;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.khs.spcmeasure.Globals;
import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.library.JSONParser;
import com.khs.spcmeasure.library.NetworkUtils;
import com.khs.spcmeasure.library.VersionUtils;
import com.khs.spcmeasure.receiver.VersionReceiver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductLoader extends AsyncTaskLoader<List<Product>>{

	private static final String TAG = "ProductLoader";

	// URL to get JSON Array
	// TODO why hardcoded IP?
	// private static String url = "http://192.168.0.111/karmax/spc/getAllProducts.php";	
	private static String url = "http://thor.kmx.cosma.com/spc/get_products.php?";

	// callee context
	private Context mContext;

	//JSON Node Names
	// TODO relocate these later on
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PRODUCT = "product";
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
	private static final String TAG_PROD_NAME = "prodName";	
	
	public ProductLoader(Context context) {
		super(context);

		mContext = context;
	}
	
    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */	
	@Override
	public List<Product> loadInBackground() {
		
		Log.d(TAG, "loadInBackground - start");

		// get global vars
		Globals g = Globals.getInstance();

		// check version and wifi.  exit if in error
		if (!g.isVersionOk() || !NetworkUtils.isWiFi(mContext)) {
			return null;
		}

		JSONParser jParser = new JSONParser();
		JSONArray jArray = null;

		Log.d(TAG, "before getJSONFromURL");

		// get JSON from URL
		JSONObject json = jParser.getJSONFromUrl(url + VersionUtils.getUrlQuery(mContext));

		Log.d(TAG, "after getJSONFromURL");

		Log.d(TAG, "json - " + json);
		if (json == null) {
			return null;
		}
				
		// TODO handle null json return?
		
		try {
			boolean versionOk = json.getBoolean(VersionUtils.TAG_VERSION_OK);

			// update version global
			g.setVersionOk(versionOk);

			if (!versionOk) {
				// broadcast version failure
				VersionReceiver.sendBroadcast(mContext);
			} else {
				// get JSON Array from URL
				jArray = json.getJSONArray(TAG_PRODUCT);

				// create array to hold the Products
				List<Product> prodList = new ArrayList<Product>(jArray.length());

				for (int i = 0; i < jArray.length(); i++) {
					JSONObject jProduct = jArray.getJSONObject(i);

					// store JSON items as variables
					long id = jProduct.getLong(TAG_ID);
					String name = jProduct.getString(TAG_NAME);

					// create Product and add to the list
					// TODO assume all returned are active?
					Product prod = new Product(id, name, true);
					prodList.add(prod);
				}

				// TODO perform any sort here... assume sorted already
				Log.d(TAG, "loadInBackground - end");
				// done
				return prodList;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
