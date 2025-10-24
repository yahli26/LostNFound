package com.gmail.yahlieyal.lostnfound;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.GeoPoint;
import com.skyfishjy.library.RippleBackground;
import com.xw.repo.BubbleSeekBar;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PointOnMap_Activity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener{

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;

    private Location mLastKnownLocation;
    private GeoPoint previousLocation=null;
    private LocationCallback locationCallback;
    private final float DEFAULT_ZOOM = 15.5f;

    private View mapView;
    private Button btnConfirm;
    private BubbleSeekBar bubbleSeekBar; //controls radius in meters
    private TextView tvRadius;
    private ImageView marker;
    private boolean isEdit=true;
    private int radius=20;
    private CircleOptions circleOptions;
    private boolean isLost;
    private boolean isSaving; // if you got to this activity directly through my list
    private String documentId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_on_map_);
        bubbleSeekBar = findViewById(R.id.bubbleSeekBar);
        createBubbleSeekbar();
        btnConfirm = findViewById(R.id.btnConfirm);
        tvRadius = findViewById(R.id.tvRadius);
        marker = findViewById(R.id.marker);

        circleOptions = new CircleOptions();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(PointOnMap_Activity.this);

        Bundle bundle = getIntent().getExtras();
        if (bundle!=null) {
            if (bundle.getString("item").equals("Lost"))
                isLost=true;
            else if (bundle.getString("item").equals("Found"))
                isLost=false;
            if (bundle.getString("ActivityType")!=null)
                if (bundle.getString("ActivityType").equals("ToSave")==true) { //if you got to this activity directly through my list.
                    documentId = Show_List_Activity.itemToShow[0].getDocumentId(); // will save the new location right after click btn confirm
                    isSaving=true;
                }
            if (bundle.getDoubleArray("Location")!=null) { // if it is not new item. Opened on previous location.
                double[] latLng = bundle.getDoubleArray("Location");
                GeoPoint temp = new GeoPoint(latLng[0], latLng[1]);
                previousLocation = temp;
                bubbleSeekBar.setProgress(bundle.getInt("Radius"));
                radius=bundle.getInt("Radius");
                tvRadius.setText("Radius: "+radius+" meters");
            }
        }
        if (isLost)
            marker.setImageResource(R.drawable.lostmarker);
        else
            marker.setImageResource(R.drawable.foundmarker);

        Places.initialize(PointOnMap_Activity.this, "AIzaSyAhgpnYp9VJsMVDLMzSjDm_7pHdaZc3i4M");
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

    }

    public void createBubbleSeekbar() {

        tvRadius =(TextView)findViewById(R.id.tvRadius);
        bubbleSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                tvRadius.setText("Radius: "+progress+ " meters");
                if (mMap!=null)
                    setCircleRadius(progress); // create new circle in the size
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
            }
        });
    }

    private void setCircleRadius(Integer radius) { // create new circle in the size
        mMap.clear();
        circleOptions.center(new LatLng(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude));
        circleOptions.radius(radius);
        mMap.addCircle(circleOptions);
    }

    private void createCircleOptions() { // create circle in the opening
        circleOptions.center(new LatLng(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude));
        circleOptions.radius(radius);
        circleOptions.strokeWidth(6f);
        circleOptions.strokeColor(R.color.quantum_grey800);
        if (isLost)
            circleOptions.fillColor(Color.parseColor("#80E41111"));
        else
            circleOptions.fillColor(Color.parseColor("#801930D8"));
        mMap.addCircle(circleOptions);
    }

    public void btnConfirm(View v) {
        if (isEdit==false) { // in this case: btnConfirm = exit
            finish();
        }
        else if (mMap != null) {
            int radius = bubbleSeekBar.getProgress();
            GeoPoint temp = new GeoPoint(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);
            if (isSaving) { // refresh location right now
                if (documentId!=null)
                    Show_List_Activity.refreshLocation(temp, radius, documentId);
            }
            else // refresh location in the create item activity (Edit Mode). Then only if the edited item will be saved location will change.
                Create_Item_Activity.setLocation(temp, radius);
            finish();
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Bundle bundle = getIntent().getExtras();
        if (bundle!=null) {
            if (bundle.getString("ActivityType")!=null) {
                if (bundle.getString("ActivityType").equals("NoEdit")) {
                    isEdit=false;
                    mMap.getUiSettings().setAllGesturesEnabled(false);
                    bubbleSeekBar.setVisibility(View.GONE);
                    btnConfirm.setText("Exit");
                }
                else {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    mMap.setMinZoomPreference(12f);
                    mMap.setMaxZoomPreference(18f);
                    mMap.setOnCameraMoveListener(this);
                }
            }
        }

        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 180, 180, 0);
            locationButton.setLayoutParams(layoutParams);
        }

        if (previousLocation!=null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()), DEFAULT_ZOOM));
            createCircleOptions();
        }
        //check if gps is enabled or not and then request user to enable it
        if (isEdit) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

            SettingsClient settingsClient = LocationServices.getSettingsClient(PointOnMap_Activity.this);
            Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

            task.addOnSuccessListener(PointOnMap_Activity.this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    getDeviceLocation();
                }
            });

            task.addOnFailureListener(PointOnMap_Activity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ResolvableApiException) {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        try {
                            resolvable.startResolutionForResult(PointOnMap_Activity.this, 51);
                        } catch (IntentSender.SendIntentException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        if (previousLocation==null) {
            mFusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful()) {
                                mLastKnownLocation = task.getResult();
                                if (mLastKnownLocation != null) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                } else {
                                    final LocationRequest locationRequest = LocationRequest.create();
                                    locationRequest.setInterval(10000);
                                    locationRequest.setFastestInterval(5000);
                                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                    locationCallback = new LocationCallback() {
                                        @Override
                                        public void onLocationResult(LocationResult locationResult) {
                                            super.onLocationResult(locationResult);
                                            if (locationResult == null) {
                                                return;
                                            }
                                            mLastKnownLocation = locationResult.getLastLocation();
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                        }
                                    };
                                    mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                                }
                            } else {
                                Toast.makeText(PointOnMap_Activity.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        createCircleOptions();
    }

    @Override
    public void onBackPressed() {
        if (isEdit) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Closing Activity")
                    .setMessage("Are you sure you want to exit without choosing location?")
                    .setPositiveButton("Yes, I'm sure", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        }
        else
            finish();
    }

    @Override
    public void onCameraMove() { // listener to camera move and create new circle in the new center of screen
        mMap.clear();
        circleOptions.center(new LatLng(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude));
        mMap.addCircle(circleOptions);
    }
}