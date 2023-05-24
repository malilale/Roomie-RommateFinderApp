package com.roomie.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private ProfileFragment profileFragment = new ProfileFragment();
    private UsersFragment usersFragment = new UsersFragment();
    private NotificationsFragment notificationsFragment = new NotificationsFragment();
    private MapsFragment mapsFragment = new MapsFragment();
    FusedLocationProviderClient fusedLocationProviderClient;
    private  final  static int REQUEST_CODE=100;
    private Double latitude, longitude;
    private LocationManager locationManager;
    private static final int GPS_TIME_INTERVAL = 1000 * 2 * 5; // get gps location every 1 min
    private static final int GPS_DISTANCE = 1000; // set the distance value in meter
    private static final int HANDLER_DELAY = 1000 * 10 * 1;
    private static final int START_HANDLER_DELAY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, usersFragment).commit();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                requestLocation();
                handler.postDelayed(this, HANDLER_DELAY);
            }
        }, START_HANDLER_DELAY);



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        //set Fragments by bottom navigation bar
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selected=null;
            switch (item.getItemId()){
                case R.id.users_menu:
                    selected = usersFragment;
                    getSupportActionBar().setTitle(R.string.users);
                    break;
                case R.id.map_menu:
                    selected = mapsFragment;
                    getSupportActionBar().setTitle(R.string.find_on_map);
                    break;
                case R.id.notifications_menu:
                    selected = notificationsFragment;
                    getSupportActionBar().setTitle(R.string.notifications);
                    break;
                case R.id.profile_menu:
                    requestLocation();
                    selected = profileFragment;
                    getSupportActionBar().setTitle(R.string.profile);
                    break;
            }
            if(selected!=null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, selected).commit();
                return true;
            }else
                return false;
        });
    }




    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            sendToLoginPage();
    }

    private void sendToLoginPage() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }



    @Override
    public void onLocationChanged(@NonNull Location location) {
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            setUserLocations(latitude, longitude,user);
        }
    }

    private void setUserLocations(Double latitude, Double longitude, FirebaseUser user) {
        String uid = user.getUid();
        FirebaseFirestore db= FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("Users").document(uid);
        documentReference.update("latitude",latitude,"longitude",longitude).addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               Toast.makeText(MainActivity.this, "" + latitude + ", " + longitude+" koordinatları kaydedildi", Toast.LENGTH_SHORT).show();
           }else{
               Toast.makeText(MainActivity.this, "Koordinatlar kaydedilemedi", Toast.LENGTH_SHORT).show();
           }
        });
    }


    private void requestLocation() {
        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        GPS_TIME_INTERVAL, GPS_DISTANCE, this);
            }else{
                askPermission();
            }
        }
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode==REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();

            } else {
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}