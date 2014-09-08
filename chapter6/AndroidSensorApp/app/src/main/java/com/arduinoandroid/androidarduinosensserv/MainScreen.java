package com.arduinoandroid.androidarduinosensserv;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainScreen extends Activity implements SensorEventListener {

    // UUIDs for UAT service and associated characteristics.
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

    // UUID for the BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    //Getting the name for Log Tags
    private final String LOG_TAG = MainScreen.class.getSimpleName();

    /**
     * Indicates which angle we are currently pointing the phone (and hence servo) in:
     * -2: 0-45 degrees
     * -1: 45-90 degrees
     * 0: 90 degrees
     * 1: 90-135 degrees
     * 2: 135-180 degrees
     * <p/>
     * Default is the neutral position, i.e. 0.
     */
    int currentPosition = 0;

    long lastSensorChangedEventTimestamp = 0;

    //Declaring UI Elements
    private TextView gyroTextView;
    private TextView bluetoothTv;

    //Declaring SensorManager variables
    private SensorManager sensorManager;

    //Sensor Delay Methods
    int PERIOD = 1000000000; // read sensor data each second
    Handler handler;
    boolean canTransmitSensorData = false;
    boolean isHandlerLive = false;

    // Mac Address of Bluetooth LE Module
    //public final String MAC_BLE_MODULE = "DD:08:72:3A:DF:E4";
    private boolean areServicesAccessible = false;

    // BTLE state
    private BluetoothAdapter bluetoothAdaper;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;

    // Main BTLE device callback where much of the logic occurs.
    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        // Called whenever the device connection state changes, i.e. from disconnected to connected.
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                writeSensorData("Connected!");
                // Discover services.
                if (!gatt.discoverServices()) {
                    writeSensorData("Failed to start discovering services!");
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                writeSensorData("Disconnected!");
            } else {
                writeSensorData("Connection state changed.  New state: " + newState);
            }
        }

        // Called when services have been discovered on the remote device.
        // It seems to be necessary to wait for this discovery to occur before
        // manipulating any services or characteristics.
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                writeSensorData("Service discovery completed!");
            } else {
                writeSensorData("Service discovery failed with status: " + status);
            }
            // Save reference to each characteristic.
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);

            // Setup notifications on RX characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(rx, true)) {
                writeSensorData("Couldn't set notifications for RX characteristic!");
            }

            // Next update the RX characteristic's client descriptor to enable notifications.
            if (rx.getDescriptor(CLIENT_UUID) != null) {
                BluetoothGattDescriptor desc = rx.getDescriptor(CLIENT_UUID);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(desc)) {
                    writeSensorData("Couldn't write RX client descriptor value!");
                }
            } else {
                writeSensorData("Couldn't get RX client descriptor!");
            }
            areServicesAccessible = true;
        }

        // Called when a remote characteristic changes (like the RX characteristic).
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            writeSensorData(characteristic.getStringValue(0));
        }
    };

    //Bluetooth Data Output
    private String output;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        handler = new Handler();

        // Setup the refresh button
        final Button refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartScan();
            }
        });

        //get the TextView from the layout file
        gyroTextView = (TextView) findViewById(R.id.tv);
        bluetoothTv = (TextView) findViewById(R.id.btView);

        //get a hook to the sensor service
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    // BTLE device scanning callback.

    //when this Activity starts
    @Override
    protected void onStart() {
        super.onResume();

        /*register the sensor listener to listen to the gyroscope sensor, use the
        callbacks defined in this class, and gather the sensor information as quick
        as possible*/
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL
        );

        //handler.post(processSensors);

        // Scan for all BTLE devices.
        // The first one with the UART service will be chosen--see the code in the scanCallback.

        bluetoothAdaper = BluetoothAdapter.getDefaultAdapter();

        startScan();
    }

    //When this Activity isn't visible anymore
    @Override
    protected void onStop() {
        //unregister the sensor listener
        sensorManager.unregisterListener(this);
        //disconnect and close Bluetooth Connection for better reliability
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
            gatt = null;
            tx = null;
            rx = null;
        }

        super.onStop();
        areServicesAccessible = false;
    }

    //BLUETOOTH METHODS

    private void startScan() {
        if (!bluetoothAdaper.isEnabled()) {
            bluetoothAdaper.enable();
        }
        if (!bluetoothAdaper.isDiscovering()) {
            bluetoothAdaper.startDiscovery();
        }
        writeSensorData("Scanning for devices...");
        bluetoothAdaper.startLeScan(scanCallback);
    }

    private void stopScan() {
        if (bluetoothAdaper.isDiscovering()) {
            bluetoothAdaper.cancelDiscovery();
        }
        writeSensorData("Stopping scan");
        bluetoothAdaper.stopLeScan(scanCallback);
    }

    private void restartScan() {
        stopScan();
        startScan();
    }

    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        // Called when a device is found.
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            Log.d(LOG_TAG, bluetoothDevice.getAddress());

            writeSensorData("Found device: " + bluetoothDevice.getAddress());

            // Check if the device has the UART service.
            if (parseUUIDs(bytes).contains(UART_UUID)) {
                // Found a device, stop the scan.
                bluetoothAdaper.stopLeScan(scanCallback);
                writeSensorData("Found UART service!");
                // Connect to the device.
                // Control flow will now go to the callback functions when BTLE events occur.
                gatt = bluetoothDevice.connectGatt(getApplicationContext(), false, callback);
            }
        }
    };

    // Filtering by custom UUID is broken in Android 4.3 and 4.4, see:
    // http://stackoverflow.com/questions/18019161/startlescan-with-128-bit-uuids-doesnt-work-on-native-android-ble-implementation?noredirect=1#comment27879874_18019161
    // This is a workaround function from the SO thread to manually parse advertisement data.
    private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0) {
                break;
            }

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer =
                                    ByteBuffer.wrap(advertisedData, offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            //Log.e(LOG_TAG, e.toString());
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return uuids;
    }

    //SENSOR METHODS

    private final Runnable processSensors = new Runnable() {
        @Override
        public void run() {
            // Do work with the sensor values.
            canTransmitSensorData = !canTransmitSensorData;
            // The Runnable is posted to run again here:
            handler.postDelayed(this, PERIOD);
        }
    };

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        //Do nothing.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if ((event.accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE)
                && (event.timestamp - lastSensorChangedEventTimestamp > PERIOD)) {

            System.out.println(event.timestamp - lastSensorChangedEventTimestamp);
            lastSensorChangedEventTimestamp = event.timestamp;

            // Truncate to an integer, since precision loss is really not a serious
            // matter here, and it will make it much easier (and cheaper) to compare.
            // We will also log the integer values of [2]
            int xTilt = (int) event.values[2];
            int yTilt = (int) event.values[1];
            int zTilt = (int) event.values[0];

            gyroTextView.setText("Orientation X (Roll) :" + xTilt + "\n" +
                    "Orientation Y (Pitch) :" + yTilt + "\n" +
                    "Orientation Z (Yaw) :" + zTilt);

            //Log.i(LOG_TAG, "The XTilt is:" + String.valueOf(xTilt));

            if (areServicesAccessible) {
                turnServoFinegrained(xTilt);
            }
        }
    }

    private void turnServoCoarseGrained(int xTilt) {
        if ((xTilt <= 90 && xTilt > 45) && currentPosition != -2) {
            String setServoMessage = "/servo?params=0 /";
            tx.setValue(setServoMessage.getBytes(Charset.forName("UTF-8")));
            if (gatt.writeCharacteristic(tx)) {
                writeSensorData("Sent: " + setServoMessage);
            } else {
                writeSensorData("Couldn't write TX characteristic!");
            }
            currentPosition = -2;

        } else if ((xTilt <= 45 && xTilt > 0) && currentPosition != -1) {
            // send 45 to servo
            String setServoMessage = "/servo?params=45 /";
            tx.setValue(setServoMessage.getBytes(Charset.forName("UTF-8")));
            if (gatt.writeCharacteristic(tx)) {
                writeSensorData("Sent: " + setServoMessage);
            } else {
                writeSensorData("Couldn't write TX characteristic!");
            }
            currentPosition = -1;

        } else if ((xTilt == 0) && currentPosition != 0) {
            // send 90 to servo
            String setTempMessage = "/servo?params=90 /";
            tx.setValue(setTempMessage.getBytes(Charset.forName("UTF-8")));
            if (gatt.writeCharacteristic(tx)) {
                writeSensorData("Sent: " + setTempMessage);
            } else {
                writeSensorData("Couldn't write TX characteristic!");
            }
            currentPosition = 0;

        } else if ((xTilt <= 0 && xTilt > -45) && currentPosition != 1) {
            // send 135 to servo
            String setServoMessage = "/servo?params=135 /";
            tx.setValue(setServoMessage.getBytes(Charset.forName("UTF-8")));
            if (gatt.writeCharacteristic(tx)) {
                writeSensorData("Sent: " + setServoMessage);
            } else {
                writeSensorData("Couldn't write TX characteristic!");
            }
            currentPosition = 1;

        } else if ((xTilt <= -45 && xTilt > -90) && currentPosition != 2) {
            // send 180 to servo
            String setServoMessage = "/servo?params=180 /";
            tx.setValue(setServoMessage.getBytes(Charset.forName("UTF-8")));
            if (gatt.writeCharacteristic(tx)) {
                writeSensorData("Sent: " + setServoMessage);
            } else {
                writeSensorData("Couldn't write TX characteristic!");
            }
            currentPosition = 2;
        }
    }

    private void turnServoFinegrained(int xTilt) {

        // Default to vertical position
        int rotationAngle = 90;

        // Turn left
        if (xTilt > 0) {
            rotationAngle = 90 - xTilt;
        }

        // Turn right
        else {
            rotationAngle = 90 + Math.abs(xTilt);
        }

        String setServoMessage = "/servo?params=" + rotationAngle + " /";
        tx.setValue(setServoMessage.getBytes(Charset.forName("UTF-8")));
        if (gatt.writeCharacteristic(tx)) {
            writeSensorData("Sent: " + setServoMessage);
        } else {
            writeSensorData("Couldn't write TX characteristic!");
        }
    }

    private void writeSensorData(final CharSequence text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(LOG_TAG, text.toString());
                //bluetoothTv = (TextView) findViewById(R.id.btView);
                output = text.toString();
                bluetoothTv.setText(output);
            }
        });
    }
}
