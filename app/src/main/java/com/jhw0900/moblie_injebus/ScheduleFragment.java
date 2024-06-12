package com.jhw0900.moblie_injebus;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ScheduleFragment extends Fragment {

    private static final String ARG_DAY = "day";
    private static String selRegion = "";

    public static ScheduleFragment newInstance(String day, String region) {
        selRegion = region;
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        // Get day argument and set TextView or other UI elements
        if (getArguments() != null) {
            String day = getArguments().getString(ARG_DAY);
            // Set day-specific UI elements here
            TextView textDay = (TextView) view.findViewById(R.id.textDay);
            textDay.setText(day);

            Spinner startTimeSpinner = view.findViewById(R.id.start_time_spinner);
            Spinner endTimeSpinner = view.findViewById(R.id.end_time_spinner);

            // Set different data for each day
            ArrayAdapter<String> startTimeAdapter;
            ArrayAdapter<String> endTimeAdapter;

            if(!selRegion.equals("울산")){
                startTimeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.start_times));
                endTimeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.end_times));
            } else {
                startTimeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.el_start_times));
                endTimeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.el_end_times));
            }

            startTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            startTimeSpinner.setAdapter(startTimeAdapter);

            endTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            endTimeSpinner.setAdapter(endTimeAdapter);
        }

        return view;
    }
}
