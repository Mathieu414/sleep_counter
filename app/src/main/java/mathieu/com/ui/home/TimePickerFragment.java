package mathieu.com.ui.home;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import java.util.Calendar;
import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment{

    private TimePickerDialog.OnTimeSetListener listener;

    public TimePickerFragment(TimePickerDialog.OnTimeSetListener listener) {
        super();
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        int hour = 23;
        int minute = 0;

        // Create a new instance of TimePickerDialog and return it
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), 3,listener, hour, minute, DateFormat.is24HourFormat(getActivity()));
        timePickerDialog.setTitle("Bedtime");
        return timePickerDialog;
    }
}
