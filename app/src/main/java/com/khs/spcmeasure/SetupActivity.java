/**
 * 
 */
package com.khs.spcmeasure;

import com.khs.spcmeasure.fragments.MntSetupFragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.FragmentManager;

/**
 * @author Mark
 *
 */
public class SetupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		FragmentManager fm = getFragmentManager();
		
		if (fm.findFragmentById(android.R.id.content)  == null) {
			MntSetupFragment mntFrag = new MntSetupFragment();
			fm.beginTransaction().add(android.R.id.content, mntFrag).commit();
		}
		
	}	
}
