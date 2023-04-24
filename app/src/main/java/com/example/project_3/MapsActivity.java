package com.example.project_3;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnShowCurrentLocation;
    private Button btnShowBluetoothLocation;
    private TextView textDistance;
    private TextView textLatitude;
    private TextView textLongitude;
    private LatLng currentLocation = new LatLng(0, 0);
    private LatLng bluetoothLocation = new LatLng(31.399080, 75.54000);
    ;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private float distance;
    private Spinner spinnerDevices;
    private Spinner spinnerViews;
    private Marker currLocMarker;
    private Marker btLocMarker;

    private ArrayAdapter<String> spinnerAdapter;
    private Marker bluetoothMarker;
    ArrayList<String> views = new ArrayList<>();
    ArrayList<String> pairedDevices = new ArrayList();
    private static final String DEVICE_NAME = "HC-05"; // Replace with your HC-05 device name
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public final String NOTIFICATION_ID = "1";
    public final String NOTIFICATION_NAME = "Example";
    private final ActivityResultLauncher<Intent> bluetoothEnableLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // Bluetooth is enabled, proceed with connecting to HC-05 device
                            // connectToBluetoothDevice();
                        } else {
                            // Bluetooth is not enabled, show an error message
                            Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
                        }
                    });
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean allGranted = true;
                        for (String permission : result.keySet()) {
                            if (!result.get(permission)) {
                                allGranted = false;
                                break;
                            }
                        }
                        if (allGranted) {
                            // All location permissions are granted, proceed with getting location coordinates
                            getCurrentLocation();
                        } else {
                            // Show an error message
                            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
                        }
                    });

    private void getCurrentLocation() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        btnShowCurrentLocation = findViewById(R.id.button_show_current_location);
        btnShowBluetoothLocation = findViewById(R.id.button_show_bluetooth_location);
        textDistance = findViewById(R.id.text_distance);
        textLatitude = findViewById(R.id.text_latitude);
        textLongitude = findViewById(R.id.text_longitude);

        spinnerDevices = findViewById(R.id.spinner_devices);
        spinnerViews = findViewById(R.id.spinner_views);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mReceiver, filter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

        }
        bluetoothAdapter.startDiscovery();
        views.add("Normal Map");
        views.add("Hybrid Map");
        views.add("Satellite Map");
        views.add("Terrain Map");
        pairedDevices.add("Select Device");

        ArrayAdapter<String> viewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, views);
        spinnerViews.setAdapter(viewAdapter);

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothEnableLauncher.launch(enableBtIntent);
        } else {
          //  getPairedDevices();
        }

        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pairedDevices);
        spinnerDevices.setAdapter(spinnerAdapter);

        spinnerDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    return;
                }
                // Connect to the selected device
                String deviceName = pairedDevices.get(position);
