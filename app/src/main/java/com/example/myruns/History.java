package com.example.myruns;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.lang.annotation.ElementType;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class History extends Fragment {

    public static HistoryListAdapter listAdapter;
    public static ListView list;

    private static EntryDataSource database;
    public static ArrayList<ExerciseEntry> entries;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        this.entries = new ArrayList<ExerciseEntry>();
        this.database = new EntryDataSource(getActivity());
        this.database.open();

        this.listAdapter = new HistoryListAdapter(getActivity(), this.entries);

        // database.deleteAllEntries();

        // thread to retrieve data from histories table
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ExerciseEntry> records = database.getAllEntries();
                // update list
                for(ExerciseEntry record : records) {
                    entries.add(record);
                }

                listAdapter.notifyDataSetChanged();
            }
        }).start();

        View view = inflater.inflate(R.layout.fragment_history, container, false);
        this.list = view.findViewById(R.id.historyEntries);

        if(listAdapter != null) {
            this.list.setAdapter(listAdapter);
        }

        this.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), HistoryEntryActivity.class);
                ExerciseEntry entry = entries.get(i);

                String entryType = "Manual Entry";

                if(entry.getInputType() == 2) {
                    entryType = "GPS";
                }

                intent.putExtra("inputType", entryType);
                intent.putExtra("activityType", entry.getActivityType());
                intent.putExtra("date", entry.getDateTime());
                intent.putExtra("duration", String.valueOf(entry.getDuration()) + " mins and 0 secs");
                intent.putExtra("distance", String.valueOf(entry.getDistance()) + " Miles");
                intent.putExtra("comment", entry.getComment());
                intent.putExtra("calories", String.valueOf(entry.getCalorie()) + "cals");
                intent.putExtra("heartRate", String.valueOf(entry.getHeartRate()) + " bpm");
                intent.putExtra("entryID", String.valueOf(entry.getId()));
                intent.putExtra("position", i);

                startActivity(intent);
            }
        });

        return view;
    }

    public static void refreshList(final String storedUnit) {
        // remove all elements and add them back then redraw list
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(ExerciseEntry e : entries) {
                    float distance = e.getDistance();

                    if(!e.getUnits().equals(storedUnit)) {
                        // needs to be converted
                        float d;
                        if(e.getUnits().equals("imperial")) {
                            // miles to kilometers
                            d = (1.609f * distance);
                        } else {
                            // kilometers to miles
                            d = (distance / 1.609f);
                        }

                        e.setDistance(d);
                        database.updateEntryDistance(e.getId(), d);
                    }

                    e.setUnits(storedUnit);
                }

                database.updateUnits(storedUnit);
            }
        }).start();
    }

    public static void removeItem(int d) {
        entries.remove(d);
        Log.d("johnmacdonald", "Removed item at index " + String.valueOf(d));
    }
}