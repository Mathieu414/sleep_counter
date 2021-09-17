package mathieu.com;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import mathieu.com.ui.dashboard.HistoryAdapter;
import mathieu.com.ui.home.DatePickerFragment;
import mathieu.com.ui.home.TimePickerFragment;
import mathieu.com.ui.home.TimePickerFragment2;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity<state> extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = MainActivity.class.getSimpleName();
    private File fileName;
    private int bedTimeHour;
    private int bedTimeMinutes;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        if(checkPermission()) {
            File file = new File(Environment.getExternalStorageDirectory()+File.separator+"SleepData");
            if (!file.exists()) {
                file.mkdir();
                Toast.makeText(MainActivity.this, "Fichier de sauvegarde créé", Toast.LENGTH_LONG).show();
            }
            try {
                File gpxFile = new File(file, "Data.txt");
                fileName = gpxFile;
            } catch (Exception e) { }
        }
        else {
            requestPermission();
            if(checkPermission()) {
                File file = new File(Environment.getExternalStorageDirectory()+File.separator+"SleepData");
                if (!file.exists()) {
                    file.mkdir();
                    Toast.makeText(MainActivity.this, "Fichier de sauvegarde créé", Toast.LENGTH_LONG).show();
                }
                try {
                    File gpxFile = new File(file, "Data.txt");
                    fileName = gpxFile;
                } catch (Exception e) { }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to create files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    public void showTimePickerDialog(View v) {
        //using the material components date picker
        final MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select the date of the night")
                .build();

        materialDatePicker.show(getSupportFragmentManager(), "datePicker");
        materialDatePicker.addOnPositiveButtonClickListener(
            new MaterialPickerOnPositiveButtonClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onPositiveButtonClick(Object selection) {
                    long selecUnixTime =(long) materialDatePicker.getSelection();

                    Log.d(TAG, "Material selection" + selecUnixTime);
                    final Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(selecUnixTime);
                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH)+1;
                    int day = c.get(Calendar.DAY_OF_MONTH);

                    if(checkPermission()) {
                        //Check if there is not already an entry corresponding to the date entered
                        File path = new File(Environment.getExternalStorageDirectory()+File.separator+"SleepData"+File.separator+"Data.txt");
                        Boolean b = true;
                        try {
                            Scanner myReader = new Scanner(path);
                            while (myReader.hasNextLine()) {
                                String data = myReader.nextLine();
                                List<String> dataList = Arrays.asList(data.split("\\s+"));
                                String test = year +"_"+month +"_"+ day;
                                if(dataList.get(0).equals(test)){
                                    b = false;
                                }
                            }
                            myReader.close();
                        } catch (FileNotFoundException e) {
                            System.out.println("An error occurred.");
                            e.printStackTrace();
                        }
                        if(b){
                            content = year +"_"+month +"_"+ day + "\t";
                            showMaterialTimePicker1();
                            //DialogFragment newFragment2 = new TimePickerFragment(timePickerListener1);
                            //newFragment2.show(getSupportFragmentManager(), "timePicker2");
                        }else{
                            Toast.makeText(MainActivity.this, "The date has already a night", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
    }

    /*
    //Old version of the datepicker, now implemented with MaterialComponents
    DatePickerDialog.OnDateSetListener DatePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            if(checkPermission()) {
                //Check there is not already an entry corresponding to the date entered
                File path = new File(Environment.getExternalStorageDirectory()+File.separator+"SleepData"+File.separator+"Data.txt");
                Boolean b = true;
                try {
                    Scanner myReader = new Scanner(path);
                    while (myReader.hasNextLine()) {
                        String data = myReader.nextLine();
                        List<String> dataList = Arrays.asList(data.split("\\s+"));
                        String test = year +"_"+month +"_"+ day;
                        if(dataList.get(0).equals(test)){
                            b = false;
                        }
                    }
                    myReader.close();
                } catch (FileNotFoundException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }
                if(b){
                    content = year +"_"+month +"_"+ day + "\t";
                    DialogFragment newFragment2 = new TimePickerFragment(timePickerListener1);
                    newFragment2.show(getSupportFragmentManager(), "timePicker2");
                }else{
                    Toast.makeText(MainActivity.this, "The date has already a night", Toast.LENGTH_LONG).show();
                }
            }
        }
    };
     */
    public void showMaterialTimePicker1(){
        final MaterialTimePicker picker =
                new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(23)
                        .setMinute(00)
                        .setTitleText("Select bed-time")
                        .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                        .build();
        picker.addOnPositiveButtonClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(checkPermission()) {
                            bedTimeHour = picker.getHour();
                            bedTimeMinutes = picker.getMinute();
                            if(Integer.toString(bedTimeMinutes).length()==1 & Integer.toString(bedTimeHour).length()==1){
                                content = content + "0" + bedTimeHour + "\t0" + bedTimeMinutes + "\t";
                            }
                            if (Integer.toString(bedTimeMinutes).length()==1 & Integer.toString(bedTimeHour).length()==2){
                                content = content + bedTimeHour + "\t0" + bedTimeMinutes + "\t";
                            }
                            if (Integer.toString(bedTimeMinutes).length()==2 & Integer.toString(bedTimeHour).length()==1){
                                content = content + "0" + bedTimeHour + "\t" + bedTimeMinutes + "\t";
                            }
                            if (Integer.toString(bedTimeMinutes).length()==2 & Integer.toString(bedTimeHour).length()==2){
                                content = content + bedTimeHour + "\t" + bedTimeMinutes + "\t";
                            }
                            showMaterialTimePicker2();
                            //DialogFragment newFragment3 = new TimePickerFragment2(timePickerListener2);
                            //newFragment3.show(getSupportFragmentManager(), "timePicker3");
                        }
                    }
                }
        );
        picker.show(getSupportFragmentManager(), "timePicker1");
    }

    public void showMaterialTimePicker2(){
        final MaterialTimePicker picker =
                new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(8)
                        .setMinute(00)
                        .setTitleText("Select wake-up time")
                        .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                        .build();
        picker.addOnPositiveButtonClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(checkPermission()) {
                            int wakeUpHour = picker.getHour();
                            int wakeUpMinutes = picker.getMinute();
                            int nightLengthHour;
                            //first calculate the amount of minutes for the duration of the night
                            int nightLengthMinutes = (60-bedTimeMinutes)+wakeUpMinutes;
                            //then dissociate the cases : 1st when the bedTimeHour is before
                            // midnight (so after midday), 2nd when it is before midday
                            // (so after midnight)
                            if(bedTimeHour>12){
                                nightLengthHour = (24-bedTimeHour)+wakeUpHour;
                            }else{
                                nightLengthHour = wakeUpHour-bedTimeHour-1;
                            }
                            if(nightLengthMinutes>=60){
                                nightLengthHour++;
                                nightLengthMinutes = nightLengthMinutes-60;
                            }
                            if(Integer.toString(nightLengthMinutes).length()==1
                                    & Integer.toString(nightLengthHour).length()==1){
                                content = content + "0" + nightLengthHour + "\t0"
                                        + nightLengthMinutes + "\t";
                            }
                            if (Integer.toString(nightLengthMinutes).length()==1
                                    & Integer.toString(nightLengthHour).length()==2){
                                content = content + nightLengthHour + "\t0"
                                        + nightLengthMinutes + "\t";
                            }
                            if (Integer.toString(nightLengthMinutes).length()==2
                                    & Integer.toString(nightLengthHour).length()==1){
                                content = content + "0" + nightLengthHour + "\t"
                                        + nightLengthMinutes + "\t";
                            }
                            if (Integer.toString(nightLengthMinutes).length()==2
                                    & Integer.toString(nightLengthHour).length()==2){
                                content = content + nightLengthHour + "\t"
                                        + nightLengthMinutes + "\t";
                            }
                            WriteFile(fileName, content);
                        }
                    }
                }
        );
        picker.show(getSupportFragmentManager(), "timePicker2");
    }

