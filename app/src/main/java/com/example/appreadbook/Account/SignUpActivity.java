package com.example.appreadbook.Account;

import static com.example.appreadbook.Constant.DATABASE_NAME;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.appreadbook.Admin.AdminCategoryAddActivity;
import com.example.appreadbook.Model.ModelUser;
import com.example.appreadbook.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private static final String TAG="SIGN_UP_TAG";
    private String userId="";
    private String userName="";
    private String userEmail="";
    private String userPassword="";
    private String userConfirmPassword="";
    private long timestamp=0L;
    private FirebaseAuth firebaseAuth;
    private Uri userAvatar = null;
    private String userTypeDefault = "userBasic";
    private int userMoneyDefault = 0;

    private ModelUser modelUser=new ModelUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        firebaseAuth = FirebaseAuth.getInstance();

        binding.backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.imageViewUserAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });
        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               validateData();
            }
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>(){
                @Override
                public void onActivityResult(ActivityResult result) {
                    //used to handle result of gallery intent
                    //get uri of image
                    if(result.getResultCode()== Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: "+userAvatar);
                        Intent data = result.getData();
                        userAvatar = data.getData();
                        Log.d(TAG, "onActivityResult: Picked from gallery "+userAvatar);
                        binding.imageViewUserAvatar.setImageURI(userAvatar);
                    }
                    else {
                        Toast.makeText(SignUpActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }

                }
            });
    private void validateData() {
        userName = binding.nameEt.getText().toString().trim();
        userEmail = binding.emailEt.getText().toString().trim();
        userPassword = binding.passwordEt.getText().toString().trim();
        userConfirmPassword = binding.cpasswordEt.getText().toString().trim();

        int check=0;
        if(TextUtils.isEmpty(userName)) {
            binding.textviewMessage.setText("Please type your name");
            return;
        }
        if(TextUtils.isEmpty(userEmail)){
            binding.textviewMessage.setText("Please type your email");
            return;
        }
        if(TextUtils.isEmpty(userPassword)){
            binding.textviewMessage.setText("Please type your password");
            return;
        }
        if(TextUtils.isEmpty(userConfirmPassword)){
            binding.textviewMessage.setText("Please confirm your password");
            return;
        }
        if(!userConfirmPassword.equals(userPassword)){
            binding.textviewMessage.setText("Confirm password does not match");
            return;
        }
        if(userAvatar != null){
            uploadImage();
        }
        checkExitEmail(userEmail);
    }

    private void uploadImage() {
        String filePathAndName = "ProfileImages/"+firebaseAuth.getUid();

        StorageReference ref = FirebaseStorage.getInstance().getReference(filePathAndName);
        ref.putFile(userAvatar)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedUserAvatar = ""+uriTask.getResult();
                        addUserInfo(uploadedUserAvatar);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to upload image due to "+e.getMessage());
                    }
                });
    }

    private void checkExitEmail(String userEmail) {

        DatabaseReference reference = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Users");
        Query query= reference
                .orderByChild("email")
                .equalTo(userEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Toast.makeText(SignUpActivity.this," Email is used for other account....",Toast.LENGTH_SHORT).show();
                }
                else {
                    createUserAccount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void createUserAccount() {
        Toast.makeText(this, "Creating...", Toast.LENGTH_SHORT).show();
        firebaseAuth.createUserWithEmailAndPassword(userEmail,userPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(SignUpActivity.this, "Sign up successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SignUpActivity.this, ""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addUserInfo(String uploadedUserAvatar) {
        timestamp = System.currentTimeMillis();
        userId =firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId",userId);
        hashMap.put("userName",userName);
        hashMap.put("userEmail",userEmail);
        if(uploadedUserAvatar != null){
            hashMap.put("userAvatar",""+uploadedUserAvatar);
        }
        hashMap.put("userType",userTypeDefault);
        hashMap.put("userMoney",userMoneyDefault);
        hashMap.put("userTimestamp",timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME)
                .getReference("Users");
        ref.child(userId).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(SignUpActivity.this, "Successfully add to database", Toast.LENGTH_SHORT).show();

                //go to Login
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("userEmail",userEmail);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
}

