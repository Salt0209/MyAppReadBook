package com.example.appreadbook.Account;

import static com.example.appreadbook.Constant.DATABASE_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.appreadbook.databinding.ActivityPasswordForgotBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class PasswordForgotActivity extends AppCompatActivity {
    private ActivityPasswordForgotBinding binding;
    private static final String TAG="FORGOT_PASS_TAG";
    private FirebaseAuth firebaseAuth;
    private String email="";

    private Handler handler = new Handler();
    private long startTimeMillis;
    private long countdownMillis = 5 * 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordForgotBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        firebaseAuth = FirebaseAuth.getInstance();

        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.gotoLoginTv.setVisibility(View.INVISIBLE);
        binding.backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
        binding.gotoLoginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PasswordForgotActivity.this, LoginActivity.class);
                intent.putExtra("userEmail",email);
                startActivity(intent);
            }
        });


    }

    private void validateData() {
        email =binding.emailEt.getText().toString().trim();

        if (email.isEmpty()){
            Toast.makeText(this, "Enter email....", Toast.LENGTH_SHORT).show();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email format....", Toast.LENGTH_SHORT).show();
        }
        else {
            checkExitEmail(email);
        }
    }

    private void checkExitEmail(String email) {

        DatabaseReference ref = FirebaseDatabase.getInstance(DATABASE_NAME).getReference("Users");
        Query query = ref.orderByChild("userEmail").equalTo(binding.emailEt.getText().toString().trim());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount()>0) {
                    Log.d(TAG, "onDataChange: Tai khoan da ton tai");
                    recoverPassword();
                }
                else {
                    Toast.makeText(PasswordForgotActivity.this, "Tai khoan chua ton tai", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onDataChange: Tai khoan chua ton tai");

                }}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recoverPassword() {
        binding.progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        binding.progressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Toast.makeText(PasswordForgotActivity.this, "Instructions to reset password sent to "+email, Toast.LENGTH_SHORT).show();

                        startTimeMillis = SystemClock.elapsedRealtime();
                        binding.gotoLoginTv.setVisibility(View.VISIBLE);
                        String textViewCountDown = binding.gotoLoginTv.getText().toString();
                        startCountdown(textViewCountDown);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.progressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Toast.makeText(PasswordForgotActivity.this, "Failed to send due to "+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }
    private void startCountdown(String textViewCountDown) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long elapsedTimeMillis = SystemClock.elapsedRealtime() - startTimeMillis;
                long remainingTimeMillis = countdownMillis - elapsedTimeMillis;

                if (remainingTimeMillis <= 0) {
                    // Countdown is complete
                    binding.gotoLoginTv.setText(textViewCountDown+" in "+"0s");
                    Intent intent = new Intent(PasswordForgotActivity.this, LoginActivity.class);
                    intent.putExtra("userEmail",email);
                    startActivity(intent);
                    finish();

                } else {
                    // Calculate remaining seconds
                    int remainingSeconds = (int) (remainingTimeMillis / 1000);

                    // Update the TextView with the remaining seconds
                    binding.gotoLoginTv.setText(textViewCountDown+" in "+remainingSeconds +"s");

                    // Continue the countdown
                    startCountdown(textViewCountDown);
                }
            }
        }, 1000); // Update every 1 second
    }
}