# Android App Build Guide

This guide explains how to build and install the Android version of the Environmental Monitor app.

## Prerequisites

1. Java Development Kit (JDK) 17
   ```bash
   brew install openjdk@17
   ```

2. Android SDK
   - Download from Android Studio or command line tools
   - Set `ANDROID_HOME` or create `local.properties` with:
     ```properties
     sdk.dir=/path/to/your/AndroidSDK
     ```

3. Gradle (automatically handled by wrapper)

## Building the App

### Option 1: Command Line Build

1. Package the PWA files:
```bash
./package-android.sh
```

2. Build the debug APK:
```bash
cd android
./gradlew assembleDebug
```

The APK will be available at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Option 2: Android Studio

1. Package the PWA files:
```bash
./package-android.sh
```

2. Open the `android` folder in Android Studio
3. Wait for project sync to complete
4. Click Build > Build Bundle(s) / APK(s) > Build APK(s)

## Installing the App

### Method 1: Using ADB

1. Enable USB debugging on your Android device:
   - Go to Settings > About phone
   - Tap "Build number" 7 times
   - Go back to Settings > System > Developer options
   - Enable "USB debugging"

2. Connect your device via USB

3. Install using Gradle:
```bash
./gradlew installDebug
```

Or using ADB directly:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Method 2: Manual Installation

1. Transfer the APK to your Android device
2. On your device, navigate to the APK file
3. Tap to install (enable "Install from unknown sources" if prompted)

## App Features

- Embedded local server for PWA hosting
- Full Web Bluetooth support
- Offline functionality
- Native Android integration

## Permissions

The app requires the following permissions:
- Internet access
- Bluetooth
- Bluetooth Admin
- Location (required for Bluetooth scanning)
- Storage (for offline functionality)

## Troubleshooting

### Build Issues

1. **SDK Location Error**
   - Create/update `local.properties` with correct SDK path
   - Verify Android SDK installation

2. **Java Version Error**
   - Ensure JDK 17 is installed
   - Update `JAVA_HOME` environment variable
   - Check `gradle.properties` configuration

3. **Gradle Sync Failed**
   - Check internet connection
   - Clear Gradle caches: `./gradlew cleanBuildCache`
   - Update Gradle wrapper: `./gradlew wrapper --gradle-version=8.0`

### Installation Issues

1. **App Not Installing**
   - Enable "Install from unknown sources"
   - Check USB debugging is enabled
   - Verify ADB connection: `adb devices`

2. **Bluetooth Not Working**
   - Grant all required permissions
   - Enable location services
   - Check Android system Bluetooth settings

## Development Notes

- The app uses NanoHTTPD for the embedded server
- PWA files are stored in `app/src/main/assets/www/`
- Default port is 8080
- WebView is configured for Web Bluetooth support

## Building Release Version

For release builds:

1. Create a keystore:
```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

2. Configure signing in `app/build.gradle`

3. Build release APK:
```bash
./gradlew assembleRelease
```

## Contributing

[Your contribution guidelines]

## License

[Your license information]
