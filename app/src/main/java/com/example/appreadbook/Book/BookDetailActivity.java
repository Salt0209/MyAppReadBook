package com.example.appreadbook.Book;

import static com.example.appreadbook.Constant.DATABASE_NAME;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.appreadbook.Adapter.AdapterComment;
import com.example.appreadbook.Model.ModelComment;
import com.example.appreadbook.MyApplication;
import com.example.appreadbook.R;
import com.example.appreadbook.databinding.ActivityBookDetailBinding;
import com.example.appreadbook.databinding.DialogCommentAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class BookDetailActivity extends AppCompatActivity {

    private ActivityBookDetailBinding binding;
    private static final String TAG = "BOOK_DETAIL_TAG";
    boolean isInMyFavourite = false;
    boolean purchased = false;

    private FirebaseAuth firebaseAuth;


    //pdf id, get from intent
    private String bookId, bookTitle, bookUrl;
    private Integer userMoney,bookPriceCheck;

    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        loadUnit();
        loadUserMoney();
        loadBookDetails();
        loadComments();
        checkIsFavourite();



        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseBook();
            }
        });
        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(BookDetailActivity.this, "READING....", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(BookDetailActivity.this, BookViewActivity.class);
                intent1.putExtra("bookId", bookId);
                startActivity(intent1);
            }
        });
        binding.downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(BookDetailActivity.this, "DOWNLOADING...", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick: Checking permission");
                if (ContextCompat.checkSelfPermission(BookDetailActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onClick: Permission already granted, can download book");
                    MyApplication.downloadBook(BookDetailActivity.this, "" + bookId, "" + bookTitle, "" + bookUrl);
                } else {
                    Log.d(TAG, "onClick: Permission was not granted , request permission...");
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });
        binding.favouriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInMyFavourite) {
                    //in favourite, remove from favourite
                    MyApplication.removeFromFavourite(BookDetailActivity.this,bookId);
                    checkIsFavourite();
                } else {
                    //not favourite, add to favourite
                    MyApplication.addToFavourite(BookDetailActivity.this,bookId);
                    checkIsFavourite();
                }

            }
        });
        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //must logged in to add cmt
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(BookDetailActivity.this, "You're not logged in...", Toast.LENGTH_SHORT).show();
                }
                else {
                    addCommentDialog();
                }
            }
        });
    }
    private void loadComments() {
        commentArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelComment model = ds.getValue(ModelComment.class);
                            commentArrayList.add(model);
                        }
                        adapterComment = new AdapterComment(BookDetailActivity.this, commentArrayList);
                        binding.commentsRv.setAdapter(adapterComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

        private String comment = "";
    private void addCommentDialog() {
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));

        //set up alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        //create and show alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //handle click.add comment
        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        //handle click, add comment
        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment = commentAddBinding.commentEt.getText().toString().trim();
                if(TextUtils.isEmpty(comment)){
                    Toast.makeText(BookDetailActivity.this, "Enter your comment...", Toast.LENGTH_SHORT).show();
                }
                else {
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });
    }
    private void addComment() {
        //show progress

        //timestamp for comment id, comment time
        String timestamp = ""+ System.currentTimeMillis();

        //setup data to add in db for comment
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("bookId",""+bookId);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("comment",""+comment);
        hashMap.put("uid",""+firebaseAuth.getUid());

        //DB path to add data into it
//        Books > bookId > Comments > commentId > commentData
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(BookDetailActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BookDetailActivity.this, "Failed to add comment due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Permission Granted ");
                    MyApplication.downloadBook(this, "" + bookId, "" + bookTitle, "" + bookUrl);
                } else {
                    Log.d(TAG, "Permission was denied...: ");
                    Toast.makeText(this, "Permission was denied...", Toast.LENGTH_SHORT).show();
                }
            });
    private void loadUnit() {
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        MyApplication.incrementBookViewsCount(bookId);
        binding.readBookBtn.setEnabled(false);
        binding.downloadBookBtn.setEnabled(false);
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference();
        ref.child("Books").child(bookId).child("bookPrice").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                while (!snapshot.exists());
                bookPriceCheck = snapshot.getValue(Integer.class);
                if(bookPriceCheck==0 ){
                    binding.readBookBtn.setEnabled(true);
                    binding.downloadBookBtn.setEnabled(true);
                    binding.buyBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            checkPurchased();
        }
    }
    private void checkPurchased() {
        //logged in check if its in favourite list or not
        DatabaseReference reference = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Users");
        reference.child(firebaseAuth.getUid()).child("PurchaseHistory").child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        purchased = snapshot.exists(); //true: if exits
                        if (purchased) {
                            //exits in purchase history
                            binding.readBookBtn.setEnabled(true);
                            binding.downloadBookBtn.setEnabled(true);
                            binding.buyBtn.setVisibility(View.GONE);
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void purchaseBook() {
        MyApplication.checkLoggedIn(BookDetailActivity.this);

        Integer cost = Integer.parseInt(binding.buyBtn.getText().toString());
        if(userMoney<cost){
            loadAlertMoneyNoEnough();
        }
        else {
            Integer moneyAfterCost = userMoney - cost;
            updateUserMoney(moneyAfterCost);
            MyApplication.addToPurchaseHistory(BookDetailActivity.this,bookId);
        }
    }

    private void checkIsFavourite() {
        //logged in check if its in favourite list or not
        DatabaseReference reference = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favourites").child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavourite = snapshot.exists(); //true: if exits
                        if (isInMyFavourite) {
                            //exits in favourite
                            binding.favouriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white, 0, 0);
                            binding.favouriteBtn.setText("Remove Favourite");
                        } else {
                            binding.favouriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0, 0);
                            binding.favouriteBtn.setText("Add Favourite");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    private void loadAlertMoneyNoEnough() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate the custom view
        View customView = inflater.inflate(R.layout.money_no_enough, null);

        // Create the AlertDialog using AlertDialog.Builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // Set the custom view to the AlertDialog
        alertDialogBuilder.setView(customView);


        // Create and show the AlertDialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        ImageView closeButton = customView.findViewById(R.id.closeBtn);
        Button continueButton = customView.findViewById(R.id.gotoPaymentBtn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss(); // Dismiss the AlertDialog when the close button is clicked
            }
        });
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }



    private void loadUserMoney() {
//        firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userMoney = snapshot.child("userMoney").getValue(Integer.class);
                        binding.userMoneyTv.setText(userMoney.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void updateUserMoney(Integer moneyAfterCost) {

//        firebaseAuth = FirebaseAuth.getInstance();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("userMoney",moneyAfterCost);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: userMoney updated");
                        Toast.makeText(BookDetailActivity.this, "User money updated", Toast.LENGTH_SHORT).show();

                        binding.buyBtn.setEnabled(false);

                        binding.readBookBtn.setEnabled(true);
                        binding.downloadBookBtn.setEnabled(true);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to update user money due to "+e.getMessage());
                        Toast.makeText(BookDetailActivity.this, "Failed to update db due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookTitle = "" + snapshot.child("bookTitle").getValue();
                        String description = "" + snapshot.child("bookDescription").getValue();
                        String categoryId = "" + snapshot.child("bookCategoryId").getValue();
                        String viewsCount = "" + snapshot.child("bookViewCount").getValue();
                        String downloadsCount = "" + snapshot.child("bookDownloadCount").getValue();
                        String bookPrice = "" + snapshot.child("bookPrice").getValue();
                        bookUrl = "" + snapshot.child("bookUrl").getValue();
                        String timestamp = "" + snapshot.child("bookTimestamp").getValue();

                        //required data is loaded, show download button
//                        binding.downloadBookBtn.setVisibility(View.VISIBLE);

                        //format date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                "" + categoryId,
                                binding.categoryTv
                        );
                        MyApplication.loadPdfFromUrlSinglePage(
                                "" + bookUrl,
                                "" + bookTitle,
                                binding.pdfView,
                                binding.progressBar,
                                binding.pagesTv
                        );
                        MyApplication.loadPdfSize(
                                "" + bookUrl,
                                "" + bookTitle,
                                binding.sizeTv
                        );
//                        MyApplication.loadPdfPageCount(
//                                PdfDetailActivity.this,
//                                "" + bookUrl,
//                                binding.pagesTv
//                        );

                        //set data
                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                        binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));
                        binding.dateTv.setText(date);
                        binding.buyBtn.setText(bookPrice.replaceAll("\\b0\\b","Free"));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}