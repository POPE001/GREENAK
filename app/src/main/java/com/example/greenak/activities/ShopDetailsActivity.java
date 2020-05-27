package com.example.greenak.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.greenak.Constants;
import com.example.greenak.R;
import com.example.greenak.adapters.AdapterCartItem;
import com.example.greenak.adapters.AdapterProductUser;
import com.example.greenak.models.ModelCartItem;
import com.example.greenak.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity
{
    private ImageView shopIv;
    private TextView shopNameTv, phoneTv, emailTv, openCloseTv, deliveryFeeTv, addressTv, filterProductsTv, cartCountTv;
    private ImageButton callBtn, mapBtn, cartBtn, backBtn, filterProductBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;


    private String shopUid;
    private String myLatitude, myLongitude, myPhone;
    private  String  shopName,shopEmail,shopPhone,shopAddress,shopLatitude, shopLongitude;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;
    public String deliveryFee;

    //progress dialog
    private ProgressDialog progressDialog;

    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

    private EasyDB easyDB;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_shop_details);

        //init ui views
        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);

        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        productsRv = findViewById(R.id.productsRv);
        filterProductsTv = findViewById(R.id.filterProductsTv);
        cartCountTv  = findViewById(R.id.cartCountTv);

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //get Uid of the shop from intent
        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();



        //declare it to class level and init in OnCreate
         easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //each shop has it own products and orders so if user add items to cart and go back and open cart in different shop then cart should be different
        //so delete cart data whenever user open this activity
        deleteCartData();//before it
        cartCount();

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                try
                {
                    adapterProductUser.getFilter().filter(s);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });





        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //show cart dialog
                showCartDialog();
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category:")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //get selected item
                                String selected = Constants.productCategories1[which];
                                filterProductsTv.setText(selected);
                                if (selected.equals("All"))
                                {
                                    loadShopProducts();
                                }

                                else
                                {
                                    //load filtered products
                                    adapterProductUser.getFilter().filter(selected);
                                }
                            }
                        })
                        .show();
            }
        });

    }

    private void deleteCartData()
    {
        easyDB.deleteAllDataFromTable(); //delete all records from cart
    }

    public void cartCount()
    {
        //public so that we can access it in adapter
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count<=0)
        {
            //no item in cart, hide cartCountTv
            cartCountTv.setVisibility(View.GONE);

        }

        else
            {
                //item in cart, show cartCountTv and set count
                cartCountTv.setVisibility(View.VISIBLE);
                cartCountTv.setText(""+count); //concatenate with string, because we can't set integer in textview
            }
    }

    public double allTotalPrice = 0.00;
    //need to access these views in adapter
    public TextView sTotalTv, dFeeTv, allTotalPriceTv;
    private void showCartDialog()
    {
        //init list
        cartItemList = new ArrayList<>();
        //inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart,null );
        //init views
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
        TextView sTotalLabelTv = view.findViewById(R.id.sTotalLabelTv);
         sTotalTv = view.findViewById(R.id.sTotalTv);
        TextView dFeeLabelTv = view.findViewById(R.id.dFeeLabelTv);
         dFeeTv = view.findViewById(R.id.dFeeTv);
        TextView totalLabelTv = view.findViewById(R.id.totalLabelTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);



        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);


        shopNameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();


        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext())
        {
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);


            allTotalPrice = allTotalPrice + Double.parseDouble(cost);


            ModelCartItem modelCartItem = new ModelCartItem(
                    ""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity);

            cartItemList.add(modelCartItem);

        }

        //setup adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);
        //set to recyclerview
        cartItemsRv.setAdapter(adapterCartItem);

        dFeeTv.setText("#"+deliveryFee);
        sTotalTv.setText("#"+String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("#"+(allTotalPrice + Double.parseDouble(deliveryFee.replace("#",""))));

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;

            }
        });

        //place order
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //first validate delivery address
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null"))
                {
                    //user didn't enter address before placing order
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your address in your profile before placing order...", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }

                if (myPhone.equals("") ||myPhone.equals("null"))
                {
                    //user didn't enter phone number before placing order
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your phone number in your profile before placing order...", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }

                if (cartItemList.size() == 0)
                {
                    //cart list is empty
                    Toast.makeText(ShopDetailsActivity.this, "No item in cart...", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }

                submitOrder();
            }
        });

    }

    private void submitOrder()
    {
        //show progress dialog
        progressDialog.setMessage("Placing order...");
        progressDialog.show();

        //for order id and order time
        final String timestamp = ""+System.currentTimeMillis();


        String cost = allTotalPriceTv.getText().toString().trim().replace("#", ""); //remove # if contains

        // setup order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", ""+timestamp);
        hashMap.put("orderTime", ""+timestamp);
        hashMap.put("orderStatus", "In Progress"); //In Progress/Completed/Cancelled
        hashMap.put("orderCost", ""+cost); //In Progress/Completed/Cancelled
        hashMap.put("orderBy", ""+firebaseAuth.getUid());
        hashMap.put("orderTo", ""+shopUid);
        hashMap.put("latitude", ""+myLatitude);
        hashMap.put("longitude", ""+myLongitude);




        //add to db
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        //order info added now add order items
                        for (int i=0; i<cartItemList.size(); i++)
                        {
                            String pId = cartItemList.get(i).getpId();
                            String id = cartItemList.get(i).getId();
                            String cost = cartItemList.get(i).getCost();
                            String name = cartItemList.get(i).getName();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();


                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pId);
                            hashMap1.put("name", name);
                            hashMap1.put("cost", cost);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);


                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);

                        }

                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Placed Sucessfully...", Toast.LENGTH_SHORT).show();

                        //after placing order, open order details activity
                        //open order details activity, we need to key there, orderId, orderTo
                        Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                        intent.putExtra("orderTo", shopUid);
                        intent.putExtra("orderId", timestamp);
                        startActivity(intent); //now get these values through intent on OrderDetailsUserActivity
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        //failed placing order
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



    }

    private void openMap()
    {
        //saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" +shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone()
    {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadMyInfo()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot ds: dataSnapshot.getChildren())
                        {
                            //get user data
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            myPhone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();
                            myLatitude = ""+ds.child("latitude").getValue();
                            myLongitude = ""+ds.child("longitude").getValue();


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadShopDetails()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //get shop data
                String name = ""+dataSnapshot.child("name").getValue();
                shopName = ""+dataSnapshot.child("shopName").getValue();
                shopEmail = ""+dataSnapshot.child("email").getValue();
                shopPhone = ""+dataSnapshot.child("phone").getValue();
                shopAddress = ""+dataSnapshot.child("address").getValue();
                shopLatitude = ""+dataSnapshot.child("latitude").getValue();
                shopLongitude = ""+dataSnapshot.child("longitude").getValue();
                deliveryFee = ""+dataSnapshot.child("deliveryFee").getValue();
                String profileImage = ""+dataSnapshot.child("profileImage").getValue();
                String shopOpen = ""+dataSnapshot.child("shopOpen").getValue();


                //set data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Delivery Fee: #" + deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if (shopOpen.equals("true"))
                {
                    openCloseTv.setText("Open");
                }
                else
                    {
                        openCloseTv.setText("Closed");

                    }

                try
                {
                    Picasso.get().load(profileImage).into(shopIv);
                }
                catch (Exception e)
                {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    private void loadShopProducts()
    {
        //init list
        productsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding items
                        productsList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren())
                        {
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productsList.add(modelProduct);

                        }

                        //setup adapter
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, productsList);
                        //set adapter
                        productsRv.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }



}
