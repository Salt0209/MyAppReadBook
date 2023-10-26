package com.example.appreadbook.Admin;

import static com.example.appreadbook.Constant.DATABASE_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.appreadbook.Adapter.AdapterBookAdmin;
import com.example.appreadbook.Model.ModelBook;
import com.example.appreadbook.databinding.ActivityAdminBookListBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminBookListActivity extends AppCompatActivity {
    private ActivityAdminBookListBinding binding;

    private String categoryId, categoryTitle;

    private ArrayList<ModelBook> bookArrayList,bookFilter;

    private AdapterBookAdmin adapterBookAdmin;

    private static final String TAG = "PDF_LIST_TAG";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBookListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null){
            categoryId = bundle.getString("categoryId","");
            categoryTitle = bundle.getString("categoryTitle","");
        }

        //set pdf category
        binding.subTitleTv.setText(categoryTitle);

        //initUI();
        loadBookList();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    loadBookListByName(s);
                }catch (Exception e){
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.addBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminBookListActivity.this, AdminBookAddActivity.class);
                startActivity(intent);
            }
        });
    }
//    private void initUI() {
//        //Constructor recycleView
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        binding.bookRv.setLayoutManager(linearLayoutManager);
//
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
//        binding.bookRv.addItemDecoration(dividerItemDecoration);
//
//
//    }

    private void loadBookList() {
        bookArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.orderByChild("bookCategoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get data
                            ModelBook model = ds.getValue(ModelBook.class);
                            if(model.getBookTitle().toString().toUpperCase().contains("")){
                                bookArrayList.add(model);
                            }
                            //add to list


                            Log.d(TAG, "onDataChange: " + model.getBookId() + " " + model.getBookTitle());
                        }
                        //set up adapter
                        adapterBookAdmin = new AdapterBookAdmin(AdminBookListActivity.this, bookArrayList);
                        binding.bookRv.setAdapter(adapterBookAdmin);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadBookListByName(CharSequence query) {
        bookFilter = new ArrayList<>();
        String query2 = query.toString().toUpperCase();

        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.orderByChild("bookCategoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookFilter.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get data
                            ModelBook model = ds.getValue(ModelBook.class);
                            if(model.getBookTitle().toString().toUpperCase().contains(query2)){
                                bookFilter.add(model);
                            }
                            //add to list


                            Log.d(TAG, "onDataChange: " + model.getBookId() + " " + model.getBookTitle());
                        }
                        //set up adapter
                        adapterBookAdmin = new AdapterBookAdmin(AdminBookListActivity.this, bookFilter);
                        binding.bookRv.setAdapter(adapterBookAdmin);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}