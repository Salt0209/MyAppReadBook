package com.example.appreadbook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appreadbook.Book.BookDetailActivity;
import com.example.appreadbook.Model.ModelBook;
import com.example.appreadbook.MyApplication;
import com.example.appreadbook.databinding.BookPurchaseHistoryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPurchaseHistory extends RecyclerView.Adapter<AdapterPurchaseHistory.PurchaseHistoryHolder>{
    private BookPurchaseHistoryBinding binding;

    private Context context;
    private static final String DATABASE_NAME = "https://appreadbook-8ae8f-default-rtdb.asia-southeast1.firebasedatabase.app";


    public ArrayList<ModelBook> arrayList_purchaseHistory;

    public AdapterPurchaseHistory(Context context, ArrayList<ModelBook> arrayList_purchaseHistory) {
        this.context = context;
        this.arrayList_purchaseHistory = arrayList_purchaseHistory;
    }

    @NonNull
    @Override
    public PurchaseHistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = BookPurchaseHistoryBinding.inflate(LayoutInflater.from(context),parent,false);
        return new PurchaseHistoryHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseHistoryHolder holder, int position) {
        ModelBook modelPurchase =arrayList_purchaseHistory.get(position);

        loadPurchaseHistory(modelPurchase,holder);

        String bookId = modelPurchase.getBookId();


        //handle click -> go to detail
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookDetailActivity.class);
                intent.putExtra("bookId",bookId);
                context.startActivity(intent);
            }
        });
    }

    private void loadPurchaseHistory(ModelBook modelPurchase, PurchaseHistoryHolder holder) {
        if(modelPurchase==null){
            return;
        }
        String bookId = modelPurchase.getBookId();
        long purchaseBookTimestamp = modelPurchase.getBookTimestamp();
        String purchaseDate = MyApplication.formatTimestamp(purchaseBookTimestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.child(bookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String bookId = ""+snapshot.child("bookId").getValue();
                String bookTitle = ""+snapshot.child("bookTitle").getValue();
                modelPurchase.setBookId(bookId);
                modelPurchase.setBookTitle(bookTitle);
                holder.bookId.setText(bookId);
                holder.bookTitle.setText(bookTitle);
                holder.purchaseDate.setText(purchaseDate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        if(arrayList_purchaseHistory != null){
            return arrayList_purchaseHistory.size();
        }
        return 0;
    }

    public class PurchaseHistoryHolder extends RecyclerView.ViewHolder{
        TextView bookId,bookTitle,purchaseDate;
        public PurchaseHistoryHolder(@NonNull View itemView) {
            super(itemView);

            bookId = binding.idTv;
            bookTitle = binding.titleTv;
            purchaseDate = binding.dateTv;





        }
    }

}
