package mathieu.com.ui.dashboard;

import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import mathieu.com.R;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<List<String>> characters;
    private static final String TAG = HistoryAdapter.class.getSimpleName();

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param c List<List<String[]>> containing the data to populate views to be used
     * by RecyclerView.
     */
    public HistoryAdapter (List<List<String>> c){
        this.characters=c;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name, date, description;
        private RelativeLayout viewForeground,viewBackground;

        private List<String> currentList;


        public ViewHolder(final View itemView) {
            super(itemView);

            date = ((TextView) itemView.findViewById(R.id.Date));
            name = ((TextView) itemView.findViewById(R.id.name));
            description = ((TextView) itemView.findViewById(R.id.description));
            viewForeground = (RelativeLayout) itemView.findViewById(R.id.view_foreground);
            viewBackground = (RelativeLayout) itemView.findViewById(R.id.view_background);
        }

        public RelativeLayout getForeView(){
            return viewForeground;
        }

        public void display(List<String> list) throws ParseException {
            currentList = list;
            if(list.size()==5) {
                String stringDate = list.get(0);
                String stringHour1 = list.get(1) + list.get(2);
                String stringHour2 = list.get(3) + list.get(4);
                date.setText(new SimpleDateFormat("dd/MM/yyyy").format(new SimpleDateFormat("yyyy_MM_dd").parse(stringDate)));
                name.setText(new SimpleDateFormat("HH:mm").format(new SimpleDateFormat("HHmm").parse(stringHour1)));
                description.setText(new SimpleDateFormat("HH:mm").format(new SimpleDateFormat("HHmm").parse(stringHour2)));
            }else{
                Log.d(TAG, "Problème de longueur de liste :" + list.toString());
            }
        }
    }


    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        List<String> list = characters.get(position);
        try {
            holder.display(list);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return characters.size();
    }

    public void removeItem(int position){
        File inputFile = new File(Environment.getExternalStorageDirectory()+File.separator+"SleepData"+File.separator+"Data.txt");
        File tempFile = new File(Environment.getExternalStorageDirectory()+File.separator+"SleepData"+File.separator+"TempData.txt");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;
            int i = 0;

            while((currentLine = reader.readLine()) != null) {
                if(i!=position) {
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
                i=i+1;
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
        characters.remove(position);
        notifyItemRemoved(position);
    }
}
