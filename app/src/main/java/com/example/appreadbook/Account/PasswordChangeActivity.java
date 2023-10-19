package com.example.appreadbook.Account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.appreadbook.R;
import com.example.appreadbook.databinding.ActivityPasswordChangeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PasswordChangeActivity extends AppCompatActivity {
    private ActivityPasswordChangeBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String oldPassword="",newPassword = "";
    private String userEmail;
    private static final String TAG = "CHANGE_PASS_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordChangeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        loadUnit();

        binding.backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateOldPassword();
                //reLoginUser();


            }
        });

    }

    private void validateOldPassword() {
        oldPassword = binding.currentPasswordEt.getText().toString().trim();

        if(TextUtils.isEmpty(oldPassword)){
            Toast.makeText(this, "Please type your old password", Toast.LENGTH_SHORT).show();
        }
        else {
            reLoginUser();
        }


    }

    private void changeLayoutButton(){
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout_changePassword);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.button_submit,ConstraintSet.TOP,R.id.newPasswordEt,ConstraintSet.BOTTOM,0);
//                constraintSet.connect(R.id.imageView,ConstraintSet.TOP,R.id.check_answer2,ConstraintSet.TOP,0);
        constraintSet.applyTo(constraintLayout);
    }

    private void loadUnit() {
        binding.textViewNewPassword.setVisibility(View.INVISIBLE);
        binding.newPasswordEt.setVisibility(View.INVISIBLE);
        firebaseAuth = FirebaseAuth.getInstance();
        //get current user
        firebaseUser = firebaseAuth.getCurrentUser();


        Intent intent = getIntent();
        userEmail = intent.getStringExtra("userEmail");
        binding.textViewUserEmail.setText(userEmail);

    }
    private void reLoginUser(){
        firebaseAuth.signInWithEmailAndPassword(userEmail,oldPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                binding.textViewNewPassword.setVisibility(View.VISIBLE);
                binding.newPasswordEt.setVisibility(View.VISIBLE);
                binding.currentPasswordEt.setEnabled(false);

                binding.textViewAnnouncement.setText("Vui lòng nhập mật khẩu mới");

                changeLayoutButton();
                binding.submitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        validateNewPassword();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PasswordChangeActivity.this, "Mật khẩu không đúng!!",Toast.LENGTH_LONG).show();
//                binding.progressBar.setVisibility(View.GONE);
//                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });

    }

    private void validateNewPassword() {
        newPassword = binding.newPasswordEt.getText().toString().trim();

        if(TextUtils.isEmpty(newPassword)){
            Toast.makeText(this, "Please type your new password", Toast.LENGTH_SHORT).show();
        }
        else {
            updateUserPassword(newPassword);
        }


    }

    private void updateUserPassword(String newPassword) {
        firebaseUser.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User password updated.");
                            firebaseAuth.signOut();
                            //Toast.makeText(ChangePasswordActivity.this, "Update password successfully", Toast.LENGTH_SHORT).show();
                            alertGoToLogin();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PasswordChangeActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void alertGoToLogin() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate the custom view
        View customView = inflater.inflate(R.layout.goto_login, null);

        // Create the AlertDialog using AlertDialog.Builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PasswordChangeActivity.this);

        // Set the custom view to the AlertDialog
        alertDialogBuilder.setView(customView);


        // Create and show the AlertDialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        Button ButtonYes = customView.findViewById(R.id.button_yes);
        ButtonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                Intent intent = new Intent(PasswordChangeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        alertDialog.show();
    }
}