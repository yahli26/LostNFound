package com.gmail.yahlieyal.lostnfound;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
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
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

import androidx.annotation.NonNull;

public class Map_Activity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, AdapterView.OnItemClickListener {

    Button btnCreateLost;
    Button btnCreateFound;
    ImageButton ibMyList;
    ImageButton ibUsersListActivity;

    private BatteryReceiver batteryReceiver;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient; // one of the location APIs in Google Play services
    private PlacesClient placesClient;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private View mapView;
    private final float DEFAULT_ZOOM = 15; // zoom when map opened

    private static ArrayList<BaseLostFound> AllList = new ArrayList<>(); // all base lost and fount that are in cloud
    public static BaseLostFound[] itemToShow = new BaseLostFound[1]; // when click on marker
    private boolean isManager;
    static boolean active = false; // if activity is active
    public static Activity map;

    private ArrayList<Marker> markers = new ArrayList<>(); // list of all markers
    private ArrayList<BaseLostFound> baseLostFoundItem; // item that showed in "one item to show" custom dialog

    DataBaseHelper db;
    private static FirebaseFirestore dbc = FirebaseFirestore.getInstance();
    private static CollectionReference notebookRef = dbc.collection("BaseLostFound");
    private static CollectionReference notebookRefUsers = dbc.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_);
        map=this;
        CheckIfUserWasBanned(); // check if username still exist in cloud
        btnCreateLost = findViewById(R.id.btnCreateLost);
        btnCreateFound = findViewById(R.id.btnCreateFound);
        ibMyList = findViewById(R.id.ibMyListGrayActivity);

        itemToShow[0]=null;
        db = new DataBaseHelper(this);
        Cursor res = db.getAllData();
        res.moveToFirst();
        String strIsManager = res.getString(2);
        if (strIsManager.equals("true")) { // if MANAGER
            isManager=true;
            btnCreateLost.setVisibility(View.GONE);
            btnCreateFound.setVisibility(View.GONE);
            ibMyList.setVisibility(View.GONE);
            ibUsersListActivity = findViewById(R.id.ibUsersListActivity);
            ibUsersListActivity.setVisibility(View.VISIBLE);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Map_Activity.this);
        Places.initialize(Map_Activity.this, "AIzaSyAhgpnYp9VJsMVDLMzSjDm_7pHdaZc3i4M");
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
    }

    public void CheckIfUserWasBanned() { // check if username still exist in cloud
        db = new DataBaseHelper(this);
        Cursor res = db.getAllData();
        res.moveToFirst();
        DocumentReference doc = notebookRefUsers.document(res.getString(1));
        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isComplete() || !task.isSuccessful()) {
                    BanUser();
                }
            }
        });
    }

    public void BanUser() {
        Toast.makeText(Map_Activity.this, "Your user has been deleted", Toast.LENGTH_SHORT).show();
        SignOut();
    }

    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnRefresh:
                initData(); // create allList and all makers
                Toast.makeText(Map_Activity.this, "Refreshed", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnCreateLost:
                intent = new Intent(Map_Activity.this, Create_Item_Activity.class);
                intent.putExtra("ActivityType", "Lost");
                startActivity(intent);
                break;
            case R.id.btnCreateFound:
                intent = new Intent(Map_Activity.this, Create_Item_Activity.class);
                intent.putExtra("ActivityType", "Found");
                startActivity(intent);
                break;
            case R.id.ibSearchGrayActivity:
                intent=new Intent(this, Show_List_Activity.class);
                intent.putExtra("ActivityType", "Search");
                startActivity(intent);
                break;
            case R.id.ibUsersListActivity:
                intent=new Intent(this, Show_Users_Activity.class);
                startActivity(intent);
                break;
            case R.id.ibMyListGrayActivity:
                intent=new Intent(this, Show_List_Activity.class);
                intent.putExtra("ActivityType", "My List");
                startActivity(intent);
                break;
            default:
                Toast.makeText(Map_Activity.this, "Doesn't have onClick", Toast.LENGTH_SHORT).show();
        }
    }

    private void initData() { // create allList
        AllList.clear();
        notebookRef.whereEqualTo("relevant", true).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()==false) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (documentSnapshot.get("lf").toString().equals("L")) {
                            Lost lost = documentSnapshot.toObject(Lost.class);
                            lost.setDocumentId(documentSnapshot.getId());
                            AllList.add(lost);
                        }
                        else if (documentSnapshot.get("lf").toString().equals("F")) {
                            Found found = documentSnapshot.toObject(Found.class);
                            found.setDocumentId(documentSnapshot.getId());
                            AllList.add(found);
                        }
                    }
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (AllList.isEmpty()) {
                    Toast.makeText(getBaseContext(), "Empty Data", Toast.LENGTH_LONG).show();
                }
                else {
                    setMarkers(); // create all markers that will represent all list
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.MyAccount:
                intent = new Intent(Map_Activity.this, Register_Activity.class);
                intent.putExtra("ActivityType", "Edit");
                startActivity(intent);
                return true;
            case R.id.Notification:
                intent = new Intent(Map_Activity.this, Notification_Activity.class);
                startActivity(intent);
                return true;
            case R.id.AboutUs:
                intent = new Intent(Map_Activity.this, About_Us_Activity.class);
                startActivity(intent);
                return true;
            case R.id.RecommendedHardware:
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Recommended Hardware")
                        .setMessage("This application designed for Samsung galaxy A720 1080*1920 dp")
                        .show();
                return true;
            case R.id.SignOut:
                SignOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void SignOut() {
        db.deleteAll(); //clears data on phone
        Intent intent = new Intent(Map_Activity.this, Login_Activity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        initData(); // create allList and all makers

        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            // set layout size and location button of map
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 180, 180, 0);
            locationButton.setLayoutParams(layoutParams);

        }

        //check if gps is enabled or not and then request user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(Map_Activity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(Map_Activity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(Map_Activity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(Map_Activity.this, 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        setMarkers();
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
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            } else { // if there's no permission for location
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
                            Toast.makeText(Map_Activity.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setMarkers() { // sets all marker that represents items of BaseLostFound
        markers.clear();
        mMap.clear();
        mMap.setOnMarkerClickListener(this);
        for (BaseLostFound item : AllList) {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(item.getLocation().getLatitude(),item.getLocation().getLongitude()))
                    .title(item.getType())
                    .snippet(item.getDescription());
            if (item instanceof Lost)
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.lostmarker));
            else
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.foundmarker));
            Marker marker = mMap.addMarker(markerOptions);
            markers.add(marker);

            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(item.getLocation().getLatitude(),item.getLocation().getLongitude()))
                    .radius(item.getRadius())
                    .strokeWidth(6f);
            circleOptions.strokeColor(R.color.quantum_grey800);
            if (item instanceof Lost)
                circleOptions.fillColor(Color.parseColor("#80E41111"));
            else
                circleOptions.fillColor(Color.parseColor("#801930D8"));
            mMap.addCircle(circleOptions);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) { // opens one item show dialog
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), mMap.getCameraPosition().zoom));
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.one_item_show);
        WindowManager.LayoutParams param = dialog.getWindow().getAttributes();
        param.y= -580;
        dialog.getWindow().setAttributes(param);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        ListView lvItem= dialog.findViewById(R.id.lvItem);
        ImageButton ibClose= dialog.findViewById(R.id.ibClose);
        baseLostFoundItem = new ArrayList<>();
        baseLostFoundItem.add(AllList.get(markers.indexOf(marker)));
        BaseArrayListAdapter adapter = new BaseArrayListAdapter(this, baseLostFoundItem);
        lvItem.setAdapter(adapter);
        lvItem.setOnItemClickListener(this);
        adapter.notifyDataSetChanged();
        oneItemDialogClickListener tdc = new oneItemDialogClickListener (dialog);
        ibClose.setOnClickListener(tdc);
        dialog.show();
        //Open item Dialog list View with One item

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { // goes to edit in create item or my list
        Cursor res = db.getAllData();
        res.moveToFirst();
        String myUsername = res.getString(1);
        Intent intent;
        if (baseLostFoundItem.get(i).getCreatorUsername().equals(myUsername)) {
            intent = new Intent(this, Create_Item_Activity.class);
        }
        else {
        intent = new Intent(this, Item_To_Show_Activity.class);
        }
        intent.putExtra("IntentFrom", "Map");
        Map_Activity.itemToShow[0]=baseLostFoundItem.get(i);
        startActivity(intent);
    }

    private class oneItemDialogClickListener implements View.OnClickListener
    {
        Dialog dialog;

        public oneItemDialogClickListener(Dialog _dialog)
        {
            this.dialog = _dialog;
        }

        public void onClick(View v)
        {

            switch (v.getId())
            {
                case R.id.ibClose:
                    dialog.dismiss();
                    break;
                default:
                    Toast.makeText(Map_Activity.this, "Invalid button clicked", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected void onResume() { // create battery receiver
        super.onResume();
        batteryReceiver = new BatteryReceiver();
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        ifilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, ifilter);
    }

    @Override
    protected void onPause() { // close battery receiver
        super.onPause();
        try {
            unregisterReceiver(batteryReceiver);
        }
        catch (IllegalArgumentException e) {
            Log.e("Battery","Error: "+e.toString());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }
}
