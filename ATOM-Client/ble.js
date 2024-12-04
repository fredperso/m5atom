// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', () => {
    // BLE UUIDs
    const SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    const TEMPERATURE_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    const HUMIDITY_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a9";
    const PRESSURE_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26aa";

    let device = null;
    let server = null;
    let temperatureCharacteristic = null;
    let humidityCharacteristic = null;
    let pressureCharacteristic = null;

    // DOM Elements
    const scanButton = document.getElementById('scanButton');
    const connectButton = document.getElementById('connectButton');
    const disconnectButton = document.getElementById('disconnectButton');
    const statusDiv = document.getElementById('statusDiv');
    const connectionSteps = document.getElementById('connectionSteps');

    // Load Google Charts
    google.charts.load('current', {'packages':['gauge']});
    google.charts.setOnLoadCallback(drawGauges);

    let temperatureGauge, humidityGauge, pressureGauge;
    let temperatureData, humidityData, pressureData;

    // Define gauge options globally so they can be used by both drawGauges and updateGauge
    const temperatureOptions = {
        width: 300, height: 300,
        redFrom: 35, redTo: 40,
        yellowFrom: 30, yellowTo: 35,
        minorTicks: 5,
        min: -10,
        max: 40
    };

    const humidityOptions = {
        width: 300, height: 300,
        redFrom: 70, redTo: 80,
        yellowFrom: 60, yellowTo: 70,
        minorTicks: 5,
        min: 20,
        max: 80
    };

    const pressureOptions = {
        width: 300, height: 300,
        redFrom: 1030, redTo: 1040,
        yellowFrom: 1020, yellowTo: 1030,
        minorTicks: 5,
        min: 980,
        max: 1040
    };

    function drawGauges() {
        temperatureData = google.visualization.arrayToDataTable([
            ['Label', 'Value'],
            ['Temp. (°C)', 20]
        ]);

        humidityData = google.visualization.arrayToDataTable([
            ['Label', 'Value'],
            ['Humidity (%)', 50]
        ]);

        pressureData = google.visualization.arrayToDataTable([
            ['Label', 'Value'],
            ['Pressure (hPa)', 1013]
        ]);

        temperatureGauge = new google.visualization.Gauge(document.getElementById('temperatureGauge'));
        humidityGauge = new google.visualization.Gauge(document.getElementById('humidityGauge'));
        pressureGauge = new google.visualization.Gauge(document.getElementById('pressureGauge'));

        temperatureGauge.draw(temperatureData, temperatureOptions);
        humidityGauge.draw(humidityData, humidityOptions);
        pressureGauge.draw(pressureData, pressureOptions);
    }

    function updateGauge(gauge, data, value) {
        let options;
        // Determine which options to use based on the gauge type
        if (gauge === temperatureGauge) {
            options = temperatureOptions;
        } else if (gauge === humidityGauge) {
            options = humidityOptions;
        } else if (gauge === pressureGauge) {
            options = pressureOptions;
        }

        data.setValue(0, 1, value);
        gauge.draw(data, options);
    }

    // Debug click handler
    scanButton.onclick = async () => {
        console.log('Scan button clicked');
        try {
            connectionSteps.classList.add('active');
            device = await navigator.bluetooth.requestDevice({
                filters: [{
                    services: [SERVICE_UUID]
                }]
            });
            
            connectButton.disabled = false;
            updateConnectionStatus('Device selected');
            
        } catch (error) {
            console.error('Error scanning:', error);
            updateConnectionStatus('Scan failed');
        }
    };

    // Connect to device
    connectButton.onclick = async () => {
        console.log('Connect button clicked');
        try {
            server = await device.gatt.connect();
            const service = await server.getPrimaryService(SERVICE_UUID);
            
            temperatureCharacteristic = await service.getCharacteristic(TEMPERATURE_UUID);
            humidityCharacteristic = await service.getCharacteristic(HUMIDITY_UUID);
            pressureCharacteristic = await service.getCharacteristic(PRESSURE_UUID);
            
            await startNotifications();
            
            updateConnectionStatus(true);
            connectButton.disabled = true;
            disconnectButton.disabled = false;
            connectionSteps.classList.remove('active');
            
        } catch (error) {
            console.error('Connection error:', error);
            updateConnectionStatus(false);
        }
    };

    // Disconnect from device
    disconnectButton.onclick = async () => {
        console.log('Disconnect button clicked');
        if (device && device.gatt.connected) {
            await device.gatt.disconnect();
        }
        onDisconnected();
    };

    // Start notifications for all characteristics
    async function startNotifications() {
        await setupCharacteristic(temperatureCharacteristic, 'temperature', temperatureGauge, temperatureData);
        await setupCharacteristic(humidityCharacteristic, 'humidity', humidityGauge, humidityData);
        await setupCharacteristic(pressureCharacteristic, 'pressure', pressureGauge, pressureData);
    }

    // Handle characteristic setup and notifications
    function setupCharacteristic(characteristic, type, gauge, data) {
        return characteristic.startNotifications().then(() => {
            characteristic.addEventListener('characteristicvaluechanged', (event) => {
                try {
                    // Get the DataView from the characteristic
                    const dataView = event.target.value;
                    
                    // Convert DataView to Uint8Array
                    const bytes = new Uint8Array(dataView.buffer);
                    
                    // Convert bytes to UTF-8 string
                    const decoder = new TextDecoder('utf-8');
                    const stringValue = decoder.decode(bytes);
                    
                    // Parse the string to float
                    let value = parseFloat(stringValue);
                    
                    // Convert values to appropriate units
                    if (type === 'pressure') {
                        // Convert Pascal to hectopascal (hPa)
                        value = value / 100;
                    }
                    
                    // Log the value for debugging
                    console.log(`${type} string value: "${stringValue}"`);
                    console.log(`${type} parsed value: ${value.toFixed(2)} ${type === 'pressure' ? 'hPa' : type === 'temperature' ? '°C' : '%'}`);
                    
                    // Update the gauge with the processed value
                    updateGauge(gauge, data, value);
                } catch (error) {
                    console.error(`Error reading ${type} value:`, error);
                    console.error('Raw bytes:', Array.from(new Uint8Array(event.target.value.buffer)));
                }
            });
        });
    }

    // Handle disconnection
    function onDisconnected() {
        connectButton.disabled = false;
        disconnectButton.disabled = true;
        updateConnectionStatus('disconnected');
        
        // Reset values and gauges
        temperatureData.setValue(0, 1, 20);
        humidityData.setValue(0, 1, 50);
        pressureData.setValue(0, 1, 1013);
        
        temperatureGauge.draw(temperatureData, temperatureOptions);
        humidityGauge.draw(humidityData, humidityOptions);
        pressureGauge.draw(pressureData, pressureOptions);
        
        device = null;
        server = null;
    }

    // Update connection status display
    function updateConnectionStatus(status) {
        if (typeof status === 'boolean') {
            statusDiv.textContent = status ? 'Connected' : 'Disconnected';
            statusDiv.className = `status ${status ? 'connected' : 'disconnected'}`;
        } else {
            statusDiv.textContent = status;
            statusDiv.className = 'status';
        }
    }

    // Check if Web Bluetooth is supported
    if (!navigator.bluetooth) {
        alert('Web Bluetooth is not supported in this browser. Please use a compatible browser like Chrome.');
        scanButton.disabled = true;
        connectButton.disabled = true;
        disconnectButton.disabled = true;
    }
});
