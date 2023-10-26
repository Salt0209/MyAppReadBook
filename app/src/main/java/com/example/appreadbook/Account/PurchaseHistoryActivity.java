package com.example.appreadbook.Account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static com.example.appreadbook.Constant.DATABASE_NAME;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.appreadbook.Adapter.AdapterPurchaseHistory;
import com.example.appreadbook.MainActivity;
import com.example.appreadbook.Model.ModelBook;
import com.example.appreadbook.databinding.ActivityPurchaseHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PurchaseHistoryActivity extends AppCompatActivity {
    private ActivityPurchaseHistoryBinding binding;
    private FirebaseAuth firebaseAuth;
    private static String TAG="PURCHASE_HISTORY_TAG";
    private ArrayList<ModelBook> arrayList_purchaseHistory;
    private AdapterPurchaseHistory adapterPurchaseHistory;

    private RecyclerView recyclerView_purchaseHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPurchaseHistoryBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        firebaseAuth=FirebaseAuth.getInstance();
        checkUserIsLoggedIn();
        initUI();
        loadPurchaseHistoryFromDatabase();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    private void checkUserIsLoggedIn() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            startActivity(new Intent(PurchaseHistoryActivity.this, MainActivity.class));
            finish();
        }
        else{
        }
    }
    private void initUI() {
        //Constructor recycleView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.purchaseHistoryRv.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        binding.purchaseHistoryRv.addItemDecoration(dividerItemDecoration);
    }
    private void loadPurchaseHistoryFromDatabase(){
        arrayList_purchaseHistory = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Users");
        ref.child(firebaseAuth.getUid()).child("PurchaseHistory")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList_purchaseHistory.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String bookId = ""+dataSnapshot.child("bookId").getValue();
                    String purchaseTimestamp = ""+dataSnapshot.child("purchaseTimestamp").getValue();

                    //set id to model
                    ModelBook modelPurchase = new ModelBook();
                    modelPurchase.setBookId(bookId);
                    modelPurchase.setBookTimestamp(Long.parseLong(purchaseTimestamp));
                    arrayList_purchaseHistory.add(modelPurchase);
                }

                adapterPurchaseHistory = new AdapterPurchaseHistory(PurchaseHistoryActivity.this,arrayList_purchaseHistory);
                binding.purchaseHistoryRv.setAdapter(adapterPurchaseHistory);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: "+ error.getMessage());
            }
        });
    }

}