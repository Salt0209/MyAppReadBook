package com.example.appreadbook.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appreadbook.Admin.AdminBookListActivity;
import com.example.appreadbook.Filter.FilterCategory;
import com.example.appreadbook.Model.ModelCategory;
import com.example.appreadbook.R;
import com.example.appreadbook.databinding.CategoryRowBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.CategoryViewHolder> implements Filterable {

    private static final String DATABASE_NAME = "https://appreadbook-8ae8f-default-rtdb.asia-southeast1.firebasedatabase.app";

    private Context context;

    public ArrayList<ModelCategory> arrayList_category,filterList;

    private CategoryRowBinding binding;
    private String newCategory;
    private FilterCategory filter;

    public AdapterCategory(Context context, ArrayList<ModelCategory> arrayList_category) {
        this.context = context;
        this.arrayList_category = arrayList_category;
        this.filterList = arrayList_category;

    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = CategoryRowBinding.inflate(LayoutInflater.from(context),parent,false);
        return new CategoryViewHolder(binding.getRoot());
        //Option 2: View view = LayoutInflater.from(context).inflate(R.layout.category_row,parent,false);
        //return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        ModelCategory modelCategory =arrayList_category.get(position);

        if(modelCategory==null){
            return;
        }
        String categoryId = modelCategory.getCategoryId();
        String categoryName = modelCategory.getCategoryName();
        long categoryTimestamp = modelCategory.getCategoryTimestamp();
        holder.categoryTv.setText(categoryName);

        //handle click -> go to detail
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AdminBookListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("categoryId",categoryId);
                bundle.putString("categoryTitle",categoryName);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AdminDashboardActivity activity = new AdminDashboardActivity();
                loadEditLayout(modelCategory,holder);
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure to delete this category?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //begin delete
                                Toast.makeText(context,"Deleting...",Toast.LENGTH_SHORT).show();
                                deleteCategory(modelCategory,holder);

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        }).show();
            }
        });
    }

    private void updateCategory(ModelCategory model, CategoryViewHolder holder) {
        String id = model.getCategoryId();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("categoryName",""+newCategory);

        //Starting updating
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("BookCategories");
        ref.child(id).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Category updated...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Update failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadEditLayout(ModelCategory modelCategory, CategoryViewHolder holder){

        LayoutInflater inflater = LayoutInflater.from(context);

        View customView = inflater.inflate(R.layout.edit_category,null, false);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // Set the custom view to the AlertDialog
        alertDialogBuilder.setView(customView);


        // Create and show the AlertDialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        EditText editText_category = customView.findViewById(R.id.editText_category);
        Button submitButton = customView.findViewById(R.id.button_submit);
        ImageView closeButton = customView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss(); // Dismiss the AlertDialog when the close button is clicked
                newCategory = editText_category.getText().toString();
                updateCategory(modelCategory,holder);
            }
        });
        alertDialog.show();
    }
    private void deleteCategory(ModelCategory model, CategoryViewHolder holder) {
        //get id of category to delete
        String id = model.getCategoryId();
        //Firebase db > Categories > categoryId
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("BookCategories");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //deleted successfully
                        Toast.makeText(context,"Successfully deleted....",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to delete
                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });

    }

    @Override
    public int getItemCount() {
        if(arrayList_category != null){
            return arrayList_category.size();
        }
        return 0;
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterCategory(filterList,this);
        }
        return filter;
    }


    public class CategoryViewHolder extends RecyclerView.ViewHolder{
        TextView categoryTv;
        ImageButton deleteBtn;
        ImageButton editBtn;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryTv = binding.textViewCategory;
            deleteBtn = binding.buttonDelete;
            editBtn = binding.buttonEdit;



        }
    }

}
