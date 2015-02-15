package com.khs.spcmeasure.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {
	
	private static final String TAG = "JSONParser";
	
	// was TODO:
//	static InputStream is = null;
//	static JSONObject jObj = null;
//	static String json = "";
	
	// constructor
	public JSONParser() {		
	}
	
	public JSONObject getJSONFromUrl(String url) {		
		JSONObject jObj = null;
		
		// http request
		try {
			// make http GET request
			DefaultHttpClient httpClient = new DefaultHttpClient();
			// was: HttpPost httpPost = new HttpPost(url);
			HttpGet httpGet = new HttpGet(url);		
			HttpResponse httpResponse = httpClient.execute( /* was: httpPost */ httpGet);
			Log.d(TAG, "httpResp - status = " +  httpResponse.getStatusLine());
								
			jObj = getJSONFromResponse(httpResponse);

			// TODO was:
			// extract json string from response, if valid
//			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//				HttpEntity httpEntity = httpResponse.getEntity();
//				Log.d(TAG, "httpEnt - length = " +  httpEntity.getContentLength());			
//				// Log.d(TAG, "httpEnt - string = " +  EntityUtils.toString(httpEntity, HTTP.UTF_8));
//				json = EntityUtils.toString(httpEntity, HTTP.UTF_8);
//						
//				// is = httpEntity.getContent();
//			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// convert response to json string
		// TODO was:
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(is, HTTP.UTF_8 /* was: , "ios-8859-1" */), 8);
//			StringBuilder sb = new StringBuilder();
//			String line = null;
//			while ((line = reader.readLine()) != null) {
//				sb.append(line + "\n");
//			}
//			is.close();
//			json = sb.toString();
//		} catch (Exception e) {
//			Log.e("Buffer Error", "Error converting result " + e.toString());
//		}
		
		// TODO was:
//		// try parse the json string to a json object
//		try {
//			jObj = new JSONObject(json);			
//		} catch (JSONException e) {
//			Log.e("JSON Parser", "Error parsing data " + e.toString());
//		}

		return jObj;	
	}
	
	public JSONObject getJSONFromUrl(String url, String body) {
		JSONObject jObj = null;
		
		Log.d(TAG, "body = " + body);
		
		// make http POST request with String body
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			
			HttpPost httpPost = new HttpPost(url);
			// TODO hard code this here... maybe method name should be changed
			httpPost.addHeader(new BasicHeader("Content-Type", "application/json"));
			httpPost.setEntity(new StringEntity(body));
			
			HttpResponse httpResponse = httpClient.execute(httpPost);			
			jObj = getJSONFromResponse(httpResponse);			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return jObj;		
	}	
	
	// extracts json from http response 
	private JSONObject getJSONFromResponse(HttpResponse httpResp) {
		JSONObject jObj = null;
		
		// extract json from response body
		try {
			String jStr = "";
			if (httpResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity httpEntity = httpResp.getEntity();
				Log.d(TAG, "httpEnt - length = " +  httpEntity.getContentLength());			
				jStr = EntityUtils.toString(httpEntity, HTTP.UTF_8);										
				jObj = new JSONObject(jStr);
			}					
		} catch (IOException e) {
				e.printStackTrace();
		} catch (JSONException e) {
			Log.e(TAG, "getJSONFromResponse - Error parsing data " + e.toString());
		}
		
		return jObj;
	}
	
}

