package com.example.k.lokalizator3044;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothScanner;
    BluetoothGatt bluetoothGatt;
    private SimpleCursorAdapter cursorAdapter;

    String checkString;
    boolean checkCheck;
    boolean truOrFals;

    Devices d;
    Uri uri;

    private Handler mHandler = new Handler();
    //(☞ ͡° ͜ʖ ͡°)☞ STOPS SCANNING AFTER 5 SECONDS
    private static final long SCAN_PERIOD = 5000;

    ListView itagList;


    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    //(☞ ͡° ͜ʖ ͡°)☞ NIE WIEM
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();

        //(☞ ͡° ͜ʖ ͡°)☞ TOOLBAR - TEN PASEK NA GÓRZE
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //(☞ ͡° ͜ʖ ͡°)☞ LATAJĄCY DODAJEK
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBluetoothEnable()) {
                    Intent intent = new Intent(getApplicationContext(), ScanningActivity.class);
                    startActivity(intent);
                } else {
                    requestBluetoothEnable();
                }
            }
        });

        //(☞ ͡° ͜ʖ ͡°)☞ TO MENU WYSUWANE Z BOKU PO LEWEJ
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        d = new Devices();
        d.devicesDiscovered.clear();

        itagList = (ListView)findViewById(R.id.itag_list);
        wypelnijListe();

        checkBleSupport();
        if(!isBluetoothEnable()) requestBluetoothEnable();
        requestLocationPermission();
    }

    public void checkBleSupport() {
        //(☞ ͡° ͜ʖ ͡°)☞ SPRAWDZA CZY TELEFON MA BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Brak BLE");
            builder.setMessage("To urządzenie nie wspiera technologii Bluetooth Low Energy.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
            builder.show();
        }
    }
    public boolean isBluetoothEnable() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            return false;
        } else return true;
    }
    public void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }
    public void requestLocationPermission() {
        if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
        }
    }

    private void wypelnijListe() {
        Log.d("LOKLIZATOR", "Wypełnij listę!");
        //ifen
        String[] mapujZ = new String[]{DBHelper.NAME, DBHelper.MAC_ADDRESS};
        int[] mapujDo = new int[]{R.id.itag_name, R.id.if_enabled};
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.itag, null, mapujZ, mapujDo, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, final Cursor cursor, int i) {

               // final int enabledColumnIndex = cursor.getColumnIndexOrThrow(DBHelper.IF_ENABLED);
                Log.d("LOKLIZATOR", "position = "+i);
                //Log.d("LOKLIZATOR", "columnIndex = "+enabledColumnIndex);

                Log.d("LOKLIZATOR", "eeeeeeeeee = "+cursorAdapter.getItemId(cursor.getPosition()));
                final int bool = cursor.getInt(i);

                if (i != cursor.getColumnIndexOrThrow(DBHelper.NAME)) {
                    final Switch s = (Switch)view;
                    //if(bool == 1) s.setChecked(true);

                    s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                            long id = cursorAdapter.getItemId(cursor.getPosition());
                            //Log.d("LOKLIZATOR", "ID DEVAJSA = "+id);




                            String item = MyContentProvider.URI_ZAWARTOSCI+"/"+id;
                            Uri uri = Uri.parse(item);


                            String[] p = new String[] {DBHelper.ID, DBHelper.MAC_ADDRESS};
                            Cursor c = getContentResolver().query(uri, p, null, null, null);
                            c.moveToFirst();

                            Log.d("LOKLIZATOR", "MAC:::: "+c.getString(c.getColumnIndex(DBHelper.MAC_ADDRESS)));


                            checkCheck = true;
                            checkString = c.getString(c.getColumnIndex(DBHelper.MAC_ADDRESS));
                            truOrFals = b;

                            startScan();


                            //BluetoothDevice bd;
                            //Devices d = new Devices();
                            //bd = d.devicesDiscovered.get(id);
                            //Log.d("LOKLIZATOR", "ID DEVAJSA = "+id);
                            //Log.d("LOKLIZATOR", "nazwa = "+bd.getName());

                            //ContentValues val = new ContentValues();
                            if(b) {
                                //val.put(DBHelper.IF_ENABLED, 1);
                                //connectToDeviceSelected(bd);
                            }
                            else {
                                //val.put(DBHelper.IF_ENABLED, 0);
                                //bluetoothGatt.disconnect();
                            }

                            //getContentResolver().update(uri, val, null, null);*/
                        }
                    });

                    return true;
                }

                return false;
            }
        });
        itagList.setAdapter(cursorAdapter);
        getLoaderManager().initLoader(0, null, this);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //ifen
        String[] projekcja = { DBHelper.ID, DBHelper.MAC_ADDRESS, DBHelper.NAME, DBHelper.WORKING_MODE, DBHelper.RINGTONE, DBHelper.DISTANCE, DBHelper.CLICK, DBHelper.DOUBLE_CLICK };
        CursorLoader loaderKursora = new CursorLoader(this, MyContentProvider.URI_ZAWARTOSCI, projekcja, null,null, null);
        return loaderKursora;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) { cursorAdapter.swapCursor(data); }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    //(☞ ͡° ͜ʖ ͡°)☞ CHOWAJKA PANELU WYSUWANEGO
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //(☞ ͡° ͜ʖ ͡°)☞ POKAZUJE TE OPCJE PO PRAWEJ NA TOOLBARZE
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //(☞ ͡° ͜ʖ ͡°)☞ POSZCZEGÓLNE OPCJE Z PRZYCISKU PO PRAWEJ NA TOOLBARZE
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reload) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //(☞ ͡° ͜ʖ ͡°)☞ POSZCZEGÓLNE OPCJE Z MENU WYSUWANEGO
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //(☞ ͡° ͜ʖ ͡°)☞ SPRAWDZA CZY PRZYZNANO PERMISSION
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (!(grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Brak uprawnień");
                    builder.setMessage("Bez uprawnień lokalizacji aplikacja nie będzie w stanie wyszukiwać urządzeń.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }


    //(☞ ͡° ͜ʖ ͡°)☞ POŁĄCZENIE DO URZĄDZENIA
    public void connectToDeviceSelected(BluetoothDevice connectedDevice) {
        //peripheralTextView.append("Trying to connect to device at index: " + deviceIndexInput.getText() + "\n");
        //int deviceSelected = Integer.parseInt(deviceIndexInput.getText().toString());
        bluetoothGatt = connectedDevice.connectGatt(this, false, btleGattCallback);
        Toast.makeText(MainActivity.this, "Połączono", Toast.LENGTH_SHORT).show();
    }

    //(☞ ͡° ͜ʖ ͡°)☞ CALLBACK DO DEVICE CONNECT
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            MainActivity.this.runOnUiThread(new Runnable() {
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
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //peripheralTextView.append("device disconnected\n");
                        }
                    });
                    break;
                case 2:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //peripheralTextView.append("device connected\n");
                            Toast.makeText(MainActivity.this, "Połączono", Toast.LENGTH_LONG).show();
                        }
                    });

                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    MainActivity.this.runOnUiThread(new Runnable() {
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
            MainActivity.this.runOnUiThread(new Runnable() {
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
            MainActivity.this.runOnUiThread(new Runnable() {
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
                MainActivity.this.runOnUiThread(new Runnable() {
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

    //(☞ ͡° ͜ʖ ͡°)☞ START WYSZUKIWANIA URZĄDZEŃ
    public void startScan() {
        //Toast.makeText(MainActivity.this, "Trwa wyszukiwanie urządzeń", Toast.LENGTH_SHORT).show();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothScanner.startScan(leScanCallback);
            }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);
    }

    //(☞ ͡° ͜ʖ ͡°)☞ STOP WYSZUKIWANIA URZĄDZEŃ
    public void stopScan() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothScanner.stopScan(leScanCallback);
            }
        });
    }

    //(☞ ͡° ͜ʖ ͡°)☞ DEVICE SCAN CALLBACK - WYNIKI WYSZUKANIA URZĄDZEŃ
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            String mac = result.getDevice().getAddress();
            //String item = MyContentProvider.URI_ZAWARTOSCI+"/"+mac;
            Uri uri = MyContentProvider.URI_ZAWARTOSCI;

            Log.d("LOKLIZATOR", "MAC = "+mac);
            Log.d("LOKLIZATOR", "URI = "+uri.toString());

            String[] p = new String[] {DBHelper.ID, DBHelper.MAC_ADDRESS};
            Cursor c = getContentResolver().query(uri, p, null, null, null);
            c.moveToFirst();

            if(checkCheck) {
                while (c.moveToNext()) {
                    if (mac.equals(c.getString(c.getColumnIndex(DBHelper.MAC_ADDRESS)))) {
                        if(mac.equals(checkString)) {
                            if(truOrFals) {
                                d.devicesDiscovered.put(c.getString(c.getColumnIndex(DBHelper.ID)), result.getDevice());
                                Log.d("L", "łączyyyyyyyyyyyyyyyy");
                                connectToDeviceSelected(result.getDevice());
                            } else {
                                Log.d("L", "rozllllllllllllllllllll");
                                result.getDevice().connectGatt(MainActivity.this, false, btleGattCallback).disconnect();

                            }

                        }
                    }
                }
            }
            checkCheck = false;

            //Log.d("LOKLIZATOR", "C = "+c.getString(c.getColumnIndex(DBHelper.MAC_ADDRESS)));

            //if(uri != null) {

            //}

            /*if (result.getDevice().getAddress() == uri.getQueryParameter(DBHelper.MAC_ADDRESS)) {
                foundIds.add(result.getDevice().getAddress());
                if (!result.getDevice().getName().equals(null)) foundItags.add(result.getDevice().getName());
                else foundItags.add("no name");
                devicesDiscovered.add(result.getDevice());
                deviceIndex++;
                adaptujListe();
                Log.d("ID: ", result.getDevice().getAddress());
            }*/
        }
    };
}
