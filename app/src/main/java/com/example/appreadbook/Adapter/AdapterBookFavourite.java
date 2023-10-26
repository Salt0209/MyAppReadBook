package com.example.appreadbook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appreadbook.Book.BookDetailActivity;
import com.example.appreadbook.Model.ModelBook;
import com.example.appreadbook.MyApplication;
import com.example.appreadbook.databinding.BookRowFavouriteBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterBookFavourite extends RecyclerView.Adapter<AdapterBookFavourite.BookFavouriteViewHolder> {

    private static final String DATABASE_NAME = "https://appreadbook-8ae8f-default-rtdb.asia-southeast1.firebasedatabase.app";

    private Context context;
    public ArrayList<ModelBook> arrayList_book;

    private BookRowFavouriteBinding binding;
    private static final String TAG = "FAV_BOOK_TAG";

    public AdapterBookFavourite(Context context, ArrayList<ModelBook> arrayList_book) {
        this.context = context;
        this.arrayList_book = arrayList_book;
    }

    @NonNull
    @Override
    public BookFavouriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = BookRowFavouriteBinding.inflate(LayoutInflater.from(context),parent,false);
        return new BookFavouriteViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull BookFavouriteViewHolder holder, int position) {
        ModelBook model = arrayList_book.get(position);

        loadBookDetails(model,holder);

        //handle click , open pdf detail page
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookDetailActivity.class);
                intent.putExtra("bookId",model.getBookId());
                context.startActivity(intent);
            }
        });

        //handle click , remove from favourite
        holder.removeFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavourite(context,model.getBookId());
            }
        });

    }

    private void loadBookDetails(ModelBook model, BookFavouriteViewHolder holder) {
        String bookId= model.getBookId();
        Log.d(TAG, "loadBookDetails: Book Details of Book ID: "+bookId);

        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book info
                        String bookTitle = ""+snapshot.child("bookTitle").getValue();
                        String description = ""+snapshot.child("bookDescription").getValue();
                        String categoryId = ""+snapshot.child("bookCategoryId").getValue();
                        String bookUrl = ""+snapshot.child("bookUrl").getValue();
                        String timestamp = ""+snapshot.child("bookTimestamp").getValue();
                        String viewsCount = ""+snapshot.child("bookViewCount").getValue();
                        String bookPrice = "" + snapshot.child("bookPrice").getValue();
                        String downloadsCount = ""+snapshot.child("bookDownloadCount").getValue();

                        //set to model
                        model.setBookFavourite(true);
                        model.setBookTitle(bookTitle);
                        model.setBookDescription(description);
                        model.setBookTimestamp(Long.parseLong(timestamp));
                        model.setBookPrice(Integer.parseInt(bookPrice));
                        model.setBookCategoryId(categoryId);
                        model.setBookUrl(bookUrl);

                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(categoryId,holder.categoryTv);
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl,bookTitle,holder.pdfView,
                                holder.progressBar,null);{
                        }
                        MyApplication.loadPdfSize(""+bookUrl,""+bookTitle,holder.sizeTv);

                        //set data to views
                        holder.titleTv.setText(bookTitle);
                        holder.descriptionTv.setText(description);
                        holder.dateTv.setText(date);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        if(arrayList_book != null){
            return arrayList_book.size();
        }
        return 0;
    }

    public class BookFavouriteViewHolder extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageView removeFavBtn;
        public BookFavouriteViewHolder(@NonNull View itemView) {
            super(itemView);

            //init ui views of row_pdf_favourite.xml
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            removeFavBtn = binding.removeFavBtn;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;

            categoryTv.setSelected(true);
        }
    }

}
