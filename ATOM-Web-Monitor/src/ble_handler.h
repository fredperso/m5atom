#ifndef BLE_HANDLER_H
#define BLE_HANDLER_H

#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// BLE service and characteristic UUIDs
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define TEMPERATURE_UUID    "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define HUMIDITY_UUID       "beb5483e-36e1-4688-b7f5-ea07361b26a9"
#define PRESSURE_UUID       "beb5483e-36e1-4688-b7f5-ea07361b26aa"

class BLEHandler {
public:
    BLEHandler();
    bool init(const char* deviceName = "M5ATOM-ENV");
    void updateSensorValues(float temperature, float humidity, float pressure);
    bool isConnected();

private:
    BLEServer* pServer;
    BLECharacteristic* pTemperatureCharacteristic;
    BLECharacteristic* pHumidityCharacteristic;
    BLECharacteristic* pPressureCharacteristic;
    bool deviceConnected;
    
    class ServerCallbacks: public BLEServerCallbacks {
    private:
        BLEHandler* handler;
    public:
        ServerCallbacks(BLEHandler* h) : handler(h) {}
        void onConnect(BLEServer* pServer);
        void onDisconnect(BLEServer* pServer);
    };
};

#endif // BLE_HANDLER_H
