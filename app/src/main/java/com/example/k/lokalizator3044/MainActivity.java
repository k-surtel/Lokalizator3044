package com.example.k.lokalizator3044;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.ActionMode;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.Toast;

import com.example.k.lokalizator3044.DatabaseManagement.DBHelper;
import com.example.k.lokalizator3044.DatabaseManagement.MyContentProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothScanner;
    private Handler mHandler = new Handler();
    private SimpleCursorAdapter cursorAdapter;

    ArrayList<BluetoothDevice> scannedDevices = new ArrayList<>();
    ArrayList<String> scannedDevicesNames = new ArrayList<>();

    HashMap<String, BluetoothDevice> myDevices = new HashMap<>();
    HashMap<String, BluetoothGatt> myGatts = new HashMap<>();

    /** MESS */
    ListView itagList;
    AlertDialog.Builder builder;
    BluetoothGatt bluetoothGatt;
    BluetoothGattCharacteristic bc;
    boolean addingMayFail;
    boolean devinfo = false;
    boolean alert = false;
    static Ringtone currentRingtone;
    private BroadcastReceiver receiver;
    boolean ringtone = false;
    Switch pressedSwitch;
    View itagAddress;
    View alarmButton;
    View settingsButton;
    LinearLayout linlaHeaderProgress;
    public static String NEW_ITAG_ACTIVITY = "new itag activity";
    public static String ITAG_ACTIVITY = "itag activity";
    boolean scanningActive;
    FloatingActionButton fab;
    String curRingtone;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyMgr;

    //finding device
    String switchDeviceAddress;
    boolean switchNewDevice;

    /** STATIC VALUES */
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    //(☞ ͡° ͜ʖ ͡°)☞ STOPS SCANNING AFTER 5 SECONDS
    private static final long SCAN_PERIOD = 5000;
    private final static int REQUEST_ENABLE_BT = 1;

    public static final UUID CHAR_BATTERY_LEVEL_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    // 180A Device Information
    public static final UUID SERVICE_DEVICE_INFORMATION_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final String SERVICE_DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final UUID CHAR_MANUFACTURER_NAME_STRING_UUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public static final UUID CHAR_MANUFACTURER_NAME_STRING = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public static final String STRING_CHAR_MANUFACTURER_NAME_STRING = "00002a29-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_MODEL_NUMBER_STRING = "00002a24-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SERIAL_NUMBEAR_STRING = "00002a25-0000-1000-8000-00805f9b34fb";

    // 1802 Immediate Alert
    public static final String SERVICE_IMMEDIATE_ALERT = "00001802-0000-1000-8000-00805f9b34fb";
    public static final UUID CHAR_ALERT_LEVEL_UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    public static final String CHAR_ALERT_LEVEL = "00002a06-0000-1000-8000-00805f9b34fb"; ///
    // StickNFindではCHAR_ALERT_LEVELに0x01をWriteすると光り、0x02では音が鳴り、0x03では光って鳴る。

    // 180F Battery Service
    public static final String SERVICE_BATTERY_SERVICE = "0000180F-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestBluetoothPermission();
        requestBluetoothAdminPermission();

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();


        checkBleSupport();
        if (!isBluetoothEnable()) requestBluetoothEnable();
        requestLocationPermission();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewDevice();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        itagList = findViewById(R.id.itag_list);
        wypelnijListe();

        itagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            int sameCount = 1;
            boolean same;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) itagList.getItemAtPosition(position);
                // Get the state's capital from this row in the database.
                final String address = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.ADDRESS));

                if(itagAddress == view.findViewById(R.id.itag_address)) {
                    same = true;
                    sameCount++;
                }
                else {
                    same = false;
                    sameCount = 1;
                }

                if((itagAddress != null && itagAddress.getTag() != null && (boolean)itagAddress.getTag()) || same) {
                    itagAddress.setVisibility(View.GONE);
                    itagAddress.setTag(false);
                }
                if((alarmButton != null && alarmButton.getTag() != null && (boolean)alarmButton.getTag()) || same) {
                    alarmButton.setVisibility(View.GONE);
                    alarmButton.setTag(false);
                }
                if((settingsButton != null && settingsButton.getTag() != null && (boolean)settingsButton.getTag()) || same) {
                    settingsButton.setVisibility(View.GONE);
                    settingsButton.setTag(false);
                }

                if(!same || (same && sameCount%2 != 0 && sameCount > 1)) {
                    itagAddress = view.findViewById(R.id.itag_address);
                    alarmButton = view.findViewById(R.id.itag_alarm);
                    settingsButton = view.findViewById(R.id.itag_settings);

                    itagAddress.setVisibility(View.VISIBLE);
                    itagAddress.setTag(true);
                    alarmButton.setVisibility(View.VISIBLE);
                    alarmButton.setTag(true);
                    settingsButton.setVisibility(View.VISIBLE);
                    settingsButton.setTag(true);
                }

                alarmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String addr = address;


                        if (myDevices.get(addr) != null && myGatts.get(addr) != null && !alert) {
                            BluetoothGatt gatt = myGatts.get(addr);
                            bc = findCharacteristic(addr, CHAR_ALERT_LEVEL_UUID);
                            if (bc != null) {
                                bc.setValue(new byte[]{(byte) 0x01});
                                alert = true;
                                gatt.writeCharacteristic(bc);
                            }
                        } else if (myDevices.get(addr) != null && myGatts.get(addr) != null && alert) {
                            BluetoothGatt gatt = myGatts.get(addr);
                            bc = findCharacteristic(addr, CHAR_ALERT_LEVEL_UUID);
                            if (bc != null) {
                                bc.setValue(new byte[]{(byte) 0x00});
                                alert = false;
                                gatt.writeCharacteristic(bc);
                            }
                        }
                    }
                });

                settingsButton.setOnClickListener(new View.OnClickListener() {

                    final String addr = address;

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), AddingActivity.class);
                        intent.putExtra(ITAG_ACTIVITY, addr);
                        intent.putExtra(NEW_ITAG_ACTIVITY, false);
                        startActivityForResult(intent, 1);
                    }
                });


            }
        });


        itagList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        itagList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.menu_delete, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        kasujZaznaczone();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {}
        });

