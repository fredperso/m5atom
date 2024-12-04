#include "ble_handler.h"

BLEHandler::BLEHandler() : deviceConnected(false) {}

bool BLEHandler::init(const char* deviceName) {
    try {
        // Create the BLE Device
        BLEDevice::init(deviceName);
        Serial.println("BLE Device initialized");

        // Create the BLE Server
        pServer = BLEDevice::createServer();
        if (!pServer) {
            Serial.println("Failed to create BLE server");
            return false;
        }
        pServer->setCallbacks(new ServerCallbacks(this));
        Serial.println("BLE Server created");

        // Create the BLE Service
        BLEService *pService = pServer->createService(SERVICE_UUID);
        if (!pService) {
            Serial.println("Failed to create BLE service");
            return false;
        }
        Serial.println("BLE Service created");

        // Create BLE Characteristics
        pTemperatureCharacteristic = pService->createCharacteristic(
            TEMPERATURE_UUID,
            BLECharacteristic::PROPERTY_READ |
            BLECharacteristic::PROPERTY_NOTIFY
        );
        pTemperatureCharacteristic->addDescriptor(new BLE2902());

        pHumidityCharacteristic = pService->createCharacteristic(
            HUMIDITY_UUID,
            BLECharacteristic::PROPERTY_READ |
            BLECharacteristic::PROPERTY_NOTIFY
        );
        pHumidityCharacteristic->addDescriptor(new BLE2902());

        pPressureCharacteristic = pService->createCharacteristic(
            PRESSURE_UUID,
            BLECharacteristic::PROPERTY_READ |
            BLECharacteristic::PROPERTY_NOTIFY
        );
        pPressureCharacteristic->addDescriptor(new BLE2902());
        Serial.println("BLE Characteristics created");

        // Start the service
        pService->start();
        Serial.println("BLE Service started");

        // Start advertising
        BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
        pAdvertising->addServiceUUID(SERVICE_UUID);
        pAdvertising->setScanResponse(true);
        pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
        pAdvertising->setMinPreferred(0x12);
        BLEDevice::startAdvertising();
        Serial.println("BLE Advertising started");

        return true;
    } catch (const std::exception& e) {
        Serial.print("BLE initialization error: ");
        Serial.println(e.what());
        return false;
    }
}

void BLEHandler::updateSensorValues(float temperature, float humidity, float pressure) {
    if (deviceConnected) {
        char tempStr[8];
        dtostrf(temperature, 6, 2, tempStr);
        pTemperatureCharacteristic->setValue(tempStr);
        pTemperatureCharacteristic->notify();
        
        dtostrf(humidity, 6, 2, tempStr);
        pHumidityCharacteristic->setValue(tempStr);
        pHumidityCharacteristic->notify();
        
        dtostrf(pressure, 6, 2, tempStr);
        pPressureCharacteristic->setValue(tempStr);
        pPressureCharacteristic->notify();
    }
}

bool BLEHandler::isConnected() {
    return deviceConnected;
}

void BLEHandler::ServerCallbacks::onConnect(BLEServer* pServer) {
    handler->deviceConnected = true;
}

void BLEHandler::ServerCallbacks::onDisconnect(BLEServer* pServer) {
    handler->deviceConnected = false;
    // Restart advertising to allow a new client to connect
    BLEDevice::startAdvertising();
}
