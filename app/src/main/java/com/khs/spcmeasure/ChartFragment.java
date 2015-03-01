package com.khs.spcmeasure;


import android.graphics.Color;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.khs.spcmeasure.dao.FeatureDao;
import com.khs.spcmeasure.dao.LimitsDao;
import com.khs.spcmeasure.dao.MeasurementDao;
import com.khs.spcmeasure.dao.PieceDao;
import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Limits;
import com.khs.spcmeasure.entity.Measurement;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.LimitType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChartFragment extends Fragment {

    final static String TAG = "ChartFragment";

    private Long mProdId = null;
    private Long mFeatId  = null;

    private XYPlot plot;

    public ChartFragment() {
        // Required empty public constructor
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

        // draw the chart
        renderChart();
    }

    // extracts arguments from provided Bundle
    private void getArguments(Bundle args) {
        // extract piece id
        if (args != null) {
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
        return (mProdId != null && mFeatId != null);
    }

    private void renderChart() {
        // initialize our XYPlot reference:
        plot = (XYPlot) getView().findViewById(R.id.mySimpleXYPlot);

        // instantiate Dao's
        FeatureDao featDao = new FeatureDao(getActivity());
        LimitsDao limDao = new LimitsDao(getActivity());
        PieceDao pieceDao = new PieceDao(getActivity());
        MeasurementDao measDao = new MeasurementDao(getActivity());

        // create y-value arrays to plot
        Feature feat = featDao.getFeature(mProdId, mFeatId);
        Limits limits = limDao.getLimit(mProdId, mFeatId, feat.getLimitRev(), LimitType.CONTROL);
        List<Piece> listPieces = pieceDao.getAllPieces(mProdId);

        // initialize series
        List<Number> seriesVal = new ArrayList<Number>();
        List<Number> seriesUcl = new ArrayList<Number>();
        List<Number> seriesLcl = new ArrayList<Number>();

        for (Piece piece : listPieces) {
            // TODO throw away unwanted pieces based upon CollectStatus?
            // Log.d(TAG, "Piece = " + piece.getProdId());

            // add measured value, if any
            Measurement meas = measDao.getMeasurement(piece.getId(), piece.getProdId(), feat.getFeatId());
            if (meas != null) {
                seriesVal.add(meas.getValue());
            } else {
                seriesVal.add(null);
            }

            // add in limits
            seriesUcl.add(limits.getUpper());
            seriesLcl.add(limits.getLower());
        }

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                // Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                seriesVal,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "X");

        // same as above
        XYSeries series2 = new SimpleXYSeries(
                // Arrays.asList(series2Numbers),
                seriesUcl,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "UCL");

        XYSeries series3 = new SimpleXYSeries(
                // Arrays.asList(series3Numbers),
                seriesLcl,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "LCL");

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getActivity().getApplicationContext(),
                R.xml.line_point_formatter_with_plf1);

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        // same as above:
        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getActivity().getApplicationContext(),
                R.xml.line_point_formatter_limits);
        plot.addSeries(series2, series2Format);

        LineAndPointFormatter series3Format = new LineAndPointFormatter();
        series3Format.setPointLabelFormatter(new PointLabelFormatter());
        series3Format.configure(getActivity().getApplicationContext(),
                R.xml.line_point_formatter_limits);
        plot.addSeries(series3, series3Format);

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);

        plot.setUserRangeOrigin(0);
        plot.setDrawRangeOriginEnabled(true);

        // TODO use this to set the lowest boundary
        // plot.setRangeLowerBoundary(-2.75, BoundaryMode.FIXED);
        plot.getGraphWidget().getDomainLabelPaint().setColor(Color.TRANSPARENT);
    }
}
