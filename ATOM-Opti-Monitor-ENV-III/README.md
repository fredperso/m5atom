# ATOM Environment Monitor

A power-optimized BLE environmental monitoring system for the M5Stack ATOM using ENV.II Unit (SHT30 and QMP6988 sensors).

## Features

- 🌡️ Temperature monitoring via SHT30
- 💧 Humidity monitoring via SHT30
- 🌪️ Pressure monitoring via QMP6988
- 📱 BLE connectivity for real-time data access
- ⚡ Power-optimized operation
- 💡 LED status indicators

## Hardware Requirements

- M5Stack ATOM Matrix/Lite
- ENV.II Unit (with SHT30 and QMP6988 sensors)

## Dependencies

- M5Atom Library
- Arduino BLE Library
- UNIT_ENV Library

## LED Status Indicators

- 🟢 Green: BLE connected and sensors working
- 🔵 Blue: BLE disconnected, sensors working
- 🔴 Red: Sensor error
- 🔴 Red Flash: Data transmission

## Installation

### 1. Install PlatformIO

If you haven't already installed PlatformIO, you can install it as a VS Code extension:

1. Open VS Code
2. Go to the Extensions view (Ctrl+Shift+X or Cmd+Shift+X)
3. Search for "PlatformIO"
4. Install "PlatformIO IDE"

### 2. Clone the Repository

```bash
git clone https://github.com/fred/ATOM-opti-Monitor.git
cd ATOM-opti-Monitor
```

### 3. Open Project in PlatformIO

1. Open VS Code
2. Click "Open Folder" and select the ATOM-opti-Monitor directory
3. Wait for PlatformIO to initialize the project

### 4. Install Dependencies

PlatformIO will automatically install the required libraries listed in `platformio.ini`.

### 5. Build and Upload

1. Connect your M5Stack ATOM to your computer
2. Click the "PlatformIO: Upload" button (→) in the bottom toolbar
   - Or use the command: `pio run -t upload`

## Project Structure

```
ATOM-opti-Monitor/
├── src/
│   ├── main.cpp           # Main application code
│   ├── ble_handler.cpp    # BLE implementation
│   └── ble_handler.h      # BLE header file
├── platformio.ini         # Project configuration
└── README.md             # This file
```

## Configuration

The default configuration can be modified in `src/main.cpp`:

```cpp
const unsigned long SENSOR_READ_INTERVAL = 3000;  // Sensor reading interval (ms)
const unsigned long LED_FLASH_DURATION = 300;     // LED flash duration (ms)
const uint8_t LED_BRIGHTNESS = 20;               // LED brightness (0-255)
```

## Power Optimization Features

### CPU Management
- Dynamic CPU frequency scaling (80MHz active, 40MHz idle)
- Automatic light sleep mode between operations
- Optimized timing for all operations

### Power-Efficient LED Feedback
- Brief LED flashes (50ms) only during data transmission
- Reduced LED brightness (100/255) for power saving
- Very dim standby light when disconnected
- LED behavior directly tied to data transmission events

### Sensor and BLE Operations
- Optimized 3-second interval for sensor readings
- Power-efficient BLE advertising
- Sleep mode when no BLE connection
- Minimal debug output

## LED Indicator Guide

- **Blue Flash**: Data is being transmitted to BLE client
- **Dim White**: Device is powered but not connected
- **Off**: LED is off between transmissions to save power
- **Initial Red**: BLE initialization failed
- **Initial Green**: BLE initialization successful

## Technical Details

### Timing Specifications
- Sensor Reading Interval: 3 seconds
- LED Flash Duration: 50ms
- Main Loop Delay: 100ms
- Sleep Duration: ~2.9 seconds (when disconnected)

### Power Management
```cpp
// CPU Configuration
max_freq_mhz = 80  // Active mode
min_freq_mhz = 40  // Idle mode
light_sleep_enable = true
```

## BLE Service Characteristics

The device broadcasts the following characteristics:

- Temperature: UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
- Humidity: UUID "beb5483e-36e1-4688-b7f5-ea07361b26a9"
- Pressure: UUID "beb5483e-36e1-4688-b7f5-ea07361b26aa"

## Troubleshooting

### Common Issues

1. **Sensor Initialization Failure**
   - LED shows solid red
   - Check I2C connections
   - Verify ENV.II Unit is properly connected

2. **BLE Connection Issues**
   - LED stays blue
   - Ensure device is in range
   - Check if BLE is enabled on your client device

3. **Compilation Errors**
   - Verify all dependencies are properly installed
   - Check PlatformIO IDE is up to date
   - Ensure correct board selection in platformio.ini

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- M5Stack for the ATOM hardware
- FastLED library contributors
- Arduino BLE library maintainers
