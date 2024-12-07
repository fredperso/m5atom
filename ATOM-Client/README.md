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
1. Download the latest release from the releases page
2. Extract the ZIP file
3. Install dependencies:
   ```bash
   npm install
   ```
4. Start the local server:
   ```bash
   npm start
   ```
5. Open Chrome and visit: `http://localhost:3000`

## Web Bluetooth Setup

### Desktop (Chrome/Edge)
1. Enable Web Bluetooth:
   - Go to `chrome://flags/#enable-web-bluetooth`
   - Enable "Web Bluetooth"
   - Click "Relaunch"
2. Refresh the app page

### Android
- Web Bluetooth is supported by default in Chrome for Android

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
