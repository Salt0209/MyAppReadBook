package com.example.appreadbook.Admin;

import static com.example.appreadbook.Constant.DATABASE_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.appreadbook.Adapter.AdapterCategory;
import com.example.appreadbook.MainActivity;
import com.example.appreadbook.Model.ModelCategory;
import com.example.appreadbook.Account.ProfileActivity;
import com.example.appreadbook.databinding.ActivityAdminHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminHomeActivity extends AppCompatActivity {
    private ActivityAdminHomeBinding binding;
    private FirebaseAuth firebaseAuth;
    private static String TAG="ADMIN_DASHBOARD_ACTIVITY_TAG";
    private ArrayList<ModelCategory> arrayList_category;
    private AdapterCategory adapterCategory;

    private RecyclerView recyclerView_category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        
        firebaseAuth=FirebaseAuth.getInstance();
        checkUserIsLoggedIn();
        initUI();
        loadCategoriesFromDatabase();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s == null || s==""){
                    loadCategoriesFromDatabase();
                }
                else {
                    try {
                        adapterCategory.getFilter().filter(s);
                    }catch (Exception e){
                    }

                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        binding.accountIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Intent intent = new Intent(AdminHomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminCategoryAddActivity.class);
                startActivity(intent);
            }
        });
        binding.addBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminBookAddActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initUI() {
        //Constructor recycleView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.categoriesRv.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        binding.categoriesRv.addItemDecoration(dividerItemDecoration);
    }
    private void loadCategoriesFromDatabase(){
        arrayList_category = new ArrayList<>();
        DatabaseReference reference =FirebaseDatabase.getInstance(DATABASE_NAME).getReference("BookCategories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList_category.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ModelCategory modelCategory =dataSnapshot.getValue(ModelCategory.class);
                    arrayList_category.add(modelCategory);
                }

                adapterCategory = new AdapterCategory(AdminHomeActivity.this,arrayList_category);
                binding.categoriesRv.setAdapter(adapterCategory);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: "+ error.getMessage());
            }
        });
    }

    private void checkUserIsLoggedIn() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            startActivity(new Intent(AdminHomeActivity.this, MainActivity.class));
            finish();
        }
        else{
            String email = firebaseUser.getEmail();
            binding.emailTv.setText(email);
        }
    }
}