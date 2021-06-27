package com.example.gpstracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class trackinggps extends AppCompatActivity implements LocationListener {

    SessionManager sessionManager;


    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    String url_link = "https://gps-locations.000webhostapp.com/api/gpsdata.php";
    String api_keys = "pv2A0M0C6NbfoQNF0lQ0QRyNRuTnWVQK";

    GoogleMap nmaps;
    Marker options;
    LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trackinggps);


        //initialize and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //set home selected
        bottomNavigationView.setSelectedItemId(R.id.home);

        //perform itemselected listener

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile:
                        startActivity(new Intent(trackinggps.this, profile.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.home:

                        return true;

                    case R.id.about:
                        startActivity(new Intent(trackinggps.this, about.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        sessionManager = new SessionManager(trackinggps.this);
        if (!sessionManager.isLoggedIn()) {
            movetoLogin();
        }


        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        client = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(trackinggps.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // When Permission Granted
            // Call Method
            getCurrentLocation();
        } else {
            //When Permission Denied
            //Request Permission
            ActivityCompat.requestPermissions(trackinggps.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getCurrentLocation();
            }
        }, 0, 5000);


    }


    private void movetoLogin() {
        Intent i = new Intent(trackinggps.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
        finish();
    }

    private void getCurrentLocation() {
        @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();

        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location1) {
                if (location1 != null) {
//                    double latitude = location.getLatitude();
//                    double longitude = location.getLongitude();
                    //sync map
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            //initialize lat lng
                            nmaps = googleMap;

                            nmaps.setMyLocationEnabled(true);
                            nmaps.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                                @Override
                                public void onMyLocationChange(@NonNull @NotNull Location location) {
                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();

                                    latLng = new LatLng(latitude, longitude);
                                    //Marker Options
                                    options = nmaps.addMarker(new MarkerOptions().position(latLng).title("Iam Here"));
                                    //Zoom map
                                    nmaps.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                                    //Add Marker on Map

                                    String lat = Double.toString(latitude);
                                    String lng = Double.toString(longitude);
                                    if (!sessionManager.isLoggedIn()) {
                                        movetoLogin();
                                    } else {
                                        new setUpdateLocationtoServer().execute(lat, lng);
                                    }
                                }
                            });

                        }
                    });

                }
            }
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //when permission granted
                //call method
                getCurrentLocation();
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull @NotNull Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            options.setPosition(new LatLng(latitude,longitude));

            String lat = Double.toString(latitude);
            String lng = Double.toString(longitude);
            if (!sessionManager.isLoggedIn()) {
                movetoLogin();
            } else {
                new setUpdateLocationtoServer().execute(lat, lng);
            }
        }
    }

    public class setUpdateLocationtoServer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String idGps = sessionManager.getUserDetail().get(SessionManager.ID_GPS);
            String latitude = strings[0];
            String longitude = strings[1];
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("id", idGps)
                    .add("lat", latitude)
                    .add("lng", longitude)
                    .add("api_key", api_keys)
                    .build();

            Request request = new Request.Builder()
                    .url(url_link)
                    .post(formBody)
                    .build();
            Response response;
            try {
                response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    String result = response.body().string();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }
    }


}