/* Old version of time Pickers

    TimePickerDialog.OnTimeSetListener timePickerListener1 = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if(checkPermission()) {
                bedTimeHour = hourOfDay;
                bedTimeMinutes = minute;
                if(Integer.toString(minute).length()==1 & Integer.toString(hourOfDay).length()==1){
                    content = content + "0" + hourOfDay + "\t0" + minute + "\t";
                }
                if (Integer.toString(minute).length()==1 & Integer.toString(hourOfDay).length()==2){
                    content = content + hourOfDay + "\t0" + minute + "\t";
                }
                if (Integer.toString(minute).length()==2 & Integer.toString(hourOfDay).length()==1){
                    content = content + "0" + hourOfDay + "\t" + minute + "\t";
                }
                if (Integer.toString(minute).length()==2 & Integer.toString(hourOfDay).length()==2){
                    content = content + hourOfDay + "\t" + minute + "\t";
                }
            }
            DialogFragment newFragment3 = new TimePickerFragment2(timePickerListener2);
            newFragment3.show(getSupportFragmentManager(), "timePicker3");
        }
    };


    TimePickerDialog.OnTimeSetListener timePickerListener2 = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if(checkPermission()) {
                int nightLengthHour;
                int nightLengthMinutes;
                if(bedTimeHour>12){
                    nightLengthHour = (23-bedTimeHour)+hourOfDay;
                    nightLengthMinutes = (60-bedTimeMinutes)+minute;
                    if(nightLengthMinutes>=60){
                        nightLengthHour++;
                        nightLengthMinutes = nightLengthMinutes-60;
                    }
                }else{
                    nightLengthHour = hourOfDay;
                    nightLengthMinutes = minute;
                }
                if(Integer.toString(nightLengthMinutes).length()==1 & Integer.toString(nightLengthHour).length()==1){
                    content = content + "0" + nightLengthHour + "\t0" + nightLengthMinutes + "\t";
                }
                if (Integer.toString(nightLengthMinutes).length()==1 & Integer.toString(nightLengthHour).length()==2){
                    content = content + nightLengthHour + "\t0" + nightLengthMinutes + "\t";
                }
                if (Integer.toString(nightLengthMinutes).length()==2 & Integer.toString(nightLengthHour).length()==1){
                    content = content + "0" + nightLengthHour + "\t" + nightLengthMinutes + "\t";
                }
                if (Integer.toString(nightLengthMinutes).length()==2 & Integer.toString(nightLengthHour).length()==2){
                    content = content + nightLengthHour + "\t" + nightLengthMinutes + "\t";
                }
                WriteFile(fileName, content);
            }
        }
    };

*/

    /**
     * Writes into a file given in parameter in the position corresponding to the date -> creates an
     * ordered database
     *
     * @param  file_path the position of the file you want to write into
     * @param  content  the content you want to add to the file : it must have date at the beginning
     *                 in the right format
     */
    public void WriteFile(File file_path, String content){
        File inputFile = new File(Environment
                .getExternalStorageDirectory()+File.separator+"SleepData"+File.separator+"Data.txt");
        File tempFile = new File(Environment
                .getExternalStorageDirectory()+File.separator+"SleepData"+File.separator+"TempData.txt");
        List<String> dataList = Arrays.asList(content.split("\\s+"));
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy_MM_dd");
        Date newDate = null;
        try {
            newDate = sdFormat.parse(dataList.get(0));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            Date dateLineBefore = null;
            String currentLine;
            Date currentDate = null;
            boolean isFileEmpty = true;
            boolean isLineNotWritten = true;

            while((currentLine = reader.readLine()) != null) {
                isFileEmpty = false;
                List<String> currentList = Arrays.asList(currentLine.split("\\s+"));
                Log.d(TAG, "La valeur de content est :" + content);
                try {
                    currentDate = sdFormat.parse(currentList.get(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(dateLineBefore == null){
                    if(newDate.compareTo(currentDate)>0){
                        writer.write(content + System.getProperty("line.separator"));
                        isLineNotWritten = false;
                    }
                }else{
                    if(newDate.compareTo(currentDate)>0 & newDate.compareTo(dateLineBefore)<0) {
                        writer.write(content + System.getProperty("line.separator"));
                        isLineNotWritten = false;
                    }
                }
                writer.write(currentLine + System.getProperty("line.separator"));
                dateLineBefore = currentDate;
            }
            //If the file is empty, then write the content as the first and only line
            if(isFileEmpty){
                writer.write(content + System.getProperty("line.separator"));
                isLineNotWritten = false;
            }
            //If date of the content has to go at the end of the file (in this case the
            // reader.readLine is null)
            if(isLineNotWritten){
                if(newDate.compareTo(dateLineBefore)<0){
                    writer.write(content + System.getProperty("line.separator"));
                }
            }
            writer.close();
            reader.close();
            boolean successful = tempFile.renameTo(inputFile);
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}