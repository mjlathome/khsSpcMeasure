package com.khs.spcmeasure.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.khs.spcmeasure.R;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.helper.DBAdapter;
import com.khs.spcmeasure.library.SecurityUtils;

// TODO handle Action Bar menu Up button - see Stack Overflow

public class FeatureReviewActivity extends Activity implements
        FeatureReviewFragment.OnFragmentInteractionListener {

    private static final String TAG = "FeatureReviewActivity";

    private Long mPieceId = null;

    @Override
    public void onFragmentInteraction(long featId) {
        Log.d(TAG, "featId = " + featId);
        selectFeature(featId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_review);

        // show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // extract piece id from intent; exit if not found
        Bundle args = getIntent().getExtras();
        if (args != null && args.containsKey(DBAdapter.KEY_PIECE_ID)) {
            mPieceId = args.getLong(DBAdapter.KEY_PIECE_ID);
        } else {
            AlertUtils.errorDialogShow(this, getString(R.string.text_mess_piece_id_invalid));
            finish();
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            FeatureReviewFragment fragment = FeatureReviewFragment.newInstance(mPieceId);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_feature_review, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                Log.d(TAG, "Home");
                return true;
            case R.id.action_login:
                Log.d(TAG, "Menu: Login");
                // show login screen
                Intent intentLogin = new Intent(this, LoginActivity.class);
                startActivity(intentLogin);
                return true;
            case R.id.action_logout:
                Log.d(TAG, "Menu: Logout");
                // set logged out
                SecurityUtils.doLogout(this);
                return true;
            case R.id.action_settings:
                Log.d(TAG, "Menu: Settings");
                // change preferences
                Intent intentPrefs = new Intent(this, SettingsActivity.class);
                startActivity(intentPrefs);
                return true;
            case R.id.action_about:
                // about activity
                Log.d(TAG, "Menu: About");
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // starts the Feature activity
    private void selectFeature(long featId) {

        // launch activity
        if (featId == ListView.INVALID_POSITION) {
            AlertUtils.errorDialogShow(this, getString(R.string.text_mess_feature_not_selected));
        } else {
            Intent featIntent = new Intent(this, FeatureActivity.class);
            featIntent.putExtra(DBAdapter.KEY_PIECE_ID, mPieceId);
            featIntent.putExtra(DBAdapter.KEY_FEAT_ID, featId);
            startActivity(featIntent);
        }

        return;
    }

}
