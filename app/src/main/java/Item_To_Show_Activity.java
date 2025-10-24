package com.gmail.yahlieyal.lostnfound;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

public class Item_To_Show_Activity extends AppCompatActivity {

    TextView tvHeadLine;
    TextView tvDescription;
    TextView tvDate;
    TextView tvTip;
    TextView tvSecurityQuestion;
    TextView tvType;
    TextView tvNamePhoneNumber;
    TextView tvRelevantReports;
    TextView tvUserData;
    LinearLayout securityAnsLayout;
    LinearLayout securityQLayout;
    LinearLayout tipLayout;
    LinearLayout callLayout;
    LinearLayout userLayout;
    LinearLayout managerLayout;
    ImageView iconLF;
    ImageView imgItem;
    EditText etSecurityAnswer;
    Button btnConfirmAnswer;
    Button btnLocation;

    private String[] Objects = {"Creditcard", "Headphones", "Bag", "Document", "Watch", "Magnetic card", "Laptop", "Book", "Other object"};
    private String[] Pets = {"Cat", "Rabbit", "Hamsters", "Other pet"};
    private String[] Clothing = {"Sun glasses", "Earring", "Jewel", "Hat", "Sweatshirt", "Coat", "Umbrella", "Other clothing"};
    private boolean haveReported = false;
    private int tries = 3; // 3 tries to answer the security Q

    private boolean isLost;
    private boolean isManager=false;
    private Lost itemLost;
    private Found itemFound;
    private BaseLostFound baseLostFound;
    private String firstName;
    private String phoneNumber;

