# Environmental Monitor Android App

A native Android application for monitoring environmental sensors via Bluetooth Low Energy (BLE).

## Features

- Scan for BLE devices
- Connect to environmental sensor devices
- Display real-time sensor data
- Native Android UI
- Efficient Bluetooth communication

## Requirements

- Android Studio Arctic Fox or newer
- Android SDK 29 or higher
- Android device with Bluetooth LE support

## Building the App

1. Open the project in Android Studio
2. Sync project with Gradle files
3. Build the project
4. Run on your Android device

## Permissions

The app requires the following permissions:
- Bluetooth
- Bluetooth Admin
- Location (for BLE scanning)
- Bluetooth Scan (Android 12+)
- Bluetooth Connect (Android 12+)

## Architecture

The app uses a native Android architecture with:
- Native UI components (ConstraintLayout, RecyclerView)
- Android Bluetooth LE APIs
- Material Design components
- MVVM pattern for sensor data handling

## License

[Your License Here]
