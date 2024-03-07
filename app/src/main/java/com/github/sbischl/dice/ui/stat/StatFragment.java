package com.github.sbischl.dice.ui.stat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.sbischl.dice.Dice;
import com.github.sbischl.dice.FrequencyDatapoint;
import com.github.sbischl.dice.R;

import java.util.ArrayList;

public class StatFragment extends Fragment{

    private SharedPreferences persistenDict;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stats, container, false);
        persistenDict = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        final LinearLayout statsLayout = root.findViewById(R.id.stats_layout);
        drawGraphs(statsLayout);



        //Add reset statistics Button:
        final Button resetStats = (Button) inflater.inflate(R.layout.button_resetstats, statsLayout, false);
        statsLayout.addView(resetStats);
        resetStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dice dice = new Dice(persistenDict, getContext());
                dice.resetDictKeys();
                statsLayout.removeAllViews();
                drawGraphs(statsLayout);
                statsLayout.addView(resetStats);
            }
        });


        return root;
    }

    public void drawGraphs(LinearLayout statsLayout) {
        addGraph(statsLayout, getResources().getString(R.string.overall_text), -1);

        if (persistenDict.getBoolean("game_mode", false)) {
            for (int i = 0; i < persistenDict.getInt("number_of_players", 0); i++) {
                addGraph(statsLayout, persistenDict.getString("playername_" + i, "Null") + ":", i);
            }
        }
    }

    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getResources().getString(R.string.stats_subtitle));
    }

    public void addGraph(LinearLayout statsLayout, String headlineText, int player) {
        // use player -1 to display the overall statistics
        //Add headline first:
        TextView headline = (TextView) getLayoutInflater().inflate(R.layout.chart_headline, statsLayout, false);
        headline.setText(headlineText);
        statsLayout.addView(headline);

        LinearLayout headlineLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.linearlayout_headline, statsLayout, false);
        statsLayout.addView(headlineLayout);

        View line = getLayoutInflater().inflate(R.layout.view_line, headlineLayout, false);
        headlineLayout.addView(line);

        CombinedChart chart = (CombinedChart) getLayoutInflater().inflate(R.layout.chart_frequency, statsLayout, false);
        statsLayout.addView(chart);

        CombinedData data = new CombinedData();
        ArrayList<FrequencyDatapoint> datapoints;
        if (player == -1) {
            datapoints = readStats();
        }
        else {
            datapoints = readStats(player);
        }
        data.setData(generateBarEntries(datapoints));
        data.setData(generateLineEntries(datapoints));

        chart.setData(data);
        chart.getDescription().setEnabled(true);
        chart.getDescription().setText("n = " + countDraws(datapoints));
        chart.getDescription().setTextSize(14f);
        chart.getDescription().setYOffset(-32f);

        chart.animateY(700);
        chart.setTouchEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        if (datapoints.size() <= 11) {
            xAxis.setLabelCount(datapoints.size());
        }

        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextSize(14f);
        xAxis.setSpaceMin(data.getBarData().getBarWidth() / 2f);
        xAxis.setSpaceMax(data.getBarData().getBarWidth() / 2f);
        //xAxis.setAxisMaximum((data.getBarData().getBarWidth() + xAxis.getSpaceMin()) * 6);
        //xAxis.setCenterAxisLabels(true);
        //xAxis.setAvoidFirstLastClipping(true);



        chart.getAxisRight().setEnabled(false);
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawAxisLine(false);
        yAxis.setTextSize(14f);
        yAxis.mAxisMinimum = 0f;
        yAxis.setGranularity(1f);

        Legend legend = chart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setTextSize(14f);

        chart.invalidate();
    }

    public BarData generateBarEntries(ArrayList<FrequencyDatapoint> datapoints) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (FrequencyDatapoint datapoint : datapoints) {
            entries.add(new BarEntry(datapoint.getName(), datapoint.getActualCount()));
        }
        BarDataSet barDataSet = new BarDataSet(entries, getResources().getString(R.string.bar_label));
        barDataSet.setColors(getResources().getColor(R.color.colorPrimary));

        BarData barData =  new BarData(barDataSet);
        barData.setDrawValues(false);
        return barData;
    }

    public LineData generateLineEntries(ArrayList<FrequencyDatapoint> datapoints) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (FrequencyDatapoint datapoint : datapoints) {
            entries.add(new Entry(datapoint.getName(), (float)datapoint.getExpectedCount()));
        }
        LineDataSet lineDataSet = new LineDataSet (entries, getResources().getString(R.string.line_label));
        lineDataSet.setLineWidth(3f);
        lineDataSet.setColor(Color.DKGRAY);
        lineDataSet.setCircleColor(Color.DKGRAY);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setCircleHoleColor(Color.DKGRAY);

        LineData lineData = new LineData(lineDataSet);
        lineData.setDrawValues(false);
        return lineData;
    }

    public ArrayList<FrequencyDatapoint> readStats() {
        ArrayList<FrequencyDatapoint> datapoints = new ArrayList<>();
        int counter = 0;
        while(persistenDict.contains("side_count_" + counter)) {
                int actualCount = persistenDict.getInt("side_count_" + counter, -1);
                double expectedCount = (double)persistenDict.getFloat("side_expected_count_" + counter, -1);
                int name = Integer.parseInt( persistenDict.getString("side_name_" + counter,"-1"));
                datapoints.add(new FrequencyDatapoint(actualCount, expectedCount, name));
                counter++;
        }
        return datapoints;
    }

    public ArrayList<FrequencyDatapoint> readStats(int player) {
        ArrayList<FrequencyDatapoint> datapoints = new ArrayList<>();
        int counter = 0;
        while(persistenDict.contains("side_count_" + counter)) {
            int actualCount = persistenDict.getInt("player" + player + "_side_count_" + counter, -1);
            double expectedCount = (double)persistenDict.getFloat("player" + player + "_side_expected_count_" + counter, -1);
            int name = Integer.parseInt( persistenDict.getString("side_name_" + counter,"-1"));
            datapoints.add(new FrequencyDatapoint(actualCount, expectedCount, name));
            counter++;
        }
        return datapoints;
    }

    public int countDraws(ArrayList<FrequencyDatapoint> data) {
        int counter = 0;
        for (FrequencyDatapoint datapoint : data) {
            counter += datapoint.getActualCount();
        }
        return counter;
    }

}
