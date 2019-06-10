package kr.ac.snu.mobcomp_project.component;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import kr.ac.snu.mobcomp_project.TabFragment1;

public class LocationMonitor {

    private Activity mActivity;
    private TabFragment1 cur_fragment;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationManager mLocationManager;

    private boolean GPSLocatingEnabled = true;
    private boolean NetworkLocationEnabled = true;

    public static final int LOCATION_PERMISSIONS = 2;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private boolean requestingLocationUpdates;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";

    public double latitude;
    public double longitude;
    public float speed;
    private int SAMPLING_INTERVAL = 5000;
    private int MIN_SAMPLING_INTERVAL = 2500;


    public LocationMonitor(Activity activity, Bundle savedInstanceState, Fragment mFragment) {
        cur_fragment = (TabFragment1)mFragment;
        mActivity = activity;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
        mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mActivity, "Need GPS Permission", Toast.LENGTH_LONG).show();

            ActivityCompat.requestPermissions(mActivity,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE
            }, LOCATION_PERMISSIONS);
        } else {
            setup();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setup();
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void setup() {
        mFusedLocationClient.getLastLocation().addOnSuccessListener(mActivity, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    speed = location.getSpeed();
                    cur_fragment.updateLocationValue(latitude,longitude,speed);
                    Log.d("OOOOO", "latitude: " + String.valueOf(latitude) + "   longitude: " + String.valueOf(longitude) + "   speed: " + String.valueOf(speed) );
                }
            }
        });

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(SAMPLING_INTERVAL);
        mLocationRequest.setFastestInterval(MIN_SAMPLING_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder mLocationSettingsRequestBuilder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        SettingsClient mSettingsClient = LocationServices.getSettingsClient(mActivity);
        Task<LocationSettingsResponse> mTask = mSettingsClient.checkLocationSettings(mLocationSettingsRequestBuilder.build());

        mTask.addOnSuccessListener(mActivity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        mTask.addOnFailureListener(mActivity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(mActivity, LocationMonitor.REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {

                    }
                }
            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    speed = location.getSpeed();
                    cur_fragment.updateLocationValue(latitude,longitude,speed);

                    Log.d("PPPP", "latitude: " + String.valueOf(latitude) + "   longitude: " + String.valueOf(longitude) + "   speed: " + String.valueOf(speed) );
                }
            };
        };

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);

//        updateValuesFromBundle(savedInstanceState);
    }

    @SuppressLint("MissingPermission")
    public void onResume() {
        if (mLocationRequest != null && mLocationCallback != null) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        }
    }

    public void onPause() {
        if (mLocationRequest != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if(outState != null) {
            outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates);
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            return;
        }

        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
        }

        // Update UI to match restored state
//        updateUI();
    }
}
