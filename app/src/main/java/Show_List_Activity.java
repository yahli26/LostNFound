package com.gmail.yahlieyal.lostnfound;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.android.volley.VolleyLog.TAG;
import static com.android.volley.VolleyLog.e;

public class Show_List_Activity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    TextView tvHeadline;
    Spinner spnType;
    Spinner spnOrderBy;
    EditText etDescription;
    static ListView lvResult;
    Switch switchLF;
    Switch switchRelevant;
    ImageButton ibHome;
    ImageButton ibMyListGray;
    ImageButton ibMyListOrange;
    ImageButton ibSearchOrange;
    ImageButton ibSearchGray;
    ImageButton ibUsersListActivity;

    private String[] Objects = {"CreditCard", "Headphones", "Bag", "Document", "Watch", "Magnetic card", "Laptop", "Book", "Other object"};
    private String[] Pets = {"Cat", "Rabbit", "Hamsters", "Other pet"};
    private String[] Clothing = {"Sun glasses", "Earring", "Jewel", "Hat", "Sweatshirt", "Coat", "Umbrella", "Other clothing"};
    private ArrayList<Type_Item> typeList;
    private ArrayList<Type_Item> orderByList;
    private Type_Adapter adapterType;

    private static ArrayList<BaseLostFound> AllList = new ArrayList<>();
    private static ArrayList<BaseLostFound> SortedAllList = new ArrayList<>();
    ;
    private static BaseArrayListAdapter adapter;
    public static BaseLostFound[] itemToShow = new BaseLostFound[1];

    // search parameters
    private Boolean isRelevant; // only manager can search by it
    private boolean isMyList; // depends how you got into the activity
    private boolean isManager = false;
    private Boolean isLost = null;
    private String type;
    private String orderBy;
    private String myUsername;
    private Search searchNow;

    private static int pos = -1; // position in the list view of the item that has been clicked.
    //after the item will be edited he will be refresh in the position that he was.
    DataBaseHelper db;
    private static FirebaseFirestore dbc = FirebaseFirestore.getInstance();
    private static CollectionReference notebookRef = dbc.collection("BaseLostFound");
    private Task uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show__list_);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        itemToShow[0] = null;

        db = new DataBaseHelper(this);
        Cursor res = db.getAllData();
        res.moveToFirst();
        myUsername = res.getString(1);
        String strIsManager = res.getString(2);
        isManager = (strIsManager.equals("true"));

        switchRelevant = findViewById(R.id.switchRelevant);
        switchLF = findViewById(R.id.switchLF);
        switchLFAdapter();
        spnType = findViewById(R.id.spnType);
        spinnerAdapter();
        spnOrderBy = findViewById(R.id.spnOrderBy);
        orderBySpinner();
        tvHeadline = findViewById(R.id.tvHeadLine3);
        etDescription = findViewById(R.id.etDescription);
        lvResult = findViewById(R.id.lvResult);
        ibHome = findViewById(R.id.ibHomeActivity);
        ibMyListGray = findViewById(R.id.ibMyListGrayActivity);
        ibMyListOrange = findViewById(R.id.ibMyListOrangeActivity);
        ibSearchGray = findViewById(R.id.ibSearchGrayActivity);
        ibSearchOrange = findViewById(R.id.ibSearchOrangeActivity);
        Bundle bundle = getIntent().getExtras();
        if (isManager) {
            ibMyListGray.setVisibility(View.GONE); // manager has no "My List"
            ibUsersListActivity = findViewById(R.id.ibUsersListActivity);  // manager can search for users
            ibUsersListActivity.setVisibility(View.VISIBLE);
            switchRelevantVisible();  // manager can search "not relevant" items.
            switchRelevant.setText("All Items");
        }
        if (bundle != null) {
            if (bundle.getString("ActivityType").equals("") == false) {
                if (bundle.getString("ActivityType").equals("Search")) {
                    ibSearchGray.setVisibility(View.GONE);
                    ibMyListOrange.setVisibility(View.GONE);
                    isMyList = false;
                    tvHeadline.setText("Search List");
                } else if (bundle.getString("ActivityType").equals("My List") || bundle.getString("ActivityType").equals("My List Service")) {
                    ibSearchOrange.setVisibility(View.GONE);
                    ibMyListGray.setVisibility(View.GONE);
                    isMyList = true;
                    tvHeadline.setText("Search My List");
                    if (bundle.getString("ActivityType").equals("My List Service")) { // if you got to the activity through notification
                        stopNotification();
                        etDescription.setText(Example_Service.description);
                        search();
                    }
                }
            }
        }
    }

    public void stopNotification() {
        if (Notification_Activity.mediaPlayer != null)
        {
            Notification_Activity.mediaPlayer.stop();
            Notification_Activity.mediaPlayer = null;
        }
        Intent serviceIntent = new Intent(this, Example_Service.class);
        stopService(serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        Menu optionsMenu = menu;
        optionsMenu.getItem(5).setVisible(true);
        optionsMenu.getItem(1).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.MyAccount:
                intent = new Intent(Show_List_Activity.this, Register_Activity.class);
                intent.putExtra("ActivityType", "Edit");
                startActivity(intent);
                finish();
                return true;
            case R.id.Notification:
                intent = new Intent(Show_List_Activity.this, Notification_Activity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.AboutUs:
                intent = new Intent(Show_List_Activity.this, About_Us_Activity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.btnSearch:
                search();
                return true;
            case R.id.btnReset:
                etDescription.setText("");
                isLost = null;
                switchLF.setText("Lost N Found");
                if (isManager) {
                    isRelevant=null;
                    switchRelevant.setText("All Items");
                }
                spnOrderBy.setSelection(0);
                if ((orderByList.size() == 4 && !isManager) || (orderByList.size() == 6 && isManager)) { // it has search by tip
                    orderByList.remove(orderByList.size() - 1); // removes search by tip
                }
                spnType.setSelection(0);
                type = null;
                searchNow = new Search();
                return true;
            case R.id.RecommendedHardware:
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Recommended Hardware")
                        .setMessage("This application designed for Samsung galaxy A720 1080*1920 dp")
                        .show();
                return true;
            case R.id.SignOut:
                db.deleteAll();
                intent = new Intent(Show_List_Activity.this, Login_Activity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClick2(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.ibSearchGrayActivity:
                intent = new Intent(this, Show_List_Activity.class);
                intent.putExtra("ActivityType", "Search");
                startActivity(intent);
                finish();
                break;
            case R.id.ibMyListGrayActivity:
                intent = new Intent(this, Show_List_Activity.class);
                intent.putExtra("ActivityType", "My List");
                startActivity(intent);
                finish();
                break;
            case R.id.ibUsersListActivity:
                intent=new Intent(this, Show_Users_Activity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.ibHomeActivity:
                if (Map_Activity.map.isFinishing()) {
                    intent=new Intent(this, Map_Activity.class);
                    startActivity(intent);
                }
                finish();
                break;
            default:
                Log.e(TAG, "Invalid item clicked");
                break;
        }
    }

    private void search() {
        if (uploadTask == null || uploadTask.isComplete()) {
            AllList.clear();
            Task relevant;
            if (isManager && isRelevant != null)
                relevant = notebookRef.whereEqualTo("relevant", isRelevant).get(); // if it's manager and he's search by relevant
            else if (isManager && isRelevant == null)
                relevant = notebookRef.whereGreaterThan("reports", -1).get(); // if it's manager and he's not search by relevant
            else
                relevant = notebookRef.whereEqualTo("relevant", true).get();  // if it's not manager. Only relevant items.
            uploadTask = relevant.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isComplete() && task.isSuccessful()) {
                        if (task.getResult().isEmpty() == false) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                if (documentSnapshot.get("lf").toString().equals("L")) {
                                    Lost lost = documentSnapshot.toObject(Lost.class);
                                    lost.setDocumentId(documentSnapshot.getId());
                                    AllList.add(lost);
                                } else if (documentSnapshot.get("lf").toString().equals("F")) {
                                    Found found = documentSnapshot.toObject(Found.class);
                                    found.setDocumentId(documentSnapshot.getId());
                                    AllList.add(found);
                                }
                            }
                            if (isManager) {
                                myUsername = null;
                                isMyList = false;
                            }
                            searchNow = new Search(etDescription.getText().toString().trim(), isLost, isMyList, type, orderBy, myUsername);
                            if (orderBy != null && orderBy.equals("Location")) { // if it's ordered by location. then gets location
                                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    Toast.makeText(getBaseContext(), "You need to go to setting to allow the location permission.", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (myLocation!=null)
                                    searchNow.setMyLocation(myLocation);
                            }
                            SortedAllList = searchNow.SearchNow(AllList); // copies the searched items in SortedAllList
                            if (SortedAllList.isEmpty())
                                Toast.makeText(getBaseContext(), "No Results Found", Toast.LENGTH_LONG).show();
                            newSearch(); // refresh list view
                        } else
                            Toast.makeText(getBaseContext(), "Empty List", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else
            Toast.makeText(getBaseContext(), "Search is in progress", Toast.LENGTH_LONG).show();
    }

    private void newSearch() { // refresh list view
        adapter = new BaseArrayListAdapter(this, SortedAllList);
        lvResult.setAdapter(adapter);
        lvResult.setOnItemClickListener(this);
        lvResult.setOnItemLongClickListener(this);
        adapter.notifyDataSetChanged();
    }

    private void spinnerAdapter() {
        TypeList();
        adapterType = new Type_Adapter(this, typeList);
        spnType.setAdapter(adapterType);
        spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Type_Item clickedItem = (Type_Item) adapterView.getItemAtPosition(i);
                String clickedTypeName = clickedItem.getName();
                type = clickedTypeName;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void switchRelevantVisible() {
        switchRelevant.setVisibility(View.VISIBLE);
        etDescription.getLayoutParams().width = 550;
        etDescription.requestLayout();
        etDescription.setSingleLine(false);
        etDescription.setTextSize(16);
        switchRelevantAdapter();
    }

    private void switchLFAdapter() {
        switchLF.setTextSize(20);
        switchLF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if (isChecked) {
                    switchLF.setText("Lost");
                    orderByList.add(new Type_Item("Tip (high to low)", R.drawable.tipicon));
                } else {
                    if ((orderByList.size() == 4 && !isManager) || (orderByList.size() == 6 && isManager)) {  // it has search by tip
                        if (spnOrderBy.getSelectedItemPosition() == orderByList.size() - 1)  // and search by tip is selected
                            spnOrderBy.setSelection(0);
                        orderByList.remove(orderByList.size() - 1);
                    }
                    switchLF.setText("Found");
                }
                isLost = isChecked;
            }
        });
    }

    private void switchRelevantAdapter() {
        switchRelevant.setTextSize(20);
        switchRelevant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if (isManager) {
                    if (isChecked) {
                        switchRelevant.setText("Not Relevant");
                        isRelevant = false;
                    }
                    else {
                        switchRelevant.setText("Relevant");
                        isRelevant = true;
                    }
                }
            }
        });
    }

    private void orderBySpinner() {
        OrderByList();
        adapterType = new Type_Adapter(this, orderByList);
        spnOrderBy.setAdapter(adapterType);
        spnOrderBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Type_Item clickedItem = (Type_Item) adapterView.getItemAtPosition(i);
                String clickedTypeName = clickedItem.getName();
                int space = clickedTypeName.indexOf(' ');
                if (clickedItem.equals("Order by"))
                    orderBy = null;
                else {
                    orderBy = clickedTypeName.substring(0, space);
                    if (lvResult.getAdapter()!=null) {
                        search();
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void OrderByList() {
        orderByList = new ArrayList<>();
        orderByList.add(new Type_Item("Order by", R.drawable.orderbyicon));
        orderByList.add(new Type_Item("Location (close to far)", R.drawable.locationicon));
        orderByList.add(new Type_Item("Date (new to old)", R.drawable.dateicon));
        if (isManager) {
            orderByList.add(new Type_Item("Report (high to low)", R.drawable.reportsearch));
            orderByList.add(new Type_Item("Upload (old to new)", R.drawable.uploadsearch));
        }
    }

    private void TypeList() {
        typeList = new ArrayList<>();
        typeList.add(new Type_Item("Type", R.drawable.typeicon));
        typeList.add(new Type_Item("Phone", R.drawable.phoneicon));
        typeList.add(new Type_Item("Dog", R.drawable.dogicon));
        typeList.add(new Type_Item("Wallet", R.drawable.walleticon));
        typeList.add(new Type_Item("Keys", R.drawable.keysicon));
        for (int i = 0; i < Objects.length + Pets.length + Clothing.length; i++) {
            if (i < Objects.length)
                typeList.add(new Type_Item(Objects[i], R.drawable.objectsicon));
            else if (i < Objects.length + Pets.length)
                typeList.add(new Type_Item(Pets[i-Objects.length], R.drawable.petsicon));
            else
                typeList.add(new Type_Item(Clothing[i-Objects.length-Pets.length], R.drawable.clothingicon));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (SortedAllList.get(i).getLocation() != null) {  // open quick view of location
                Intent intent =new Intent(this, PointOnMap_Activity.class);
                double[] latLng ={SortedAllList.get(i).getLocation().getLatitude(), SortedAllList.get(i).getLocation().getLongitude()} ;
                intent.putExtra("Location", latLng);
                intent.putExtra("Radius", SortedAllList.get(i).getRadius());
                intent.putExtra("DocId", SortedAllList.get(i).getDocumentId());
                if (isMyList==false)
                    intent.putExtra("ActivityType", "NoEdit"); // means it cannot change the location or radius
                else
                    intent.putExtra("ActivityType", "ToSave"); // means it will directly save the location
                if (SortedAllList.get(i) instanceof Lost)
                    intent.putExtra("item", "Lost");
                else if (SortedAllList.get(i) instanceof Found)
                    intent.putExtra("item", "Found");
                itemToShow[0]=SortedAllList.get(i);
                pos=i;
                startActivity(intent);
            }
            else {
                Toast.makeText(this, "No location found", Toast.LENGTH_LONG).show();
            }
        }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

        Intent intent;
        if (isMyList)
            intent = new Intent(this, Create_Item_Activity.class);
        else
            intent = new Intent(this, Item_To_Show_Activity.class);
        intent.putExtra("IntentFrom", "List");
        Show_List_Activity.itemToShow[0]=SortedAllList.get(i); // sets the public static item that will be taken in the next activity
        pos=i;
        startActivity(intent);
        return true;
    }

    public static void refreshItemList () { // refresh the list view
        if (SortedAllList != null&& pos>=0) {
            SortedAllList.set(pos, itemToShow[0]);
            adapter.notifyDataSetChanged();
        }
    }

    public static void deleteItem() { // delete item from the list view
        if (adapter != null && pos>=0) {
            SortedAllList.remove(pos);
            adapter.notifyDataSetChanged();
        }
    }

    public static void refreshLocation (GeoPoint location, int radius, String docId) { // refresh location directly. cloud and list.
        WriteBatch batch = dbc.batch();
        DocumentReference doc = notebookRef.document(docId);
        batch.update(doc, "location", location);
        batch.update(doc, "radius", radius);
        if (SortedAllList!=null&&pos>=0) {
            BaseLostFound temp = SortedAllList.get(pos);
            temp.setLocation(location);
            temp.setRadius(radius);
            SortedAllList.set(pos, temp);
            adapter.notifyDataSetChanged();
        }
        batch.commit();
    }
}
