package com.gmail.yahlieyal.lostnfound;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Calendar;
import java.util.regex.Pattern;


public class Register_Activity extends AppCompatActivity  {

    private static final String MANAGER_KEY = "L20F20";

    EditText etUsername;
    EditText etPassword;
    EditText etMail;
    EditText etPhoneNumber;
    EditText etFirstName;
    EditText etManCode;
    Button btnSignUp;
    ImageButton ibMyListGrayActivity;
    ImageButton ibUsersListActivity;
    TextView tvHeadLine;
    TextView tvChange;
    LinearLayout menuLayout;
    LinearLayout usernameLayout;
    LinearLayout nameLayout;
    LinearLayout phoneNumberLayout;
    LinearLayout mailLayout;
    LinearLayout manCodeLayout;
    ImageView ivIconHeadline;

    private DataBaseHelper db;
    private boolean isEdit = false;
    private String isManager = "false";

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
        if (res.getCount() > 0) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                if (bundle.getString("ActivityType") != null) {
                    if (bundle.getString("ActivityType").equals("Edit")) { // if it has already an account and it is edit mode
                        isEdit = true;
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
                }
                else { // has an account already but it's not edit mode. Means it's auto sign in.
                    GoToPermissionActivity();
                }
            } else { // has an account already but it's not edit mode. Means it's auto sign in.
                GoToPermissionActivity();
            }
        }
        etMail = findViewById(R.id.etMail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etFirstName = findViewById(R.id.etFirstName);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUserName);
        etManCode = findViewById(R.id.etManCode);
        btnSignUp = findViewById(R.id.btnRegister);
        tvChange = findViewById(R.id.tvChange);
        tvHeadLine = findViewById(R.id.tvHeadLine1);
        menuLayout = findViewById(R.id.menuLayout);
        usernameLayout = findViewById(R.id.userNameLayout);
        nameLayout = findViewById(R.id.firstNameLayout);
        phoneNumberLayout = findViewById(R.id.phonenumberLayout);
        mailLayout = findViewById(R.id.mailLayout);
        manCodeLayout = findViewById(R.id.manCodeLayout);
        ivIconHeadline = findViewById(R.id.ivIconHeadline);

        if (isEdit) {
            tvHeadLine.setText("Edit Account");
            btnSignUp.setText("Save Changes");
            menuLayout.setVisibility(View.VISIBLE);
            usernameLayout.setVisibility(View.GONE);
            tvChange.setVisibility(View.GONE);
            manCodeLayout.setVisibility(View.GONE);
            ivIconHeadline.setImageResource(R.drawable.nametagicon);

            if (res.getCount() == 0) {
                Toast.makeText(this, "No Data", Toast.LENGTH_LONG).show();
            } else {
                res.moveToFirst();
                DocumentReference doc = notebookRef.document(res.getString(1));
                doc.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) { // Sets fields of edit with current user details
                                        User myUser = documentSnapshot.toObject(User.class);
                                        etFirstName.setText(myUser.getFirstName());
                                        etPassword.setText(myUser.getPassword());
                                        etPhoneNumber.setText(myUser.getPhoneNumber());
                                        etMail.setText(myUser.getMail());
                                    }
                            }
                        });
            }
        }
    }

    private void GoToPermissionActivity() {
        Intent intent = new Intent(this, Permission_Activity.class);
        startActivity(intent);
        finish();
    }

    public void Change(View v) { // change to login activity
        Intent intent = new Intent(this, Login_Activity.class);
        startActivity(intent);
        finish();
    }

    public void FinishActivity(View v) { // confirm button
        String UserName = etUsername.getText().toString().trim();
        String Password = etPassword.getText().toString().trim();
        String FirstName = etFirstName.getText().toString().trim();
        String Mail = etMail.getText().toString().trim();
        String PhoneNumber = etPhoneNumber.getText().toString().trim();
        String ManagerCode = etManCode.getText().toString().trim();

        Cursor res = db.getAllData();
        res.moveToFirst();
        if (isEdit)
            UserName = res.getString(1);
        if (isMangerCodeNotValid(ManagerCode)) // if you tried to answer the Manager Code and you're wrong
            Toast.makeText(this, "Your Manger Code isn't right", Toast.LENGTH_LONG).show();
        else {
            if (isMangerCodeValid(ManagerCode)) // if you tried to answer the Manager Code and you're right
                isManager="true";
            if (isUsernameValid(UserName) == false)
                Toast.makeText(this, "Your Username isn't Valid", Toast.LENGTH_LONG).show();
            else if (isPasswordValid(Password) == false)
                Toast.makeText(this, "Your Password isn't Valid", Toast.LENGTH_LONG).show();
            else if (isFirstNameValid(FirstName) == false)
                Toast.makeText(this, "Your input name isn't  Valid", Toast.LENGTH_LONG).show();
            else if (isPhoneNumberValid(PhoneNumber) == false)
                Toast.makeText(this, "Your Phone number isn't Valid", Toast.LENGTH_LONG).show();
            else if (isMailValid(Mail) == false)
                Toast.makeText(this, "Your email isn't Valid", Toast.LENGTH_LONG).show();
            else if (isEdit)
                updateData(UserName, Password, FirstName, PhoneNumber, Mail); // update user details in cloud
            else {
                DocumentReference doc = notebookRef.document(UserName);
                doc.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    Toast.makeText(getBaseContext(), "The Username has been taken", Toast.LENGTH_LONG).show();
                                }
                                else { // if username hasn't taken
                                    uploadData();
                                }
                            }
                        });
            }
        }
    }



    public void updateData(String UserName, String Password, String FirstName, String PhoneNumber, String Mail) {
            WriteBatch batch = dbc.batch();
            DocumentReference doc = notebookRef.document(UserName);
            batch.update(doc, "password", Password);
            batch.update(doc, "firstName", FirstName);
            batch.update(doc, "phoneNumber", PhoneNumber);
            batch.update(doc, "mail", Mail);
            batch.commit();
            Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();
            finish();
        }

    public void uploadData() { // upload to cloud and set data of username & isManager in phone
        String UserName = etUsername.getText().toString().trim();
        String Password = etPassword.getText().toString().trim();
        String FirstName = etFirstName.getText().toString().trim();
        String Mail = etMail.getText().toString().trim();
        String PhoneNumber = etPhoneNumber.getText().toString().trim();
        boolean isMan = isManager.equals("true");

        Boolean isInserted = db.insertData(UserName, isManager); // put in phone memory
        if (isInserted) {
            User myUser = new User(UserName, Password, FirstName, PhoneNumber, Mail, getDateToday(), isMan);
            DocumentReference doc1 = notebookRef.document(UserName);
            doc1.set(myUser); // put in cloud

            Toast.makeText(this, "Created", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, Permission_Activity.class);
            startActivity(intent);
            finish();
        } else
            Toast.makeText(this, "NOT Created", Toast.LENGTH_LONG).show();
    }

    private String getDateToday() {
        Calendar today = Calendar.getInstance();
        return  today.get(Calendar.DAY_OF_MONTH) + "/" + (1 + today.get(Calendar.MONTH)) + "/" + today.get(Calendar.YEAR);
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
            case R.id.ibMyListGrayActivity:
                intent=new Intent(this, Show_List_Activity.class);
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
                Toast.makeText(Register_Activity.this, "Doesn't have onClick", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        Menu optionsMenu = menu;
        Bundle bundle = getIntent().getExtras();
        boolean isEdit = false;
        if (bundle != null) {
            if (bundle.getString("ActivityType") != null){
                if (bundle.getString("ActivityType").equals("Edit")) {
                    isEdit = true;
                }
            }
        }
        if (!isEdit)
            optionsMenu.getItem(7).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.MyAccount:
                intent = new Intent(Register_Activity.this, Register_Activity.class);
                intent.putExtra("ActivityType", "Edit");
                startActivity(intent);
                finish();
                return true;
            case R.id.Notification:
                intent = new Intent(Register_Activity.this, Notification_Activity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.AboutUs:
                intent=new Intent(this, About_Us_Activity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.SignOut:
                db.deleteAll();
                intent = new Intent(Register_Activity.this, Login_Activity.class);
                startActivity(intent);
                if (!Map_Activity.map.isFinishing())
                    Map_Activity.map.finish();
               finish();
                return true;
        }
        return true;
    }

    public boolean isMangerCodeNotValid(String managerCode) {
        return (managerCode !=null && managerCode.equals("")==false && managerCode.equals(MANAGER_KEY)==false);
    }

    private boolean isPasswordValid(String password) {
        if (password.length()>=4)
            return true;
        return false;
    }

    public boolean isMangerCodeValid(String managerCode) {
        return (managerCode !=null && managerCode.equals("")==false && managerCode.equals(MANAGER_KEY));
    }

    public Boolean isFirstNameValid (String firstName) {
        if (firstName!=null) {
            if (firstName.length()>=2 && firstName.length()<=15) {
                if (firstName.indexOf(' ')==-1) {
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean isPhoneNumberValid (String phoneNumber) {
        if (phoneNumber!=null) {
            if (phoneNumber.length()==10) {
                if (phoneNumber.substring(0,2).equals("05")==true) {
                    for (int i=2; i<phoneNumber.length(); i++) {
                        if (phoneNumber.charAt(i)>'9' && phoneNumber.charAt(i)<'0') {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isMailValid(String mail)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (mail == null)
            return false;
        return pat.matcher(mail).matches();
    }

    private Boolean isUsernameValid(String Username) {
        if (Username.length()>4) {
            for (int i=0; i<Username.length(); i++) {
                if (Username.charAt(i)<'0' || Username.charAt(i)>'9')
                    if (Username.charAt(i)<'a' || Username.charAt(i)>'z')
                        if (Username.charAt(i)<'A' || Username.charAt(i)>'Z')
                            return false;
            }
            return true;
        }
        else
            return false;
    }

    @Override
    public void onBackPressed() {
        if (isEdit) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Closing Activity")
                    .setMessage("Are you sure you want to exit without saving?")
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
}

