package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class edit_trainer extends AppCompatActivity {

    ProgressBar progressBar,edt_t_progressbar;
    EditText edit_name,edit_phone,edit_address,edit_blood_grp,edit_email;
    Button trainer_upgrade;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;

    ImageView trainer_DP;
    Uri uriProfile_image;
    String profile_imageURL;
    private static final int CHOOSE_IMAGE = 101;
    String t_uri;

    Toolbar toolbar;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trainer);

        intent = new Intent(edit_trainer.this, fragment_main.class);
        String i = "trainer";
        intent.putExtra("flag", i);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.edit_trainer_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String t_name = intent.getStringExtra("name");
        String t_phone = intent.getStringExtra("phone");
        String t_email = intent.getStringExtra("email");
        String t_address = intent.getStringExtra("address");
        String t_bloodgrp = intent.getStringExtra("blood_grp");
        t_uri = intent.getStringExtra("uri");

        edit_name = findViewById(R.id.edt_edit_name);
        edit_phone = findViewById(R.id.edt_edit_phone);
        edit_email = findViewById(R.id.edt_edit_email);
        edit_address = findViewById(R.id.edt_edit_address);
        edit_blood_grp = findViewById(R.id.edt_edit_bloodgrp);
        trainer_upgrade = findViewById(R.id.btn_trainer_save_changes);
        progressBar = findViewById(R.id.progress_bar);

        trainer_DP = findViewById(R.id.trainer_edit_img);
        edt_t_progressbar = findViewById(R.id.edt_t_progress_bar);

        edit_name.setText(t_name);
        edit_phone.setText(t_phone);
        edit_email.setText(t_email);
        edit_address.setText(t_address);
        edit_blood_grp.setText(t_bloodgrp);
        Picasso.get().load(t_uri).into(trainer_DP);

        trainer_DP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });

        trainer_upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trainerUpgrade();
            }
        });
    }

    private void showImageChooser() {

        Intent img_choose_intent = new Intent();
        img_choose_intent.setType("image/*");
        img_choose_intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(img_choose_intent,"Select profile image"),CHOOSE_IMAGE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CHOOSE_IMAGE && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            uriProfile_image = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uriProfile_image);
                trainer_DP.setImageBitmap(bitmap);
                uploadImage();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {

        edt_t_progressbar.setVisibility(View.VISIBLE);
        trainer_upgrade.setEnabled(false);
        trainer_upgrade.setAlpha(.5f);
        final StorageReference profile_imageREF = FirebaseStorage.getInstance().getReference("profilePics/trainers_DP/"+System.currentTimeMillis()+".jpg");
        if(uriProfile_image!=null)
        {
            profile_imageREF.putFile(uriProfile_image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(getApplicationContext(),"Looking nice",Toast.LENGTH_SHORT).show();
                    edt_t_progressbar.setVisibility(View.GONE);
                    trainer_upgrade.setEnabled(true);
                    trainer_upgrade.setAlpha(1);

                    profile_imageREF.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            profile_imageURL = String.valueOf(uri);
                            Log.d("TAG", profile_imageURL);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast toast = Toast.makeText(edit_trainer.this,e.getMessage(),Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }
    }

    private void trainerUpgrade() {

        String name = edit_name.getText().toString();
        String phone = edit_phone.getText().toString();
        String email = edit_email.getText().toString();
        String address = edit_address.getText().toString();
        String blood_grp = edit_blood_grp.getText().toString();

        if(progressBar.getVisibility()==View.GONE)
            progressBar.setVisibility(View.VISIBLE);

        if(name.isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            edit_name.setError("Name is required");
            edit_name.requestFocus();
            return;
        }
        if(phone.isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            edit_phone.setError("Phone number is required");
            edit_phone.requestFocus();
            return;
        }
        if(email.isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            edit_email.setError("Email is required");
            edit_email.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            progressBar.setVisibility(View.GONE);
            edit_email.setError("Invalid email address");
            edit_email.requestFocus();
            return;
        }
        if(address.isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            edit_address.setError("Address is required");
            edit_address.requestFocus();
            return;
        }
        if(blood_grp.isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            edit_blood_grp.setError("Blood group is required");
            edit_blood_grp.requestFocus();
            return;
        }

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if(profile_imageURL!=null)
        {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(profile_imageURL)).build();
            firebaseUser.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast toast = Toast.makeText(edit_trainer.this, "You're good to go!!!",Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });
        }

        DocumentReference documentReference;
        String userID = mAuth.getUid();
        documentReference = firebaseFirestore.collection(userID).document("user info").collection("trainers").document(edit_name.getText().toString().trim().toLowerCase());

        Map<String, Object> save_trainer_changes = new HashMap<>();
        save_trainer_changes.put("name",edit_name.getText().toString().toLowerCase());
        save_trainer_changes.put("phone",edit_phone.getText().toString());
        save_trainer_changes.put("address",edit_address.getText().toString());
        save_trainer_changes.put("blood_grp",edit_blood_grp.getText().toString());
        save_trainer_changes.put("email",edit_email.getText().toString());
        if(profile_imageURL!=null)
        {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(t_uri);
            if(!(t_uri.equals("https://firebasestorage.googleapis.com/v0/b/gym-management-d98ff.appspot.com/o/profilePics%2Fdefault_photo.png?alt=media&token=4ebef1e3-55cc-47e4-acf9-3bb027c6b90c")))
                storageReference.delete();
            Log.d("TAG", "previous photo deleted");
            save_trainer_changes.put("uri", profile_imageURL);
        }
        else
            save_trainer_changes.put("uri", t_uri);

        documentReference.set(save_trainer_changes).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Success", "trainer added");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "Error");
            }
        });

        Toast.makeText(getApplicationContext(), "Changes saved",Toast.LENGTH_SHORT).show();
        finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        StorageReference storageReference;
        try {
            if(!(profile_imageURL.equals(null)))
            {
                if(!(profile_imageURL.equals("https://firebasestorage.googleapis.com/v0/b/gym-management-d98ff.appspot.com/o/profilePics%2Fdefault_photo.png?alt=media&token=4ebef1e3-55cc-47e4-acf9-3bb027c6b90c")))
                {
                    Log.d("TAG", "photo deleted (on back pressed)");
                    storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(profile_imageURL);
                    storageReference.delete();
                }
            }
        }
        catch (Exception e)
        {
            Log.d("TAG", e.getMessage());
        }

        finish();
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        StorageReference storageReference;
        try {
            if(!(profile_imageURL.equals(null)))
            {
                if(!(profile_imageURL.equals("https://firebasestorage.googleapis.com/v0/b/gym-management-d98ff.appspot.com/o/profilePics%2Fdefault_photo.png?alt=media&token=4ebef1e3-55cc-47e4-acf9-3bb027c6b90c")))
                {
                    Log.d("TAG", "photo deleted (on back pressed)");
                    storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(profile_imageURL);
                    storageReference.delete();
                }
            }
        }
        catch (Exception e)
        {
            Log.d("TAG", e.getMessage());
        }

        finish();
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }
}
