package com.example.appreadbook;

import static com.example.appreadbook.Constant.DATABASE_NAME;
import static com.example.appreadbook.Constant.MAX_BYTES_PDF;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.appreadbook.Account.LoginActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {
    private static final String TAG_DOWNLOAD = "APPLICATION_TAG";
    private static final String STORAGE_NAME = "gs://appreadbook-8ae8f.appspot.com/";




    //created a static method to convert timestamp to proper date format
    public static final String formatTimestamp(long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        //format timestamp to dd/MM/yyyy
        String date = DateFormat.format("dd/MM/yyyy", cal).toString();

        return date;
    }
    public static void loadCategory(String categoryId, TextView categoryTv) {
        //get category using categoryID

        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("BookCategories");
        Query query = ref.child(categoryId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                String category = String.valueOf(snapshot.child("categoryName").getValue());

                categoryTv.setText(category);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView pagesTv) {
        //using url we can get file and its metadata from firebase storage
        String TAG = "PDF_LOAD_SINGLE_TAG";
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: " + pdfTitle + " successfully got the file");

                        //set to pdfView
                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        //hide progress
                                        progressBar.setVisibility(View.VISIBLE);
                                        Log.d(TAG, "onError: " + t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: " + t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded ");

                                        //if pagesTv param is not null then set page numbers
                                        if(pagesTv != null){
                                            pagesTv.setText(""+nbPages);
                                        }
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: failed getting file from url due to " + e.getMessage());

                    }
                });
    }
    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG = "PDF_SIZE_TAG";
        //using url we can get file and its metadata from firebase storage

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
//                        get size in bytes
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: " + pdfTitle + " " + bytes);

                        //convert bytes to KB,MB
                        double kb = bytes / 1024;
                        double mb = kb / 1024;
                        if (mb >= 1) {
                            sizeTv.setText(String.format("%.2f", mb) + " MB");
                        } else if (kb >= 1) {
                            sizeTv.setText(String.format("%.2f", kb) + " KB");
                        } else {
                            sizeTv.setText(String.format("%.2f", bytes) + " bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }
    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "deleteBook: Deleting...");

        Log.d(TAG, "deleteBook: Deleting from storage...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted from storage");
                        Log.d(TAG, "onSuccess: Now deleting info from db");
                        DatabaseReference reference = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from db too");
                                        Toast.makeText(context, "Xoá thành công....", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete from db due to " + e.getMessage());
                                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from storage due to " + e.getMessage());
                    }
                });


    }
    public static void checkLoggedIn(Context context){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            //not logged in
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setMessage("You're not logged in! Please log in!")
                    .setPositiveButton("Go to log in", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Handle the "OK" button click action here
                            dialog.dismiss();
                            Intent intent = new Intent(context, LoginActivity.class);
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Handle the "Cancel" button click action here
                            dialog.dismiss();
                            Intent intent = new Intent(context, context.getClass());
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(intent);
                            ((Activity)(context)).finish();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public static void addToPurchaseHistory(Context context,String bookId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        long timestamp = System.currentTimeMillis();

        //setup data to add in firebase db of current user for favourite book
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("bookId",""+bookId);
        hashMap.put("purchaseTimestamp",""+timestamp);

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Users");
        ref.child(firebaseAuth.getUid()).child("PurchaseHistory").child(bookId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Toast.makeText(context, "Added to your purchase history...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to add to your purchase history due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
    public static void addToFavourite(Context context,String bookId){
        //We can add only if user is logged in
        //1) check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        long timestamp = System.currentTimeMillis();

        //setup data to add in firebase db of current user for favourite book
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("bookId",""+bookId);
        hashMap.put("timestamp",""+timestamp);

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favourites").child(bookId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Đã thêm vào danh mục yêu thích...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to add to favourite due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public static void removeFromFavourite(Context context, String bookId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favourites").child(bookId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Đã xoá khỏi danh mục yêu thích...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to remove from favourite due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public static void downloadBook(Context context, String bookId, String bookTitle, String bookUrl) {
        Log.d(TAG_DOWNLOAD, "downloadBook: downloading book....");
        String nameWithExtension = bookTitle + ".pdf";
        Log.d(TAG_DOWNLOAD, "downloadBook: NAME: " + nameWithExtension);

        //download from firebase using url
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: Book Downloaded");
                        Log.d(TAG_DOWNLOAD, "onSuccess: Saving book...");
                        saveDownloadedBook(context, bytes, nameWithExtension, bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to download due to " + e.getMessage());
                        Toast.makeText(context, "Failted to download due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private static void saveDownloadedBook(Context context, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saving downloaded book");
        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            String filePath = downloadsFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Tải xuống thành công!", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saved to Download Folder");

            incrementBookDownloadedCount(bookId);

        } catch (Exception e) {
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Failed saving to Download folder due to " + e.getMessage());
            Toast.makeText(context, "Failed saving to download folder due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void incrementBookDownloadedCount(String bookId) {
        Log.d(TAG_DOWNLOAD, "incrementBookDownloadedCount: Increment Book Download Count");

        //Step 1: get previous download count
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloadsCount = "" + snapshot.child("bookDownloadCount").getValue();
                        Log.d(TAG_DOWNLOAD, "onDataChange: Downloads Count: " + downloadsCount);
                        if (downloadsCount.equals("") || downloadsCount.equals("null")) {
                            downloadsCount = "0";
                        }

                        //convert to long
                        long newDownloadedCount = Long.parseLong(downloadsCount) + 1;
                        Log.d(TAG_DOWNLOAD, "onDataChange: New download count: " + newDownloadedCount);

                        //set up data to update
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("bookDownloadCount",newDownloadedCount);

                        //step 2) update new incremented download count to db
                        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
                        ref.child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG_DOWNLOAD, "onSuccess: Downloads Count updated...");

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to update Downloads Count due to "+e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public static void incrementBookViewsCount(String bookId) {
        //1) Get book views count
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get views count
                        String viewsCount = "" + snapshot.child("bookViewCount").getValue();
                        //in case of null replace with 0
                        if (viewsCount.equals("") || viewsCount.equals("null")) {
                            viewsCount = "0";
                        }

                        //2)Increment views count
                        long newViewsCount = Long.parseLong(viewsCount) + 1;

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("bookViewCount", newViewsCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
                        reference.child(bookId)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
