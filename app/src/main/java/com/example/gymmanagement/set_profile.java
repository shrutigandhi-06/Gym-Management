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

    //private static final int CHOOSE_IMAGE = 101;

    ProgressBar progressBar;
    EditText set_name,set_phone,set_address,set_blood_grp;
    Button button;
    ImageView imageView, default_img;
    //Uri uriProfile_image;
    //String profile_imageURL;

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
        //set_address = findViewById(R.id.edt_reg_address);
        //set_blood_grp = findViewById(R.id.edt_reg_bloodgrp);
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


        /*imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showImageChooser();
            }
        });*/
    }

    private void saveUserData() {

        final String name = set_name.getText().toString();
        String phone = set_phone.getText().toString();
        //String address = set_address.getText().toString();
        //String blood_grp = set_blood_grp.getText().toString();

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
        /*if(address.isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            set_address.setError("Address is required");
            set_address.requestFocus();
            return;
        }
        if(blood_grp.isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            set_blood_grp.setError("Blood group is required");
            set_blood_grp.requestFocus();
            return;
        }*/

        //FirebaseUser firebaseUser = mAuth.getCurrentUser();

        //if(firebaseUser!=null /*&& profile_imageURL!=null*/)
        /*{

            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(name).build();//.setPhotoUri(Uri.parse(profile_imageURL)).build();
            firebaseUser.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast toast = Toast.makeText(set_profile.this, "You're good to go!!!",Toast.LENGTH_SHORT);
                        toast.show();

                        finish();
                        Intent intent = new Intent(set_profile.this, demo.class);
                        startActivity(intent);
                    }
                }
            });
        }*/

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
                        //save_trainer_data.put("address", set_address.getText().toString());
                        //save_trainer_data.put("blood_grp", set_blood_grp.getText().toString());
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

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CHOOSE_IMAGE && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            uriProfile_image = data.getData();
            try {

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uriProfile_image);
                    imageView.setImageBitmap(bitmap);
                    uploadImage();
            } catch (IOException e) {

                    e.printStackTrace();
            }
        }
    }

    private void uploadImage() {

        final StorageReference profile_imageREF = FirebaseStorage.getInstance().getReference("profilePics/"+System.currentTimeMillis()+".jpg");

        if(uriProfile_image!=null)
        {
            profile_imageREF.putFile(uriProfile_image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    profile_imageURL = profile_imageREF.getDownloadUrl().toString();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast toast = Toast.makeText(set_profile.this,e.getMessage(),Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }
    }

    private void showImageChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select profile image"),CHOOSE_IMAGE);
    }*/
}