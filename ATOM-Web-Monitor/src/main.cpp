/******************************************************************************
 * M5ATOM ENV web-monitor
 * The M5Atom is used as a web server to display the environment sensor data as a web page.
 * The M5Atom is connected to a WiFi network and a web browser is used to access the web page.
 * There is also a REST API to access the sensor data.
 * The M5Atom is connected to a QMP6988 sensor for pressure and a SHT30 sensor for temperature and humidity.
 * The M5Atom is also connected to a BLE device to send the sensor data to a mobile device ou a simple browser with bluetooth capability like chrome
 * 
 *
 * based on code by Hague Nusseck @ electricidea
 * https://github.com/electricidea/M5ATOM/tree/master/ATOM-Web-Monitor
 * 
 * 
 * used external Libraries:
 * - M5Atom
 * - QMP6988 sensor
 * - SHT30 sensor
 * - BLE
 * 
 * The MIT License (MIT)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 * Distributed as-is; no warranty is given.
 ******************************************************************************/
#include <Arduino.h>
#include "M5Atom.h"
#include "UNIT_ENV.h"
#include "WiFi.h"
#include <WiFiClientSecure.h>
#include "index.h"
#include "ble_handler.h"

// WiFi credentials
const char* ssid = "XXXXXXX";
const char* password = "XXXXXX";

// Constants for timing
const unsigned long SENSOR_READ_INTERVAL = 3000;  // Read sensors every 3 seconds
const unsigned long WIFI_CHECK_INTERVAL = 100;    // Check WiFi clients every 100ms
const unsigned long IP_DISPLAY_INTERVAL = 10000;  // Display IP every 10 seconds
unsigned long next_sensor_read = 0;
unsigned long next_wifi_check = 0;
unsigned long next_ip_display = 0;

// ENV sensors
SHT3X sht30;
QMP6988 qmp6988;

float qmp_Pressure = 0.0;
float sht30_Temperature = 0.0;
float sht30_Humidity = 0.0;
int n_average = 1;

// WiFi server
WiFiServer server(80);
String currentLine = "";

// BLE handler
BLEHandler bleHandler;

// GET request types
#define GET_unknown 0
#define GET_index_page 1
#define GET_api_data 2
#define GET_api_temperature 3
#define GET_api_humidity 4
#define GET_api_pressure 5
#define GET_data_js 6
int html_get_request = GET_unknown;  // Initialize with unknown

// Function declarations
void displayIP();
void readSensors();
void parseHTTPRequest(const String& line);
void sendHTTPResponse(WiFiClient& client);
void handleHTTPRequest(WiFiClient& client);
void sendJSONResponse(WiFiClient& client, const char* json);
boolean connect_Wifi();

void setup() {
    // Initialize M5Atom
    M5.begin(true, false, true);
    Serial.begin(115200);
    Serial.println("M5Atom ENV Monitor Starting...");
    
    Wire.begin(26, 32);
    
    // Initialize sensors
    qmp6988.init();
    Serial.println("QMP6988 sensor initialized");
    
    // Give some time for the system to stabilize
    delay(1000);
    
    // Initialize BLE
    if (!bleHandler.init()) {
        Serial.println("BLE initialization failed!");
        return;
    }
    Serial.println("BLE initialized successfully");
    
    // Connect to WiFi
    if (connect_Wifi()) {
        Serial.println("WiFi connected successfully");
        displayIP();
    } else {
        Serial.println("WiFi connection failed!");
    }
    
    // Start server
    server.begin();
    Serial.println("HTTP server started");
    
    next_sensor_read = millis() + SENSOR_READ_INTERVAL;
    next_wifi_check = millis() + WIFI_CHECK_INTERVAL;
}

void readSensors() {
    float temp_sum = 0.0;
    float humid_sum = 0.0;
    float press_sum = 0.0;
    int valid_readings = 0;
    
    for(int i=0; i<n_average; i++) {
        if(sht30.get()==0) {
            temp_sum += sht30.cTemp;
            humid_sum += sht30.humidity;
            valid_readings++;
        }
        press_sum += qmp6988.calcPressure();
        delay(50);
    }
    
    if(valid_readings > 0) {
        sht30_Temperature = temp_sum / valid_readings;
        sht30_Humidity = humid_sum / valid_readings;
        qmp_Pressure = press_sum / n_average;
        
        Serial.print("Temperature: ");
        Serial.println(sht30_Temperature, 1);
        Serial.print("Humidity: ");
        Serial.println(sht30_Humidity);
        Serial.print("Pressure: ");
        Serial.println(qmp_Pressure);
        
        bleHandler.updateSensorValues(sht30_Temperature, sht30_Humidity, qmp_Pressure);
    } else {
        Serial.println("No valid sensor readings");
    }
}

