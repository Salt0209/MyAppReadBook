package com.example.appreadbook.Admin;

import static com.example.appreadbook.Constant.DATABASE_NAME;
import static com.example.appreadbook.Constant.STORAGE_NAME;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.appreadbook.databinding.ActivityAdminBookAddBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class AdminBookAddActivity extends AppCompatActivity {
    private ActivityAdminBookAddBinding binding;
    public static final String TAG= "ADMIN_BOOK_ADD_TAG";



    private String pdfName="";
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    private String selectedCategoryId, selectedCategoryTitle;

    private Uri pdfUri = null;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBookAddBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        firebaseAuth = FirebaseAuth.getInstance();

        loadUnit();
        loadBookCategories();

        binding.backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.attachIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickCategory();
            }
        });
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
                //continueDialogFragment();
            }
        });


    }

    private void loadUnit() {
        binding.progressBarLoadPdf.setVisibility(View.INVISIBLE);
        binding.progressBarSubmit.setVisibility(View.INVISIBLE);
    }

    private void pickCategory() {
            Log.d(TAG, "categoryPickDialog: showing  category pick dialog");

            //get string array of categories from arraylist

            String[] categoriesArray = new String[categoryTitleArrayList.size()];
            for (int i = 0; i < categoryTitleArrayList.size(); i++) {
                categoriesArray[i] = categoryTitleArrayList.get(i);
            }

            //alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick Category")
                    .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //handle item click
                            //get cliked item from list
                            selectedCategoryTitle = categoryTitleArrayList.get(which);
                            selectedCategoryId = categoryIdArrayList.get(which);
                            //set to category textview
                            binding.categoryTv.setText(selectedCategoryTitle);

                            Log.d(TAG, "onClick: Selected category " + selectedCategoryId + " " + selectedCategoryTitle);
//                            Toast.makeText(AdminBookAddActivity.this, "" + selectedCategoryId + " " + selectedCategoryTitle, Toast.LENGTH_SHORT).show();

                        }
                    }).show();

    }

    private void loadBookCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        //db reference to load categories ... db> categories
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("BookCategories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear(); //clear before adding data
                categoryIdArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    //get id and title of category
                    String categoryId = "" + ds.child("categoryId").getValue();
                    String categoryTitle = "" + ds.child("categoryName").getValue();

                    //add to respective arraylists
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String title = "", description = "";
    private Integer bookPrice = 0;
    private void validateData() {
        Log.d(TAG, "validateData: validating data");

        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        bookPrice = Integer.parseInt(binding.priceEt.getText().toString().trim());

        //validate data
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Enter title...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter description...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Pick category...", Toast.LENGTH_SHORT).show();
        } else if (bookPrice == null) {
            Toast.makeText(this, "Enter price...", Toast.LENGTH_SHORT).show();
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pick pdf...", Toast.LENGTH_SHORT).show();
        } else {
            //all data is valid
            uploadPdfToStorage();
        }
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intentActivityResultLauncher.launch(intent);

    }
    private ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK){
                        Intent intent = result.getData();
                        pdfUri = intent.getData();
                        Toast.makeText(AdminBookAddActivity.this, "Get success", Toast.LENGTH_SHORT).show();

                        binding.progressBarLoadPdf.setVisibility(View.VISIBLE);
                        //load pdf image
                        binding.pdfView.fromUri(pdfUri)
                                .defaultPage(0)
                                .spacing(0)
                                .enableSwipe(true)
                                .swipeHorizontal(true)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        //hide progress
                                        binding.progressBarLoadPdf.setVisibility(View.VISIBLE);
                                        Log.d(TAG, "onError: " + t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        binding.progressBarLoadPdf.setVisibility(View.GONE);
                                        Toast.makeText(AdminBookAddActivity.this, "Pages error", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "onPageError: " + t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        binding.progressBarLoadPdf.setVisibility(View.GONE);
                                        Log.d(TAG, "loadComplete: pdf loaded ");
                                    }
                                })
                                .onPageChange(new OnPageChangeListener() {
                                    @Override
                                    public void onPageChanged(int page, int pageCount) {
                                        //set current and total pages in toolbar subtitle
                                        int currentPage = (page + 1); //do +1 because page start from 0
                                        //binding.toolbarSubtitleTv.setText(currentPage + "/" + pageCount);
                                        Log.d(TAG, "onPageChanged: "+currentPage + "/" + pageCount);
                                    }
                                })
                                .load();

                    }else {
                        Log.d(TAG, "onActivityResult: cancelled picking pdf");
                        Toast.makeText(AdminBookAddActivity.this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void uploadPdfToStorage() {
        //Step 2:Upload pdf to firebase storage
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...");

        binding.submitBtn.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        //path of pdf in firebase storage
        String filePathAndName = "Books/" + title;
        //storage reference
        StorageReference storageReference = FirebaseStorage.getInstance(STORAGE_NAME).getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: PDF uploaded to storage...");
                        Log.d(TAG, "onSuccess: getting pdf url");

                        Toast.makeText(AdminBookAddActivity.this, "Upload to storage successfully", Toast.LENGTH_SHORT).show();

                        //get pdf url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = "" + uriTask.getResult();
                        //upload a firebase db
                        uploadPdfIntoDB(uploadedPdfUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: PDF upload failed due to " + e.getMessage());
                        Toast.makeText(AdminBookAddActivity.this, "PDF upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPdfIntoDB(String uploadedPdfUrl) {
        Log.d(TAG, "uploadPdfToStorage: uploading pdf info to firebase db...");

//        binding.progressBarUpload.setVisibility(View.VISIBLE);

        String uid = firebaseAuth.getUid();
        long timestamp = System.currentTimeMillis();

        //setup data to upload
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("bookId", "" + timestamp);
        hashMap.put("bookTitle", "" + title);
        hashMap.put("bookDescription", "" + description);
        hashMap.put("bookCategoryId", "" + selectedCategoryId);
        hashMap.put("bookUrl", "" + uploadedPdfUrl);
        hashMap.put("bookTimestamp", timestamp);
        hashMap.put("bookPrice",bookPrice);
        hashMap.put("bookViewCount",0);
        hashMap.put("bookDownloadCount",0);

        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        binding.submitBtn.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Log.d(TAG, "onSuccess: Successfully uploaded... ");
                        Toast.makeText(AdminBookAddActivity.this, "Successfully uploaded...", Toast.LENGTH_SHORT).show();

                        //alert dialog to choose: continue or back to index
                        continueDialogFragment();



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.submitBtn.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Log.d(TAG, "onFailure: Failed to upload to db due to " + e.getMessage());
                        Toast.makeText(AdminBookAddActivity.this, "Failed to upload to db due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void continueDialogFragment() {
            // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Add book successfully. Please choose to continue!")
                .setPositiveButton("Add another book", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handle the "OK" button click action here
                        dialog.dismiss();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);

                    }
                })
                .setNegativeButton("Back to home page", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handle the "Cancel" button click action here
                        dialog.dismiss();
                        Intent intent = new Intent(AdminBookAddActivity.this, AdminHomeActivity.class);
                        startActivity(intent);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        }
    }

