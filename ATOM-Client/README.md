# Environmental Monitor PWA

A Progressive Web App for environmental monitoring using Web Bluetooth.

## Installation Options

### 1. Install as PWA (Recommended)
1. Visit the deployed app at `[Your deployed URL]`
2. In Chrome/Edge:
   - Click the install icon ("+") in the address bar
   - Or click the three dots menu → "Install Environmental Monitor"
3. On Android:
   - Tap the three dots menu
   - Select "Add to Home Screen"
4. On iOS:
   - Tap the share button
   - Select "Add to Home Screen"

### 2. Local Installation

#### Quick Start (Recommended)
1. Download the latest release from the releases page
2. Extract the ZIP file
3. Launch the application:
   - On Windows: Double-click `start.bat`
   - On macOS: Double-click `start.command`

The launcher will automatically:
- Check if Node.js is installed
- Install required dependencies
- Start the local server
- Open the application in your default browser

#### Manual Installation
If you prefer to run the server manually:
1. Download the latest release from the releases page
2. Extract the ZIP file
3. Install dependencies:
   ```bash
   npm install
   ```
4. Start the server using one of these options:

   Option A - Using npm (recommended):
   ```bash
   npm start
   ```

   Option B - Using Python:
   ```bash
   python3 -m http.server 3000
   ```

5. Open Chrome and visit: `http://localhost:3000`

Note: If using Python server, you'll need Python 3 installed on your system.

## Web Bluetooth Setup

### Desktop (Chrome/Edge)
1. Enable Web Bluetooth:
   - Go to `chrome://flags/#enable-web-bluetooth`
   - Enable "Web Bluetooth"
   - Click "Relaunch"
2. Refresh the app page

### Android
- Web Bluetooth is supported by default in Chrome for Android
- Requirements:
  1. Chrome version 56 or later
  2. Android 6.0 (Marshmallow) or later
  3. Bluetooth enabled in phone settings
  4. Location services enabled
  5. Location permission granted to Chrome
  6. Secure context (HTTPS or localhost)

Testing on Android:

Option 1 - Using ADB (Recommended):
1. Connect your Android device via USB
2. Enable USB debugging on your device
3. Install ADB on your computer
4. Run this command:
   ```bash
   adb reverse tcp:3000 tcp:3000
   ```
5. Access the app at: `http://localhost:3000`

Option 2 - Using HTTPS:
1. Deploy the app to a secure hosting service
2. Access via HTTPS URL

Troubleshooting Android:
1. Go to Android Settings > Apps > Chrome > Permissions
2. Enable both "Location" and "Bluetooth" permissions
3. Enable Location Services in Android Quick Settings
4. Make sure Bluetooth is turned on
5. Ensure you're using either:
   - `localhost` (with adb reverse)
   - HTTPS connection

### iOS
- Due to iOS limitations, Web Bluetooth is not supported on iOS devices

## Features
- Real-time environmental monitoring
- Offline support
- Installable as a desktop/mobile app
- Bluetooth device connectivity
- Data visualization

## Requirements
- Chrome or Edge browser (desktop)
- Chrome for Android (mobile)
- Bluetooth 4.0+ capable device
- For development: Node.js 14+

## Troubleshooting
1. **Bluetooth Not Working**
   - Ensure Bluetooth is enabled on your device
   - Check browser compatibility
   - Enable Web Bluetooth flag in Chrome if needed

2. **App Won't Install**
   - Ensure you're using a supported browser
   - Check if you're on HTTPS or localhost
   - Clear browser cache and try again

## Support
For issues or questions, please [create an issue](https://github.com/fredperso/environmental-monitor/issues)
