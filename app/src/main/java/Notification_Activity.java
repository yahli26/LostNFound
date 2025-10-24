package com.gmail.yahlieyal.lostnfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Notification_Activity extends AppCompatActivity {

    Switch swOldItems;
    ImageButton ibMyListGrayActivity;
    ImageButton ibUsersListActivity;

    public static MediaPlayer mediaPlayer;

    private Calendar c;
    int hour, minute;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private static ArrayList<BaseLostFound> MyList = new ArrayList<>();

    private DataBaseHelper db;
    private static FirebaseFirestore dbc = FirebaseFirestore.getInstance();
    private static CollectionReference notebookRef = dbc.collection("BaseLostFound");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new DataBaseHelper(this);
        swOldItems = findViewById(R.id.swOldItems);
        switchOldItemsAdapter();


        Cursor res = db.getAllData();
        res.moveToFirst();
        String strIsManager = res.getString(2);
        boolean isManager = (strIsManager.equals("true"));
        if (isManager) {
            ibMyListGrayActivity = findViewById(R.id.ibMyListGrayActivity);
            ibMyListGrayActivity.setVisibility(View.GONE);
            ibUsersListActivity = findViewById(R.id.ibUsersListActivity);
            ibUsersListActivity.setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.ibSearchGrayActivity:
                intent=new Intent(this, Show_List_Activity.class);
                intent.putExtra("ActivityType", "Search");
                startActivity(intent);
                finish();
                break;
            case R.id.ibUsersListActivity:
                intent=new Intent(this, Show_Users_Activity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.ibMyListGrayActivity:
                intent=new Intent(this, Show_List_Activity.class);
                intent.putExtra("ActivityType", "My List");
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
                Toast.makeText(this, "Doesn't have onClick", Toast.LENGTH_SHORT).show();
        }
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
                intent = new Intent(Notification_Activity.this, Register_Activity.class);
                intent.putExtra("ActivityType", "Edit");
                startActivity(intent);
                finish();
                return true;
            case R.id.AboutUs:
                intent = new Intent(Notification_Activity.this, About_Us_Activity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.SignOut:
                db.deleteAll();
                intent = new Intent(Notification_Activity.this, Login_Activity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return true;
        }
    }


    private void switchOldItemsAdapter() {
        swOldItems.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if (isChecked) { //turn on
                    if (mediaPlayer != null)
                    {
                        mediaPlayer.stop();
                        mediaPlayer = null;
                    }
                    swOldItems.setText("is On");
                    searchForOldItems();
                } else {
                    swOldItems.setText("is Off");
                    stop();
                }
            }
        });
    }

    public void searchForOldItems() {
        final boolean[] hasAlert = {false};
        MyList.clear();//creating my list
        notebookRef.whereEqualTo("creatorUsername", getMyUsername()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()==false) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (documentSnapshot.get("lf").toString().equals("L")) {
                            Lost lost = documentSnapshot.toObject(Lost.class);
                            lost.setDocumentId(documentSnapshot.getId());
                            MyList.add(lost);
                        }
                        else if (documentSnapshot.get("lf").toString().equals("F")) {
                            Found found = documentSnapshot.toObject(Found.class);
                            found.setDocumentId(documentSnapshot.getId());
                            MyList.add(found);
                        }
                    }
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!MyList.isEmpty()) {
                    for (BaseLostFound temp : MyList) {
                        if (temp.isRelevant()) {
                            if (getDaysBetween(temp.getDateUploaded())>=14) { //checks if it was 14 days ago or more
                                startService(temp);
                                hasAlert[0] =true;
                                break;
                            }
                        }
                    }
                    if (!hasAlert[0])
                        Toast.makeText(getBaseContext(), "No Results Found", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "No Results Found", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startService(final BaseLostFound temp) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Choose Time for Repeated Alert");
        TimePicker tp = new TimePicker(this); //time picker for alert
        tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                hour = i;
                minute = i1;
            }
        });
        adb.setView(tp);
        adb.setPositiveButton("Set Alarm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY, hour);
                        c.set(Calendar.MINUTE, minute);
                        c.setTimeInMillis(System.currentTimeMillis());
                        String input = temp.getType() +", "+ temp.getDescription() + " uploaded before " + getDaysBetween(temp.getDateUploaded()) + " days." ;
                        Intent serviceIntent = new Intent(Notification_Activity.this, Example_Service.class);
                        serviceIntent.putExtra("strExtra", input);
                        serviceIntent.putExtra("strDescription", temp.getDescription());
                        alarmIntent = PendingIntent.getService(Notification_Activity.this,
                                0, serviceIntent, 0);

                        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                                AlarmManager.INTERVAL_DAY, alarmIntent);
                    }
                });
        adb.create().show();
    }

    public void stop() {
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
        Intent serviceIntent = new Intent(this, Example_Service.class);
        stopService(serviceIntent);
    }

    public String getMyUsername() {
        Cursor res = db.getAllData();
        res.moveToFirst();
        return res.getString(1);
    }

    public Date StrToDate(String Strdate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = sdf.parse(Strdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public long getDaysBetween(String date) {
        Date today = Calendar.getInstance().getTime();
        Date temp = StrToDate(date);
        long diff = today.getTime() - temp.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }
}
