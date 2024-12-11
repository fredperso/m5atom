package com.envmonitor.app;

public class SensorData {
    public float temperature;
    public float humidity;
    public float pressure;

    public SensorData() {
        // Default constructor for Gson
    }

    public SensorData(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
    }
}