    DataBaseHelper db;
    private static FirebaseFirestore dbc = FirebaseFirestore.getInstance();
    private static CollectionReference notebookRef = dbc.collection("Users");
    private static CollectionReference notebookRefItems = dbc.collection("BaseLostFound");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item__to__show_);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new DataBaseHelper(this);
        Cursor res = db.getAllData();
        res.moveToFirst();
        String strIsManager = res.getString(2);
        isManager = (strIsManager.equals("true"));

        tvHeadLine = findViewById(R.id.tvHeadLine5);
        tvDescription = findViewById(R.id.tvDescription);
        tvDate = findViewById(R.id.tvDate);
        tvTip = findViewById(R.id.tvTip);
        tvNamePhoneNumber = findViewById(R.id.tvNamePhoneNumber);
        tvSecurityQuestion = findViewById(R.id.tvSecurityQuestion);
        tvType = findViewById(R.id.tvType);
        tvRelevantReports = findViewById(R.id.tvRelevantReports);
        tvUserData = findViewById(R.id.tvUserData);
        securityAnsLayout = findViewById(R.id.securityAnsLayout);
        callLayout = findViewById(R.id.callLayout);
        securityQLayout = findViewById(R.id.securityQLayout);
        tipLayout = findViewById(R.id.tipLayout);
        managerLayout = findViewById(R.id.managerLayout);
        userLayout = findViewById(R.id.userLayout);
        iconLF = findViewById(R.id.iconLF);
        imgItem = findViewById(R.id.imgItem);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
        btnConfirmAnswer = findViewById(R.id.btnConfirmAnswer);
        btnLocation = findViewById(R.id.btnLocation);

        Bundle bundle = getIntent().getExtras();
        if (bundle!=null) {
            if (bundle.getString("IntentFrom")!=null) {
                //gets the item from one of the following activities
                if (bundle.getString("IntentFrom").equals("Map")) {
                    if (Map_Activity.itemToShow[0] != null)
                        baseLostFound = Map_Activity.itemToShow[0];
                }
                else if (bundle.getString("IntentFrom").equals("List")) {
                    if (Show_List_Activity.itemToShow[0] != null)
                        baseLostFound = Show_List_Activity.itemToShow[0];
                }
            }
        }
        if (baseLostFound instanceof Lost) {
            isLost = true;
            iconLF.setBackground(getDrawable(R.drawable.lostmarker));
            securityAnsLayout.setVisibility(View.GONE);
            securityQLayout.setVisibility(View.GONE);
            itemLost = (Lost) baseLostFound;tvHeadLine.setText("Lost");
            //setIconLF
            if (itemLost.getTip() != 0)
                tvTip.setText("Tip: " + itemLost.getTip());
            else
                tvTip.setText("Tip: No tip");
        } else if (baseLostFound instanceof Found) {
            isLost = false;
            iconLF.setBackground(getDrawable(R.drawable.foundmarker));
            tipLayout.setVisibility(View.GONE);
            itemFound = (Found) baseLostFound;
            tvHeadLine.setText("Found");
            //setIconLF
            if (itemFound.getSecurityQustation() != null && itemFound.getSecurityAnswer() != null) {// if Found has Security Q&A
                if (isManager) {
                    securityAnsLayout.setVisibility(View.GONE);
                    tvSecurityQuestion.setText(tvSecurityQuestion.getText().toString()+" Answer: "+ itemFound.getSecurityAnswer());
                }
                else
                    tvSecurityQuestion.setText("Security Question: " + itemFound.getSecurityQustation());
                callLayout.setVisibility(View.GONE);
            } else {
                tvSecurityQuestion.setText("Security Question: No security question");
                securityAnsLayout.setVisibility(View.GONE);
            }
        }
        tvDescription.setText(baseLostFound.getDescription());
        tvType.setText(baseLostFound.getType());
        String strDate = baseLostFound.getDate();
        tvDate.setText("Date: " + strDate);
        if (baseLostFound.getImg() == null)
            setImage(baseLostFound.getType());
        else
            setPicassoImage(imgItem);
        if (isManager) {
            callLayout.setVisibility(View.GONE);
            setManagerLayout(); // sets and shows isRelevant and Report count
        }
        setUserData(); //sets user layout. first name + phone number + mail
        }


    public void setUserData() { //sets user layout. first name + phone number + mail
        DocumentReference doc = notebookRef.document(baseLostFound.getCreatorUsername());
        doc.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            firstName = user.getFirstName();
                            phoneNumber = user.getPhoneNumber();
                            if (!isManager) {
                                if (firstName != null && phoneNumber != null) {
                                    tvNamePhoneNumber.setText(firstName + ": " + PhoneNumberToString(phoneNumber));
                                    if (firstName.length() >= 8)
                                        tvNamePhoneNumber.setTextSize(20);
                                }
                            }
                            else {
                                userLayout.setVisibility(View.VISIBLE);
                                String mail = user.getMail();
                                String Line1 =  "User: "+ baseLostFound.getCreatorUsername()+", Name: "+ firstName+".";
                                String Line2 =  "Phone number: " +PhoneNumberToString(phoneNumber)+".";
                                String Line3 =  "Mail: " + mail+".";
                                tvUserData.setText(Html.fromHtml(Line1+"<br>"+Line2 + " <br>"+Line3));
                            }
                        }
                    }
                });
    }

    public String PhoneNumberToString(String PhoneNumber) {
        String phoneNumber = PhoneNumber.substring(0,3) +'-'+ PhoneNumber.substring(3,6) +'-'+ PhoneNumber.substring(6,10);
        return phoneNumber;
    }

    public void setManagerLayout() { // sets and shows isRelevant and Report count
        managerLayout.setVisibility(View.VISIBLE);
        String Line1 =  "Is Relevant: "+baseLostFound.isRelevant();
        String Line2 =  "Reports: "+ baseLostFound.getReports();
        tvRelevantReports.setText(Html.fromHtml(Line1+"<br>"+Line2));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        Menu optionsMenu = menu;
        Cursor res = db.getAllData();
        res.moveToFirst();
        String strIsManager = res.getString(2);
        if (strIsManager.equals("true")) {
            optionsMenu.getItem(4).setVisible(true);
            optionsMenu.getItem(1).setVisible(true);
        }
        else {
            optionsMenu.getItem(0).setVisible(true);
            optionsMenu.getItem(6).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.btnReport:
                if (haveReported == true)
                    Toast.makeText(Item_To_Show_Activity.this, "You already have reported item", Toast.LENGTH_SHORT).show();
                else
                    confirmReport();
                return true;
            case R.id.MyAccount:
                intent = new Intent(Item_To_Show_Activity.this, Register_Activity.class);
                intent.putExtra("ActivityType", "Edit");
                startActivity(intent);
                finish();
                return true;
            case R.id.Notification:
                intent = new Intent(Item_To_Show_Activity.this, Notification_Activity.class);
                startActivity(intent);
                return true;
            case R.id.AboutUs:
                intent = new Intent(Item_To_Show_Activity.this, About_Us_Activity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.btnShare:
                shareContent();//shares the item with image and massage
                return true;
            case R.id.btnReset:
                ClearReports(); // only manager can
                return true;
            case R.id.SignOut:
                db.deleteAll();
                intent = new Intent(Item_To_Show_Activity.this, Login_Activity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.btnDelete:
                deleteAlert();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void ClearReports() { // sets reports to 0
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Clear Reports")
                .setMessage("Do you want to clear the reports?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes i'm sure", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WriteBatch batch = dbc.batch();
                        DocumentReference doc = notebookRefItems.document(baseLostFound.getDocumentId());
                        batch.update(doc, "reports", (0));
                        batch.commit();
                        baseLostFound.clearReport();
                        setManagerLayout(); // sets and shows isRelevant and Report count
                        Show_List_Activity.itemToShow[0] = baseLostFound;
                        Show_List_Activity.refreshItemList();
                        Toast.makeText(getBaseContext(), "Reports Cleared", Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConfirmAnswer:
                ConfirmAnswer();
                break;
            case R.id.btnLocation:
                OpenLocation();
                break;
            case R.id.callLayout: // calling the phone of the uploader of the item
                if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Item_To_Show_Activity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                }
                else if (phoneNumber!=null) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                }
                else {
                    Toast.makeText(Item_To_Show_Activity.this, "No Phonenumber Found", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.imgItem: // when clicks on image
                if (baseLostFound.getImg()!=null)
                    showImgActivity();
        }
    }

    private void showImgActivity(){ // when clicks on image
        // Toast.makeText(this, "Custom", Toast.LENGTH_LONG).show();
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.img_to_show_dialog);
        TextView tvExplanation =  dialog.findViewById(R.id.tvExplanation);
        ImageButton ibClose= dialog.findViewById(R.id.ibClose);
        ImageButton ibDelete= dialog.findViewById(R.id.ibDelete);
        ibDelete.setVisibility(View.GONE);
        ImageView ImgView = dialog.findViewById(R.id.img);
        setPicassoImage(ImgView);
        Item_To_Show_Activity.ImgDialogClickListener tdc = new Item_To_Show_Activity.ImgDialogClickListener(dialog);
        ibClose.setOnClickListener(tdc);
        dialog.show();
        // set the custom dialog components - text, image and button
    }

    public void setPicassoImage(ImageView imageV) { //sets with picasso method different image view with different sizes
        if (imageV.getId()==imgItem.getId()) {
            Picasso.with(this)
                    .load(baseLostFound.getImg())
                    .resizeDimen(R.dimen.image_width_square, R.dimen.image_height_square)
                    .centerInside()
                    .into(imageV);
        }
        else
            Picasso.with(this)
                    .load(baseLostFound.getImg())
                    .resizeDimen(R.dimen.image_width_show, R.dimen.image_height_show)
                    .centerInside()
                    .into(imageV);
    }

    private class ImgDialogClickListener implements View.OnClickListener
    {
        Dialog dialog;

        public ImgDialogClickListener(Dialog _dialog)
        {
            this.dialog = _dialog;
        }

        public void onClick(View v)
        {
            if (v.getId()==R.id.ibClose)
                dialog.dismiss();
        }
    }

    public String GetShareMassage() {
        String massage = "";
        if (isLost)
            massage += "Help me find this ";
        else
            massage += "Someone found this ";
        massage += baseLostFound.getType().toLowerCase() + ", " + baseLostFound.getDescription().toLowerCase()+".";
        massage += " The location of the " + baseLostFound.getType().toLowerCase() + " is in the app LostNFound.";
        if (isLost || (itemFound.getSecurityQustation()==null && itemFound.getSecurityAnswer()!=null)) {
            massage += " Call me, " + firstName + " - " + phoneNumber;
            if (isLost)
                massage += " if you found it.";
            else
                massage += " if you know the owners.";
        }
        return massage;
    }

    private void shareContent(){ //shares the item with image and massage
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        try {
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_TEXT, GetShareMassage());
            if (baseLostFound.getImg() != null) {
                Bitmap bitmap =getBitmapFromView(imgItem);
                File file = new File(this.getExternalCacheDir(),"logicchip.png");
                FileOutputStream fOut = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
                file.setReadable(true, false);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                intent.setType("image/png");
            }
            startActivity(Intent.createChooser(intent, "Share item"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapFromView(View view) { //gets image bitmap from image view for sharing
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            bgDrawable.draw(canvas);
        }   else{
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }



    private void setImage(String type)
    {
        if (type.equals("Dog"))
            imgItem.setImageResource(R.drawable.dogicon);
        else if (type.equals("Wallet"))
            imgItem.setImageResource(R.drawable.walleticon);
        else if (type.equals("Phone"))
            imgItem.setImageResource(R.drawable.phoneicon);
        else if (type.equals("Keys"))
            imgItem.setImageResource(R.drawable.keysicon);
        else {
            for (int i=0; i<Objects.length; i++) {
                if(type.equals(Objects[i])) {
                    imgItem.setImageResource(R.drawable.objectsicon);
                    break;
                }
            }
            for (int i=0; i<Pets.length; i++) {
                if(type.equals(Pets[i])) {
                    imgItem.setImageResource(R.drawable.petsicon);
                    break;
                }
            }
            for (int i=0; i<Clothing.length; i++) {
                if(type.equals(Clothing[i])) {
                    imgItem.setImageResource(R.drawable.clothingicon);
                    break;
                }
            }
        }
    }
    private void OpenLocation() {
        if (baseLostFound.getLocation() != null) {
            Intent intent =new Intent(this, PointOnMap_Activity.class);
            double[] latLng ={baseLostFound.getLocation().getLatitude(), baseLostFound.getLocation().getLongitude()} ;
            intent.putExtra("Location", latLng);
            Integer radius = baseLostFound.getRadius();
            intent.putExtra("Radius", radius);
            intent.putExtra("ActivityType", "NoEdit");
            if (isLost)
                intent.putExtra("item", "Lost");
            else
                intent.putExtra("item", "Found");
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "No location found", Toast.LENGTH_LONG).show();
        }

    }

    public void confirmReport () {
       new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Confirm Report")
                .setMessage("Are you sure that you want to report this?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
            }
        })
        .setPositiveButton("Yes i'm sure", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WriteBatch batch = dbc.batch();
                        DocumentReference doc = notebookRefItems.document(baseLostFound.getDocumentId());
                        batch.update(doc, "reports", (baseLostFound.getReports()+1));
                        batch.commit();
                        Toast.makeText(getBaseContext(), "Have reported", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
       .show();
    }

    public void deleteAlert() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Deleting item")
                .setMessage("Are you sure you want to delete item?")
                .setPositiveButton("Yes i'm sure", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (baseLostFound.getDocumentId() != null) {
                            DocumentReference doc = notebookRefItems.document(baseLostFound.getDocumentId());
                            minusUpload(baseLostFound.getCreatorUsername());
                            doc.delete();
                            if (baseLostFound.getImg() != null)
                                deleteStorage();
                            Bundle bundle = getIntent().getExtras();
                            if (bundle.getString("IntentFrom") != null)
                                if (bundle.getString("IntentFrom").equals("List")) {
                                    Show_List_Activity.deleteItem();
                                }
                            Toast.makeText(getBaseContext(), "Deleted", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                })
                .setNegativeButton("No",  new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                })
                .show();
    }

    public void minusUpload(final String username) { // when manager deletes item
        DocumentReference doc = notebookRef.document(username);
        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isComplete() && task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        User myUser = task.getResult().toObject(User.class);
                        WriteBatch batch = dbc.batch();
                        DocumentReference doc = notebookRef.document(username);
                        batch.update(doc, "sumUploads", myUser.getSumUploads()-1);
                        batch.commit();
                    }
                    else
                        Toast.makeText(getBaseContext(), "User wasn't found", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void deleteStorage() { // when manager deletes item
        // final StorageReference fileReference = mStorageRef.child(storageFileName);
        String url = baseLostFound.getImg();
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
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

    private void ConfirmAnswer() { // checks if answer is right
        if (itemFound.getSecurityQustation() != null) {
            if (tries > 1) {
                if (itemFound.checkSecAnswer(etSecurityAnswer.getText().toString().trim())) {
                    callLayout.setVisibility(View.VISIBLE);
                    securityAnsLayout.setVisibility(View.GONE);
                } else {
                    tries--;
                    Toast.makeText(this, "The answer is wrong you have more " + tries + " tries", Toast.LENGTH_LONG).show();
                }
            } else
                Toast.makeText(this, "You have wasted all your tries", Toast.LENGTH_LONG).show();
        }
    }
}
