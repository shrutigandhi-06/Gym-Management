package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class set_profile extends AppCompatActivity {

    ProgressBar progressBar;
    EditText set_name,set_phone;
    Button button;
    ImageView imageView, default_img;

    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    FirebaseAuth mAuth;

    String set_email,set_password;

    LinearLayout linearLayout;
    ConstraintLayout constraintLayout;
    AnimatedVectorDrawable vectorDrawable;
    AnimatedVectorDrawableCompat vectorDrawableCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        set_name = findViewById(R.id.edt_reg_name);
        set_phone = findViewById(R.id.edt_reg_phone);
        default_img = findViewById(R.id.img_default);
        button = findViewById(R.id.btn_reg_done);
        progressBar = findViewById(R.id.progress_bar);

        linearLayout = findViewById(R.id.linear_layout);
        constraintLayout = findViewById(R.id.constraint_layout);
        imageView = findViewById(R.id.imageView);

        Intent intent = getIntent();
        set_email = intent.getStringExtra("set_email");
        set_password = intent.getStringExtra("set_password");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveUserData();
            }
        });
    }

    private void saveUserData() {

        final String name = set_name.getText().toString();
        String phone = set_phone.getText().toString();

        if(progressBar.getVisibility()==View.GONE)
            progressBar.setVisibility(View.VISIBLE);

        if(name.isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            set_name.setError("Name is required");
            set_name.requestFocus();
            return;
        }
        if(phone.isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            set_phone.setError("Phone number is required");
            set_phone.requestFocus();
            return;
        }
        if(phone.length()<10)
        {
            progressBar.setVisibility(View.GONE);
            set_phone.setError("Invalid phone number");
            set_phone.requestFocus();
            return;
        }

        //setting up account

        mAuth.createUserWithEmailAndPassword(set_email, set_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

            if(task.isSuccessful())
            {
                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull final Task<Void> task) {

                    if (task.isSuccessful())
                    {
                        String userID = mAuth.getUid();
                        documentReference = firebaseFirestore.collection(userID).document("user info");

                        Map<String, Object> save_trainer_data = new HashMap<>();
                        save_trainer_data.put("name", set_name.getText().toString());
                        save_trainer_data.put("phone", set_phone.getText().toString());
                        save_trainer_data.put("email", set_email);

                        documentReference.set(save_trainer_data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("TAG", "details saved");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("TAG", "error in saving details");
                            }
                        });

                        progressBar.setVisibility(View.GONE);
                        Toast toast = Toast.makeText(getApplicationContext(), "Verification email sent", Toast.LENGTH_SHORT);
                        toast.show();

                        linearLayout.setVisibility(View.GONE);
                        default_img.setVisibility(View.GONE);
                        button.setVisibility(View.GONE);
                        TransitionManager.beginDelayedTransition(constraintLayout, new AutoTransition());
                        constraintLayout.setVisibility(View.VISIBLE);
                        Drawable drawable = imageView.getDrawable();
                        if(drawable instanceof AnimatedVectorDrawableCompat)
                        {
                            vectorDrawableCompat = (AnimatedVectorDrawableCompat) drawable;
                            vectorDrawableCompat.start();
                        }
                        else if(drawable instanceof AnimatedVectorDrawable)
                        {
                            vectorDrawable = (AnimatedVectorDrawable) drawable;
                            vectorDrawable.start();
                        }
                        MainActivity mainActivity = new MainActivity();
                        mainActivity.new_user = true;
                        mAuth.getInstance().signOut();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(set_profile.this, MainActivity.class);
                                finish();
                                startActivity(intent);
                            }
                        },3000);
                    }
                    else
                    {
                        progressBar.setVisibility(View.GONE);
                        Toast toast = Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG);
                        toast.show();
                    }
                    }
                });
            }
            else
            {
                if (task.getException() instanceof FirebaseAuthUserCollisionException)
                {
                    progressBar.setVisibility(View.GONE);
                    Toast toast = Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_LONG);
                    toast.show();
                    mAuth.getInstance().signOut();
                }
                else
                {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            }
        });
        //end here
    }
}