//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setAction(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//                intent, 0);

        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(getPackageManager().getLaunchIntentForPackage(getPackageName()).getComponent());

        mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.common_ic_googleplayservices)
                        .setContentTitle("Lokalizator 3044")
                        .setContentText("Aplikacja jest połączona do iTagów!")
                        .setOngoing(true)
                        .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));

        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    //TODO: ZROBIĆ TO NA BLE NIE ZWYŁYM BT [https://www.bignerdranch.com/blog/bluetooth-low-energy-part-1/]
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

    public void requestBluetoothPermission() {
        if (!(checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, PERMISSION_REQUEST_FINE_LOCATION);
        }
    }

    public void requestBluetoothAdminPermission() {
        if (!(checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, PERMISSION_REQUEST_FINE_LOCATION);
        }
    }

    private void wypelnijListe() {
        Log.d("LOKLIZATOR", "Wypełnij listę!");
        String[] mapujZ = new String[]{DBHelper.NAME, DBHelper.ADDRESS, DBHelper.ID};
        int[] mapujDo = new int[]{R.id.itag_name, R.id.itag_address, R.id.if_enabled};

        cursorAdapter = new SimpleCursorAdapter(this, R.layout.selected_itag, null, mapujZ, mapujDo, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(final View view, final Cursor cursor, int i) {

                if (i == cursor.getColumnIndexOrThrow(DBHelper.ID)) {
                    final Switch s = (Switch) view;

                    if (myGatts.get(cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS))) != null) s.setChecked(true);
                    else s.setChecked(false);

                    if(view.getTag() == null) view.setTag(cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS)));

                    s.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pressedSwitch = s;
                            switchDeviceAddress = (String) view.getTag();
                            if (s.isChecked()) {
                                if(myGatts.get(switchDeviceAddress) == null) {
                                    Log.d("MainAcitvity", "tag: "+(String)view.getTag());
                                    switchNewDevice = false;
                                    startScan();
                                }
                            } else {
                                Log.d("MainAcitvity", "switch - disconnect");
                                disconnectDeviceSelected(switchDeviceAddress);
                            }
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


    private void kasujZaznaczone() {
        long zaznaczone[] = itagList.getCheckedItemIds();
        Cursor cursor;
        String addr = "";
        for (int i = 0; i < zaznaczone.length; ++i) {
            cursor = getContentResolver().query(ContentUris.withAppendedId(MyContentProvider.URI_ZAWARTOSCI, zaznaczone[i]), null, null, null, null);
            if(cursor.moveToFirst()) addr = cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS));
            if(myGatts.get(addr) != null && !myGatts.get(addr).equals("")) disconnectDeviceSelected(addr);
            getContentResolver().delete(ContentUris.withAppendedId(MyContentProvider.URI_ZAWARTOSCI, zaznaczone[i]), null, null);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                addingMayFail = false;
            } else if (resultCode == RESULT_CANCELED) {
                if(data.getExtras() != null) {
                    Log.d("MainAcitvity", "addingactivity - result cancel");
                    disconnectDeviceSelected(data.getExtras().getString("a"));
                    addingMayFail = false;
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projekcja = {DBHelper.ID, DBHelper.ADDRESS, DBHelper.NAME, DBHelper.WORKING_MODE, DBHelper.RINGTONE, DBHelper.DISTANCE, DBHelper.CLICK, DBHelper.DOUBLE_CLICK};
        CursorLoader loaderKursora = new CursorLoader(this, MyContentProvider.URI_ZAWARTOSCI, projekcja, null, null, null);
        return loaderKursora;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

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


    public void startScan() {
        Log.d("MainActivity", "StartScan()");

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        linlaHeaderProgress.setVisibility(View.VISIBLE);

        if (!scanningActive) {

            scanningActive = true;
            scannedDevicesNames.clear();
            scannedDevices.clear();

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    mHandler.removeCallbacks(this);
                    mBluetoothScanner.startScan(scanCallback);
                }
            });
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }

            }, SCAN_PERIOD);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("MainActivity", "onScanResult()");

            if (!scannedDevices.contains(result.getDevice())) {
                boolean isInDatabase = false;
                if (switchNewDevice) {
                    Cursor cursor = getContentResolver()
                            .query(MyContentProvider.URI_ZAWARTOSCI, new String[]{DBHelper.ADDRESS}, null, null, null);

                    if (cursor.moveToFirst()) {
                        do {
                            if (cursor.getString(cursor.getColumnIndex(DBHelper.ADDRESS)).equals(result.getDevice().getAddress()))
                                isInDatabase = true;
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }

                if (!isInDatabase || !switchNewDevice) {
                    scannedDevices.add(result.getDevice());
                    if (result.getDevice().getName() == null || result.getDevice().getName().equals("")) {
                        Log.d("MainActivity", "onScanResult() - new device (no name)");
                        scannedDevicesNames.add(result.getDevice().getAddress());
                    } else {
                        Log.d("MainActivity", "onScanResult() - new iTag");
                        scannedDevicesNames.add(result.getDevice().getName());
                    }
                    Log.d("MainActivity", "newitag = true");
                } else {
                    Log.d("MainActivity", "newitag = false");
                }
            }
        }
    };


    public void stopScan() {
        Log.d("BluetoothLEService", "StopScan()");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothScanner.stopScan(scanCallback);
            }
        });
        if (switchNewDevice) {
            linlaHeaderProgress.setVisibility(View.GONE);
            adaptujListe();
        }
        else findDevice();
        scanningActive = false;
    }

    void adaptujListe() {
        Log.d("MainActivity", "adaptujListe()");

        if (!scannedDevices.isEmpty()) {
            Log.d("MainActivity", "devicesDiscovered nie jest empty");

            builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Wybierz urządzenie");

            builder.setItems(scannedDevicesNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    myDevices.put(scannedDevices.get(which).getAddress(), scannedDevices.get(which));

                    Log.d("MainAcitvity", "connect - new itag from list");
                    connectToDeviceSelected(scannedDevices.get(which));
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else
            Toast.makeText(MainActivity.this, "Nie znaleziono żadnych urządzeń!", Toast.LENGTH_SHORT).show();
    }

    public void findDevice() {
        boolean found = false;
        for (BluetoothDevice bd : scannedDevices) {
            if (bd.getAddress().equals(switchDeviceAddress)) {
                found = true;
                Log.d("MainAcitvity", "connect to found device");
                connectToDeviceSelected(bd);
            }
        }

        if (found) pressedSwitch.setChecked(true);
        else {
            Toast.makeText(this, "Nie znaleziono urządzenia!", Toast.LENGTH_SHORT).show();
            linlaHeaderProgress.setVisibility(View.GONE);
            pressedSwitch.setChecked(false);
        }

    }

    public void addNewDevice() {
        //TODO: TYLE WYSTARCZY?
        addingMayFail = true;
        if (isBluetoothEnable()) {
            switchNewDevice = true;
            startScan();
        } else requestBluetoothEnable();
    }


    //(☞ ͡° ͜ʖ ͡°)☞ POŁĄCZENIE DO URZĄDZENIA
    public void connectToDeviceSelected(BluetoothDevice bd) {
        Log.d("MainActivity", "connectToDeviceSelected()");
        bluetoothGatt = bd.connectGatt(this, false, gattCallback);

        if(myGatts.isEmpty()) mNotifyMgr.notify(0, mBuilder.build());

        myGatts.put(bd.getAddress(), bluetoothGatt);
        myDevices.put(bd.getAddress(), bd);
        Toast.makeText(MainActivity.this, "Połączono do " + bd.getAddress(), Toast.LENGTH_SHORT).show();
        linlaHeaderProgress.setVisibility(View.GONE);

        if (switchNewDevice) {
            Intent intent = new Intent(getApplicationContext(), AddingActivity.class);
            intent.putExtra(ITAG_ACTIVITY, bd.getAddress());
            intent.putExtra(NEW_ITAG_ACTIVITY, switchNewDevice);
            startActivityForResult(intent, 1);
        }
    }

    public void disconnectDeviceSelected(String address) {
        Log.d("MainActivity", "disconnectDeviceSelected()");
        if (myGatts.get(address) != null) {
            myGatts.get(address).disconnect();
            myGatts.remove(address);

            myDevices.remove(address);

            if(!addingMayFail) {
                if(pressedSwitch.isChecked()) pressedSwitch.setChecked(false);
            }
            if(myGatts.isEmpty()) mNotifyMgr.cancel(0);
        }
    }

    public BluetoothGattCharacteristic findCharacteristic(String macAddress, UUID characteristicUUID) {
        Log.d("AddingActivity", "findCharacteristic()");
        BluetoothGatt bluetoothGatt = myGatts.get(macAddress);

        if (bluetoothGatt == null) {
            return null;
        }

        for (BluetoothGattService service : bluetoothGatt.getServices()) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
            if (characteristic != null) {
                return characteristic;
            }
        }
        return null;
    }


    //(☞ ͡° ͜ʖ ͡°)☞ CALLBACK DO DEVICE CONNECT
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation

            Log.d("MainActivity", "onCharacteristicChanged()");

            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

            UUID uuid = characteristic.getUuid();
            String address = gatt.getDevice().getAddress();

            if (!ringtone) {
                ringtone = true;
                startRing(address);
            } else {
                ringtone = false;
                stopRing();
            }
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    gatt.discoverServices();
                    Log.d("MainActivity", "POŁĄCZONO Z " + gatt.getDevice().getAddress());
                    //cursorAdapter.notifyDataSetChanged();

                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.d("MainActivity", "ROZŁĄCZONO Z " + gatt.getDevice().getAddress());
                    //cursorAdapter.notifyDataSetChanged();

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
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // this will get called after the client initiates a luetoothGatt.discoverServices() call


                Log.d("MainActivity", "onServicesDiscovered()");


                for (BluetoothGattService gattService : gatt.getServices()) {

                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                        for (BluetoothGattDescriptor descriptor : gattCharacteristic.getDescriptors()) {
                            if (descriptor != null && gattCharacteristic.getUuid() == CHAR_ALERT_LEVEL_UUID) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                            }
                        }
                        gatt.setCharacteristicNotification(gattCharacteristic, true);


                    }
                }
            } else {
                // failure
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                Log.d("MainActivity", "UDAŁO SIĘ ODCZYTAĆ NAPEWNOOOOOOOOOOOOO");

                UUID uuid = characteristic.getUuid();

                if(uuid.toString().equals(CHAR_BATTERY_LEVEL_UUID.toString())) {

                    byte[] x = characteristic.getValue();
                    int uint8 = convertByteToInt(x);

                    Log.d("MainActivity", "X");
                    Log.d("MainActivity", "X = "+uint8);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            runOnUiThread(new Runnable() {
                public void run() {
                }
            });
        }
    };

    public int convertByteToInt(byte[] b)
    {
        int value= 0;
        for(int i=0; i<b.length; i++)
            value = (value << 8) | b[i];
        return value;
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        System.out.println(characteristic.getUuid());
    }

    private void startRing(String addr) {
        Log.d("sylwka", "startRing()");
        if (currentRingtone != null) {
            currentRingtone.stop();
            currentRingtone = null;
        }


        Cursor c  = getContentResolver().query(MyContentProvider.URI_ZAWARTOSCI, new String[]{DBHelper.RINGTONE}, DBHelper.ADDRESS+"='"+addr+"'", null,null, null);
        if(c.moveToFirst()) Log.d("MainActivity", c.getString(c.getColumnIndexOrThrow(DBHelper.RINGTONE)));
        Uri sound = Uri.parse(c.getString(c.getColumnIndexOrThrow(DBHelper.RINGTONE)));
        c.close();

        //Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        currentRingtone = RingtoneManager.getRingtone(this, sound);

        if (currentRingtone == null) {
            Log.d("sylwka", "R.string.ring_tone_not_found");
            return;
        }

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, max, 0);

        currentRingtone.play();
    }

    private void stopRing() {
        Log.d("sylwka", "stopRing()");
        if (currentRingtone != null) {
            currentRingtone.stop();
        }
    }
}
