package com.example.appreadbook.Book;

import static com.example.appreadbook.Constant.DATABASE_NAME;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.appreadbook.Adapter.AdapterBookAdmin;
import com.example.appreadbook.Adapter.AdapterBookUser;
import com.example.appreadbook.Admin.AdminBookListActivity;
import com.example.appreadbook.Model.ModelBook;
import com.example.appreadbook.User.UserBasicDashboardActivity;
import com.example.appreadbook.databinding.FragmentBookUserBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookUserFragment extends Fragment {

    //that we passed while we creating instance of this fragment
    private String categoryId;
    private String category;

    private ArrayList<ModelBook> pdfArrayList,bookFilter;
    private AdapterBookUser adapterBookUser;

    private FragmentBookUserBinding binding;

    private static final String TAG = "BOOKS_USER_TAG";

    public BookUserFragment() {
        // Required empty public constructor
    }

    public static BookUserFragment newInstance(String categoryId, String category) {
        BookUserFragment fragment = new BookUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("categoryName", category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("categoryName");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate/bind the layout for this fragment
        binding = FragmentBookUserBinding.inflate(LayoutInflater.from(getContext()),container,false);

        Log.d(TAG, "onCreateView: Category: "+category);
        if(category.equals("All")){
            //load all books
            loadAllBooks();
        }
        else if(category.equals("Most Viewed")){
            loadMostViewedDownloadedBook("bookViewCount");

        }
        else if(category.equals("Most Downloaded")){
            loadMostViewedDownloadedBook("bookDownloadCount");
        }
        else {
            //load selected category books
            loadCategorizeBooks();

        }
        //Search
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

        return binding.getRoot();
    }

    private void loadBookListByName(CharSequence query) {
        bookFilter = new ArrayList<>();
        String query2 = query.toString().toUpperCase();

        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
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
                        adapterBookUser = new AdapterBookUser(getContext(), bookFilter);
                        binding.booksRv.setAdapter(adapterBookUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllBooks() {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    ModelBook model = ds.getValue(ModelBook.class);
                    //add to list
                    pdfArrayList.add(model);
                }
                //set up adapter
                adapterBookUser = new AdapterBookUser(getContext(),pdfArrayList);
                //set adapter to recyleview
                binding.booksRv.setAdapter(adapterBookUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadMostViewedDownloadedBook(String orderBy) {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.orderByChild(orderBy).limitToLast(10) //load most viewed or downloaded books
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    ModelBook model = ds.getValue(ModelBook.class);
                    //add to list
                    pdfArrayList.add(model);
                }
                //set up adapter
                adapterBookUser = new AdapterBookUser(getContext(),pdfArrayList);
                //set adapter to recyleview
                binding.booksRv.setAdapter(adapterBookUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadCategorizeBooks() {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Books");
        ref.orderByChild("bookCategoryId").equalTo(categoryId) //load most viewed or downloaded books
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before starting adding data into it
                        pdfArrayList.clear();
                        for(DataSnapshot ds:snapshot.getChildren()){
                            //get data
                            ModelBook model = ds.getValue(ModelBook.class);
                            //add to list
                            pdfArrayList.add(model);
                        }
                        //set up adapter
                        adapterBookUser = new AdapterBookUser(getContext(),pdfArrayList);
                        //set adapter to recyleview
                        binding.booksRv.setAdapter(adapterBookUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}