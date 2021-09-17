package mathieu.com.ui.notifications;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import mathieu.com.R;

public class StatsFragment extends Fragment {

    private CombinedChart chart;

    private static final String TAG = StatsFragment.class.getSimpleName();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_stats, container, false);

        chart = root.findViewById(R.id.chart1);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setHighlightFullBarEnabled(false);

        // draw bars behind lines
        chart.setDrawOrder(new DrawOrder[]{
                DrawOrder.BAR, DrawOrder.LINE
        });


        Legend l = chart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);


        ValueFormatter myFormatter1 = new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd/MM");

            @Override
            public String getAxisLabel(float value, AxisBase axis) {

                long millis = TimeUnit.DAYS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }

        };

        ValueFormatter myFormatter2 = new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm");

            @Override
            public String getAxisLabel(float value, AxisBase axis) {

                long millis = TimeUnit.MINUTES.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }

            @Override
            public String getPointLabel(Entry entry){

                long millis = TimeUnit.MINUTES.toMillis((long) entry.getY());
                return mFormat.format(new Date(millis));
            }

            @Override
            public String getBarLabel(BarEntry entry){

                long millis = TimeUnit.MINUTES.toMillis((long) entry.getY());
                return mFormat.format(new Date(millis));
            }

        };

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(7);
        rightAxis.setValueFormatter(myFormatter2);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setLabelCount(7);
        leftAxis.setValueFormatter(myFormatter2);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(myFormatter1);

        CombinedData data = new CombinedData();

        ArrayList<Object> genData = generateData();
        LineData lData = (LineData) genData.get(0);
        lData.setValueFormatter(myFormatter2);
        BarData bData = (BarData) genData.get(1);
        bData.setValueFormatter(myFormatter2);

        data.setData(lData);
        data.setData(bData);

        leftAxis.setAxisMinimum(lData.getYMin()-200);
        leftAxis.setAxisMaximum(lData.getYMax() + 60);
        rightAxis.setAxisMinimum(bData.getYMin()-60);
        rightAxis.setAxisMaximum(bData.getYMax()+60);
        xAxis.setAxisMaximum(data.getXMax() + 1f);
        xAxis.setAxisMinimum(data.getXMin() - 1f);

        chart.setData(data);
        chart.invalidate();

        return root;
        }


    private ArrayList<Object> generateData() {

        LineData lineData = new LineData();

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<BarEntry> entriesBar = new ArrayList<>();

        File path = new File(Environment.getExternalStorageDirectory()+File.separator+"SleepData"+File.separator+"Data.txt");
        try {
            Scanner myReader = new Scanner(path);
            int i = 0;
            while (myReader.hasNextLine() & i!=7) {
                String dataString = myReader.nextLine();
                List<String> dataStringList = Arrays.asList(dataString.split("\\s+"));

                String stringDate = dataStringList.get(0);
                String stringHour1 = dataStringList.get(1) + dataStringList.get(2);
                String stringHour2 = dataStringList.get(3) + dataStringList.get(4);

                SimpleDateFormat sdFormat = new SimpleDateFormat("HHmm");
                SimpleDateFormat sdFormat2 = new SimpleDateFormat("yyyy_MM_dd");

                Date entryDate = sdFormat2.parse(stringDate);
                Calendar calEntryDate = Calendar.getInstance();
                calEntryDate.setTime(entryDate);

                Date bedTimeHour = sdFormat.parse(stringHour1);
                Calendar calBedTimeHour = Calendar.getInstance();
                calBedTimeHour.setTime(bedTimeHour);

                Date sleepTime = sdFormat.parse(stringHour2);
                Calendar calSleepTime = Calendar.getInstance();
                calSleepTime.setTime(sleepTime);

                Log.d(TAG, "calBedTimeHour" + calBedTimeHour.get(Calendar.HOUR_OF_DAY));


                if (calBedTimeHour.get(Calendar.HOUR_OF_DAY)<12){
                    calBedTimeHour.add(Calendar.DAY_OF_MONTH,1);
                }

                long unixBedTimeHour = calBedTimeHour.getTimeInMillis();
                long unixSleepTime = calSleepTime.getTimeInMillis();
                long unixEntryDate = calEntryDate.getTimeInMillis();
                Log.d(TAG, "unixBedTimeHour" + unixBedTimeHour);
                Log.d(TAG, "unixSleepTime" + unixSleepTime);
                Log.d(TAG, "unixEntryDate" + unixEntryDate);

                float entryDateDays = TimeUnit.MILLISECONDS.toDays(unixEntryDate);
                float unixBTHour = TimeUnit.MILLISECONDS.toMinutes(unixBedTimeHour);
                float unixSTHour = TimeUnit.MILLISECONDS.toMinutes(unixSleepTime);


                entries.add(0,new Entry(entryDateDays, unixBTHour));
                entriesBar.add(0,new BarEntry(entryDateDays, unixSTHour));
                i ++;

            }
            myReader.close();
        } catch (FileNotFoundException | ParseException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        Log.d(TAG, "entriesBar" + entriesBar);
        Log.d(TAG, "entries" + entries);

        BarDataSet set1 = new BarDataSet(entriesBar, "Time of sleep");
        set1.setColor(ContextCompat.getColor(getContext(),R.color.primaryDarkColor));
        set1.setValueTextColor(ContextCompat.getColor(getContext(),R.color.secondaryTextColor));
        set1.setValueTextSize(10f);
        set1.setAxisDependency(YAxis.AxisDependency.RIGHT);

        LineDataSet set = new LineDataSet(entries, "Evening bed time");
        set.setColor(ContextCompat.getColor(getContext(),R.color.secondaryColor));
        set.setLineWidth(2.5f);
        set.setCircleColor(ContextCompat.getColor(getContext(),R.color.secondaryColor));
        set.setCircleRadius(5f);
        set.setFillColor(ContextCompat.getColor(getContext(),R.color.secondaryColor));
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(false);
        set.setValueTextSize(10f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        //set.setValueTextColor(Color.rgb(0, 0, 0));
        lineData.addDataSet(set);

        BarData barData = new BarData(set1);

        ArrayList<Object> res = new ArrayList<>();

        res.add(lineData);
        res.add(barData);

        return res;
    }
}