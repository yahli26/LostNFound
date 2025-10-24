package com.gmail.yahlieyal.lostnfound;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class Login_Activity extends AppCompatActivity {

    EditText etUsername;
    EditText etPassword;
    EditText etMail;
    EditText etPhoneNumber;
    EditText etFirstName;
    Button btnLogin;
    ImageButton ibMyListGrayActivity;
    TextView tvHeadLine;
    TextView tvChange;
    LinearLayout menuLayout;
    LinearLayout usernameLayout;
    LinearLayout nameLayout;
    LinearLayout phoneNumberLayout;
    LinearLayout mailLayout;
    LinearLayout manCodeLayout;
    ImageView ivIconHeadline;

    DataBaseHelper db;

    private FirebaseFirestore dbc = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = dbc.collection("Users");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new DataBaseHelper(this);

        Cursor res = db.getAllData();
        if(res.getCount() > 0) {
            db.deleteAll();
           // clear the data on phone (username & isManager)
        }

        etMail = findViewById(R.id.etMail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etFirstName = findViewById(R.id.etFirstName);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUserName);
        btnLogin = findViewById(R.id.btnRegister);
        ibMyListGrayActivity = findViewById(R.id.ibMyListGrayActivity);
        tvChange = findViewById(R.id.tvChange);
        tvHeadLine = findViewById(R.id.tvHeadLine1);
        menuLayout = findViewById(R.id.menuLayout);
        menuLayout.setVisibility(View.GONE);
        usernameLayout = findViewById(R.id.userNameLayout);
        nameLayout = findViewById(R.id.firstNameLayout);
        phoneNumberLayout = findViewById(R.id.phonenumberLayout);
        mailLayout = findViewById(R.id.mailLayout);
        manCodeLayout = findViewById(R.id.manCodeLayout);
        ivIconHeadline = findViewById(R.id.ivIconHeadline);

        nameLayout.setVisibility(View.GONE);
        phoneNumberLayout.setVisibility(View.GONE);
        mailLayout.setVisibility(View.GONE);
        manCodeLayout.setVisibility(View.GONE);
        tvHeadLine.setText("Login");
        tvChange.setText("Not a member? Join us.");
        btnLogin.setText("Login");
    }

    public void Change(View v) { // change to register activity
        Intent intent = new Intent(this, Register_Activity.class);
        startActivity(intent);
        finish();
    }

    public void FinishActivity(View v) { // when sign in button is pressed
        final String UserName = etUsername.getText().toString().trim();
        final String Password = etPassword.getText().toString().trim();

        DocumentReference doc = notebookRef.document(UserName);
        doc.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) { // if username is exist
                            User myUser = documentSnapshot.toObject(User.class);
                            if (myUser.getPassword().equals(Password)) {  // and it has the same password
                                String strIsManager = "false";
                                if (myUser.getIsManager())
                                    strIsManager ="true";
                                Boolean isInserted = db.insertData(UserName, strIsManager); // insert new data to phone
                                if (isInserted) {
                                    Toast.makeText(getBaseContext(), "Connected", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getBaseContext(), Permission_Activity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } else
                                Toast.makeText(getBaseContext(), "Wrong Password", Toast.LENGTH_LONG).show();
                        }
                        else
                            Toast.makeText(getBaseContext(), "No Username Found", Toast.LENGTH_LONG).show();
                    }
                });
    }
}

