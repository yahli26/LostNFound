package com.gmail.yahlieyal.lostnfound;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.android.volley.VolleyLog.TAG;

public class Show_Users_Activity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    TextView tvHeadline;
    Spinner spnOrderBy;
    EditText etUsername;
    static ListView lvResult;
    Spinner spnType;
    Switch switchLF;
    Switch switchIsManager;
    ImageButton ibHome;
    ImageButton ibMyListGray;
    ImageButton ibMyListOrange;
    ImageButton ibSearchOrange;
    ImageButton ibSearchGray;
    ImageButton ibUsersOrange;

    private ArrayList<Type_Item> orderByList;
    private Type_Adapter adapterType;

    private static ArrayList<User> AllList = new ArrayList<>();
    private static ArrayList<User> SortedAllList = new ArrayList<>();

    private static UserArrayListAdapter adapter;
 // search parameters
    private Boolean isManager = null;
    private String orderBy;
    private SearchUsers searchNow;

    DataBaseHelper db;
    private static FirebaseFirestore dbc = FirebaseFirestore.getInstance();
    private static CollectionReference notebookRef = dbc.collection("Users");
    private static CollectionReference notebookItemsRef = dbc.collection("BaseLostFound");
    private Task uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show__list_);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new DataBaseHelper(this);

        switchIsManager = findViewById(R.id.switchRelevant);
        switchLF = findViewById(R.id.switchLF);
        switchLF.setVisibility(View.GONE);
        spnType = findViewById(R.id.spnType);
        spnType.setVisibility(View.GONE);
        spnOrderBy = findViewById(R.id.spnOrderBy);
        orderBySpinner();
        tvHeadline = findViewById(R.id.tvHeadLine3);
        etUsername = findViewById(R.id.etDescription);
        lvResult = findViewById(R.id.lvResult);
        ibHome = findViewById(R.id.ibHomeActivity);
        ibMyListGray = findViewById(R.id.ibMyListGrayActivity);
        ibMyListOrange = findViewById(R.id.ibMyListOrangeActivity);
        ibSearchGray = findViewById(R.id.ibSearchGrayActivity);
        ibSearchOrange = findViewById(R.id.ibSearchOrangeActivity);

        ibMyListGray.setVisibility(View.GONE);
        switchIsManagerVisible();
        switchIsManager.setText("All Users");
        tvHeadline.setText("Search Users");
        etUsername.setHint("Search for username or name");
        ibSearchOrange.setVisibility(View.GONE);
        ibMyListOrange.setVisibility(View.GONE);
        ibUsersOrange = findViewById(R.id.ibUsersListOrangeActivity);
        ibUsersOrange.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        Menu optionsMenu = menu;
        optionsMenu.getItem(5).setVisible(true);
        optionsMenu.getItem(1).setVisible(true);
        optionsMenu.getItem(0).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.MyAccount:
                intent = new Intent(Show_Users_Activity.this, Register_Activity.class);
                intent.putExtra("ActivityType", "Edit");
                startActivity(intent);
                finish();
                return true;
            case R.id.Notification:
                intent = new Intent(Show_Users_Activity.this, Notification_Activity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.AboutUs:
                intent = new Intent(Show_Users_Activity.this, About_Us_Activity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.btnSearch:
                search();
                return true;
            case R.id.btnReset:
                etUsername.setText("");
                isManager = null;
                orderBy = null;
                switchIsManager.setText("All Users");
                spnOrderBy.setSelection(0);
                searchNow = new SearchUsers();
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
        db.deleteAll();
        Intent intent = new Intent(Show_Users_Activity.this, Login_Activity.class);
        startActivity(intent);
        finish();
    }

    public void onClick2(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.ibHomeActivity:
                finish();
                break;
            case R.id.ibSearchGrayActivity:
                intent=new Intent(this, Show_List_Activity.class);
                intent.putExtra("ActivityType", "Search");
                startActivity(intent);
                break;
            default:
                Log.e(TAG, "Invalid item clicked");
                break;
        }
    }

    private void search() {
        if (uploadTask == null || uploadTask.isComplete()) {
            AllList.clear();
            Task isMan;
            if (isManager == null) // checks if it doesn't search for manger users
                isMan = notebookRef.whereGreaterThan("sumUploads", -1).get();
            else
                isMan = notebookRef.whereEqualTo("isManager", isManager).get();
            uploadTask = isMan.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isComplete() && task.isSuccessful()) {
                        if (task.getResult().isEmpty() == false) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    User user = documentSnapshot.toObject(User.class);
                                    user.setUsername(documentSnapshot.getId());
                                    AllList.add(user);
                            }
                            searchNow = new SearchUsers(orderBy, etUsername.getText().toString().trim());
                            SortedAllList = searchNow.SearchNow(AllList);
                            if (SortedAllList.isEmpty())
                                Toast.makeText(getBaseContext(), "No Results Found", Toast.LENGTH_LONG).show();
                            newSearch(); // refresh list
                        } else
                            Toast.makeText(getBaseContext(), "Empty List", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else
            Toast.makeText(getBaseContext(), "Search is in progress", Toast.LENGTH_LONG).show();
    }

    private void newSearch() { // refresh list
        adapter = new UserArrayListAdapter(this, SortedAllList); //CREATE NEW ADAPTER
        lvResult.setAdapter(adapter);
        lvResult.setOnItemClickListener(this);
        adapter.notifyDataSetChanged();
    }

    public void switchIsManagerVisible() {
        switchIsManager.setVisibility(View.VISIBLE);
        etUsername.getLayoutParams().width = 550;
        etUsername.requestLayout();
        etUsername.setSingleLine(false);
        etUsername.setTextSize(16);
        switchIsManagerAdapter();
    }

    private void switchIsManagerAdapter() {
        switchIsManager.setTextSize(20);
        switchIsManager.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if (isChecked)
                    switchIsManager.setText("Managers");
                else
                    switchIsManager.setText("Guests");
                isManager=isChecked;
                search();
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
        orderByList.add(new Type_Item("Date (old to new)", R.drawable.dateicon));
        orderByList.add(new Type_Item("Uploads (low to high)", R.drawable.uploadsearch));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) { // shoet click = deleting user
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Deleting")
                .setMessage("Do you want to delete the account?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes i'm sure", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DocumentReference doc = notebookRef.document(SortedAllList.get(i).getUsername());
                        DeleteItemsOfUser(SortedAllList.get(i).getUsername());
                        doc.delete();
                        Toast.makeText(getBaseContext(), "User Deleted", Toast.LENGTH_LONG).show();
                        if (SortedAllList.get(i).getUsername().equals(getMyUsername())) {
                            SignOut();
                        }
                        finish();
                    }
                })
                .show();
    }

    public void DeleteItemsOfUser(String User) { // deletes all items of the deleted user
        notebookItemsRef.whereEqualTo("creatorUsername", User).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete() && task.isSuccessful()) {
                    if (task.getResult().isEmpty() == false) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            DocumentReference doc =  documentSnapshot.getReference();
                            if (documentSnapshot.contains("img") && documentSnapshot.get("img")!=null && documentSnapshot.get("img").equals("")==false)
                                deleteStorage((String) documentSnapshot.get("img"));
                            doc.delete();
                        }
                    }
                }
            }
        });
    }

    public void deleteStorage(String img) { // delete storage of the items of the user that have been deleted
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(img);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getBaseContext(), "Success Deleting", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "ERROR: " +e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getMyUsername() {
        db = new DataBaseHelper(this);
        Cursor res = db.getAllData();
        res.moveToFirst();
        return res.getString(1);
    }




}
