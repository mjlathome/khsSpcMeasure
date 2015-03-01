package com.khs.spcmeasure;

import android.app.ActionBar;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.khs.spcmeasure.dao.FeatureDao;
import com.khs.spcmeasure.dao.PieceDao;
import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.library.AlertUtils;

import java.util.List;


public class FeatureActivity extends FragmentActivity implements ActionBar.OnNavigationListener {

    final static String TAG = "FeatureActivity";

    /// tab constants
    private static final int TAB_POS_MEASUREMENT = 0;
    private static final int TAB_POS_CHART = 1;
    private static final int TAB_POS_LIMITS = 2;

    private Long mPieceId = null;
    private Long mFeatId  = null;
    private PieceDao mPieceDao = new PieceDao(this);
    private FeatureDao mFeatDao = new FeatureDao(this);
    private Piece mPiece;
    public List<Feature> mFeatList;

    MyAdapter mAdapter;

    ViewPager mPager;

    public int mTabPos = TAB_POS_MEASUREMENT;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature);

        // extract intent arguments, if any
        getArguments(getIntent().getExtras());

        // extract saved instance state arguments, if any
        getArguments(savedInstanceState);

        // verify arguments
        if (!chkArguments()) {
            AlertUtils.errorDialogShow(this, getString(R.string.text_mess_arguments_invalid));
            finish();
        }

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        // Show the Up button in the action bar.
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[]{
                                getString(R.string.title_measurement),
                                getString(R.string.title_chart),
                                getString(R.string.title_limits),
                        }),
                this);

        // extract data
        mPiece = mPieceDao.getPiece(mPieceId);
        mFeatList = mFeatDao.getAllFeatures(mPiece.getProdId());

        // configure ViewPager
        mAdapter = new MyAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.goto_first);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(0);
            }
        });

        button = (Button)findViewById(R.id.goto_last);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(mFeatList.size() - 1);
            }
        });
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feature, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {

        // handle tab change
        Log.d(TAG, "pos = " + position + "; id = " + id);
        mTabPos = position;
        mAdapter.notifyDataSetChanged();

        return true;
    }

    // extracts arguments from provided Bundle
    private void getArguments(Bundle args) {
        // extract piece id
        if (args != null) {
            if (args.containsKey(DBAdapter.KEY_PIECE_ID)) {
                mPieceId = args.getLong(DBAdapter.KEY_PIECE_ID);
            }
            if (args.containsKey(DBAdapter.KEY_FEAT_ID)) {
                mFeatId = args.getLong(DBAdapter.KEY_FEAT_ID);
            }
        }
    }

    // checks arguments
    private boolean chkArguments() {
        // verify arguments
        return (mPieceId != null && mFeatId != null);
    }

    public class MyAdapter extends FragmentStatePagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFeatList.size();
        }

        @Override
        public Fragment getItem(int position) {
            Feature feat = mFeatList.get(position);
            Bundle args;

            switch(mTabPos) {
                case TAB_POS_MEASUREMENT:
                    // create the measurement fragment
                    args = new Bundle();
                    args.putLong(DBAdapter.KEY_PIECE_ID, mPieceId);
                    args.putLong(DBAdapter.KEY_FEAT_ID, feat.getFeatId());
                    MeasurementFragment measFrag = new MeasurementFragment();
                    measFrag.setArguments(args);
                    return measFrag;
                case TAB_POS_CHART:
                    // create the measurement fragment
                    args = new Bundle();
                    args.putLong(DBAdapter.KEY_PROD_ID, feat.getProdId());
                    args.putLong(DBAdapter.KEY_FEAT_ID, feat.getFeatId());
                    ChartFragment chartFrag = new ChartFragment();
                    chartFrag.setArguments(args);
                    return chartFrag;
                case TAB_POS_LIMITS:
                    // create the measurement fragment
                    args = new Bundle();
                    args.putLong(DBAdapter.KEY_PROD_ID, feat.getProdId());
                    args.putLong(DBAdapter.KEY_FEAT_ID, feat.getFeatId());
                    LimitsFragment limFrag = new LimitsFragment();
                    limFrag.setArguments(args);
                    return limFrag;
                default:
                    // TODO handle invalid tab position
                    return null;
            }
        }

        // override item position change in order to refresh fragments
        @Override
        public int getItemPosition(Object object) {
            // return super.getItemPosition(object);
            Log.d(TAG, "getItemPosition = " + object.toString());
            return PagerAdapter.POSITION_NONE;  // changed
        }
    }

    // TODO remove later
//    public static class ArrayListFragment extends ListFragment {
//        int mNum;
//
//        /**
//         * Create a new instance of CountingFragment, providing "num"
//         * as an argument.
//         */
//        static ArrayListFragment newInstance(int num) {
//            ArrayListFragment f = new ArrayListFragment();
//
//            // Supply num input as an argument.
//            Bundle args = new Bundle();
//            args.putInt("num", num);
//            f.setArguments(args);
//
//            return f;
//        }
//
//        /**
//         * When creating, retrieve this instance's number from its arguments.
//         */
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
//        }
//
//        /**
//         * The Fragment's UI is just a simple text view showing its
//         * instance number.
//         */
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View v = inflater.inflate(R.layout.fragment_pager_list, container, false);
//            View tv = v.findViewById(R.id.text);
//            ((TextView)tv).setText("Fragment #" + mNum);
//            return v;
//        }
//
//        @Override
//        public void onActivityCreated(Bundle savedInstanceState) {
//            super.onActivityCreated(savedInstanceState);
//            setListAdapter(new ArrayAdapter<String>(getActivity(),
//                    android.R.layout.simple_list_item_1, Cheeses.CHEESES));
//        }
//
//        @Override
//        public void onListItemClick(ListView l, View v, int position, long id) {
//            Log.i("FragmentList", "Item clicked: " + id);
//        }
//    }

}
