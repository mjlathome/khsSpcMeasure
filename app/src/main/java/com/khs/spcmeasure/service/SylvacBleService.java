package com.khs.spcmeasure.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.khs.spcmeasure.library.ConnectionState;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.library.SylvacGattAttributes;
import com.khs.spcmeasure.library.NotificationId;
import com.khs.spcmeasure.ui.SetupListActivity;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

// handle all Bluetooth Low Energy (BLE) communication
// queues are now used for the write/read requests.  see:
// http://stackoverflow.com/questions/17910322/android-ble-api-gatt-notification-not-received
//
// I'm going to implement Bluetooth Gatt,
// Cuz that's where the measurements at at.
// For my operations I'm using a queue,
// Cuz without that the measurements won't come through.
// I'll take your Android App to another dimension,
// I'll take your Android App to another dimension,
// Pay close attention.
//
public class SylvacBleService extends Service {	
	private static final String TAG = "SylvacBleService";

    // queue read/write requests
    private Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<BluetoothGattDescriptor>();
    private Queue<BluetoothGattCharacteristic> characteristicReadQueue = new LinkedList<BluetoothGattCharacteristic>();
    // TODO use queue of characteristic and the value written so that this can be correctly returned instead of mLastWrite
    private Queue<BluetoothGattCharacteristic> characteristicWriteQueue = new LinkedList<BluetoothGattCharacteristic>();

	private static final String DEVICE_NAME_BONDED   = "SY";
    private static final String DEVICE_NAME_UNBONDED = "SY289";
	
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
	
	private final IBinder myBinder = new MyLocalBinder();	

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mConnectedGatt;
    private BluetoothDevice mDevice;
    private String mDeviceAddress = null;
    
    private Handler mHandler;
    
    private NotificationManager mNotificationManager;
    
    private boolean mScanning;
    private String mLastWrite = "";
    private boolean mCanWrite = true;

    private int mNotifyId = NotificationId.getId();
    
    // local broadcast intent actions
    // public final static String ACTION_PREFIX = "com.example.localbound_";
    public final static String ACTION_PREFIX = "com.khs.spcmeasure_";
    public final static String ACTION_BLE_NOT_SUPPORTED = ACTION_PREFIX + "ACTION_BLE_NOT_SUPPORTED";
    public final static String ACTION_BLUETOOTH_NOT_ENABLED = ACTION_PREFIX + "ACTION_BLUETOOTH_NOT_ENABLED";
    public final static String ACTION_GATT_CONNECTED    = ACTION_PREFIX + "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = ACTION_PREFIX + "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = ACTION_PREFIX + "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_CHARACTERISTIC_CHANGED = ACTION_PREFIX + "ACTION_CHARACTERISTIC_CHANGED";
    
    // local broadcast intent data
    public final static String EXTRA_DATA_CHAR_DATA = "CHAR_DATA";
    public final static String EXTRA_DATA_CHAR_UUID = "CHAR_UUID";
    public final static String EXTRA_DATA_CHAR_LAST_WRITE = "CHAR_LAST_WRITE";
    
    // sylvac commands
    public final static String COMMAND_GET_INSTRUMENT_ID_CODE = "ID?\r";
    public final static String COMMAND_SET_ZERO_RESET = "SET\r";
    public final static String COMMAND_GET_CURRENT_VALUE = "?\r";
    public final static String COMMAND_SET_MEASUREMENT_UOM_MM = "MM\r";
    public final static String COMMAND_GET_BATTERY_STATUS = "BAT?\r";
    public final static String COMMAND_SET_FAVOURITE_DATA_TRANSMISSION = "FCT0\r";
        
    // sylvac battery states
    public final static String BATTERY_OK  = "BAT1\r";
    public final static String BATTERY_LOW = "BAT0\r";
    
    // service state
    private ConnectionState mConnectionState = ConnectionState.DISCONNECTED;

