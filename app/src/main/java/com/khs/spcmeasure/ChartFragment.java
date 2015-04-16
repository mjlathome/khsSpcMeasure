package com.khs.spcmeasure;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.khs.spcmeasure.dao.FeatureDao;
import com.khs.spcmeasure.dao.LimitsDao;
import com.khs.spcmeasure.dao.MeasurementDao;
import com.khs.spcmeasure.dao.PieceDao;
import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Limits;
import com.khs.spcmeasure.entity.Measurement;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.DateTimeUtils;
import com.khs.spcmeasure.library.LimitType;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * {@link Fragment} subclass for rendering a chart based upon the provided arguments
 */
public class ChartFragment extends Fragment {

    final static String TAG = "ChartFragment";

    // supported chart types
    final static String CHART_TYPE = "CHART_TYPE";
    final static int CHART_TYPE_XBAR  = 0;
    final static int CHART_TYPE_RANGE = 1;

    // argument members
    private Integer mChartType = null;
    private Long mProdId = null;
    private Long mFeatId  = null;

    // chart members
    private String mChartTitle;
    private String mLabelValue;
    private String mLabelUpper;
    private String mLabelLower;
    private XYPlot plot;
    private List<Date>   mCollectDate;
    private List<Number> mSeriesValue;
    private List<Number> mSeriesUpper;
    private List<Number> mSeriesLower;

    private RenderChartTask renderTask = new RenderChartTask();

    // format chart values using 3dp
    private DecimalFormat df = new DecimalFormat("#.000");

    public ChartFragment() {
        // Required empty public constructor
    }

    // render chart nested class -  ensures work is done off the UI thread to prevent ANR
    private class RenderChartTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // get out if plot is now invalid - due to user interaction
            if (isCancelled() == true) {
                return;
            }
            // initialize our XYPlot reference:
            plot = (XYPlot) getView().findViewById(R.id.mySimpleXYPlot);
            plot.setTitle(mChartTitle);
            plot.setRangeLabel(mLabelValue);
            plot.setRangeValueFormat(df);   // format range using 3dp
            plot.getGraphWidget().getGridBackgroundPaint().setColor(getResources().getColor(R.color.chartBgColour));
            plot.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            // get out if plot is now invalid - due to user interaction
            if (isCancelled() == true) {
                return null;
            }

