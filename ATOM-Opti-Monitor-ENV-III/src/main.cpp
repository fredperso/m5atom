/******************************************************************************
 * M5ATOM ENV web-monitor
 * The M5Atom is used as a web server to display the environment sensor data as a web page.
 * The M5Atom is connected to a WiFi network and a web browser is used to access the web page.
 * There is also a REST API to access the sensor data.
 * The M5Atom is connected to a QMP6988 sensor for pressure and a SHT30 sensor for temperature and humidity.
 * The M5Atom is also connected to a BLE device to send the sensor data to a mobile device ou a simple browser with bluetooth capability like chrome
 ******************************************************************************/
#include <Arduino.h>
#include "M5Atom.h"
#include "UNIT_ENV.h"
#include "ble_handler.h"
#include "esp_pm.h"
#include "driver/rtc_io.h"

// Environment sensor objects
QMP6988 qmp6988;
SHT3X sht30;

// BLE handler
BLEHandler bleHandler;

// Global variables for sensor data
float temperature = 0.0;
float humidity = 0.0;
float pressure = 0.0;

// Timing variables
unsigned long lastUpdate = 0;
const unsigned long UPDATE_INTERVAL = 3000;  // Update every 3 seconds to save power
const unsigned long LED_FLASH_DURATION = 50;   // Brief flash for data transmission
unsigned long lastLedChange = 0;
bool ledOn = false;

// LED settings
const uint8_t LED_BRIGHTNESS = 100;  // Reduced LED brightness (0-255)

// Function declarations
void readSensors();
void updateLED();
void setupPowerManagement();

void setupPowerManagement() {
    // Configure CPU frequency scaling
    esp_pm_config_esp32_t pm_config;
    pm_config.max_freq_mhz = 80;       // Set CPU frequency to 80MHz when active
    pm_config.min_freq_mhz = 40;       // Allow CPU to scale down to 40MHz when idle
    pm_config.light_sleep_enable = true;// Enable automatic light sleep
    esp_pm_configure(&pm_config);
}

void setup() {
    // Initialize M5Stack Atom
    M5.begin(true, false, true);
    delay(50);
    
    // Setup power management
    setupPowerManagement();
    
    // Set LED brightness
    M5.dis.setBrightness(LED_BRIGHTNESS);
    
    // Initialize I2C for sensors
    Wire.begin(26, 32);
    
    // Initialize sensors
    qmp6988.init();
    
    // Initialize BLE
    if (!bleHandler.init("M5ATOM-ENV")) {
        Serial.println("Failed to initialize BLE!");
        M5.dis.drawpix(0, 0xff0000);  // Red LED indicates error
    } else {
        Serial.println("BLE initialized successfully");
        M5.dis.drawpix(0, 0x00ff00);  // Green LED indicates success
    }
    
    Serial.println("Setup complete");
}

void readSensors() {
    // Read pressure from QMP6988
    pressure = qmp6988.calcPressure();
    
    // Read temperature and humidity from SHT30
    if (sht30.get() == 0) {
        temperature = sht30.cTemp;
        humidity = sht30.humidity;
    }
    
    // Print sensor values to Serial for debugging
    Serial.printf("Temperature: %.2f°C\n", temperature);
    Serial.printf("Humidity: %.2f%%\n", humidity);
    Serial.printf("Pressure: %.2fPa\n", pressure);
}

void updateLED() {
    unsigned long currentTime = millis();
    
    if (ledOn && (currentTime - lastLedChange >= LED_FLASH_DURATION)) {
        M5.dis.drawpix(0, 0x000000);  // Turn off LED
        ledOn = false;
    }
}

void loop() {
    M5.update();  // Update button state
    unsigned long currentTime = millis();
    
    // Read sensor data and update BLE at regular intervals
    if (currentTime - lastUpdate >= UPDATE_INTERVAL) {
        readSensors();
        
        if (bleHandler.isConnected()) {
            // Flash LED blue briefly to indicate data transmission
            M5.dis.drawpix(0, 0x0000ff);
            ledOn = true;
            lastLedChange = currentTime;
            
            bleHandler.updateSensorValues(temperature, humidity, pressure);
        } else {
            // Show very dim white when not connected
            M5.dis.drawpix(0, 0x010101);
        }
        
        lastUpdate = currentTime;

        // Light sleep between updates if no BLE connection
        if (!bleHandler.isConnected()) {
            esp_sleep_enable_timer_wakeup((UPDATE_INTERVAL - 100) * 1000);
            esp_light_sleep_start();
        }
    }
    
    // Update LED state (turn off after flash duration)
    updateLED();
    
    // Sleep delay - can be longer now since we're not constantly updating LED
    delay(100);
}
