package com.envmonitor.app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final long SCAN_PERIOD = 10000;
    
    // BLE UUIDs
    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID TEMPERATURE_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private static final UUID HUMIDITY_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a9");
    private static final UUID PRESSURE_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26aa");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private Handler handler;
    private boolean scanning;
    private TextView statusText;
    private DeviceListAdapter deviceListAdapter;
    private Map<String, BluetoothDevice> deviceMap;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MainPagerAdapter pagerAdapter;
    private Runnable pendingCallback;

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceName = device.getName();
            if (deviceName != null && !deviceName.isEmpty()) {
                deviceMap.put(device.getAddress(), device);
                deviceListAdapter.addDevice(device);
                deviceListAdapter.updateRssi(device.getAddress(), result.getRssi());
            }
        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                runOnUiThread(() -> {
                    statusText.setText("Connected");
                    statusText.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
                    viewPager.setCurrentItem(1); // Switch to gauges tab
                    deviceListAdapter.setDeviceConnected(gatt.getDevice().getAddress(), true);
                });
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                runOnUiThread(() -> {
                    statusText.setText("Disconnected");
                    statusText.setTextColor(Color.GRAY);
                    viewPager.setCurrentItem(0); // Switch to device selection tab
                    if (gatt != null && gatt.getDevice() != null) {
                        deviceListAdapter.setDeviceConnected(gatt.getDevice().getAddress(), false);
                    }
                    // Clean up GATT resources
                    if (bluetoothGatt != null) {
                        bluetoothGatt.close();
                        bluetoothGatt = null;
                    }
                });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    enableNotifications(service);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            try {
                byte[] data = characteristic.getValue();
                String value = new String(data, StandardCharsets.UTF_8);
                Log.d(TAG, "Received data: " + value); // Debug log
                
                UUID characteristicUuid = characteristic.getUuid();
                // Clean up the value string by removing non-numeric characters except decimal point and minus
                String cleanValue = value.replaceAll("[^\\d.-]", "");
                float numericValue = Float.parseFloat(cleanValue);
                
                runOnUiThread(() -> {
                    GaugesFragment gaugesFragment = pagerAdapter.getGaugesFragment();
                    if (gaugesFragment != null) {
                        if (characteristicUuid.equals(TEMPERATURE_UUID)) {
                            gaugesFragment.updateTemperature(numericValue);
                        } else if (characteristicUuid.equals(HUMIDITY_UUID)) {
                            gaugesFragment.updateHumidity(numericValue);
                        } else if (characteristicUuid.equals(PRESSURE_UUID)) {
                            // Convert pressure from Pa to hPa (divide by 100)
                            gaugesFragment.updatePressure(numericValue / 100.0f);
                        }
                    }
                });
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing numeric value: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Error processing characteristic data: " + e.getMessage());
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS && pendingCallback != null) {
                runOnUiThread(pendingCallback);
                pendingCallback = null;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        deviceMap = new HashMap<>();
        deviceListAdapter = new DeviceListAdapter(this, new DeviceListAdapter.OnDeviceSelectedListener() {
            @Override
            public void onDeviceSelected(BluetoothDevice device) {
                connectToDevice(device);
            }

            @Override
            public void onDeviceDisconnect(BluetoothDevice device) {
                disconnectDevice(device);
            }
        });

        statusText = findViewById(R.id.statusText);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Devices");
                            break;
                        case 1:
                            tab.setText("Gauges");
                            break;
                        case 2:
                            tab.setText("Settings");
                            break;
                    }
                }).attach();

        // Disable swipe between tabs
        viewPager.setUserInputEnabled(false);

        // Set text colors for tabs
        tabLayout.setTabTextColors(Color.BLACK, getResources().getColor(R.color.colorPrimary, getTheme()));
        
        // Optional: Set tab ripple color
        tabLayout.setTabRippleColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent, getTheme())));

        setupBluetooth();
    }

    private void setupBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        String[] permissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions = new String[]{
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
        
        requestPermissions(permissions);
    }

    private void requestPermissions(String[] permissions) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        
        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Log.e(TAG, "Required permissions not granted");
                finish();
            }
        }
    }

    public void startScan() {
        if (!scanning) {
            deviceMap.clear();
            deviceListAdapter.clearDevices();
            
            handler.postDelayed(() -> {
                scanning = false;
                bluetoothLeScanner.stopScan(scanCallback);
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(scanCallback);
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
    }

    private void disconnectDevice(BluetoothDevice device) {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        deviceListAdapter.setDeviceConnected(device.getAddress(), false);
        runOnUiThread(() -> {
            statusText.setText("Disconnected");
            statusText.setTextColor(Color.GRAY);
            viewPager.setCurrentItem(0); // Switch back to device selection tab
        });
    }

    private void enableNotifications(BluetoothGattService service) {
        // Enable notifications for temperature first
        enableCharacteristicNotification(service, TEMPERATURE_UUID, () -> {
            // After temperature is done, enable humidity
            enableCharacteristicNotification(service, HUMIDITY_UUID, () -> {
                // After humidity is done, enable pressure
                enableCharacteristicNotification(service, PRESSURE_UUID, null);
            });
        });
    }

    private void enableCharacteristicNotification(BluetoothGattService service, UUID characteristicUuid, Runnable onComplete) {
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
        if (characteristic != null) {
            bluetoothGatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")); // Client Characteristic Configuration
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                // Store the callback for when the descriptor write completes
                pendingCallback = onComplete;
                bluetoothGatt.writeDescriptor(descriptor);
            }
        }
    }

    public DeviceListAdapter getDeviceListAdapter() {
        return deviceListAdapter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }
}