    // receiver for local broadcast messages
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // handle app closure by stopping BLE service
            if (action.equals(SetupListActivity.SPC_MEASURE_CLOSE)) {
                stopSelf();
            }
        }
    };

	@Override
	public void onCreate() {
        Log.d(TAG, "onCreate");
		super.onCreate();

        // register for app closure local broadcast event
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(SetupListActivity.SPC_MEASURE_CLOSE));

		// create new handler for queuing runnables i.e. stopLeScan
		mHandler = new Handler();
		
		// extract notification manager
		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		updateNotification();
		
        // Check for Bluetooth LE Support.  In production, the manifest entry will keep this
        // from installing on these devices, but this will allow test devices or other
        // sideloads to report whether or not the feature exists.
        // NOTE: Not really needed as included in Manifest.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        	broadcastUpdate(ACTION_BLE_NOT_SUPPORTED);        	
        } else {	
        	// obtain bluetooth adapter
        	mBluetoothAdapter = getBluetoothAdapter();
			
	        // Ensures Bluetooth is available on the device and it is enabled. If not,
	        // displays a dialog requesting user permission to enable Bluetooth.
	        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
	        	broadcastUpdate(ACTION_BLUETOOTH_NOT_ENABLED);
	        } else {
                // TODO remove later - now calls connectDevice
//	        	// initiate Ble scan
//	        	scanLeDevice(true);

                // No longer immediately to the device, this will be established when necessary
                // connectDevice();
	        }	        
        }
        
        return;
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ServiceStarted");

        // let it continue running until it is stopped.
        return START_STICKY;
    }

    @Override
	public IBinder onBind(Intent intent) {
		// binder has getService method to obtain reference to this Service
		return myBinder;
	}

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: " + this.getClass().getName());
        return super.onUnbind(intent);
    }

    // perform clean-uo prior to exit
	@Override
	public void onDestroy() {
        Log.d(TAG, "onDestroy");

        // unregister for app closure local broadcast event
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        // ensure Ble resources are released
        mConnectedGatt = disconnectGatt(mConnectedGatt);

        // TODO do this first?
        removeNotification();

		super.onDestroy();

        // TODO remove later
//        Log.d(TAG, "onDestroy: mConnGatt = " + mConnectedGatt);
//
//		// ensure Ble resources are released
//        disconnectDevice();
//
//        // TODO do this first?
//	    removeNotification();
//
//	    return;
	}

	public class MyLocalBinder extends Binder {
		// allow bound component to obtain a reference to the Service for internal calls
		public SylvacBleService getService() {
			return SylvacBleService.this;
		}
	}	
	
	// scan for the Sylvac Ble device
    public void scanLeDevice(final boolean enable) {
        if (enable) {        	
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "stopLeScan - delayed");
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            // does not work correctly
            // UUID[] uuidService = { UUID.fromString(SylvacGattAttributes.DATA_RECEIVED_FROM_INSTRUMENT) };

            mScanning = true;
//            if (mBluetoothAdapter == null) {
//            	mBluetoothAdapter = getBluetoothAdapter();
//            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            // does not work correctly
            // mBluetoothAdapter.startLeScan(uuidService, mLeScanCallback);
        } else {
            Log.i(TAG, "stopLeScan - immediate");
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }	
	
	// Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device != null) {
                // check whether device name is for Sylvac device
                String name = device.getName();
                Log.d(TAG, "device name = " + name);

                // ignore null
                if (name == null) {
                    return;
                }

                if (name.equals(DEVICE_NAME_BONDED) || name.equals(DEVICE_NAME_UNBONDED)) {
                    // TODO comment out later on
//                    Log.d(TAG, "fetch = " + device.fetchUuidsWithSdp());
//                    Log.d(TAG, "UUID = " + device.getUuids());
//                    Log.d(TAG, "Name = " + device.getName());
//                    Log.d(TAG, "Type = " + device.getType());
//                    Log.d(TAG, "BT Class = " + device.getBluetoothClass());
//                    Log.d(TAG, "Address = " + device.getAddress());
//                    Log.d(TAG, "String = " + device.toString());

                    // store device address
                    mDeviceAddress = device.getAddress();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "onLeScan - runnable - connectGatt:" + mDeviceAddress);
