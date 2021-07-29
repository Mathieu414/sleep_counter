package mathieu.com.ui.dashboard;

import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import mathieu.com.R;

public class HistoryFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private HistoryAdapter adapter;
    private RecyclerView rv;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_history, container, false);

        updateAdapter();

        rv = root.findViewById(R.id.list);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(adapter);


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return true;
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (viewHolder != null) {
                    final View foregroundView = ((HistoryAdapter.ViewHolder) viewHolder).getForeView();

                    getDefaultUIUtil().onSelected(foregroundView);
                }
            }

            @Override
            public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                        int actionState, boolean isCurrentlyActive) {
                final View foregroundView = ((HistoryAdapter.ViewHolder) viewHolder).getForeView();
                getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                        actionState, isCurrentlyActive);
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                final View foregroundView = ((HistoryAdapter.ViewHolder) viewHolder).getForeView();
                getDefaultUIUtil().clearView(foregroundView);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                final View foregroundView = ((HistoryAdapter.ViewHolder) viewHolder).getForeView();
                getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                        actionState, isCurrentlyActive);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                adapter.removeItem(viewHolder.getAdapterPosition());
                Snackbar.make(getActivity()
                        .findViewById(android.R.id.content),"Nuit supprimée !",Snackbar.LENGTH_SHORT)
                        .show();
            }
        });

        itemTouchHelper.attachToRecyclerView(rv);

        return root;
    }

    public void updateAdapter(){
        List<List<String>> nightsList = new ArrayList<List<String>>();
        File path = new File(Environment.getExternalStorageDirectory()+File.separator+"SleepData"+File.separator+"Data.txt");
        try {
            Scanner myReader = new Scanner(path);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                List<String> dataList = Arrays.asList(data.split("\\s+"));
                nightsList.add(dataList);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        adapter = new HistoryAdapter(nightsList);
    }
}