package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class add_trainer extends AppCompatActivity {

    ProgressBar progressBar,add_dp_progressbar;
    EditText add_name,add_phone,add_address,add_email;
    Button trainer_add;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    FirebaseAuth mAuth;

    SearchableSpinner spinner_blood_grp;
    ArrayList<String> blood_groups, trainers;
    String t_blood_grp;

    ImageView imageView;
    Uri uriProfile_image;
    String profile_imageURL;
    private static final int CHOOSE_IMAGE = 101;

    Toolbar toolbar;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trainer);

        intent = new Intent(add_trainer.this, fragment_main.class);
        String i = "trainer";
        intent.putExtra("flag", i);

        toolbar = findViewById(R.id.add_trainer_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        add_name = findViewById(R.id.edt_add_name);
        add_phone = findViewById(R.id.edt_add_phone);
        add_email = findViewById(R.id.edt_add_email);
        add_address = findViewById(R.id.edt_add_address);
        //add_blood_grp = findViewById(R.id.edt_add_bloodgrp);
        spinner_blood_grp = findViewById(R.id.spinner_blood_grp);
        trainer_add = findViewById(R.id.btn_trainer_save);
        progressBar = findViewById(R.id.progress_bar);
        add_dp_progressbar = findViewById(R.id.add_t_progress_bar);

        imageView = findViewById(R.id.img_trainer_DP);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        trainers = new ArrayList<>();
        firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot documentSnapshot:task.getResult())
                    {
                        documentSnapshot.getData();
                        trainers.add(documentSnapshot.get("name")+"");
                    }
                }
            }
        });

        blood_groups = new ArrayList<>();
        blood_groups.add("Select Blood Group");
        blood_groups.add("B+");
        blood_groups.add("B-");
        blood_groups.add("O+");
        blood_groups.add("O-");
        blood_groups.add("A+");
        blood_groups.add("A-");
        blood_groups.add("AB+");
        blood_groups.add("AB-");

        spinner_blood_grp.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, blood_groups));
        spinner_blood_grp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                t_blood_grp = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        trainer_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTrainer();
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
                imageView.setImageBitmap(bitmap);
                uploadImage();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {

        add_dp_progressbar.setVisibility(View.VISIBLE);
        trainer_add.setEnabled(false);
        trainer_add.setAlpha(0.5f);

        final StorageReference profile_imageREF = FirebaseStorage.getInstance().getReference("profilePics/trainers_DP/"+System.currentTimeMillis()+".jpg");
        if(uriProfile_image!=null)
        {
            profile_imageREF.putFile(uriProfile_image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(getApplicationContext(),"Looking nice",Toast.LENGTH_SHORT).show();
                    add_dp_progressbar.setVisibility(View.GONE);
                    trainer_add.setEnabled(true);
                    trainer_add.setAlpha(1);

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

                    Toast toast = Toast.makeText(add_trainer.this,e.getMessage(),Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }
    }

    private void addTrainer() {

        final String name = add_name.getText().toString();
        String phone = add_phone.getText().toString();
        String email = add_email.getText().toString().trim();
        String address = add_address.getText().toString();
        boolean trainer_collision = false;
        //String blood_grp = add_blood_grp.getText().toString();

        if(progressBar.getVisibility()==View.GONE)
            progressBar.setVisibility(View.VISIBLE);

        if(name.isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            add_name.setError("Name is required");
            add_name.requestFocus();
            return;
        }
        try
        {
            for(int i=0;i<trainers.size();i++)
            {
                String t_names = trainers.get(i);
                if(add_name.getText().toString().toLowerCase().equals(t_names))
                {
                    trainer_collision = true;
                }
            }
            if(trainer_collision)
            {
                progressBar.setVisibility(View.GONE);
                add_name.setError("Trainer already exists");
                add_name.requestFocus();
                return;
            }
        }
        catch (Exception e)
        {

        }

        if(phone.isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            add_phone.setError("Phone number is required");
            add_phone.requestFocus();
            return;
        }
        if(add_phone.getText().toString().length() < 10)
        {
            progressBar.setVisibility(View.GONE);
            add_phone.setError("Invalid contact number");
            add_phone.requestFocus();
            return;
        }
        if(email.isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            add_email.setError("Email is required");
            add_email.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            progressBar.setVisibility(View.GONE);
            add_email.setError("Invalid email address");
            add_email.requestFocus();
            return;
        }
        if(address.isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            add_address.setError("Address is required");
            add_address.requestFocus();
            return;
        }
        if(t_blood_grp.equals("Select Blood Group"))
        {
            progressBar.setVisibility(View.GONE);
            TextView errorText = (TextView)spinner_blood_grp.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText("Select Blood Group");
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
                        Toast toast = Toast.makeText(add_trainer.this, "You're good to go!!!",Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });
        }

        String userID = mAuth.getUid();
        documentReference = firebaseFirestore.collection(userID).document("user info").collection("trainers").document(name.trim().toLowerCase());

        Map<String, Object> add_trainer_data = new HashMap<>();
        add_trainer_data.put("name",add_name.getText().toString().toLowerCase());
        add_trainer_data.put("phone",add_phone.getText().toString());
        add_trainer_data.put("address",add_address.getText().toString());
        add_trainer_data.put("blood_grp",t_blood_grp);
        add_trainer_data.put("email",add_email.getText().toString().trim());
        if(profile_imageURL!=null)
            add_trainer_data.put("uri", profile_imageURL);
        else
            add_trainer_data.put("uri", "https://firebasestorage.googleapis.com/v0/b/gym-management-d98ff.appspot.com/o/profilePics%2Fdefault_photo.png?alt=media&token=4ebef1e3-55cc-47e4-acf9-3bb027c6b90c");

        documentReference.set(add_trainer_data);
        Toast.makeText(getApplicationContext(), "trainer added", Toast.LENGTH_LONG).show();
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

