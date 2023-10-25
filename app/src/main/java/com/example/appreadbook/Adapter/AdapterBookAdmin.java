package com.example.appreadbook.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appreadbook.Admin.AdminBookEditActivity;
import com.example.appreadbook.Book.BookDetailActivity;
import com.example.appreadbook.Model.ModelBook;
import com.example.appreadbook.MyApplication;
import com.example.appreadbook.databinding.BookRowAdminBinding;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterBookAdmin extends RecyclerView.Adapter<AdapterBookAdmin.BookViewHolder>{

    private BookRowAdminBinding binding;

    private Context context;

    public ArrayList<ModelBook> arrayList_book, filterList;

    private static final String TAG = "BOOK_LIST_TAG";

    public AdapterBookAdmin(Context context, ArrayList<ModelBook> arrayList_book) {
        this.context = context;
        this.arrayList_book = arrayList_book;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = BookRowAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new BookViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        ModelBook model = arrayList_book.get(position);
        String bookId = model.getBookId();
        String bookCategoryId = model.getBookCategoryId();
        String bookTitle = model.getBookTitle();
        String bookDescription = model.getBookDescription();
        String pdfUrl = model.getBookUrl();
        long bookTimestamp = model.getBookTimestamp();

        //we need to convert timestamp to dd/MM/yyyy format
        String formatDate = MyApplication.formatTimestamp(bookTimestamp);

        //set data
        holder.bookTitle.setText(bookTitle);
        holder.bookDescription.setText(bookDescription);
        holder.bookDate.setText(formatDate);


        MyApplication.loadCategory(
                ""+bookCategoryId,
                holder.bookCategory);
        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+bookTitle,
                holder.pdfView,
                holder.progressBar_loadPdf,
                null
        );
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+bookTitle,
                holder.bookSize);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookDetailActivity.class);
                intent.putExtra("bookId",bookId);
                context.startActivity(intent);
            }
        });
        holder.buttonOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model,holder);
            }
        });

    }

    private void moreOptionsDialog(ModelBook model, BookViewHolder holder) {
        String bookID = model.getBookId();
        String bookUrl = model.getBookUrl();
        String bookTitle = model.getBookTitle();

        //option to show in dialog
        String[] options = {"Edit", "Delete"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle dialog option click
                        if(which==0){
                            //Edit clicked, Open PdfEditActivity to edit the book info
                            Intent intent = new Intent(context, AdminBookEditActivity.class);
                            intent.putExtra("bookId",bookID);
                            context.startActivity(intent);
                        }
                        else if(which == 1){
                            //Delete clicked
                            MyApplication.deleteBook(
                                    context,
                                    ""+bookID,
                                    ""+bookUrl,
                                    ""+bookTitle
                            );
                        }
                    }
                }).show();
    }

    @Override
    public int getItemCount() {
        if(arrayList_book != null){
            return arrayList_book.size();
        }
        return 0;
    }

    public class BookViewHolder extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar_loadPdf;
        TextView bookTitle, bookDescription, bookCategory, bookSize, bookDate;
        ImageView buttonOption;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar_loadPdf = binding.progressBarLoadPdf;
            bookTitle = binding.textViewBookTitle;
            bookDescription = binding.descriptionTv;
            bookCategory = binding.categoryTv;
            bookSize = binding.sizeTv;
            bookDate = binding.dateTv;
            buttonOption = binding.optionIv;
        }
    }
}
