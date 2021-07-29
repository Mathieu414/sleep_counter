package mathieu.com.ui.notifications;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.charts.Pie;
import com.anychart.core.cartesian.series.Column;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Orientation;
import com.anychart.enums.ScaleStackMode;
import com.anychart.scales.DateTime;
import com.anychart.scales.Linear;

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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import mathieu.com.R;
import mathieu.com.ui.dashboard.HistoryAdapter;

import static java.lang.String.format;

public class StatsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private static final String TAG = HistoryAdapter.class.getSimpleName();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stats, container, false);

        AnyChartView anyChartView = root.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(root.findViewById(R.id.progress_bar));

        Cartesian cartesian = AnyChart.cartesian();

        cartesian.animation(true);


        cartesian.title("Combination of Stacked Column and Line Chart (Dual Y-Axis)");

        cartesian.yScale().stackMode(ScaleStackMode.VALUE);

        SimpleDateFormat sdFormat = new SimpleDateFormat("HHmm");
        Date dateMinimum;
        Date dateMaximum;
        Date dateMaximum2;
        long minimum = 0;
        long maximum = 0;
        long maximum2 = 0;
        try {
            dateMinimum = sdFormat.parse("0000");
            Calendar calMinimum = Calendar.getInstance();
            calMinimum.setTime(dateMinimum);
            minimum = calMinimum.getTimeInMillis();

            dateMaximum = sdFormat.parse("2000");
            Calendar calMaximum = Calendar.getInstance();
            calMaximum.setTime(dateMaximum);
            calMaximum.add(Calendar.DAY_OF_MONTH,1);
            maximum = calMaximum.getTimeInMillis();

            dateMaximum2 = sdFormat.parse("1400");
            Calendar calMaximum2 = Calendar.getInstance();
            calMaximum2.setTime(dateMaximum2);
            maximum2 = calMaximum2.getTimeInMillis();

        } catch (ParseException e) {
            e.printStackTrace();
        }


        Linear scalesDateline = Linear.instantiate();
        scalesDateline.minimum(minimum);
        Log.d(TAG, "Minimum" + minimum);
        scalesDateline.maximum(maximum);
        Log.d(TAG, "Maximum" + maximum);

        Linear scalesDateColumn = Linear.instantiate();
        scalesDateColumn.minimum(minimum);
        Log.d(TAG, "Minimum" + minimum);
        scalesDateColumn.maximum(maximum2);
        Log.d(TAG, "Maximum" + maximum2);
        //scalesDate.ticks("{ interval: 20 }");


        List<DataEntry> data = dataGen();

        Set set = Set.instantiate();
        set.data(data);
        Mapping lineData = set.mapAs("{ x: 'x', value: 'value' }");
        Mapping column1Data = set.mapAs("{ x: 'x', value: 'value2' }");

        Column column = cartesian.column(column1Data);
        cartesian.crosshair(true);
        column.yScale(scalesDateColumn);
        column.tooltip().format("{%Value}{type:time}");


        Line line = cartesian.line(lineData);
        line.yScale(scalesDateline);

        //cartesian.tooltip().format("{%Value}{type:number}");

        anyChartView.setChart(cartesian);

        return root;
    }

    private class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value, Number value2) {
            super(x,value);
            setValue("value2", value2);
        }
    }

    public List<DataEntry> dataGen(){
        List<DataEntry> data = new ArrayList<>();
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
                Log.d(TAG, "unixBedTimeHour" + unixBedTimeHour);
                Log.d(TAG, "unixSleepTime" + unixSleepTime);


                data.add(new CustomDataEntry(stringDate, unixBedTimeHour, unixSleepTime));
                i ++;

            }
            myReader.close();
        } catch (FileNotFoundException | ParseException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return data;
    }
}