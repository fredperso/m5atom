package com.envmonitor.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DeviceSelectionFragment extends Fragment {
    private RecyclerView deviceList;
    private Button scanButton;
    private MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_selection, container, false);
        
        activity = (MainActivity) getActivity();
        deviceList = view.findViewById(R.id.deviceList);
        scanButton = view.findViewById(R.id.scanButton);
        
        deviceList.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceList.setAdapter(activity.getDeviceListAdapter());
        
        scanButton.setOnClickListener(v -> activity.startScan());
        
        return view;
    }
}
