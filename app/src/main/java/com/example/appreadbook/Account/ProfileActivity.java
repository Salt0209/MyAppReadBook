package com.example.appreadbook.Account;

import static com.example.appreadbook.Constant.DATABASE_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.appreadbook.Adapter.AdapterBookFavourite;
import com.example.appreadbook.Admin.AdminBookEditActivity;
import com.example.appreadbook.Admin.AdminHomeActivity;
import com.example.appreadbook.Model.ModelBook;
import com.example.appreadbook.MyApplication;
import com.example.appreadbook.R;
import com.example.appreadbook.User.UserHomeActivity;
import com.example.appreadbook.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String userType="";
    private String userAvatar="";
    private String userEmail = "";

    private String userName="";
    private ArrayList<ModelBook> arrayList_book;
    private AdapterBookFavourite adapterBookFavourite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        firebaseAuth = FirebaseAuth.getInstance();
        //get current user
        firebaseUser = firebaseAuth.getCurrentUser();

        loadUserInfo();
        loadFavouriteBooks();
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userType.equals("userBasic")){
                    Intent intent = new Intent(ProfileActivity.this, UserHomeActivity.class);
                    startActivity(intent);
                }
                else if(userType.equals("admin")){
                    Intent intent = new Intent(ProfileActivity.this, AdminHomeActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(ProfileActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionDialog();
            }
        });
    }

    private void moreOptionDialog() {
        String[] options = {"Sửa hồ sơ", "Đổi mật khẩu","Lịch sử mua hàng"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Lựa chọn:")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle dialog option click
                        if(which==0){
                            Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("userName",userName);
                            bundle.putString("userAvatar",userAvatar);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                        else if(which == 1){
                            Intent intent = new Intent(ProfileActivity.this, PasswordChangeActivity.class);
                            intent.putExtra("userEmail",userEmail);
                            startActivity(intent);
                        }
                        else if(which == 2){
                            Intent intent = new Intent(ProfileActivity.this, PurchaseHistoryActivity.class);
                            intent.putExtra("userEmail",userEmail);
                            startActivity(intent);
                        }
                    }
                }).show();
    }


    private void loadUserInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get all info of user here from snapshot
                        userEmail = "" + snapshot.child("userEmail").getValue();
                        userName = "" + snapshot.child("userName").getValue();
                        userType = "" + snapshot.child("userType").getValue();
                        String userMoney = "" + snapshot.child("userMoney").getValue();
                        userAvatar = "" + snapshot.child("userAvatar").getValue();
                        String timestamp = "" + snapshot.child("userTimestamp").getValue();

                        String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));
                        if(userType.equals("userBasic")){
                            binding.accountTypeTv.setText("Cơ bản");
                        }
                        else if(userType.equals("Admin")){
                            binding.accountTypeTv.setText(userType);
                        }

                        binding.memberDateTv.setText(formattedDate);
                        binding.nameTv.setText(userName);
                        binding.emailTv.setText(userEmail);
                        binding.moneyTv.setText(userMoney);

                        //set image,using glide
                        Glide.with(ProfileActivity.this)
                                .load(userAvatar)
                                .placeholder(R.drawable.avatar_default)
                                .into(binding.profileIv);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadFavouriteBooks(){
        //init list
        arrayList_book = new ArrayList<>();

        //load favourite book from db
        //Users > userId > Favourites
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favourites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before starting data
                        arrayList_book.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            String bookId = ""+ds.child("bookId").getValue();

                            //set id to model
                            ModelBook modelPdf = new ModelBook();
                            modelPdf.setBookId(bookId);

                            //add model to list
                            arrayList_book.add(modelPdf);
                        }
                        //set number of favourite books
                        binding.favouriteBookCountTv.setText(""+arrayList_book.size());
                        adapterBookFavourite = new AdapterBookFavourite(ProfileActivity.this,arrayList_book);
                        binding.booksRv.setAdapter(adapterBookFavourite);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}