//                            // immediately stop the BLE scan
//                            scanLeDevice(false);

                            // TODO remove later if not required
                            //	                    	mConnectionState = ConnectionState.CONNECTING;
                            //	                    	updateNotification();


                            // connect to the device
                            connectGatt(device);
                        }
                    });
                }
            }
        }
    };
	
    /*
     * In this callback, we've created a bit of a state machine to enforce that only
     * one characteristic be read or written at a time until all of our sensors
     * are enabled and we are registered to get notifications.
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.d(TAG, "onConnectionStateChange - newState = " + newState);

            switch(newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "gattCallback - STATE_CONNECTED");
                    mConnectionState = ConnectionState.CONNECTED;
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "gattCallback - STATE_DISCONNECTED");
                    mConnectionState = ConnectionState.DISCONNECTED;
                    // disconnect gatt called earlier
                    // mConnectedGatt = disconnectGatt(gatt);

                    // TODO see Google bug:
                    // http://stackoverflow.com/questions/29758890/bluetooth-gatt-callback-not-working-with-new-api-for-lollipop
                    gatt.close();
                    break;
                default:
                    Log.e(TAG, "gattCallback - STATE_OTHER");
            }

            // TODO old code remove later
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//            	mConnectionState = ConnectionState.CONNECTED;
//                Log.i(TAG, "Connected to GATT server.");
//                Log.i(TAG, "Attempting to start service discovery:" +
//                        mConnectedGatt.discoverServices());
//
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//            	mConnectionState = ConnectionState.DISCONNECTED;
//                Log.i(TAG, "Disconnected from GATT server.");
//            }

            updateNotification();
        }

        // new services discovered
        @Override        
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onServicesDiscovered GATT_SUCCESS: " + status);
                Log.d(TAG, "onServicesDiscovered Services = " + gatt.getServices());
                displayGattServices(gatt.getServices());
                // TODO need to handle the response to this.  maybe ask favourite first too
                // writeCharacteristic(COMMAND_SET_FAVOURITE_DATA_TRANSMISSION);
                writeCharacteristic(COMMAND_GET_BATTERY_STATUS);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        // remove top descriptor write and dequeue next BLE operation
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            // super.onDescriptorWrite(gatt, descriptor, status);

            // pop the item that we just finishing writing
            descriptorWriteQueue.remove();

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Callback: Wrote GATT Descriptor successfully.");
            }
            else{
                Log.d(TAG, "Callback: Error writing GATT Descriptor: "+ status);
            }

            // dequeue next BLE command
            dequeueBleCommand();
        }

        // result of a characteristic read operation
        @Override        
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            // pop the item that we just finishing reading
            characteristicReadQueue.remove();

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onCharacteristicRead GATT_SUCCESS: " + characteristic);
                Log.d(TAG, "onCharacteristicRead UUID = " + characteristic.getUuid());

                // For all other profiles, writes the data formatted in HEX.
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for(byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));

                    Log.d(TAG, "onCharacteristicRead value = " + new String(data) + "\n" + stringBuilder.toString());
                }
            }
            else {
                Log.d(TAG, "onCharacteristicRead error: " + status);
            }

            // dequeue next BLE command
            dequeueBleCommand();
        }

        // result of a characteristic read operation
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            // TODO remove later
//            Log.d(TAG, "onCharacteristicChanged char = : " + characteristic);
//            Log.d(TAG, "onCharacteristicChanged UUID = " + characteristic.getUuid());
//
//            if (characteristic.getUuid().equals(UUID.fromString(SylvacGattAttributes.ANSWER_TO_REQUEST_OR_CMD_FROM_INSTRUMENT))) {
//                Log.d(TAG, "onCharacteristicChanged last write = " + mLastWrite);
//                Log.d(TAG, "onCharacteristicChanged getValue() = " + new String(characteristic.getValue()));
//            }
//
//            // For all other profiles, writes the data formatted in HEX.
//            final byte[] data = characteristic.getValue();
//            Log.d(TAG, "onCharacteristicChanged value = " + new String(data) + "\n" + byteArrayToString(data));

            // TODO only remove the last characteristic written once the result is obtained, so long as characteristic is for the write queue
            // TODO need to dequeue the next operation here (write queue only)

            broadcastUpdate(characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            // TODO only remove the last characteristic written once the result is obtained
            // pop the item that we just finishing writing
            characteristicWriteQueue.remove();

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onCharacteristicWrite GATT_SUCCESS: " + status);
                Log.d(TAG, "onCharacteristicWrite UUID = " + characteristic.getUuid());
                Log.i(TAG, "onCharacteristicWrite Char Value = " + characteristic.getValue().toString());
            } else {
                Log.w(TAG, "onCharacteristicWrite received: " + status);
            }

            // dequeue next BLE command
            dequeueBleCommand();
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.d(TAG, "onReliableWriteCompleted(" + status + ")");
        }
    };

    // returns device address as a String or null if not found
    private String getDeviceAddress(String devName) {
        String devAddr = null;

        try {
            // ensure Bluetooth state is on or device list won't work
            if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                // extract paired Bluetooth device list
                Set<BluetoothDevice> setBtDevices = mBluetoothAdapter.getBondedDevices();

                // find the required Bluetooth device
                for (BluetoothDevice btDevice : setBtDevices) {
                    if (btDevice.getName().equals(devName)) {
                        devAddr = btDevice.getAddress();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return devAddr;
    }

    // connect to the device
    public void connectDevice() {

        Log.d(TAG, "connectDevice: mDevAddr (before) = " + mDeviceAddress);

        // find device address if not found already
        if (mDeviceAddress == null) {
            // TODO for now always scan - maybe this will fix connection issues
            // mDeviceAddress = getDeviceAddress(DEVICE_NAME_BONDED);
        }

        Log.d(TAG, "connectDevice: mDevAddr (after) = " + mDeviceAddress);

        if (mDeviceAddress != null) {
            // device already discovered so connect directly via MAC address
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
            if(device != null && device.getName().equals(DEVICE_NAME_BONDED)) {
                connectGatt(device);
            }
        } else {
            // perform device discovery and connect the device in the callback
            scanLeDevice(true);
        }
    }

    // connect to the gatt
    private void connectGatt(final BluetoothDevice device) {
        // TODO comment out later on
//        Log.d(TAG, "connectGatt: fetch = " + device.fetchUuidsWithSdp());
//        Log.d(TAG, "connectGatt: UUID = " + device.getUuids());
//        Log.d(TAG, "connectGatt: Name = " + device.getName());
//        Log.d(TAG, "connectGatt: Type = " + device.getType());
//        Log.d(TAG, "connectGatt: BT Class = " + device.getBluetoothClass());
//        Log.d(TAG, "connectGatt: Address = " + device.getAddress());
//        Log.d(TAG, "connectGatt: String = " + device.toString());

        if (device != null && device.getName().equals(DEVICE_NAME_BONDED)) {

            mConnectionState = ConnectionState.CONNECTING;
            updateNotification();

            mConnectedGatt = device.connectGatt(SylvacBleService.this, false, mGattCallback);

            // stop the BLE scan
            scanLeDevice(false);
        }
    }

    // disconnect from gatt
    public BluetoothGatt disconnectGatt(BluetoothGatt gatt) {
        if (gatt != null) {
            Log.d(TAG, "gattCallback - before close mConnectedGatt");
            gatt.disconnect();
            // TODO remove later see Google bug
            // gatt.close();
            gatt = null;
            Log.d(TAG, "gattCallback - after close mConnectedGatt");
        }

        return gatt;
    }

    // disconnect device
    public void disconnectDevice() {
        Log.d(TAG, "disconnectDevice: " + mConnectedGatt);

        // ensure Ble resources are released
        mConnectedGatt = disconnectGatt(mConnectedGatt);

        // TODO old code remove later
//        if (mConnectedGatt != null) {
//            // close gatt connection
//            mConnectedGatt.disconnect();
//            mConnectedGatt.close();
//            mConnectedGatt = null;
//        }
    }

    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    // ExpandableListView on the UI.
    // FUTURE rename this method as is registering for services too!
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        Log.d(TAG, "displayGattServices - START");

        if (gattServices == null) return;

        // clear BLE command queues
        descriptorWriteQueue.clear();
        characteristicReadQueue.clear();
        characteristicWriteQueue.clear();

        for (BluetoothGattService service : gattServices) {
            Log.d(TAG, "Found service: " + service.getUuid());
            Log.d(TAG, "Included service(s): " + service.getIncludedServices());

            // skip if not Sylvac Metrology service
            if (!service.getUuid().equals(UUID.fromString(SylvacGattAttributes.SYLVAC_METROLOGY_SERVICE))) {
                continue;
            }

            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                Log.d(TAG, "Found characteristic: " + characteristic.getUuid());
                Log.d(TAG, "Descriptor: " + characteristic.getDescriptors());
                Log.d(TAG, "Properties: " + characteristic.getProperties());

                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    Log.d(TAG, "Found descriptor: " + descriptor.getUuid());
                    Log.d(TAG, "Value: " + descriptor.getValue());
                    Log.d(TAG, "Permissions: " + descriptor.getPermissions());
                }

                if(hasProperty(characteristic,
                        BluetoothGattCharacteristic.PROPERTY_READ)) {
                    Log.d(TAG, "Found Read characteristic: " + characteristic.getUuid());
                    // TODO before queue - remove later
                    // mConnectedGatt.readCharacteristic(characteristic);
                    // TODO read here not required - remove later
                    // readCharacteristic(characteristic);
                }

                if(hasProperty(characteristic,
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) {
                    Log.d(TAG, "Found Write No Resp characteristic: " + characteristic.getUuid());
                }

                if(hasProperty(characteristic,
                        BluetoothGattCharacteristic.PROPERTY_INDICATE)) {
                    Log.d(TAG, "Found indication for characteristic: " + characteristic.getUuid());

                    // enable indication on the Sylvac data received (from instrument) characteristic only
                    if(characteristic.getUuid().equals(UUID.fromString(SylvacGattAttributes.DATA_RECEIVED_FROM_INSTRUMENT))) {
                        Log.d(TAG, "Register indication for characteristic: " + characteristic.getUuid());
                        Log.d(TAG, "Register Success = " + mConnectedGatt.setCharacteristicNotification(characteristic, true));

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString(SylvacGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);

                            // TODO before queue - remove later
                            // mConnectedGatt.writeDescriptor(descriptor);
                            writeGattDescriptor(descriptor);
                        } else {
                            Log.e(TAG, "displayGattServices: descriptor is null - Data Received");
                        }
                    }
                }

                if(hasProperty(characteristic,
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
                    Log.d(TAG, "Found notification for characteristic: " + characteristic.getUuid());

                    // enable notify on the Sylvac answer to request or cmd (from instrument) characteristic only
                    if(characteristic.getUuid().equals(UUID.fromString(SylvacGattAttributes.ANSWER_TO_REQUEST_OR_CMD_FROM_INSTRUMENT))) {
                        Log.d(TAG, "Register notification for characteristic: " + characteristic.getUuid());
                        Log.d(TAG, "Register Success = " + mConnectedGatt.setCharacteristicNotification(characteristic, true));

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString(SylvacGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);

                            // TODO before queue - remove later
                            // mConnectedGatt.writeDescriptor(descriptor);
                            writeGattDescriptor(descriptor);
                        } else {
                            Log.e(TAG, "displayGattServices: descriptor is null - Answer To Request or Command");
                        }
                    }
                }
            }
        }
    }
    
    // returns whether or not the provided Bluetooth GATT Characteristic has a the specified property
    private static boolean hasProperty(BluetoothGattCharacteristic characteristic, int property) {
    	int prop = characteristic.getProperties() & property;
    	return prop == property;
    }

    // dequeue next BLE command
    private boolean dequeueBleCommand() {

        // handle asynchronous BLE callbacks via queues
        // GIVE PRECEDENCE to descriptor writes.  They must all finish first?
        if (descriptorWriteQueue.size() > 0) {
            return mConnectedGatt.writeDescriptor(descriptorWriteQueue.element());
        } else if (characteristicReadQueue.size() > 0) {
            return mConnectedGatt.readCharacteristic(characteristicReadQueue.element());
        } else if (characteristicWriteQueue.size() > 0) {
            return mConnectedGatt.writeCharacteristic(characteristicWriteQueue.element());
        } else {
            return true;
        }
    }

    // queue Gatt Descriptor writes
    private boolean writeGattDescriptor(BluetoothGattDescriptor d){
        boolean success = false;

        // check Bluetooth GATT connected
        if (mConnectedGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }

        //put the descriptor into the write queue
        success = descriptorWriteQueue.add(d);

        // execute BLE command immediately if there is nothing else queued up
        if((descriptorWriteQueue.size() == 1) && (characteristicReadQueue.size() == 0) && (characteristicWriteQueue.size() == 0)) {
            return mConnectedGatt.writeDescriptor(d);
        } else {
            return success;
        }
    }

    // queue BLE characteristic writes
    private boolean writeCharacteristic(BluetoothGattCharacteristic c) {
        boolean success = false;

        // check Bluetooth GATT connected
        if (mConnectedGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }

        // BluetoothGattService s = mBluetoothGatt.getService(UUID.fromString(kYourServiceUUIDString));
        // BluetoothGattCharacteristic c = s.getCharacteristic(UUID.fromString(characteristicName));

        //put the characteristic into the read queue
        success = characteristicWriteQueue.add(c);

        // execute BLE command immediately if there is nothing else queued up
        if((descriptorWriteQueue.size() == 0) && (characteristicReadQueue.size() == 0) && (characteristicWriteQueue.size() == 1)) {
            return mConnectedGatt.writeCharacteristic(c);
        }
        else {
            return success;
        }
    }

    // allows bound application component to write to the Sylvac Ble request or command characteristic
    public boolean writeCharacteristic(String value) {

    	Log.d(TAG, "writeChar = " + value);
    	
        // check Bluetooth GATT connected
        if (mConnectedGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }

        /*
        // check write is allowed
        if (mCanWrite == false) {
            Log.e(TAG, "write not allowed");
            return false;
        }
        */

        // extract the Service
        BluetoothGattService gattService = mConnectedGatt.getService(UUID.fromString(SylvacGattAttributes.SYLVAC_METROLOGY_SERVICE));
        if (gattService == null) {
            Log.e(TAG, "service not found");
            return false;
        }

        // extract the Characteristic
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(UUID.fromString(SylvacGattAttributes.DATA_REQUEST_OR_CMD_TO_INSTRUMENT));
        if (gattChar == null) {
            Log.e(TAG, "characteristic not found");
            return false;
        }

        // set the Characteristic
        if (gattChar.setValue(value) == false) {
            Log.e(TAG, "characteristic set failed");
            return false;
        }

        // write the Characteristic
        if (writeCharacteristic(gattChar) == false) {
            Log.e(TAG, "characteristic write failed");
            return false;
        }

        // TODO handle last value via getValue or getSTringValue of BLE GATT CHaracteristic
        mLastWrite = value;
       // mCanWrite = false;
        return true;
    }

    // queue BLE characteristic reads
    private boolean readCharacteristic(BluetoothGattCharacteristic c) {
        boolean success = false;

        // check Bluetooth GATT connected
        if (mConnectedGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }

        // BluetoothGattService s = mBluetoothGatt.getService(UUID.fromString(kYourServiceUUIDString));
        // BluetoothGattCharacteristic c = s.getCharacteristic(UUID.fromString(characteristicName));

        //put the characteristic into the read queue
        success = characteristicReadQueue.add(c);

        // execute BLE command immediately if there is nothing else queued up
        if((descriptorWriteQueue.size() == 0) && (characteristicReadQueue.size() == 1) && (characteristicWriteQueue.size() == 0)) {
            return mConnectedGatt.readCharacteristic(c);
        } else {
            return success;
        }
    }

    // allows bound application component to read from the Sylvac Ble request or command characteristic
    public boolean readCharacteristic() {
        // check Bluetooth GATT connected
        if (mConnectedGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }

        // extract the Service
        BluetoothGattService gattService = mConnectedGatt.getService(UUID.fromString(SylvacGattAttributes.SYLVAC_METROLOGY_SERVICE));
        if (gattService == null) {
            Log.e(TAG, "service not found");
            return false;
        }

        // extract the Characteristic
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(UUID.fromString(SylvacGattAttributes.ANSWER_TO_REQUEST_OR_CMD_FROM_INSTRUMENT));
        // BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(uuidChar);
        if (gattChar == null) {
            Log.e(TAG, "characteristic not found");
            return false;
        }

        // read the Characteristic
        // TODO - before queue; remove later
        // return mConnectedGatt.readCharacteristic(gattChar);
        return readCharacteristic(gattChar);
    }
        
	// broadcast action - no extras
	private void broadcastUpdate(final String action) {
	    final Intent intent = new Intent(action);
	    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	// broadcast action - extras from the characteristic
	private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicChanged char = : " + characteristic);
        Log.d(TAG, "onCharacteristicChanged UUID = " + characteristic.getUuid());
        Log.d(TAG, "onCharacteristicChanged toString() = " + characteristic.getUuid().toString());
        Log.d(TAG, "onCharacteristicChanged getValue() = " + new String(characteristic.getValue()));

    	final Intent intent = new Intent(ACTION_CHARACTERISTIC_CHANGED);
    	intent.putExtra(EXTRA_DATA_CHAR_UUID, characteristic.getUuid().toString());
    	intent.putExtra(EXTRA_DATA_CHAR_DATA, characteristic.getValue());
    	intent.putExtra(EXTRA_DATA_CHAR_LAST_WRITE, mLastWrite);
    	LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        
//        // FUTURE use switch via enum?
//        if (characteristic.getUuid().equals(UUID.fromString(SylvacGattAttributes.ANSWER_TO_REQUEST_OR_CMD_FROM_INSTRUMENT))) {
//        	Log.d(TAG, "onCharacteristicChanged last write = " + mLastWrite);
//        	final Intent intent = new Intent(ACTION_CHAR_CHANGED_REQ_OR_CMD);
//        	intent.putExtra(EXTRA_DATA_BYTES, characteristic.getValue());
//            intent.putExtra(EXTRA_DATA_STRING, new String(characteristic.getValue()));        	
//        	intent.putExtra(EXTRA_CHAR_LAST_WRITE, mLastWrite);
//        	LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//        } else if (characteristic.getUuid().equals(UUID.fromString(SylvacGattAttributes.DATA_RECEIVED_FROM_INSTRUMENT))) {        	
//        	double myDouble = Double.parseDouble(new String(characteristic.getValue()));
//        	final Intent intent = new Intent(ACTION_CHAR_CHANGED_MEASUREMENT);
//        	intent.putExtra(EXTRA_DATA_BYTES, characteristic.getValue());
//            intent.putExtra(EXTRA_DATA_STRING, new String(characteristic.getValue()));  
//            intent.putExtra(EXTRA_DATA_DECIMAL, myDouble);            
//            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//        }
	            
	}

	// extract Bluetooth adapter under Android 4.3+
	private BluetoothAdapter getBluetoothAdapter() {		
	    BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
	    return manager.getAdapter();	    
	}
	
	// create service notification
	private Notification getNotification(String text) {
		NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
		nb.setSmallIcon(R.drawable.ic_launcher);
		nb.setContentTitle("Sylvac Ble Service");
		nb.setContentText(text);
		nb.setOngoing(true);	// block user cancel
		return nb.build();
	}
	
	// update service notification - uses connection state as text
	private void updateNotification() {
		updateNotification(mConnectionState.getValue());
		return;
	}
	
	// update service notification - uses text string provided
	private void updateNotification(String text) {
        Log.d(TAG, "updateNotification: text = " + text);
		mNotificationManager.notify(mNotifyId, getNotification(text));
		return;
	}	
	
	// remove service notification
	private void removeNotification() {
        Log.d(TAG, "removeNotification");
		mNotificationManager.cancel(mNotifyId);
		return;
	}
	
	// converts byte array to a double
	public static double byteArrayToDouble(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getDouble();
	}
	
	// converts byte array to a String
	// TODO is this really necessary?  If so move into utility class
    public static String byteArrayToString(byte[] data) {
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));

            return stringBuilder.toString();
        }
        else {
            return "";
        }
    }

}
