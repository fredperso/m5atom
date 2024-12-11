package com.envmonitor.app;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {
    private final Context context;
    private List<BluetoothDevice> devices;
    private final OnDeviceSelectedListener listener;
    private Map<String, Integer> deviceRssiMap = new HashMap<>();
    private Map<String, Boolean> connectedDevices = new HashMap<>();

    public interface OnDeviceSelectedListener {
        void onDeviceSelected(BluetoothDevice device);
        void onDeviceDisconnect(BluetoothDevice device);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        ImageView signalStrength;
        Button disconnectButton;

        public ViewHolder(View view) {
            super(view);
            deviceName = view.findViewById(R.id.deviceName);
            deviceAddress = view.findViewById(R.id.deviceAddress);
            signalStrength = view.findViewById(R.id.signalStrength);
            disconnectButton = view.findViewById(R.id.disconnectButton);
        }
    }

    public DeviceListAdapter(Context context, OnDeviceSelectedListener listener) {
        this.context = context;
        this.devices = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.device_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        String deviceName = device.getName();
        holder.deviceName.setText(deviceName != null ? deviceName : "");
        holder.deviceAddress.setText(device.getAddress());

        // Set signal strength icon based on RSSI
        Integer rssi = deviceRssiMap.get(device.getAddress());
        if (rssi != null) {
            int iconResource = getSignalStrengthIcon(rssi);
            holder.signalStrength.setImageResource(iconResource);
        }

        // Handle device connection state
        boolean isConnected = connectedDevices.getOrDefault(device.getAddress(), false);
        holder.disconnectButton.setVisibility(isConnected ? View.VISIBLE : View.GONE);
        
        holder.itemView.setOnClickListener(v -> {
            if (!isConnected) {
                listener.onDeviceSelected(device);
            }
        });

        holder.disconnectButton.setOnClickListener(v -> {
            listener.onDeviceDisconnect(device);
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addDevice(BluetoothDevice device) {
        if (!devices.contains(device)) {
            devices.add(device);
            notifyDataSetChanged();
        }
    }

    public void clearDevices() {
        devices.clear();
        deviceRssiMap.clear();
        notifyDataSetChanged();
    }

    public void updateRssi(String deviceAddress, int rssi) {
        deviceRssiMap.put(deviceAddress, rssi);
        notifyDataSetChanged();
    }

    private int getSignalStrengthIcon(int rssi) {
        if (rssi >= -60) {
            return R.drawable.ic_signal_4;
        } else if (rssi >= -70) {
            return R.drawable.ic_signal_3;
        } else if (rssi >= -80) {
            return R.drawable.ic_signal_2;
        } else if (rssi >= -90) {
            return R.drawable.ic_signal_1;
        } else {
            return R.drawable.ic_signal_0;
        }
    }

    public void setDeviceConnected(String deviceAddress, boolean connected) {
        connectedDevices.put(deviceAddress, connected);
        notifyDataSetChanged();
    }
}