            try {
                // instantiate Dao's
                FeatureDao featDao = new FeatureDao(getActivity());
                LimitsDao limDao = new LimitsDao(getActivity());
                PieceDao pieceDao = new PieceDao(getActivity());
                MeasurementDao measDao = new MeasurementDao(getActivity());

                // extract data
                Feature feat = featDao.getFeature(mProdId, mFeatId);
                Limits limits = null;
                List<Piece> listPieces = pieceDao.getAllPieces(mProdId);

                switch (mChartType) {
                    case CHART_TYPE_XBAR:
                        limits = limDao.getLimit(mProdId, mFeatId, feat.getLimitRev(), LimitType.CONTROL);
                        break;
                    case CHART_TYPE_RANGE:
                        limits = limDao.getLimit(mProdId, mFeatId, feat.getLimitRev(), LimitType.RANGE);
                        break;
                }

                // reverse Piece list so it's oldest to newest
                Collections.reverse(listPieces);

                // initialize series
                mCollectDate = new ArrayList<Date>();
                mSeriesValue = new ArrayList<Number>();
                mSeriesUpper = new ArrayList<Number>();
                mSeriesLower = new ArrayList<Number>();

                // create y-value arrays to plot
                for (Piece piece : listPieces) {
                    // TODO throw away unwanted pieces based upon CollectStatus?
                    // Log.d(TAG, "Piece = " + piece.getProdId());

                    // add Piece collect date
                    mCollectDate.add(piece.getCollectDt());

                    // add value, if any
                    Measurement meas = measDao.getMeasurement(piece.getId(), piece.getProdId(), feat.getFeatId());
                    if (meas != null) {
                        Log.d(TAG, "renderChart: meas = " + meas.getValue());
                        switch (mChartType) {
                            case CHART_TYPE_XBAR:
                                mSeriesValue.add(meas.getValue());
                                break;
                            case CHART_TYPE_RANGE:
                                mSeriesValue.add(meas.getRange());
                                break;
                        }
                    } else {
                        Log.d(TAG, "renderChart: meas = null");
                        mSeriesValue.add(null);
                    }

                    // add in limits
                    mSeriesUpper.add(limits.getUpper());
                    mSeriesLower.add(limits.getLower());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // get out if plot is now invalid - due to user interaction
            if (isCancelled() == true) {
                return;
            }

            // Turn the above arrays into XYSeries':
            XYSeries seriesValue = new SimpleXYSeries(
                    mSeriesValue,
                    SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                    mLabelValue);

            // same as above
            XYSeries seriesUpper = new SimpleXYSeries(
                    mSeriesUpper,
                    SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                    mLabelUpper);

            XYSeries seriesLower = new SimpleXYSeries(
                    mSeriesLower,
                    SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                    mLabelLower);

            // Create a formatter to use for drawing a series using LineAndPointRenderer
            // and configure it from xml:
            LineAndPointFormatter series1Format = new LineAndPointFormatter();
            series1Format.setPointLabelFormatter(new PointLabelFormatter());
            series1Format.configure(getActivity().getApplicationContext(),
                    R.xml.line_point_formatter_with_plf1);

            // format series using 3dp
            series1Format.setPointLabeler(new PointLabeler() {
                @Override
                public String getLabel(XYSeries series, int index) {
                    return df.format(series.getY(index));
                }
            });

            // add a new series' to the xyplot:
            plot.addSeries(seriesValue, series1Format);

            // same as above:
            LineAndPointFormatter series2Format = new LineAndPointFormatter();
            series2Format.setPointLabelFormatter(new PointLabelFormatter());
            series2Format.configure(getActivity().getApplicationContext(),
                    R.xml.line_point_formatter_limits);
            plot.addSeries(seriesUpper, series2Format);

            LineAndPointFormatter series3Format = new LineAndPointFormatter();
            series3Format.setPointLabelFormatter(new PointLabelFormatter());
            series3Format.configure(getActivity().getApplicationContext(),
                    R.xml.line_point_formatter_limits);
            plot.addSeries(seriesLower, series3Format);

            // reduce the number of range labels
            plot.setTicksPerRangeLabel(3);
            plot.getGraphWidget().setDomainLabelOrientation(-90);

            plot.setUserRangeOrigin(0);
            plot.setDrawRangeOriginEnabled(true);

            // TODO use this to set the lowest boundary
            // plot.setRangeLowerBoundary(-2.75, BoundaryMode.FIXED);

            // TODO don't display domain labels (i.e. x-axis) - remove later
            // plot.getGraphWidget().getDomainLabelPaint().setColor(Color.TRANSPARENT);

            // label the domain (i.e. x-axis)
            plot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
            plot.setDomainValueFormat(new Format() {
                @Override
                public StringBuffer format(Object o, StringBuffer stringBuffer, FieldPosition fieldPosition) {
                    int i = ((Number) o).intValue();
                    Date date = mCollectDate.get(i);
                    return new StringBuffer(DateTimeUtils.getDateTimeStr(date, DateTimeUtils.CHART_DATETIME_FORMAT));
                }

                @Override
                public Object parseObject(String s, ParsePosition parsePosition) {
                    return null;
                }
            });

            // draw plot
            plot.redraw();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // extract intent arguments, if any
        getArguments(getArguments());

        // extract saved instance state arguments, if any
        getArguments(savedInstanceState);

        // verify arguments
        if (!chkArguments()) {
            AlertUtils.errorDialogShow(getActivity(), getString(R.string.text_mess_arguments_invalid));
            getActivity().finish();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chart,
            container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set title and labels
        switch(mChartType) {
            case CHART_TYPE_XBAR:
                mChartTitle = ChartFragment.this.getString(R.string.chart_title_xbar);
                mLabelValue = ChartFragment.this.getString(R.string.chart_label_individual_value);
                mLabelUpper = ChartFragment.this.getString(R.string.chart_label_individual_upper);
                mLabelLower = ChartFragment.this.getString(R.string.chart_label_individual_lower);
                break;
            case CHART_TYPE_RANGE:
                mChartTitle = ChartFragment.this.getString(R.string.chart_title_range);
                mLabelValue = ChartFragment.this.getString(R.string.chart_label_range_value);
                mLabelUpper = ChartFragment.this.getString(R.string.chart_label_range_upper);
                mLabelLower = ChartFragment.this.getString(R.string.chart_label_range_lower);
                break;
        }

        // draw the chart
        renderTask.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        renderTask.cancel(true);
    }

    // extracts arguments from provided Bundle
    private void getArguments(Bundle args) {
        // extract piece id
        if (args != null) {
            if (args.containsKey(CHART_TYPE)) {
                mChartType = args.getInt(CHART_TYPE, CHART_TYPE_XBAR);
            }
            if (args.containsKey(DBAdapter.KEY_PROD_ID)) {
                mProdId = args.getLong(DBAdapter.KEY_PROD_ID);
            }
            if (args.containsKey(DBAdapter.KEY_FEAT_ID)) {
                mFeatId = args.getLong(DBAdapter.KEY_FEAT_ID);
            }
        }
    }

    // checks arguments
    private boolean chkArguments() {
        // verify arguments
        return (mChartType != null && mProdId != null && mFeatId != null);
    }

}
