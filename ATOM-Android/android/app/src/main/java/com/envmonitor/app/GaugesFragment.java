package com.envmonitor.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.github.anastr.speedviewlib.DeluxeSpeedView;

public class GaugesFragment extends Fragment {
    private static final String TAG = "GaugesFragment";
    private DeluxeSpeedView temperatureGauge;
    private DeluxeSpeedView humidityGauge;
    private DeluxeSpeedView pressureGauge;
    private float lastTemperature = 0;
    private float lastHumidity = 0;
    private float lastPressure = 0;
    private ViewGroup container;
    private LayoutInflater inflater;
    private float temperatureAdjustment = 0.0f;
    private float humidityAdjustment = 0.0f;
    private float pressureAdjustment = 0.0f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);  // Retain the fragment instance during configuration changes
        Log.d(TAG, "onCreate called, savedInstanceState: " + (savedInstanceState != null ? "exists" : "null"));
        
        // Load saved adjustments
        SharedPreferences prefs = requireActivity().getSharedPreferences("EnvMonitorSettings", Context.MODE_PRIVATE);
        temperatureAdjustment = prefs.getFloat("temperature_adjustment", 0.0f);
        humidityAdjustment = prefs.getFloat("humidity_adjustment", 0.0f);
        pressureAdjustment = prefs.getFloat("pressure_adjustment", 0.0f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.container = container;
        this.inflater = inflater;
        
        int orientation = getResources().getConfiguration().orientation;
        String orientationStr = orientation == Configuration.ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "PORTRAIT";
        Log.d(TAG, "onCreateView - Current orientation: " + orientationStr);
        
        return createView();
    }

    private View createView() {
        int orientation = getResources().getConfiguration().orientation;
        String orientationStr = orientation == Configuration.ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "PORTRAIT";
        Log.d(TAG, "createView - Creating view for orientation: " + orientationStr);
        
        // Try to inflate the layout
        View view = null;
        try {
            Log.d(TAG, "Attempting to inflate layout for " + orientationStr);
            // Android will automatically use the landscape layout from layout-land when in landscape mode
            view = inflater.inflate(R.layout.fragment_gauges, container, false);
            Log.d(TAG, "Layout inflated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error inflating layout: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        
        // Initialize and log gauge references
        Log.d(TAG, "Initializing gauges...");
        try {
            temperatureGauge = view.findViewById(R.id.temperatureGauge);
            humidityGauge = view.findViewById(R.id.humidityGauge);
            pressureGauge = view.findViewById(R.id.pressureGauge);
            
            Log.d(TAG, "Gauge references - Temperature: " + (temperatureGauge != null ? "found" : "null") +
                      ", Humidity: " + (humidityGauge != null ? "found" : "null") +
                      ", Pressure: " + (pressureGauge != null ? "found" : "null"));
        } catch (Exception e) {
            Log.e(TAG, "Error finding gauge views: " + e.getMessage());
        }
        
        setupGauges();
        
        // Restore last known values
        Log.d(TAG, "Restoring last values - Temp: " + lastTemperature + 
              ", Humidity: " + lastHumidity + 
              ", Pressure: " + lastPressure);
              
        if (temperatureGauge != null) updateTemperatureGauge(lastTemperature);
        if (humidityGauge != null) updateHumidityGauge(lastHumidity);
        if (pressureGauge != null) updatePressureGauge(lastPressure);
        
        return view;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String orientationStr = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "PORTRAIT";
        Log.d(TAG, "onConfigurationChanged - New orientation: " + orientationStr);
        
        // Log the current fragment state
        Log.d(TAG, "Fragment state - isAdded: " + isAdded() + 
              ", isDetached: " + isDetached() + 
              ", isRemoving: " + isRemoving() + 
              ", isVisible: " + isVisible());
              
        // Create and set the new view
        View newView = createView();
        if (newView != null && getView() != null) {
            ViewGroup parent = (ViewGroup) getView().getParent();
            if (parent != null) {
                Log.d(TAG, "Replacing old view with new view for orientation: " + orientationStr);
                parent.removeView(getView());
                parent.addView(newView);
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called");
    }
    
    private void setupGauges() {
        Log.d(TAG, "Setting up gauges...");
        try {
            if (temperatureGauge != null) {
                temperatureGauge.setMinSpeed(-10);
                temperatureGauge.setMaxSpeed(40);
                Log.d(TAG, "Temperature gauge configured");
            }
            
            if (humidityGauge != null) {
                humidityGauge.setMinSpeed(0);
                humidityGauge.setMaxSpeed(100);
                Log.d(TAG, "Humidity gauge configured");
            }
            
            if (pressureGauge != null) {
                pressureGauge.setMinSpeed(960);
                pressureGauge.setMaxSpeed(1100);
                Log.d(TAG, "Pressure gauge configured");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up gauges: " + e.getMessage());
        }
    }
    
    public void updateTemperature(float temperature) {
        lastTemperature = temperature;
        if (isAdded() && temperatureGauge != null) {
            Log.d(TAG, "Updating temperature to: " + temperature);
            updateTemperatureGauge(temperature);
        } else {
            Log.w(TAG, "Cannot update temperature - isAdded: " + isAdded() + ", gauge: " + (temperatureGauge != null));
        }
    }
    
    public void updateHumidity(float humidity) {
        lastHumidity = humidity;
        if (isAdded() && humidityGauge != null) {
            Log.d(TAG, "Updating humidity to: " + humidity);
            updateHumidityGauge(humidity);
        } else {
            Log.w(TAG, "Cannot update humidity - isAdded: " + isAdded() + ", gauge: " + (humidityGauge != null));
        }
    }
    
    public void updatePressure(float pressure) {
        lastPressure = pressure;
        if (isAdded() && pressureGauge != null) {
            Log.d(TAG, "Updating pressure to: " + pressure);
            updatePressureGauge(pressure);
        } else {
            Log.w(TAG, "Cannot update pressure - isAdded: " + isAdded() + ", gauge: " + (pressureGauge != null));
        }
    }

    public void setMeasurementAdjustment(String measurementType, float adjustment) {
        switch (measurementType) {
            case "temperature":
                temperatureAdjustment = adjustment;
                if (temperatureGauge != null) {
                    updateTemperatureGauge(lastTemperature);
                }
                break;
            case "humidity":
                humidityAdjustment = adjustment;
                if (humidityGauge != null) {
                    updateHumidityGauge(lastHumidity);
                }
                break;
            case "pressure":
                pressureAdjustment = adjustment;
                if (pressureGauge != null) {
                    updatePressureGauge(lastPressure);
                }
                break;
        }
    }

    private void updateTemperatureGauge(float value) {
        lastTemperature = value;
        if (temperatureGauge != null) {
            temperatureGauge.speedTo(value + temperatureAdjustment);
        }
    }

    private void updateHumidityGauge(float value) {
        lastHumidity = value;
        if (humidityGauge != null) {
            humidityGauge.speedTo(value + humidityAdjustment);
        }
    }

    private void updatePressureGauge(float value) {
        lastPressure = value;
        if (pressureGauge != null) {
            pressureGauge.speedTo(value + pressureAdjustment);
        }
    }
}