//                BluetoothDevice selectedDevice = getPairedDeviceByName(deviceName);
//                if (selectedDevice != null) {
//                    connectToBluetoothDevice(deviceName);
//                } else {
//                    Toast.makeText(getApplicationContext(), "Device not found", Toast.LENGTH_SHORT).show();
//                }
                Toast.makeText(getApplicationContext(), "Connected to " + deviceName, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerViews.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        Toast.makeText(MapsActivity.this, "Normal Map type enabled", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        Toast.makeText(MapsActivity.this, "Hybrid Map type enabled", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        Toast.makeText(MapsActivity.this, "Satellite Map type enabled", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        Toast.makeText(MapsActivity.this, "Terrain Map type enabled", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // Set click listener for "Show Current Location" button
        btnShowCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentLocation != null) {
                    String address = getAddressFromLocation(currentLocation.latitude, currentLocation.longitude);
                    currLocMarker.setPosition(currentLocation);
                    currLocMarker.setTitle("Current location: " + address);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                } else {
                    Toast.makeText(MapsActivity.this, "Current Location not available", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnShowBluetoothLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bluetoothLocation != null) {

                    String address = getAddressFromLocation(bluetoothLocation.latitude, bluetoothLocation.longitude);
//                    btLocMarker.setPosition(bluetoothLocation);
//                    btLocMarker.setTitle("Bluetooth location: " + address);
                    mMap.addMarker(new MarkerOptions().position(bluetoothLocation).title("Bluetooth Location: " + address));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bluetoothLocation, 15f));
                } else {
                    connectToHC05();
                    //  Toast.makeText(MapsActivity.this, "Bluetooth Location not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //    BroadcastReceiver myReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//             String action = intent.getAction();
//             if(BluetoothDevice.ACTION_FOUND.equals(action)){
//                 BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                 pairedDevices.add(device.getName());
//             }
//        }
//    };
    // Get the list of paired devices
    private void getPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        Set<BluetoothDevice> pairedDevs = bluetoothAdapter.getBondedDevices();

        if (pairedDevs.size() > 0) {
            for (BluetoothDevice device : pairedDevs) {
                pairedDevices.add(device.getName());
            }
            // spinnerAdapter.notifyDataSetChanged();

        }
    }

    private void connectToHC05() {
        // Get the Bluetooth device by its address (replace with the actual MAC address of your HC-05 module)
        bluetoothDevice = bluetoothAdapter.getRemoteDevice("00:00:00:00:00:00"); // Replace with your HC-05 MAC address

        try {
            // Create a Bluetooth socket for communication with the device
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Standard SPP UUID
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

            // Start reading data from the Bluetooth socket
            InputStream inputStream = bluetoothSocket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            // Read data from Bluetooth module
            String data;
            while ((data = bufferedReader.readLine()) != null) {
                // Parse the received data to get location coordinates
                String[] coordinates = data.split(","); // Assuming the data is in comma-separated format (e.g. "latitude,longitude")
                double latitude = Double.parseDouble(coordinates[0]);
                double longitude = Double.parseDouble(coordinates[1]);

                // Update marker on map
                LatLng latLng = new LatLng(latitude, longitude);
                bluetoothLocation = latLng;
                if (bluetoothMarker == null) {
                    // If marker is not yet initialized, create marker

                    String address = getAddressFromLocation(bluetoothLocation.latitude, bluetoothLocation.longitude);
                    mMap.addMarker(new MarkerOptions().position(bluetoothLocation).title("Bluetooth Location: " + address));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bluetoothLocation, 15f));
                } else {
                    // If marker is already initialized, update position

                    String address = getAddressFromLocation(bluetoothLocation.latitude, bluetoothLocation.longitude);
                    btLocMarker.setPosition(bluetoothLocation);
                    btLocMarker.setTitle("Bluetooth location: " + address);
                }

                // Calculate distance between current location and Bluetooth device location
                float[] results = new float[1];
                if (currentLocation != null) {
                    Location.distanceBetween(currentLocation.latitude, currentLocation.longitude, latitude, longitude, results);

                    float distance = results[0];
                    textDistance.setText(String.format("%6f m", distance));
                    if (distance > 200) {
                        // If distance is greater than 1 km, send notification
                        sendNotification();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel(NOTIFICATION_ID, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            nc.enableVibration(true);
            nm.createNotificationChannel(nc);
        }

        NotificationCompat.Builder n = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_ID);
        n.setContentTitle("Notification");
        n.setContentText("The transmitter is out of the bound by " + distance + " m");
        n.setSmallIcon(R.drawable.ic_launcher_background);
        nm.notify(1, n.build());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        } else {
            locationPermissionLauncher.launch(permissions);
        }
        mMap.setMyLocationEnabled(true);
        String address = getAddressFromLocation(currentLocation.latitude, currentLocation.longitude);
        currLocMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current location: " + address));
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                textLatitude.setText(String.format("%.6f", currentLocation.latitude));
                textLongitude.setText(String.format("%.6f", currentLocation.longitude));

                String address = getAddressFromLocation(currentLocation.latitude, currentLocation.longitude);

                currLocMarker.setPosition(currentLocation);
                currLocMarker.setTitle("Current location: " + address);
                float[] results = new float[1];
                if (currentLocation != null) {
                    Location.distanceBetween(currentLocation.latitude, currentLocation.longitude, bluetoothLocation.latitude, bluetoothLocation.longitude, results);

                    distance = results[0];
                    textDistance.setText(String.format("%6f m", distance));
                    if (distance > 200) {
                        // If distance is greater than 1 km, send notification
                        sendNotification();
                    }
                }
            }
        });
    }


    private String getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        String address = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address returnedAddress = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    sb.append(returnedAddress.getAddressLine(i)).append(" ");
                }
                address = sb.toString().trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    // Get Bluetooth device by name
    private BluetoothDevice getPairedDeviceByName(String deviceName) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(deviceName)) {
                    return device;
                }
            }
        }
        return null; // Device not found
    }

    // Method to connect to a Bluetooth device

    private void connectToBluetoothDevice(String deviceName) {
        BluetoothDevice device = getPairedDeviceByName(deviceName);
        if (device != null) {
            // Check if Bluetooth is enabled
            if (bluetoothAdapter.isEnabled()) {
                // Create a BluetoothSocket to connect to the device
                BluetoothSocket socket = null;
                try {
                    // Use the device's UUID for the connection
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {


                    }
                    UUID uuid = device.getUuids()[0].getUuid();
                    socket = device.createRfcommSocketToServiceRecord(uuid);
                    // Connect to the device
                    socket.connect();
                    // Connection successful, you can now send/receive data
                    // using the socket.getInputStream() and socket.getOutputStream() methods
                } catch (IOException e) {
                    e.printStackTrace();
                    // Failed to connect to the device
                    Toast.makeText(this, "Failed to connect to the device", Toast.LENGTH_SHORT).show();
                } finally {
                    // Close the socket when done
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                // Bluetooth is not enabled, prompt the user to enable it

            }
        } else {
            Toast.makeText(this, "Device not found", Toast.LENGTH_SHORT).show();
        }
    }

//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                }
//                String deviceName = device.getName();
//                if (deviceName != null) {
//                    pairedDevices.add(deviceName);
//                    spinnerAdapter.notifyDataSetChanged();
//                }
//            }
//        }
//    };

    // Method to get Bluetooth Location coordinates from address
    private LatLng getBluetoothLocationFromAddress(Context context, String address) {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && addresses.size() > 0) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                return new LatLng(latitude, longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to calculate distance between two coordinates in meters
    private float calculateDistance(LatLng startPoint, LatLng endPoint) {
        Location startLocation = new Location("");
        startLocation.setLatitude(startPoint.latitude);
        startLocation.setLongitude(startPoint.longitude);

        Location endLocation = new Location("");
        endLocation.setLatitude(endPoint.latitude);
        endLocation.setLongitude(endPoint.longitude);

        return startLocation.distanceTo(endLocation);
    }


}

