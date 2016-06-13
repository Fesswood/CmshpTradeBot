package com.github.fesswood.cmshptradebot.presentation.charts;

/**
 * Created by fesswood on 03.06.16.
 */

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.fesswood.cmshptradebot.R;
import com.github.fesswood.cmshptradebot.app.App;
import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.DbRepository;
import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticModel;
import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticObjectMapper;
import com.github.fesswood.cmshptradebot.data.db.TradingBotOnMovingTimeSeries;
import com.github.fesswood.cmshptradebot.data.event.TradeEvent;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.greenrobot.eventbus.Subscribe;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private TextView mTextView;
    private CombinedChart mStickChart;

    public PlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabbed, container, false);
        mTextView = (TextView) rootView.findViewById(R.id.section_label);
        mTextView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        mStickChart = (CombinedChart) rootView.findViewById(R.id.chart);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onStart();
        App.getBus().register(this);
    }

    @Override
    public void onPause() {
        App.getBus().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onMessageEvent(TradeEvent event) {
        mTextView.append(event.getEvent());
        DateFormat format = new SimpleDateFormat("yyyyy-mm-dd hh:mm");
        List<TradeStatisticModel> modelList = DbRepository.getAllTradeStatisticModel();
        ArrayList<String> xVals = getXVals(format, modelList);

        mStickChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.LINE
        });
        CombinedData combinedData = new CombinedData(xVals);
        combinedData.setData(getCandleEntries(modelList, xVals));
        combinedData.setData(getBarEntries(modelList, xVals));
        combinedData.setData(getLineSmaShortEntries(modelList, xVals));
        mStickChart.setData(combinedData);
        mStickChart.setBackgroundColor(Color.WHITE);

        mStickChart.setDescription("");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mStickChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mStickChart.setPinchZoom(false);

        mStickChart.setDrawGridBackground(false);

        Legend l = mStickChart.getLegend();
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        l.setWordWrapEnabled(true);

        XAxis xAxis = mStickChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(2);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = mStickChart.getAxisLeft();
//        leftAxis.setEnabled(false);
        leftAxis.setLabelCount(7, false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);

        YAxis rightAxis = mStickChart.getAxisRight();
        rightAxis.setEnabled(false);
        mStickChart.invalidate();
    }

    @NonNull
    private ArrayList<String> getXVals(DateFormat format, List<TradeStatisticModel> modelList) {
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < modelList.size(); i++) {
            xVals.add(format.format(new Date(modelList.get(i).getTimestamp())));
        }
        return xVals;
    }

    @NonNull
    private CandleData getCandleEntries(List<TradeStatisticModel> modelList, List<String> xVals) {
        ArrayList<CandleEntry> candleEntries = new ArrayList<>();
        for (int i = 0; i < modelList.size(); i++) {
            candleEntries.add(TradeStatisticObjectMapper.mapToCandleEntry(i, modelList.get(i)));
        }
        CandleDataSet candleDataSet = new CandleDataSet(candleEntries, "динамика цены");
        candleDataSet.setColor(Color.rgb(80, 80, 80));
        candleDataSet.setValueTextSize(10f);
        candleDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        return new CandleData(xVals, candleDataSet);
    }

    @NonNull
    private BarData getBarEntries(List<TradeStatisticModel> modelList, List<String> xVals) {
        ArrayList<BarEntry> candleEntries = new ArrayList<>();
        for (int i = 0; i < modelList.size(); i++) {
            candleEntries.add(TradeStatisticObjectMapper.mapToBarEntry(i, modelList.get(i)));
        }
        BarDataSet barDataSet = new BarDataSet(candleEntries, "Объем торгов");
        barDataSet.setColor(Color.rgb(60, 220, 78));
        barDataSet.setValueTextColor(Color.rgb(60, 220, 78));
        barDataSet.setValueTextSize(10f);
        barDataSet.setDrawValues(false);
        barDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        return new BarData(xVals, barDataSet);
    }

    @NonNull
    private LineData getLineSmaShortEntries(List<TradeStatisticModel> modelList, List<String> xVals) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(TradingBotOnMovingTimeSeries.initMovingTimeSeries(100));
        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
        ArrayList<Entry> lineEntryShort = new ArrayList<>();
        for (int i = 0; i < modelList.size(); i++) {
            lineEntryShort.add(new Entry((float) shortSma.getValue(i).toDouble(), i));
        }

        SMAIndicator longSma = new SMAIndicator(closePrice, 15);
        ArrayList<Entry> lineLongEntry = new ArrayList<>();
        for (int i = 0; i < modelList.size(); i++) {
            lineLongEntry.add(new Entry((float) longSma.getValue(i).toDouble(), i));
        }
        LineDataSet lineDataSetLong = new LineDataSet(lineLongEntry, "SMA 15 индикатор");
        lineDataSetLong.setColor(Color.rgb(50, 238, 50));
        lineDataSetLong.setLineWidth(1.5f);
        lineDataSetLong.setCircleColor(Color.rgb(240, 238, 70));
        lineDataSetLong.setCircleRadius(1f);
        lineDataSetLong.setFillColor(Color.rgb(240, 238, 70));
        lineDataSetLong.setDrawValues(false);
        lineDataSetLong.setValueTextSize(4f);
        lineDataSetLong.setValueTextColor(Color.rgb(240, 238, 70));
        lineDataSetLong.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineDataSet lineDataSetShort = new LineDataSet(lineEntryShort, "SMA 5 индикатор");
        lineDataSetShort.setColor(Color.rgb(240, 90, 70));
        lineDataSetShort.setLineWidth(2.5f);
        lineDataSetShort.setCircleColor(Color.rgb(240, 238, 70));
        lineDataSetShort.setCircleRadius(1f);
        lineDataSetShort.setFillColor(Color.rgb(240, 238, 70));
        lineDataSetShort.setDrawValues(false);
        lineDataSetShort.setValueTextSize(4f);
        lineDataSetShort.setValueTextColor(Color.rgb(240, 238, 70));

        lineDataSetShort.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineData lineData = new LineData(xVals);
        lineData.addDataSet(lineDataSetShort);
        lineData.addDataSet(lineDataSetLong);
        return lineData;
    }

}