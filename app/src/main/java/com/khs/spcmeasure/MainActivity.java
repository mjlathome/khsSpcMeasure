package com.khs.spcmeasure;

import java.util.List;

import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.fragments.MntSetupFragment;
import com.khs.spcmeasure.old.ImportSetupActivity;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;



public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);	// TODO remove later
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
        // TODO DEBUG read all products currently stored
//        DBAdapter db = new DBAdapter(MainActivity.this);
//        db.open();
//        Log.d("DEBUG localProd = ", String.valueOf(db.getProductsCount()));        
//        Cursor c = db.getAllProducts();
//		// looping through all rows and adding to list
//		if (c.moveToFirst()) {
//			do {				
//				// get Product from cursor
//				Product prod = db.cursorToProduct(c);
//				String debug = "Id: " + prod.getId() + " Name: " + prod.getName();
//				Log.d("DEBUG Product = ", debug);
//			} while(c.moveToNext());
//		}
//		db.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
    
    // handle Import Setup button
    public void importSetup(View view) {
    	// run the Import Setup Activity
    	Intent intent = new Intent(this, ImportSetupActivity.class);
    	startActivity(intent);
    }
    
    // handle Import Setup button
    public void maintainSetup(View view) {
    	// run the Import Setup Activity
    	Intent intent = new Intent(this, MntSetupActivity.class);
    	startActivity(intent);    	
    }    
    
    // handle Piece button
    public void maintainPiece(View view) {
    	// run the Import Setup Activity
    	Intent intent = new Intent(this, PieceListActivity.class);
    	startActivity(intent);    	
    }     
}
