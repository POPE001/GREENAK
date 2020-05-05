package com.example.greenak.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.greenak.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProfileEditSellerActivity extends AppCompatActivity implements LocationListener
{
    private ImageButton backBtn, gpsBtn;
    private ImageView profileIv;
    private EditText nameEt, phoneEt, countryEt, stateEt, cityEt,addressEt,
            deliveryFeeEt, shopNameEt;
    private Button updateBtn;
    private SwitchCompat shopOpenSwitch;


    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //Image Pick Code Constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //Permission arrays
    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //image picked uri
    private Uri image_uri;


    private double latitude = 0.0 , longitude = 0.0;
    private LocationManager locationManager;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_profile_edit_seller);

        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileIv = findViewById(R.id.profileIv);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        countryEt = findViewById(R.id.countryEt);
        stateEt = findViewById(R.id.stateEt);
        cityEt = findViewById(R.id.cityEt);
        addressEt = findViewById(R.id.addressEt);
        deliveryFeeEt = findViewById(R.id.deliveryFeeEt);
        shopNameEt = findViewById(R.id.shopNameEt);
        updateBtn = findViewById(R.id.updateBtn);
        shopOpenSwitch = findViewById(R.id.shopOpenSwitch);



        //init permission array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        firebaseAuth = FirebaseAuth.getInstance();

        checkUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);





        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PICK IMAGE
                showImagePickerDialog();

            }
        });



        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect current location
                if (checkLocationPermission()) {
                    //already allowed
                    detectLocation();
                } else {
                    //not allowed, request
                    requestLocationPersmission();
                }

            }
        });


        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //begin update profile
                inputData();
            }
        });
    }

    private String name, shopName, phone, deliveryFee, country, state, city, address;
    private Boolean shopOpen;

    private void inputData()
    {
//input data
        name = nameEt.getText().toString().trim();
        shopName = shopNameEt.getText().toString().trim();
        phone = phoneEt.getText().toString().trim();
        deliveryFee = deliveryFeeEt.getText().toString().trim();
        country = countryEt.getText().toString().trim();
        state = stateEt.getText().toString().trim();
        city = cityEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();
        shopOpen = shopOpenSwitch.isChecked(); //true or false


        updateProfile();

    }

    private void updateProfile()
    {
        progressDialog.setMessage("Updating Profile...");
        progressDialog.dismiss();


        if (image_uri == null)
        {
            //update without image

            //setup data to update
            //setup data to save
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("name",""+name);
            hashMap.put("shopName",""+shopName);
            hashMap.put("phone",""+phone);
            hashMap.put("deliveryFee",""+deliveryFee);
            hashMap.put("country",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("shopOpen","true");

            //update to db
            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            //DB UPDATED
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this, "Profile Updated...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            //failed updating db
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }



        else {
            //update with image

            //upload image first

            String filePathAndName = "profile_images/" + "" + firebaseAuth.getUid();
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get uri of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadImageUri = uriTask.getResult();


                            if (uriTask.isSuccessful())
                            {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("name",""+name);
                                hashMap.put("shopName",""+shopName);
                                hashMap.put("phone",""+phone);
                                hashMap.put("deliveryFee",""+deliveryFee);
                                hashMap.put("country",""+country);
                                hashMap.put("state",""+state);
                                hashMap.put("city",""+city);
                                hashMap.put("address",""+address);
                                hashMap.put("latitude",""+latitude);
                                hashMap.put("longitude",""+longitude);
                                hashMap.put("shopOpen","true");
                                hashMap.put("profileImage",""+downloadImageUri);

                                //update to db
                                //save to db
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid)
                                            {
                                                //DB UPDATED
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditSellerActivity.this, "Profile Updated...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                //failed updating db
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


        }
    }

    private void checkUser()
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null)
        {
            startActivity(new Intent(getApplicationContext(), LoginUser.class));
            finish();
        }

        else
            {
                loadMyInfo();
            }
    }

    private void loadMyInfo()
    {
        //load user info, and set to views
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot ds: dataSnapshot.getChildren())
                        {

                            String accountType = ""+ds.child("accountType").getValue();
                            String address = ""+ds.child("address").getValue();
                            String city = ""+ds.child("city").getValue();
                            String state = ""+ds.child("state").getValue();
                            String country = ""+ds.child("country").getValue();
                            String deliveryFee = ""+ds.child("deliveryFee").getValue();
                            String email = ""+ds.child("email").getValue();
                            latitude = Double.parseDouble(""+ds.child("latitude").getValue());
                            longitude = Double.parseDouble(""+ds.child("longitude").getValue());
                            String name = ""+ds.child("name").getValue();
                            String online = ""+ds.child("online").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String shopName = ""+ds.child("shopName").getValue();
                            String shopOpen = ""+ds.child("shopOpen").getValue();
                            String uid = ""+ds.child("uid").getValue();



                            nameEt.setText(name);
                            phoneEt.setText(phone);
                            countryEt.setText(country);
                            stateEt.setText(state);
                            cityEt.setText(city);
                            addressEt.setText(address);
                            shopNameEt.setText(shopName);
                            deliveryFeeEt.setText(deliveryFee);


                            if (shopOpen.equals("true"))
                            {
                                shopOpenSwitch.setChecked(true);
                            }
                            else
                                {
                                    shopOpenSwitch.setChecked(false);
                                }

                            try
                            {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_grey).into(profileIv);
                            }

                            catch (Exception e)
                            {
                                profileIv.setImageResource(R.drawable.ic_person_grey);
                            }









                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void showImagePickerDialog() {
        //options to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks
                        if (which == 0) {
                            //camera clicked
                            if (checkCameraPermission()) {
                                //camera permission allowed
                                pickFromCamera();

                            } else {
                                //not allowed, request
                                requestCameraPermission();
                            }


                        } else {
                            //gallery clicked
                            if (checkStoragePermission()) {
                                //storage permission allowed
                                pickFromGallery();
                            } else {
                                //not allowed, request
                                requestStoragePermission();

                            }

                        }
                    }
                })
                .show();

    }

    private void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission()
    {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestCameraPermission()
    {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkCameraPermission()
    {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestLocationPersmission()
    {
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (Build.VERSION.SDK_INT >= 23) { // Marshmallow

                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                }

            } else { // no need to ask for permission

                // start to find location...
            }
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private boolean checkLocationPermission()
    {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_DENIED);
        return result;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();

    }

    private void findAddress()
    {
        //find address, country, state, city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());


        try
        {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0); //complete address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();


            //set Address
            countryEt.setText(country);
            stateEt.setText(state);
            cityEt.setText(city);
            addressEt.setText(address);

        }
        catch (Exception e)
        {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Please turn on your location...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:
            {
                if (grantResults.length>0)
                {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted)
                    {
                        //permission allowed
                        detectLocation();
                    }

                    else
                    {
                        //permission denied
                        Toast.makeText(this, "Location permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            break;
            case CAMERA_REQUEST_CODE:
            {
                if (grantResults.length>0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted)
                    {
                        //permission allowed
                        pickFromCamera();
                    }

                    else
                    {
                        //permission denied
                        Toast.makeText(this, "Camera permissions are necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            break;
            case STORAGE_REQUEST_CODE:
            {
                if (grantResults.length>0)
                {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (  storageAccepted)
                    {
                        //permission allowed
                        pickFromGallery();
                    }

                    else
                    {
                        //permission denied
                        Toast.makeText(this, "Storage permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            break;

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (resultCode==RESULT_OK)
        {
            if (requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                image_uri = data.getData();
                //set to imageview
                profileIv.setImageURI(image_uri);

            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {

                profileIv.setImageURI(image_uri);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}