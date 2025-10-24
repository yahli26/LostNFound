package com.gmail.yahlieyal.lostnfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class About_Us_Activity extends AppCompatActivity {

    TextView tvAboutUs;
    DataBaseHelper db;
    ImageButton ibMyListGrayActivity;
    ImageButton ibUsersListActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about__us_);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new DataBaseHelper(this);
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
        tvAboutUs = findViewById(R.id.tvAboutUs);
        String Line1 =  "This application created and designed bt Yahli Eyal for school project.";
        String Line2 =  "This application can help you and others to find their lost.";
        String Line3 =  "You can find your lost in the map or search and then call the user who found it";
        String Line4 =  "Help the community and create also founds if you find a lost.";
        String Line5 =  "LostNFound are not responsible for any negative exploitation of this app.";
        String Line6 = "Hope you got it all and give me the 100 grade ;)";
        tvAboutUs.setText(Html.fromHtml(Line1+"<br>"+Line2 + " <br>"+Line3+ " <br>"+Line4+"<br>"+Line5+"<br>"+Line6));
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
                Toast.makeText(About_Us_Activity.this, "Doesn't have onClick", Toast.LENGTH_SHORT).show();
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
                intent = new Intent(About_Us_Activity.this, Register_Activity.class);
                intent.putExtra("ActivityType", "Edit");
                startActivity(intent);
                finish();
                return true;
            case R.id.Notification:
                intent = new Intent(About_Us_Activity.this, Notification_Activity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.SignOut:
                db.deleteAll();
                intent = new Intent(About_Us_Activity.this, Login_Activity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
