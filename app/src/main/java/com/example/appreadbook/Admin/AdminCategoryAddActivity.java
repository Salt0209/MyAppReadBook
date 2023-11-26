package com.example.appreadbook.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.appreadbook.Constant;
import com.example.appreadbook.databinding.ActivityCategoryAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AdminCategoryAddActivity extends AppCompatActivity {
    private ActivityCategoryAddBinding binding;
    private String bookCategory = "";
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        firebaseAuth = FirebaseAuth.getInstance();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void validateData() {
        bookCategory = binding.categoryEt.getText().toString().trim();

        if(TextUtils.isEmpty(bookCategory)){
            Toast.makeText(this,"Please enter category....",Toast.LENGTH_SHORT).show();
        }
        else {
            checkExitCategory(bookCategory);
        }
    }

    private void checkExitCategory(String category) {
        DatabaseReference reference = FirebaseDatabase.getInstance(Constant.DATABASE_NAME).getReference("BookCategories");
        Query query= reference
                .orderByChild("categoryName")
                .equalTo(category);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Toast.makeText(AdminCategoryAddActivity.this,"Category exits....",Toast.LENGTH_SHORT).show();
                }
                else {
                    addCategoryFirebase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addCategoryFirebase() {
        long timestamp = System.currentTimeMillis();

        //setup info to add in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("categoryId",""+timestamp);
        hashMap.put("categoryName",""+bookCategory);
        hashMap.put("categoryTimestamp",timestamp);


        //add to firebase db.... Database Root > Categories > categoryId > category Info
        DatabaseReference ref = FirebaseDatabase.getInstance(Constant.DATABASE_NAME).getReference("BookCategories");

        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(AdminCategoryAddActivity.this,"Them danh muc thanh cong!....",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdminCategoryAddActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
}