package com.example.rachel.drones;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPointStyle;

import java.util.ArrayList;
import java.io.IOException;
import org.json.JSONException;

import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.FlightController.DJIFlightControllerDataType;
import dji.sdk.FlightController.DJIFlightControllerDelegate;
import dji.sdk.MissionManager.DJIMission;
import dji.sdk.MissionManager.DJIMissionManager;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIError;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnMyLocationButtonClickListener, DJIMissionManager.MissionProgressStatusCallback, ActivityCompat.OnRequestPermissionsResultCallback, DJIBaseComponent.DJICompletionCallback{

    private GoogleMap mMap;
    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView mInfoTextView;
    private double droneLocationLat = 181, droneLocationLng = 181;
    private DJIFlightController mFlightController;
    private Marker droneMarker = null;
    private Marker weatherMarker = null;
    private double weatherLat = 0, weatherLon = 0;
    private double deviceLat = 0,deviceLon=0;
    private Marker flightMarker = null;
    private double flightLat=0, flightLon=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mInfoTextView = (TextView)findViewById(R.id.info_textView);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIDemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap = map;


        try {
            GeoJsonLayer layer = new GeoJsonLayer(mMap, R.raw.airport, getApplicationContext());
            layer.addLayerToMap();
            GeoJsonLayer layer2 = new GeoJsonLayer(mMap, R.raw.military, getApplicationContext());
            layer2.addLayerToMap();
           GeoJsonLayer layer3 = new GeoJsonLayer(mMap, R.raw.ca_national_park, getApplicationContext());
            layer3.addLayerToMap();
        } catch (IOException e) {

            //e.printStackTrace();
        } catch (JSONException e) {
            //e.printStackTrace();
            System.out.print("ggg");
        }
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        enableMyLocation();
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        String bestProvider = locationManager.getBestProvider(criteria,true);
//        Location location = locationManager.getLastKnownLocation(bestProvider);
//        Log.d("Location", "LOCATION!!!! "+String.valueOf(location.getLatitude())+", "+location.getLongitude());
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            Log.d("Location", "LOCATION!!!! "+String.valueOf(location.getLatitude())+", "+location.getLongitude());
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            deviceLat = location.getLatitude();
            deviceLon = location.getLongitude();
//            if(mMap != null){
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
//            }
        }
    };

    @Override
    protected void onResume(){
        super.onResume();
        initFlightController();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }


    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    private void onProductConnectionChange()
    {
        initFlightController();
    }

    private void initFlightController() {

        DJIBaseProduct product = DJIDemoApplication.getProductInstance();
//        setResultToToast("initFlightController");
        if(product == null) setResultToToast("product == null");
//        if(!product.isConnected())setResultToToast("product.isConnected()");
        if (product != null && product.isConnected()) {
            setResultToToast("product != null && product.isConnected()");
            if (product instanceof DJIAircraft) {
                setResultToToast("product instanceof DJIAircraft");
                mFlightController = ((DJIAircraft) product).getFlightController();
            }
        }

        if (mFlightController != null) {
            setResultToToast("mFlightController != null");
            mFlightController.setUpdateSystemStateCallback(new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {

                @Override
                public void onResult(DJIFlightControllerDataType.DJIFlightControllerCurrentState state) {
                    droneLocationLat = state.getAircraftLocation().getLatitude();
                    droneLocationLng = state.getAircraftLocation().getLongitude();
                    updateDroneLocation();
                }
            });
        }
    }

    // Update the drone location based on states from MCU.
    private void updateDroneLocation(){

        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }
                mInfoTextView.setText("lat: "+ String.valueOf(droneLocationLat)+", lng: "+String.valueOf(droneLocationLng));
                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                    droneMarker = mMap.addMarker(markerOptions);
                }
            }
        });
    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    public void getLocation(View view){
        updateDroneLocation();
    }

    private void setResultToToast(final String string){
        MapsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MapsActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * DJIMissionManager Delegate Methods
     */
    @Override
    public void onResult(DJIError error) {
        setResultToToast("Execution finished: " + (error == null ? "Success!" : error.getDescription()));
    }

    /**
     * DJIMissionManager Delegate Methods
     */
    @Override
    public void missionProgressStatus(DJIMission.DJIMissionProgressStatus progressStatus) {

    }

    public void getData(View view){
        getweatherData();
    }

    private void getweatherData() {

        QueryWeatherData weatherTask = new QueryWeatherData(this,deviceLat,deviceLon);
        weatherTask.execute();
        QueryFlight flightTask = new QueryFlight(this,getResources().openRawResource(R.raw.flightstats));
        flightTask.execute();
    }

    public void setWeatherData(ArrayList<WeatherData> result){
        for(int i = 0; i < result.size();i++){
            weatherLat = result.get(i).lat;
            weatherLon = result.get(i).lon;
            LatLng pos = new LatLng(weatherLat, weatherLon);
//            Log.d("SET","setWeatherData!!!!!!" +String.valueOf(result.get(i).lat));
            //Create MarkerOptions object
            final MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(pos);
            if(result.get(i).windSpeed >2){
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.wind_red));
            }else{
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.wind));
            }

            markerOptions.title("Wind Speed: "+String.valueOf(result.get(i).windSpeed));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    if ( weatherMarker!= null) {
//                        weatherMarker.remove();
//                    }
                    Log.d("SET","setWeatherData!!!!!!" +String.valueOf(weatherLat));

                    if (checkGpsCoordination(weatherLat, weatherLon)) {
                        weatherMarker = mMap.addMarker(markerOptions);
                    }
                }
            });
        }
//        Log.d("SET","setWeatherData!!!!!!" +String.valueOf(result.get(0).windSpeed));
    }

    public void setFlightData(ArrayList<FlightData> result){
//        Log.d("SET","setWeatherData!!!!!!" +String.valueOf(result.get(0).lat));
        for(int i = 0; i < result.size();i++){
            flightLat = result.get(i).lat;
            flightLon = result.get(i).lon;
            LatLng pos = new LatLng(flightLat, flightLon);
//            Log.d("SET","setWeatherData!!!!!!" +String.valueOf(result.get(i).lat));
            //Create MarkerOptions object
            final MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(pos);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

//            markerOptions.title("Wind Speed: "+String.valueOf(result.get(i).windSpeed));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    if ( weatherMarker!= null) {
//                        weatherMarker.remove();
//                    }
//                    Log.d("SET","setWeatherData!!!!!!" +String.valueOf(weatherLat));

                    if (checkGpsCoordination(flightLat, flightLon)) {
                        flightMarker = mMap.addMarker(markerOptions);
                    }
                }
            });
        }
    }
}