void parseHTTPRequest(const String& line) {
    html_get_request = GET_unknown;
    Serial.print("Raw request: ");
    Serial.println(line);
    
    // Extract the path from the GET request
    int pathStart = line.indexOf("GET ") + 4;
    int pathEnd = line.indexOf(" HTTP");
    
    if (pathStart >= 4 && pathEnd > pathStart) {
        String path = line.substring(pathStart, pathEnd);
        Serial.print("Extracted path: '");
        Serial.print(path);
        Serial.println("'");
        
        // Match paths
        if (path == "/" || path.length() == 0) {
            html_get_request = GET_index_page;
            Serial.println("Matched: index page");
        }
        else if (path == "/data.js") {
            html_get_request = GET_data_js;
            Serial.println("Matched: data.js");
        }
        else if (path == "/api/data") {
            html_get_request = GET_api_data;
            Serial.println("Matched: api/data");
        }
        else if (path == "/api/temperature") {
            html_get_request = GET_api_temperature;
            Serial.println("Matched: api/temperature");
        }
        else if (path == "/api/humidity") {
            html_get_request = GET_api_humidity;
            Serial.println("Matched: api/humidity");
        }
        else if (path == "/api/pressure") {
            html_get_request = GET_api_pressure;
            Serial.println("Matched: api/pressure");
        }
    }
    
    Serial.print("Final request type: ");
    Serial.println(html_get_request);
}

void handleHTTPRequest(WiFiClient& client) {
    String currentLine = "";
    unsigned long requestStart = millis();
    
    while (client.connected() && millis() - requestStart < 1000) { // 1 second timeout
        if (client.available()) {
            char c = client.read();
            if (c == '\n') {
                if (currentLine.length() == 0) {
                    sendHTTPResponse(client);
                    break;
                } else {
                    if (currentLine.startsWith("GET")) {
                        parseHTTPRequest(currentLine);
                    }
                    currentLine = "";
                }
            } else if (c != '\r') {
                currentLine += c;
            }
        }
    }
    client.stop();
}

void sendHTTPResponse(WiFiClient& client) {
    Serial.print("Sending response for request type: ");
    Serial.println(html_get_request);
    
    switch(html_get_request) {
        case GET_index_page:
            Serial.println("Sending index.html");
            client.println("HTTP/1.1 200 OK");
            client.println("Content-Type: text/html");
            client.println("Connection: close");
            client.println();
            client.write_P(index_html, sizeof(index_html));
            break;
            
        case GET_api_data: {
            Serial.println("Sending API data");
            char json[128];
            snprintf(json, sizeof(json), 
                    "{\"temperature\":%.1f,\"humidity\":%.1f,\"pressure\":%.1f}",
                    sht30_Temperature, sht30_Humidity, qmp_Pressure/100.0F);
            sendJSONResponse(client, json);
            break;
        }
        
        case GET_api_temperature: {
            char json[64];
            snprintf(json, sizeof(json), "{\"temperature\":%.1f}", sht30_Temperature);
            sendJSONResponse(client, json);
            break;
        }
        
        case GET_api_humidity: {
            char json[64];
            snprintf(json, sizeof(json), "{\"humidity\":%.1f}", sht30_Humidity);
            sendJSONResponse(client, json);
            break;
        }
        
        case GET_api_pressure: {
            char json[64];
            snprintf(json, sizeof(json), "{\"pressure\":%.1f}", qmp_Pressure/100.0F);
            sendJSONResponse(client, json);
            break;
        }
        
        case GET_data_js:
            client.println("HTTP/1.1 200 OK");
            client.println("Content-Type: application/javascript");
            client.println("Connection: close");
            client.println();
            client.printf("var temperatureValue = %.1f;\n", sht30_Temperature);
            client.printf("var humidityValue = %.1f;\n", sht30_Humidity);
            client.printf("var pressureValue = %.1f;", qmp_Pressure/100.0F);
            break;
            
        default:
            Serial.println("Sending 404 Not Found");
            client.println("HTTP/1.1 404 Not Found");
            client.println("Content-Type: text/html");
            client.println("Connection: close");
            client.println();
            client.println("404 Not Found");
            break;
    }
}

void sendJSONResponse(WiFiClient& client, const char* json) {
    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: application/json");
    client.println("Access-Control-Allow-Origin: *");
    client.println("Connection: close");
    client.println();
    client.println(json);
}

void loop() {
    unsigned long current_time = millis();
    
    // Read sensors at regular intervals
    if (current_time > next_sensor_read) {
        readSensors();
        next_sensor_read = current_time + SENSOR_READ_INTERVAL;
    }
    
    // Handle WiFi clients more frequently
    if (current_time > next_wifi_check) {
        WiFiClient client = server.available();
        if (client) {
            handleHTTPRequest(client);
        }
        
        // Check WiFi connection
        if (WiFi.status() != WL_CONNECTED) {
            connect_Wifi();
        }
        
        next_wifi_check = current_time + WIFI_CHECK_INTERVAL;
    }
    
    displayIP();
}

boolean connect_Wifi() {
    WiFi.begin(ssid, password);
    int retry_counter = 0;
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        retry_counter++;
        if (retry_counter > 10) {
            return false;
        }
    }
    return true;
}

void displayIP() {
    unsigned long current_time = millis();
    if (current_time > next_ip_display && WiFi.status() == WL_CONNECTED) {
        Serial.print("IP Address: ");
        Serial.println(WiFi.localIP());
        next_ip_display = current_time + IP_DISPLAY_INTERVAL;
    }
}
