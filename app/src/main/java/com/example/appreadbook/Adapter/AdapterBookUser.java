package com.example.appreadbook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appreadbook.Book.BookDetailActivity;
import com.example.appreadbook.Model.ModelBook;
import com.example.appreadbook.MyApplication;
import com.example.appreadbook.databinding.BookRowUserBinding;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterBookUser extends RecyclerView.Adapter<AdapterBookUser.BookUserHolder>{

    private Context context;
    public ArrayList<ModelBook> pdfArrayList, filterList;

    private BookRowUserBinding binding;

    private static final String TAG = "ADAPTER_PDF_USER_TAG";

    public AdapterBookUser(Context context, ArrayList<ModelBook> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    @NonNull
    @Override
    public BookUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the view
        binding = BookRowUserBinding.inflate(LayoutInflater.from(context),parent,false);

        return new BookUserHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterBookUser.BookUserHolder holder, int position) {
//        Get data, set data, handle click etc

        //get data
        ModelBook model = pdfArrayList.get(position);
        String bookId = model.getBookId();
        String title = model.getBookTitle();
        String description = model.getBookDescription();
        String pdfUrl = model.getBookUrl();
        String categoryId = model.getBookCategoryId();
        long timestamp = model.getBookTimestamp();

        //convert time
        String date = MyApplication.formatTimestamp(timestamp);

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);

        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar,
                null
        );
        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv
        );
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTv
        );

        //handle click, show pdf details activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookDetailActivity.class);
                intent.putExtra("bookId",bookId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size(); //return list size || number of records
    }

    class BookUserHolder extends RecyclerView.ViewHolder{

        TextView titleTv,descriptionTv,categoryTv,sizeTv,dateTv;
        PDFView pdfView;
        ProgressBar progressBar;

        public BookUserHolder(@NonNull View itemView) {
            super(itemView);

            titleTv = binding.textViewBookTitle;
            descriptionTv = binding.textViewBookDescription;
            categoryTv = binding.textViewBookCategory;
            sizeTv = binding.textViewBookSize;
            dateTv = binding.dateTv;
            pdfView = binding.pdfView;
            progressBar = binding.progressBarLoadPdf;
        }
    }
}

