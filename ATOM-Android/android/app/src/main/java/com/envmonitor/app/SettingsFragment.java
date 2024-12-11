package com.envmonitor.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import android.util.Log;
import java.util.Locale;
import java.text.NumberFormat;
import java.text.ParseException;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";
    private EditText tempAdjustment;
    private EditText humidityAdjustment;
    private EditText pressureAdjustment;
    private static final float ADJUSTMENT_STEP = 0.1f;
    private NumberFormat numberFormat;
    private SharedPreferences sharedPreferences;
    
    // Keys for SharedPreferences
    private static final String PREFS_NAME = "EnvMonitorSettings";
    private static final String KEY_TEMP_ADJUSTMENT = "temperature_adjustment";
    private static final String KEY_HUMIDITY_ADJUSTMENT = "humidity_adjustment";
    private static final String KEY_PRESSURE_ADJUSTMENT = "pressure_adjustment";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        // Initialize number format
        numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setMinimumFractionDigits(1);
        numberFormat.setMaximumFractionDigits(1);
        
        // Initialize views
        tempAdjustment = view.findViewById(R.id.tempAdjustment);
        humidityAdjustment = view.findViewById(R.id.humidityAdjustment);
        pressureAdjustment = view.findViewById(R.id.pressureAdjustment);

        // Load saved values
        loadSavedValues();

        // Set up temperature adjustment buttons
        Button tempMinusButton = view.findViewById(R.id.tempMinusButton);
        Button tempPlusButton = view.findViewById(R.id.tempPlusButton);
        tempMinusButton.setOnClickListener(v -> adjustValue(tempAdjustment, -ADJUSTMENT_STEP, "temperature"));
        tempPlusButton.setOnClickListener(v -> adjustValue(tempAdjustment, ADJUSTMENT_STEP, "temperature"));

        // Set up humidity adjustment buttons
        Button humidityMinusButton = view.findViewById(R.id.humidityMinusButton);
        Button humidityPlusButton = view.findViewById(R.id.humidityPlusButton);
        humidityMinusButton.setOnClickListener(v -> adjustValue(humidityAdjustment, -ADJUSTMENT_STEP, "humidity"));
        humidityPlusButton.setOnClickListener(v -> adjustValue(humidityAdjustment, ADJUSTMENT_STEP, "humidity"));

        // Set up pressure adjustment buttons
        Button pressureMinusButton = view.findViewById(R.id.pressureMinusButton);
        Button pressurePlusButton = view.findViewById(R.id.pressurePlusButton);
        pressureMinusButton.setOnClickListener(v -> adjustValue(pressureAdjustment, -ADJUSTMENT_STEP, "pressure"));
        pressurePlusButton.setOnClickListener(v -> adjustValue(pressureAdjustment, ADJUSTMENT_STEP, "pressure"));

        // Add text change listeners
        setupTextChangeListener(tempAdjustment, "temperature");
        setupTextChangeListener(humidityAdjustment, "humidity");
        setupTextChangeListener(pressureAdjustment, "pressure");

        return view;
    }

    private void loadSavedValues() {
        float tempValue = sharedPreferences.getFloat(KEY_TEMP_ADJUSTMENT, 0.0f);
        float humidityValue = sharedPreferences.getFloat(KEY_HUMIDITY_ADJUSTMENT, 0.0f);
        float pressureValue = sharedPreferences.getFloat(KEY_PRESSURE_ADJUSTMENT, 0.0f);

        tempAdjustment.setText(numberFormat.format(tempValue));
        humidityAdjustment.setText(numberFormat.format(humidityValue));
        pressureAdjustment.setText(numberFormat.format(pressureValue));

        // Update gauges with saved values
        updateGaugesAdjustment("temperature", tempValue);
        updateGaugesAdjustment("humidity", humidityValue);
        updateGaugesAdjustment("pressure", pressureValue);
    }

    private void saveValue(String key, float value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    private float parseNumber(String value) throws ParseException {
        if (value == null || value.trim().isEmpty()) {
            return 0.0f;
        }
        // Replace comma with period for parsing
        String normalizedValue = value.replace(',', '.');
        return Float.parseFloat(normalizedValue);
    }

    private void adjustValue(EditText editText, float adjustment, String measurementType) {
        try {
            String currentText = editText.getText().toString();
            float currentValue = parseNumber(currentText);
            float newValue = currentValue + adjustment;
            String formattedValue = numberFormat.format(newValue);
            Log.d(TAG, measurementType + ": Adjusting from " + currentValue + " to " + newValue);
            editText.setText(formattedValue);
            updateGaugesAdjustment(measurementType, newValue);
            
            // Save the new value
            String key = getPreferenceKey(measurementType);
            if (key != null) {
                saveValue(key, newValue);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adjusting value: " + e.getMessage());
            editText.setText(numberFormat.format(0.0));
            updateGaugesAdjustment(measurementType, 0.0f);
        }
    }

    private String getPreferenceKey(String measurementType) {
        switch (measurementType) {
            case "temperature":
                return KEY_TEMP_ADJUSTMENT;
            case "humidity":
                return KEY_HUMIDITY_ADJUSTMENT;
            case "pressure":
                return KEY_PRESSURE_ADJUSTMENT;
            default:
                return null;
        }
    }

    private void setupTextChangeListener(EditText editText, String measurementType) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    float adjustment = parseNumber(s.toString());
                    Log.d(TAG, measurementType + ": Text changed to " + adjustment);
                    updateGaugesAdjustment(measurementType, adjustment);
                    
                    // Save the new value
                    String key = getPreferenceKey(measurementType);
                    if (key != null) {
                        saveValue(key, adjustment);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing input: " + e.getMessage());
                }
            }
        });
    }

    private void updateGaugesAdjustment(String measurementType, float adjustment) {
        ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
        MainPagerAdapter pagerAdapter = (MainPagerAdapter) viewPager.getAdapter();
        if (pagerAdapter != null) {
            GaugesFragment gaugesFragment = pagerAdapter.getGaugesFragment();
            gaugesFragment.setMeasurementAdjustment(measurementType, adjustment);
        }
    }
}
