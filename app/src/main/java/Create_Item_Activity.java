package com.gmail.yahlieyal.lostnfound;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
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
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.xw.repo.BubbleSeekBar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

public class Create_Item_Activity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String TAG = "MainActivity";
    private static final int RESULT_LOAD_IMG = 1;

    ImageView ivIconHeadline;
    ImageView ivItem;
    TextView tvHeadline;
    EditText etDescription;
    ListView lvButtons;
    Spinner spnType;
    CheckBox cbIsRelevant;
    LinearLayout menuLayout;

    private String[] Objects = {"Creditcard", "Headphones", "Bag", "Document", "Watch", "Magnetic card", "Laptop", "Book", "Other object"};
    private String[] Pets = { "Cat","Rabbit","Hamsters", "Other pet"};
    private String[] Clothing = {"Sun glasses", "Earring", "Jewel", "Hat", "Sweatshirt", "Coat", "Umbrella", "Other clothing"};
    private String[] Types = {"Phone", "Dog", "Wallet", "Keys", "Creditcard", "Headphones", "Bag", "Document", "Watch", "Magnetic card", "Laptop", "Book", "Other object", "Cat","Rabbit","Hamsters", "Other pet", "Sun glasses", "Earring", "Jewel", "Hat", "Sweatshirt", "Coat", "Umbrella", "Other clothing"};
    private ArrayList<Type_Item> typeList;
    private static ArrayList<Button_Item> btnList;
    private Type_Adapter adapterType;
    private static ArrayButtonListAdapter adapterButtons;
    private static BaseLostFound baseLostFound;
    private boolean isLost;
    private boolean isEdit;
    private boolean isCamera; //camera photo or gallery
    private boolean hasImg; // if there's an image

    private String phoneNumber;
    private String firstName;

    private String Username;
    private String type;
    private String secQ;
    private String secA;
    public static GeoPoint location;
    public static int radius=0;
    private String date;
    private String img;
    private int tip;
    private boolean isDelete; // is deleting item

    private StorageTask mUploadTask;
    private Uri uriImg;

    private  DataBaseHelper db;

    private FirebaseFirestore dbc = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = dbc.collection("BaseLostFound");
    private CollectionReference notebookUsersRef = dbc.collection("Users");
    private StorageReference mStorageRef;
    private String documentId;
    private String currentPhotoPath; // url of the picture from camera

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create__item_);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new DataBaseHelper(this);
        Cursor res = db.getAllData();
        if(res.getCount() ==0) {
            Toast.makeText(this, "No Data", Toast.LENGTH_LONG).show();
        }
        else {
            res.moveToFirst();
            Username=res.getString(1);
            setUserDetails(); // set the phone number and first name for sharing massage when it's edit mode
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle!=null) {
            if (bundle.getString("IntentFrom")!=null) {
                //EDIT MODE
                //if it is Edit mode it gets the item from one of the following activities
                if (bundle.getString("IntentFrom").equals("Map")) {
                    if (Map_Activity.itemToShow[0] != null)
                        baseLostFound = Map_Activity.itemToShow[0];
                }
                else if (bundle.getString("IntentFrom").equals("List")) {
                    if (Show_List_Activity.itemToShow[0] != null)
                        baseLostFound = Show_List_Activity.itemToShow[0];
                }
                documentId = baseLostFound.getDocumentId();
                isEdit = true;
                if (baseLostFound instanceof Lost)
                    isLost = true;
                else if (baseLostFound instanceof Found)
                    isLost = false;
            }
            else {
                // CREATE ITEM MODE
                baseLostFound = new Lost();
                isEdit=false;
                Create_Item_Activity.location=null;
                Create_Item_Activity.radius=0;
                if (bundle.getString("ActivityType").equals("Lost"))
                    isLost=true;
                else if (bundle.getString("ActivityType").equals("Found"))
                    isLost=false;
            }
        }

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        tvHeadline = findViewById(R.id.tvHeadLine2);
        ivIconHeadline = findViewById(R.id.ivIconHeadline);
        ivItem = findViewById(R.id.ivItem);
        ivItem.setImageDrawable(getDrawable(R.drawable.imageicon));
        menuLayout = findViewById(R.id.menuLayout);
        cbIsRelevant = findViewById(R.id.cbIsRelevant);
        etDescription = findViewById(R.id.etDescription);
        btnList(isLost);
        lvButtons =findViewById(R.id.lvButtons);
        lvButtons.setAdapter(adapterButtons);
        adapterButtons = new ArrayButtonListAdapter(this, btnList);
        lvButtons.setAdapter(adapterButtons);
        lvButtons.setOnItemClickListener(this);
        lvButtons.setOnItemLongClickListener(this);
        spnType = findViewById(R.id.spnType);
        spinnerAdapter();

        if(isLost)
            tvHeadline.setText("Create Lost");
        else if (isLost==false)
            tvHeadline.setText("Create Found");

        if (isEdit) {
            menuLayout.setVisibility(View.GONE);
            cbIsRelevant.setVisibility(View.VISIBLE); // possibility to set your item to not relevant
            setParametersToPrevious(); // sets all fields to the item that you edit
        }
    }

    public void setParametersToPrevious() {
        if (isLost) {
            Lost temp = (Lost) baseLostFound;
            tvHeadline.setText("Edit Lost");
            if (temp.getTip() != 0) {
                tip = temp.getTip();
                btnList.get(4).setName("Tip: "+temp.getTip());
                btnList.get(4).setExistent(true);
            }
            else {
                tip = 0;
                btnList.get(4).setName("Tip");
                btnList.get(4).setExistent(false);
            }
        }
        else if (isLost==false) {
            Found temp=(Found)baseLostFound;
            tvHeadline.setText("Edit Found");
            if (temp.getSecurityQustation() != null) {
                secQ = temp.getSecurityQustation();
                secA = temp.getSecurityAnswer();
                btnList.get(4).setExistent(true);
            }
            else {
                secQ = null;
                secA = null;
                btnList.get(4).setExistent(false);
            }
        }
        for (int i =0; i<Types.length; i++) {
            if (Types[i].equals(baseLostFound.getType())) {
                type=baseLostFound.getType();
                spnType.setSelection(i+1);
                break;
            }
        }
        ivIconHeadline.setImageResource(R.drawable.editbutton);
        ivIconHeadline.setBackground(getDrawable(R.drawable.circle));
        etDescription.setText(baseLostFound.getDescription());
        if (baseLostFound.getLocation()!=null) {
            btnList.get(0).setExistent(true);
            location = baseLostFound.getLocation();
            radius = baseLostFound.getRadius();
        }
        date = baseLostFound.getDate();
        btnList.get(1).setName("Date: "+ date);
        btnList.get(1).setExistent(true);
        if (baseLostFound.getImg() != null) {
            img = baseLostFound.getImg();
            hasImg=true;
            uriImg=null;
            setPicassoImage(ivItem);
            if (baseLostFound.getImg().contains("JPEG"))
                btnList.get(3).setExistent(true);
            else
                btnList.get(2).setExistent(true);
        }
        else {
            img=null;
            uriImg=null;
            hasImg=false;
            ivItem.setImageDrawable(getDrawable(R.drawable.imageicon));
            btnList.get(3).setExistent(false);
        }
        adapterButtons.notifyDataSetChanged();
    }

    public void resetAllParameters() { // RESET BUTTON
        location=null;
        radius=0;
        date=null;
        type=null;
        img=null;
        uriImg=null;
        hasImg=false;
        secQ=null;
        secA=null;
        tip=0;
        spnType.setSelection(0);
        cbIsRelevant.setChecked(false);
        etDescription.setText("");
        btnList.get(1).setName("Date");
        ivItem.setImageDrawable(getDrawable(R.drawable.imageicon));
        if (isLost)
            btnList.get(4).setName("Tip");
        for (Button_Item btn : btnList) {
            btn.setExistent(false);
        }
        adapterButtons.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        Menu optionsMenu = menu;
        if (isEdit) {
            optionsMenu.getItem(0).setVisible(true);
            optionsMenu.getItem(3).setVisible(true);
            optionsMenu.getItem(4).setVisible(true);
        }
        else {
            optionsMenu.getItem(1).setVisible(true);
            optionsMenu.getItem(2).setVisible(true);
        }
        optionsMenu.getItem(7).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.btnReset:
                resetAllParameters();
                return true;
            case R.id.btnUpload:
                if (isUploadInProgress()) {
                    Toast.makeText(getBaseContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (IsReadyItem()) { // if all "must" fields are filled
                        if (uploadImage()==false) { // upload image to cloud if there's image to upload and returns false if there's no image
                            // saves the item if there is no img
                            WriteBatch batch = dbc.batch();
                            BaseLostFound[] baseArr = getLF(); //gets array with one item with all the parameters
                            DocumentReference doc1 = notebookRef.document();
                            batch.set(doc1, baseArr[0]);
                            batch.commit();
                            isDelete=false;
                            HandleItemsOfUser(); // set the sum of the items that the user has
                            Toast.makeText(getBaseContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    else
                        Toast.makeText(getBaseContext(), "You are missing required attribute", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.btnShare:
                shareContent(); //shares the item with image and massage
                return true;
            case R.id.btnSave:
                if (isUploadInProgress()) {
                    Toast.makeText(getBaseContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(IsReadyItem()) { // if all "must" fields are filled
                        if (isDelete())  // if img needs to be deleted
                            deleteStorage();
                        if (uploadImage()==false) {
                            BaseLostFound[] baseArr = getLF(); //gets array with one item with all the parameters
                            DocumentReference doc1 = notebookRef.document(documentId);
                            doc1.set(baseArr[0]);
                            RefreshList();  // refresh the list in Show List Activity
                            Toast.makeText(getBaseContext(), "Saved", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    else
                        Toast.makeText(getBaseContext(), "You are missing required attribute", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.btnDelete:
                showAlert("Delete");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUserDetails() {  // set the phone number and first name for sharing massage when it's edit mode
        DocumentReference doc = notebookUsersRef.document(Username);
        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isComplete() && task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        User myUser = task.getResult().toObject(User.class);
                        phoneNumber = myUser.getPhoneNumber();
                        firstName = myUser.getFirstName();
                    }
                }
            }
        });
    }

    private void shareContent(){ //shares the item with image and massage
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        try {
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_TEXT, GetShareMassage());
            if (baseLostFound.getImg() != null) {
                Bitmap bitmap =getBitmapFromView(ivItem);
                File file = new File(this.getExternalCacheDir(),"logicchip.png");
                FileOutputStream fOut = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
                file.setReadable(true, false);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                intent.setType("image/png");
            }
            else
                intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, "Share item"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String GetShareMassage() {
        String massage = "";
        if (isLost)
            massage += "Help me find my ";
        else
            massage += "Found a ";
        massage += baseLostFound.getType().toLowerCase() + ", " + baseLostFound.getDescription().toLowerCase()+".";
        massage += " The location of the " + baseLostFound.getType().toLowerCase() + " is In the app LostNFound.";
        massage += " Call me, " + firstName + " - " + phoneNumber;
        if (isLost)
            massage += " if you found it.";
        else
            massage += " if you know the owners.";
        return massage;
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

    public void HandleItemsOfUser() { // set the sum of the items that the user has
        Cursor res = db.getAllData();
        res.moveToFirst();
        Username=res.getString(1);
        DocumentReference doc = notebookUsersRef.document(Username);
        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isComplete() && task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        User myUser = task.getResult().toObject(User.class);
                        WriteBatch batch = dbc.batch();
                        DocumentReference doc = notebookUsersRef.document(Username);
                        if (!isDelete)
                            batch.update(doc, "sumUploads", myUser.getSumUploads()+1);
                        else
                            batch.update(doc, "sumUploads", myUser.getSumUploads()-1);
                        batch.commit();
                    }
                    else
                        Toast.makeText(getBaseContext(), "User wasn't found", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void RefreshList() {  // refresh the list in Show List Activity
        Bundle bundle = getIntent().getExtras();
        if (bundle.getString("IntentFrom")!=null)
            if (bundle.getString("IntentFrom").equals("List")) {
                BaseLostFound[] baseArr = getLF(); //gets array with one item with all the parameters
                Show_List_Activity.itemToShow[0] = baseArr[0];
                Show_List_Activity.refreshItemList();
            }
    }

    private void spinnerAdapter() {
        TypeList();
        adapterType = new Type_Adapter(this, typeList);
        spnType.setAdapter(adapterType);
        spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Type_Item clickedItem = (Type_Item)adapterView.getItemAtPosition(i);
                if (clickedItem.getName().equals("Type"))
                    type=null;
                else
                    type = clickedItem.getName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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

    private void btnList(Boolean isLost) {
        btnList = new ArrayList<>();
        btnList.add(new Button_Item("Location", R.drawable.locationicon, false, true));
        btnList.add(new Button_Item("Date", R.drawable.dateicon, false, true));
        btnList.add(new Button_Item("Upload image", R.drawable.imageicon, false, false));
        btnList.add(new Button_Item("Take a picure", R.drawable.cameraicon, false, false));
        if (isLost)
            btnList.add(new Button_Item("Tip", R.drawable.tipicon, false, false));
        else
            btnList.add(new Button_Item("Security QA", R.drawable.securityicon, false, false));
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId())
        {
            case R.id.ibSearchGrayActivity:
                showAlert("Quit Search");
                break;
            case R.id.ibMyListGrayActivity:
                showAlert("Quit My List");
                break;
            case R.id.ibHomeActivity:
                showAlert("Quit");
                break;
            case R.id.cbIsRelevant:
                if (cbIsRelevant.isChecked())
                    showAlert("No Relevant");
                break;
            case R.id.ivItem:
                if (hasImg)
                    showImgActivity();
                break;
            default:
                Log.e(TAG, "Invalid item clicked");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean isOk = false;
        if (isCamera==false) {
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null && data.getData() != null) {
                isOk=true;
                uriImg = data.getData();
                setPicassoImage(ivItem);
                img = System.currentTimeMillis()+ "." + getFileExtension(uriImg);
            }
        }
        else if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
            isOk=true;
            File f = new File(currentPhotoPath);
            uriImg = Uri.fromFile(f);
            setPicassoImage(ivItem);
            //ivItem.setImageURI(uriImg);
            img = f.getName();

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uriImg);
            this.sendBroadcast(mediaScanIntent);
        }
        if (isOk) {
            hasImg=true;
            btnList.get(2).setExistent(!isCamera);
            btnList.get(3).setExistent(isCamera);
            adapterButtons.notifyDataSetChanged();   
        }
    }


    private void showMyDateDialog()
    {
        DatePickerDialog.OnDateSetListener listener = new MyDateSetListener();
        Calendar cal = Calendar.getInstance();
        if(baseLostFound.getDate()!=null)
            cal=StrToCalender(baseLostFound.getDate());

        DatePickerDialog dpd = new DatePickerDialog(this,listener,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    public Calendar StrToCalender(String date) {
        String[] DateArray = date.split("\\s*/\\s*");
        Calendar result = Calendar.getInstance();
        result.set(Integer.parseInt(DateArray[2]), Integer.parseInt(DateArray[1])-1,  Integer.parseInt(DateArray[0]));
        return result;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        int btnPressed = btnList.get(i).getImg();
        Intent intent;
        switch (btnPressed){
            case R.drawable.locationicon:
                Intent intent1 =new Intent(this, PointOnMap_Activity.class);
                if (location != null) {
                    double[] latLng ={location.getLatitude(), location.getLongitude()} ;
                    intent1.putExtra("Location", latLng);
                    intent1.putExtra("Radius", radius);
                }
                if (isLost)
                    intent1.putExtra("item", "Lost");
                else
                    intent1.putExtra("item", "Found");
                intent1.putExtra("ActivityType", "Edit");
                startActivity(intent1);
                break;
            case R.drawable.dateicon:
                showMyDateDialog();  // easy method
                break;
            case R.drawable.imageicon:
                isCamera=false;
                Intent galleryIntent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                break;
            case R.drawable.cameraicon:
                isCamera=true;
                askCameraPermissions();
                break;
            case R.drawable.securityicon:
                showSecurityActivity();
                break;
            case R.drawable.tipicon:
                showTipActivity();
                break;
        }
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, 101);
        }else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 101){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) // checks if there's permission to camera
                dispatchTakePictureIntent();
            else
                Toast.makeText(this, "Camera Permission is Required.", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == 2) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // checks if there's permission to Write External Storage
                try {
                    createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
                Toast.makeText(this, "Write External Storage Permission is Required.", Toast.LENGTH_SHORT).show();
        }
    }

    private void dispatchTakePictureIntent() { // handle the saving of photo in gallery after took a picture
        Toast.makeText(this, "The picture need to be Vertically!", Toast.LENGTH_LONG).show();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.gmail.yahlieyal.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 102);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "JPEG_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = null;
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            currentPhotoPath = image.getAbsolutePath();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) { // explanation long click on buttons
        int btnPressed = btnList.get(i).getImg();
        switch (btnPressed){
            case R.drawable.locationicon:
                Toast.makeText(this, "The estimated location, the area is it most likely to be found.", Toast.LENGTH_LONG).show();
                break;
            case R.drawable.dateicon:
                Toast.makeText(this, "The date you had lost/found the item", Toast.LENGTH_LONG).show();
                break;
            case R.drawable.imageicon:
                Toast.makeText(this, "Uploading a image of your item is helpful", Toast.LENGTH_LONG).show();
                break;
            case R.drawable.cameraicon:
                Toast.makeText(this, "Taking a picture of your item is helpful", Toast.LENGTH_LONG).show();
                break;
            case R.drawable.securityicon:
                for (int j=0; j < 2; j++)
                {
                    Toast.makeText(getBaseContext(), "The security question prevent people who didn't loose the item to get your phone number", Toast.LENGTH_LONG).show();
                }
                break;
            case R.drawable.tipicon:
                Toast.makeText(this, "You can tip the person who finds the lost", Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }


    // a nested class that implements the DateSetListener Interface
    private class MyDateSetListener implements DatePickerDialog.OnDateSetListener
    {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            Calendar result = Calendar.getInstance();
            Calendar today = Calendar.getInstance();
            result.set(year, monthOfYear, dayOfMonth);
            if(!result.after(today)) {
                date = dayOfMonth+"/"+(1+monthOfYear)+"/"+year;
                btnList.get(1).setName("Date: "+date);
                btnList.get(1).setExistent(true);
                adapterButtons.notifyDataSetChanged();
            }
            else
                Toast.makeText(getBaseContext(), "How was your time travel? Error future date..", Toast.LENGTH_LONG).show();
        }
    }

    private void showImgActivity(){ // custom dialog when click on image of item
        // Toast.makeText(this, "Custom", Toast.LENGTH_LONG).show();
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.img_to_show_dialog);
        ImageButton ibClose= dialog.findViewById(R.id.ibClose);
        ImageButton ibDelete= dialog.findViewById(R.id.ibDelete);
        ImageView ImgView = dialog.findViewById(R.id.img);
        setPicassoImage(ImgView);
        ImgDialogClickListener tdc = new ImgDialogClickListener (dialog);
        ibClose.setOnClickListener(tdc);
        ibDelete.setOnClickListener(tdc);
        dialog.show();
        // set the custom dialog components - text, image and button
    }

    public void setPicassoImage(ImageView imageV) { //sets with picasso method different image view with different sizes
        if (imageV.getId()==ivItem.getId()) {
            if (uriImg != null) {
                Picasso.with(this)
                        .load(uriImg.toString())
                        .resizeDimen(R.dimen.image_width_small, R.dimen.image_height_small)
                        .centerInside()
                        .into(imageV);
            }
            else {
                Picasso.with(this)
                        .load(baseLostFound.getImg())
                        .resizeDimen(R.dimen.image_width_small, R.dimen.image_height_small)
                        .centerInside()
                        .into(imageV);
            }
        }
        else if (uriImg!=null) {
            Picasso.with(this)
                    .load(uriImg.toString())
                    .resizeDimen(R.dimen.image_width_show, R.dimen.image_height_show)
                    .centerInside()
                    .into(imageV);
        }
        else {
            Picasso.with(this)
                    .load(baseLostFound.getImg())
                    .resizeDimen(R.dimen.image_width_show, R.dimen.image_height_show)
                    .centerInside()
                    .into(imageV);
        }
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

            switch (v.getId())
            {
                case R.id.ibClose:
                    dialog.dismiss();
                    break;
                case R.id.ibDelete:
                    img=null;
                    uriImg=null;
                    hasImg=false;
                    ivItem.setImageDrawable(getDrawable(R.drawable.imageicon));
                    btnList.get(2).setExistent(false);
                    btnList.get(3).setExistent(false);
                    adapterButtons.notifyDataSetChanged();
                    Toast.makeText(getBaseContext(), "Image Deleted", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    break;
                default:
                    Log.e(TAG, "Invalid button clicked");
                    break;
            }
        }
    }

    private String getDateToday() {
        Calendar today = Calendar.getInstance();
        return  today.get(Calendar.DAY_OF_MONTH) + "/" + (1 + today.get(Calendar.MONTH)) + "/" + today.get(Calendar.YEAR);
    }

    private void showSecurityActivity(){
        // Toast.makeText(this, "Custom", Toast.LENGTH_LONG).show();
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.security_dialog);
        TextView tvExplanation =  dialog.findViewById(R.id.tvExplanation);
        EditText etAnswer= dialog.findViewById(R.id.etAnswer);
        EditText etQuestion= dialog.findViewById(R.id.etQuestion);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        if (isLost==false) {
            if (secA!=null && secQ!=null) {
                    etQuestion.setText(secQ);
                    etAnswer.setText(secA);
            }
        }
        String Line1 =  "A good security question maintains the following conditions:";
        String Line2 =  "Has only one clear answer.";
        String Line3 =  "Only the owner knows the answer.";
        String Line4 =  "The photo of the item don't reveal the answer.";
        String Line5 = "You can ask about: company, color, id tags and more..";
        tvExplanation.setText(Html.fromHtml(Line1+"<br>"+Line2 + " <br>"+Line3+ " <br>"+Line4+"<br>"+Line5));
        SecurityDialogClickListener tdc = new SecurityDialogClickListener (dialog, etAnswer, etQuestion);
        btnConfirm.setOnClickListener(tdc);
        dialog.show();
        // set the custom dialog components - text, image and button
    }

    private class SecurityDialogClickListener implements View.OnClickListener
    {
        Dialog dialog;
        EditText etAnswer, etQuestion;

        public SecurityDialogClickListener(Dialog _dialog, EditText etAnswer, EditText etQuestion)
        {
            this.dialog = _dialog;
            this.etAnswer=etAnswer;
            this.etQuestion=etQuestion;
        }


        public void onClick(View v)
        {

            switch (v.getId())
            {
                case R.id.btnConfirm:
                    if (isLost==false) {
                        if (etQuestion.getText().toString().trim().equals("")==false && etAnswer.getText().toString().trim().equals("")==false) {
                            secQ = etQuestion.getText().toString().trim();
                            secA = etAnswer.getText().toString().trim();
                            btnList.get(4).setExistent(true);
                            adapterButtons.notifyDataSetChanged();
                        }
                        else
                            Toast.makeText(getBaseContext(), "Error missing attribute", Toast.LENGTH_LONG).show();
                    }
                    dialog.dismiss();
                    break;
                default:
                    Log.e(TAG, "Invalid button clicked");
                    break;
            }
        }
    }

    private void showTipActivity(){
        // Toast.makeText(this, "Custom", Toast.LENGTH_LONG).show();
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.tip_dialog);
        final TextView tvTip =  dialog.findViewById(R.id.tvTip);
        BubbleSeekBar bubbleSeekBar =  dialog.findViewById(R.id.bubbleSeekBar);
        if (isLost) {
                bubbleSeekBar.setProgress(tip);
                tvTip.setText("Tip: "+ tip);
        }
        bubbleSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                tvTip.setText("Tip: "+progress);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        TipDialogClickListener tdc = new TipDialogClickListener (dialog, tvTip);
        btnConfirm.setOnClickListener(tdc);
        dialog.show();
        // set the custom dialog components - text, image and button
    }

    private class TipDialogClickListener implements View.OnClickListener
    {
        Dialog dialog;
        TextView tvTip;

        public TipDialogClickListener(Dialog _dialog, TextView tvTip)
        {
            this.dialog = _dialog;
            this.tvTip=tvTip;
        }


        public void onClick(View v)
        {

            switch (v.getId())
            {
                case R.id.btnConfirm:
                    if (isLost) {
                        String Stip = tvTip.getText().toString().trim().substring(5);
                        tip =  Integer.valueOf(Stip);
                        if (tip!=0) {
                            btnList.get(4).setExistent(true);
                            btnList.get(4).setName("Tip: "+Stip);
                            adapterButtons.notifyDataSetChanged();
                        }
                        else {
                            btnList.get(4).setExistent(false);
                            btnList.get(4).setName("Tip");
                            adapterButtons.notifyDataSetChanged();
                        }
                    }
                    dialog.dismiss();
                    break;
                default:
                    Log.e(TAG, "Invalid button clicked");
                    break;
            }
        }
    }

    public BaseLostFound[] getLF() { //gets array with one item with all the parameters
        BaseLostFound temp= baseLostFound;
        BaseLostFound[] baseArray = new BaseLostFound[1];
        if (isEdit==false)
            temp.setisRelevant(true);
        temp.setCreatorUsername(Username);
        temp.setType(type);
        temp.setLocation(location);
        temp.setRadius(radius);
        temp.setDate(date);
        if (!isEdit)
            temp.setDateUploaded(getDateToday());
        temp.setDescription(etDescription.getText().toString().trim());
        if (img!=null)
            temp.setImg(img);
        else if (hasImg==false)
            temp.setImg(null);
        if (isLost) {
            Lost Ltemp = new Lost();
            Ltemp.setByParent(temp);
            if (tip!=0)
                Ltemp.setTip(tip);
            Ltemp.setLF("L");
            baseArray[0]=Ltemp;
        }
        else {
            Found Ftemp = new Found();
            Ftemp.setByParent(temp);
            if (secQ!=null && secA!=null) {
                Ftemp.setSecurityQustation(secQ);
                Ftemp.setSecurityAnswer(secA);
            }
            Ftemp.setLF("F");
            baseArray[0]=Ftemp;
        }
        return baseArray;
    }

    public Boolean IsReadyItem() { // if all "must" fields are filled
        if (date!=null && etDescription.getText().toString().trim().equals("")==false && location!=null && radius!=0 && type!=null)
            return true;
        return false;
    }



    public static void setLocation(GeoPoint location, int radius) { // static setter action that called in point on map activity
        Create_Item_Activity.location=location;
        Create_Item_Activity.radius=radius;
        btnList.get(0).setExistent(true);
        adapterButtons.notifyDataSetChanged();
    }

    public void showAlert(final String type) { // shows different alert
        String question="", title="";
        if (type.equals("Delete")) {
            question = "Are you sure you want to delete item?";
            title = "Deleting item";
        }
        else if (type.equals("No Relevant")) {
            question="Are you sure that the item is not relevant anymore?";
            title = "Set item No Relevant";
        }

        else if (type.contains("Quit")) {
            question ="Are you sure you want to exit without saving?";
            title = "Quit without saving";
        }
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(question)
                .setPositiveButton("Yes i'm sure", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (type.equals("No Relevant")) {
                            if (documentId != null) {
                                WriteBatch batch = dbc.batch();
                                DocumentReference doc = notebookRef.document(documentId);
                                batch.update(doc, "relevant", false);
                                batch.commit();
                                Toast.makeText(getBaseContext(), "No Relevant", Toast.LENGTH_LONG).show();
                            }
                        } else if (type.equals("Delete")) {
                            if (documentId != null) {
                                DocumentReference doc = notebookRef.document(documentId);
                                doc.delete();
                                if (baseLostFound.getImg() != null)
                                    deleteStorage();
                                isDelete=true;
                                HandleItemsOfUser(); // set the sum of the items that the user has
                                Toast.makeText(getBaseContext(), "Deleted", Toast.LENGTH_LONG).show();
                            }
                        }
                        else if (type.equals("Quit Search") || type.equals("Quit My List"))
                            exitActivity(type);
                        if (type.equals("Delete") || type.equals("No Relevant")) {
                            Bundle bundle = getIntent().getExtras();
                            if (bundle.getString("IntentFrom") != null)
                                if (bundle.getString("IntentFrom").equals("List"))
                                    Show_List_Activity.deleteItem();
                        }
                        finish();
                    }
                })
                .setNegativeButton("No",  new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (type.equals("No Relevant"))
                            cbIsRelevant.setChecked(false);
                    }
                })
                .show();
    }

    public void exitActivity (String type) { // exit to Show_List_Activity types
        Intent intent = new Intent(this, Show_List_Activity.class);
        if (type.equals("Quit Search"))
            intent.putExtra("ActivityType", "Search");
        else if (type.equals("Quit My List"))
            intent.putExtra("ActivityType", "My List");
        startActivity(intent);
    }

    public void deleteStorage() { // deletes the photo of edited item
       // final StorageReference fileReference = mStorageRef.child(storageFileName);
            String url = baseLostFound.getImg();
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            imageRef.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getBaseContext(), "ERROR: " +e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    public boolean isUploadInProgress() {
        return mUploadTask != null && mUploadTask.isInProgress();
    }

    public boolean isDelete() { // if img needs to be deleted
        if (uriImg!=null && baseLostFound.getImg()!=null && uriImg.toString().equals(baseLostFound.getImg())==false)
                return true;
        else if (baseLostFound.getImg()!=null && hasImg==false)
                return true;
        else
            return false;
    }


    private boolean uploadImage() {
        if (uriImg!=null && (documentId==null || baseLostFound.getImg()==null || uriImg.toString().equals(baseLostFound.getImg())==false)) {
                    final StorageReference fileReference = mStorageRef.child(img);
                    mUploadTask = fileReference.putFile(uriImg)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    if (taskSnapshot.getMetadata() != null && taskSnapshot.getMetadata().getReference() != null) {
                                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                              img = uri.toString(); // gets url
                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                if (task.isComplete() && task.isSuccessful()) {
                                                    BaseLostFound[] baseArr = getLF(); //gets array with one item with all the parameters
                                                    DocumentReference doc1;
                                                    if (documentId==null) { // NEW ITEM
                                                         doc1 = notebookRef.document();
                                                    }
                                                    else { // EDIT THE ITEM
                                                       doc1 = notebookRef.document(documentId);
                                                    }
                                                    doc1.set(baseArr[0]);
                                                    isDelete=false;
                                                    HandleItemsOfUser(); // set the sum of the items that the user has
                                                    RefreshList(); // refresh the list in Show List Activity
                                                    if (documentId==null)
                                                        Toast.makeText(getBaseContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                                                    else
                                                        Toast.makeText(getBaseContext(), "Saved", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            }
                                        });
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    String Se = e.toString();
                                    Toast.makeText(getBaseContext(), "ERROR: " +Se, Toast.LENGTH_SHORT).show();
                                }
                            });
                    return true;
            }
        return false;
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    @Override
    public void onBackPressed() {
        showAlert("Quit");
    }
}


