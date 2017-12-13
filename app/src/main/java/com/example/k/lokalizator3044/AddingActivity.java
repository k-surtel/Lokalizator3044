package com.example.k.lokalizator3044;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddingActivity extends AppCompatActivity {

    boolean ifEdit;

    EditText addName;
    Spinner modeSpinner;
    Spinner ringtoneSpinner;
    Spinner distanceSpinner;
    Spinner clickSpinner;
    Spinner doubleClickSpinner;

    Button cancelBtn;
    Button saveBtn;

    Uri uri;

    BluetoothDevice connectedDevice;
    BluetoothGatt bluetoothGatt;

    //(☞ ͡° ͜ʖ ͡°)☞ NIE WIEM
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //tuuuuu?
        Bundle myBundle = getIntent().getExtras();
        connectedDevice = (BluetoothDevice)myBundle.get("itag");
        ifEdit = myBundle.getBoolean("edit");

        connectToDeviceSelected(connectedDevice);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding);

        addName = (EditText)findViewById(R.id.add_name);

        //(☞ ͡° ͜ʖ ͡°)☞ LISTY ROZWIJANE - SPINNERY
        modeSpinner = (Spinner)findViewById(R.id.add_mode_spinner);
        ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(this,
                R.array.mode_array, android.R.layout.simple_spinner_item);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(modeAdapter);

        ringtoneSpinner = (Spinner)findViewById(R.id.add_ringtone_spinner);
        ArrayAdapter<CharSequence> ringtoneAdapter = ArrayAdapter.createFromResource(this,
                R.array.ringtones_array, android.R.layout.simple_spinner_item);
        ringtoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ringtoneSpinner.setAdapter(ringtoneAdapter);

        distanceSpinner = (Spinner)findViewById(R.id.add_distance_spinner);
        ArrayAdapter<CharSequence> distanceAdapter = ArrayAdapter.createFromResource(this,
                R.array.distance_array, android.R.layout.simple_spinner_item);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceSpinner.setAdapter(distanceAdapter);

        clickSpinner = (Spinner)findViewById(R.id.add_button_spinner);
        ArrayAdapter<CharSequence> clickAdapter = ArrayAdapter.createFromResource(this,
                R.array.click_array, android.R.layout.simple_spinner_item);
        clickAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clickSpinner.setAdapter(clickAdapter);

        doubleClickSpinner = (Spinner)findViewById(R.id.add_button_double_spinner);
        final ArrayAdapter<CharSequence> doubleClickAdapter = ArrayAdapter.createFromResource(this,
                R.array.double_click_array, android.R.layout.simple_spinner_item);
        doubleClickAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        doubleClickSpinner.setAdapter(doubleClickAdapter);

        //if ifedit trueeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee!!!!

        cancelBtn = findViewById(R.id.add_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        saveBtn = findViewById(R.id.add_save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LOKLIZATOR", "Save button pressed!");
                ContentValues values = new ContentValues();
                values.put(DBHelper.MAC_ADDRESS, connectedDevice.getAddress().toString());
                values.put(DBHelper.NAME, addName.getText().toString());
                values.put(DBHelper.WORKING_MODE, modeSpinner.getSelectedItem().toString());
                values.put(DBHelper.RINGTONE, ringtoneSpinner.getSelectedItem().toString());
                values.put(DBHelper.DISTANCE, distanceSpinner.getSelectedItem().toString());
                values.put(DBHelper.WORKING_MODE, modeSpinner.getSelectedItem().toString());
                values.put(DBHelper.CLICK, clickSpinner.getSelectedItem().toString());
                values.put(DBHelper.DOUBLE_CLICK, doubleClickSpinner.getSelectedItem().toString());
                //values.put(DBHelper.IF_ENABLED, 1);

                //if(ifEdit) getContentResolver().update(uri, values, null, null);
                //else {
                   /* final AlertDialog.Builder builder = new AlertDialog.Builder(AddingActivity.this);
                    builder.setTitle("XOXO");
                builder.setMessage(new StringBuilder().append("Mac address: " + connectedDevice.getAddress().toString() + "\nName: " + addName.getText().toString()).append("\nWorking mode: ").append(modeSpinner.getSelectedItem().toString()).toString());
                builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();*/

                getContentResolver().insert(MyContentProvider.URI_ZAWARTOSCI, values);

                //String id = uri.getQueryParameter(DBHelper.ID);

                //Devices d = new Devices();
                //d.devicesDiscovered.put(id, connectedDevice);
                finish();

               //}

                //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //startActivity(intent);
            }
        });

    }

    //(☞ ͡° ͜ʖ ͡°)☞ POŁĄCZENIE DO URZĄDZENIA
    public void connectToDeviceSelected(BluetoothDevice connectedDevice) {
        //peripheralTextView.append("Trying to connect to device at index: " + deviceIndexInput.getText() + "\n");
        //int deviceSelected = Integer.parseInt(deviceIndexInput.getText().toString());
        bluetoothGatt = connectedDevice.connectGatt(this, false, btleGattCallback);
        Toast.makeText(AddingActivity.this, "Połączono", Toast.LENGTH_SHORT).show();
    }

    //(☞ ͡° ͜ʖ ͡°)☞ CALLBACK DO DEVICE CONNECT
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            AddingActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //peripheralTextView.append("device read or wrote to\n");
                }
            });
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    AddingActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //peripheralTextView.append("device disconnected\n");
                        }
                    });
                    break;
                case 2:
                    AddingActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //peripheralTextView.append("device connected\n");
                            Toast.makeText(AddingActivity.this, "Połączono", Toast.LENGTH_LONG).show();
                        }
                    });

                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    AddingActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //peripheralTextView.append("we encounterned an unknown state, uh oh\n");
                        }
                    });
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a 			BluetoothGatt.discoverServices() call
            AddingActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    // peripheralTextView.append("device services have been discovered\n");
                }
            });
            displayGattServices(bluetoothGatt.getServices());
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
    };


    //(☞ ͡° ͜ʖ ͡°)☞ NIE WIEM
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            final String uuid = gattService.getUuid().toString();
            System.out.println("Service discovered: " + uuid);
            AddingActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //peripheralTextView.append("Service disovered: "+uuid+"\n");
                }
            });
            new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                final String charUuid = gattCharacteristic.getUuid().toString();
                System.out.println("Characteristic discovered for service: " + charUuid);
                AddingActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        //peripheralTextView.append("Characteristic discovered for service: "+charUuid+"\n");
                    }
                });

            }
        }
    }

    //(☞ ͡° ͜ʖ ͡°)☞ NIE WIEM
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {

        System.out.println(characteristic.getUuid());
    }
}
