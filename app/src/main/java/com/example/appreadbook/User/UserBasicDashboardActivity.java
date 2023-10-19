package com.example.appreadbook.User;

import static com.example.appreadbook.Constant.DATABASE_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.appreadbook.Account.ProfileActivity;
import com.example.appreadbook.Book.BookUserFragment;
import com.example.appreadbook.MainActivity;
import com.example.appreadbook.Model.ModelCategory;
import com.example.appreadbook.MyApplication;
import com.example.appreadbook.databinding.ActivityUserBasicDashboardBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class UserBasicDashboardActivity extends AppCompatActivity {
    private ActivityUserBasicDashboardBinding binding;

    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBasicDashboardBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(UserBasicDashboardActivity.this,MainActivity.class));
                finish();
            }
        });

        //handle click, open profile
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserBasicDashboardActivity.this,ProfileActivity.class));
            }
        });



    }
            // clear before adding to list

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            //not logged in
            binding.subTitleTv.setText("Not Logged In");
        }
        else{
            String email = firebaseUser.getEmail();
            binding.subTitleTv.setText(email);
        }
    }

    private void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this);

        categoryArrayList = new ArrayList<>();

        //load bookCategories from firebase
        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("BookCategories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // clear before adding to list
                categoryArrayList.clear();

                //Load bookCategories = static eg. all, most viewed, most downloaded
                // add data to models
                ModelCategory modelAll = new ModelCategory("01","All",1);
                ModelCategory modelMostViewed = new ModelCategory("02","Most Viewed",1);
                ModelCategory modelMostDownloaded = new ModelCategory("03","Most Downloaded",1);

                //add model to list
                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                categoryArrayList.add(modelMostDownloaded);

                //add data to view pager adapter
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+modelAll.getCategoryId(),
                        ""+modelAll.getCategoryName()),
                        modelAll.getCategoryName());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+modelMostViewed.getCategoryId(),
                        ""+modelMostViewed.getCategoryName()
                ),modelMostViewed.getCategoryName());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+modelMostDownloaded.getCategoryId(),
                        ""+modelMostDownloaded.getCategoryName()
                ),modelMostDownloaded.getCategoryName());
                //refresh list


                viewPagerAdapter.notifyDataSetChanged();

                //Now load from firebase
                for(DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    ModelCategory model = ds.getValue(ModelCategory.class);

                    categoryArrayList.add(model);
                    //add data to viewPagerAdapter
                    viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                            ""+model.getCategoryId(),
                            ""+model.getCategoryName()),model.getCategoryName());

                    //refresh list
                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //set adapter to view pager
        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<BookUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context=context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(BookUserFragment fragment, String title){
            //add
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

}