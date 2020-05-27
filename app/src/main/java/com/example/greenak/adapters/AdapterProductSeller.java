package com.example.greenak.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenak.FilterProduct;
import com.example.greenak.FilterProductUser;
import com.example.greenak.R;
import com.example.greenak.activities.EditProductActivity;
import com.example.greenak.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable

{
    private Context context;
    public ArrayList<ModelProduct> productList, filterList;
    private FilterProduct filter;


    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller, parent, false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position)
    {
        //get data
        final ModelProduct modelProduct = productList.get(position);
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String timestamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();


        //set data
        holder.titleTv.setText(title);
        holder.quantityTv.setText(quantity);
        holder.discountedNoteTv.setText(discountNote);
        holder.discountedPriceTv.setText("#"+discountPrice);
        holder.discountedPriceTv.setText("#"+discountPrice);
        if (discountAvailable.equals("true"))
        {
            //product is on discount
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //Add strikethrough on original price


        }

        else
            {
                //product is not on discount
                holder.discountedPriceTv.setVisibility(View.GONE);
                holder.discountedNoteTv.setVisibility(View.GONE);
                holder.originalPriceTv.setPaintFlags(0);


            }

        try
        {
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(holder.productIconId);
        }

        catch (Exception e)
        {
            holder.productIconId.setImageResource(R.drawable.ic_add_shopping_primary);
        }

        //show dialog

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v )
            {
                //handle item clicks, show item details(in bottom sheet)
                detailsBottomSheet(modelProduct); //here modelProduct contains details of clicked item

            }
        });


    }

    private void detailsBottomSheet(ModelProduct modelProduct)
    {
        //bottom sheet
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);


        //inflate views for bottom sheet
        //View view = LayoutInflater.from(context).inflate(R.layout.)
       View view = LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller, null);
       //set view to bottom sheet
        bottomSheetDialog.setContentView(view);

        //init views of bottom sheet
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        ImageButton deleteBtn = view.findViewById(R.id.deleteBtn);
        ImageButton editBtn = view.findViewById(R.id.editBtn);
        ImageView productIconIv = view.findViewById(R.id.productIconIv);
        TextView discountNoteTv = view.findViewById(R.id.discountNoteTv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        final TextView categoryTv = view.findViewById(R.id.categoryTv);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        TextView discountedPriceTv = view.findViewById(R.id.discountedPriceTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);


        //get data
        final String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        final String title = modelProduct.getProductTitle();
        String timestamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();

        //set data
        titleTv.setText(title);
        descriptionTv.setText(productDescription);
        categoryTv.setText(productCategory);
        quantityTv.setText(quantity);
        discountNoteTv.setText(discountNote);
        discountedPriceTv.setText("#"+discountPrice);
        originalPriceTv.setText("#"+originalPrice);

        if (discountAvailable.equals("true"))
        {
            //product is on discount
            discountedPriceTv.setVisibility(View.VISIBLE);
            discountNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //Add strikethrough on original price


        }

        else
        {
            //product is not on discount
            discountedPriceTv.setVisibility(View.GONE);
            discountNoteTv.setVisibility(View.GONE);

        }

        try
        {
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(productIconIv);
        }

        catch (Exception e)
        {
          productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }

        bottomSheetDialog.show();


        //edit click
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //open edit product activity, pass id of product
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId", id);
                context.startActivity(intent);

            }
        });

        //delete click
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                bottomSheetDialog.dismiss();
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete product"+title+"?")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //delete
                        deleteProduct(id); //id is product id

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //confirm, dismiss dialog
                        dialog.dismiss();

                    }
                })
                .show();


            }
        });

        //back click

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                bottomSheetDialog.dismiss();
            }
        });




    }

    private void deleteProduct(String id)
    {
        //delete product using its id
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //product deleted
                Toast.makeText(context, "Product deleted...", Toast.LENGTH_SHORT).show();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                //Failed deleting product
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter()
    {
        if (filter==null)
        {
            filter = new FilterProduct(this, filterList);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder
        {

            private ImageView productIconId, nextIv;
            private TextView discountedNoteTv, titleTv, quantityTv, discountedPriceTv, originalPriceTv;
            /*Holds Views of RecyclewView*/
            public HolderProductSeller(@NonNull View itemView) {
                super(itemView);

                productIconId = itemView.findViewById(R.id.productIconId);
                discountedNoteTv = itemView.findViewById(R.id.discountedNoteTv);
                titleTv = itemView.findViewById(R.id.titleTv);
                quantityTv = itemView.findViewById(R.id.quantityTv);
                discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
                originalPriceTv = itemView.findViewById(R.id.originalPriceTv);
                nextIv = itemView.findViewById(R.id.nextIv);





            }
        }
}
