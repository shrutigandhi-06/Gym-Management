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
import android.text.Editable;
import android.text.TextWatcher;
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

public class edit_client extends AppCompatActivity {

    EditText name, phone, email, plan, join_date, due_date, amount, paid_amt, due_amt;
    Button save_changes;
    ProgressBar progressBar,DP_progressbar;

    ImageView edt_client_DP;
    Uri uriProfile_image;
    String profile_imageURL;
    private static final int CHOOSE_IMAGE = 101;


    Toolbar toolbar;

    Intent fragment_intent;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;

   String c_name, c_phone, c_email, c_plan, c_join_date, c_due_date, c_amount, c_paid_amt, c_due_amt, c_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_client);

        fragment_intent = new Intent(edit_client.this, fragment_main.class);
        String i = "client";
        fragment_intent.putExtra("flag", i);

        Intent intent= getIntent();

        toolbar = findViewById(R.id.edit_client_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        name = findViewById(R.id.edt_c_name);
        phone = findViewById(R.id.edt_c_phone);
        email = findViewById(R.id.edt_c_email);
        plan = findViewById(R.id.edt_c_plan);
        join_date = findViewById(R.id.edt_c_join_date);
        due_date= findViewById(R.id.edt_c_due_date);
        amount = findViewById(R.id.edt_c_amount);
        paid_amt= findViewById(R.id.edt_c_amtpaid);
        due_amt= findViewById(R.id.edt_c_amtdue);
        save_changes = findViewById(R.id.btn_c_save_changes);
        progressBar = findViewById(R.id.c_progress_bar);

        edt_client_DP = findViewById(R.id.edt_client_DP);
        DP_progressbar = findViewById(R.id.edt_c_progressbar);

        c_name = intent.getStringExtra("name");
        c_phone = intent.getStringExtra("phone");
        c_email = intent.getStringExtra("email");
        c_plan = intent.getStringExtra("plan");
        c_join_date = intent.getStringExtra("join_date");
        c_due_date = intent.getStringExtra("due_date");
        c_amount = intent.getStringExtra("amount");
        c_paid_amt = intent.getStringExtra("paid_amt");
        c_due_amt = intent.getStringExtra("due_amt");
        c_uri = intent.getStringExtra("uri");

        name.setText(c_name);
        phone.setText(c_phone);
        email.setText(c_email);
        plan.setText(c_plan);
        join_date.setText(c_join_date);
        due_date.setText(c_due_date);
        amount.setText(c_amount);
        paid_amt.setText(c_paid_amt);
        due_amt.setText(c_due_amt);
        Picasso.get().load(c_uri).into(edt_client_DP);

        paid_amt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String amt = amount.getText().toString();
                String paid = paid_amt.getText().toString();
                String due;

                if(charSequence.length() != 0)
                {
                    due = (Long.parseLong(amt) - Long.parseLong(paid))+"";
                    if(Long.parseLong(due)<0)
                        due_amt.setText(0);
                    else
                        due_amt.setText(due);
                }
                else
                    due_amt.setText(amt);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        save_changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clientSaveChanges();
            }
        });

        edt_client_DP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
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
                edt_client_DP.setImageBitmap(bitmap);
                uploadImage();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {

        DP_progressbar.setVisibility(View.VISIBLE);
        save_changes.setEnabled(false);
        save_changes.setAlpha(.5f);
        final StorageReference profile_imageREF = FirebaseStorage.getInstance().getReference("profilePics/clients_DP/"+System.currentTimeMillis()+".jpg");
        if(uriProfile_image!=null)
        {
            profile_imageREF.putFile(uriProfile_image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(getApplicationContext(),"Looking nice",Toast.LENGTH_SHORT).show();
                    DP_progressbar.setVisibility(View.GONE);
                    save_changes.setEnabled(true);
                    save_changes.setAlpha(1);

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

                    Toast toast = Toast.makeText(edit_client.this,e.getMessage(),Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }
    }

    private void clientSaveChanges() {

        if(progressBar.getVisibility()==View.GONE)
            progressBar.setVisibility(View.VISIBLE);

        if(phone.getText().toString().isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            phone.setError("This field is required");
            phone.requestFocus();
            return;
        }
        if((phone.getText().toString().length()) < 10)
        {
            progressBar.setVisibility(View.GONE);
            phone.setError("Inappropriate input");
            phone.requestFocus();
            return;
        }
        if(email.getText().toString().isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            email.setError("This field is required");
            email.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches())
        {
            progressBar.setVisibility(View.GONE);
            email.setError("Invalid email address");
            email.requestFocus();
            return;
        }
        if(paid_amt.getText().toString().isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            paid_amt.setError("This field is required");
            paid_amt.requestFocus();
            return;
        }
        if(due_amt.getText().toString().isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            due_amt.setError("This field is required");
            due_amt.requestFocus();
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
                        Toast toast = Toast.makeText(edit_client.this, "You're good to go!!!",Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });
        }

        String userID = mAuth.getUid();
        DocumentReference documentReference = firebaseFirestore.collection(userID).document("user info").collection("clients").document(c_name.toLowerCase());

        HashMap<String, Object> save_client_changes = new HashMap<>();
        save_client_changes.put("name", c_name.toLowerCase());
        save_client_changes.put("phone", phone.getText().toString());
        save_client_changes.put("email", email.getText().toString().trim());
        save_client_changes.put("plan", plan.getText().toString());
        save_client_changes.put("amount", amount.getText().toString());
        save_client_changes.put("join_date", join_date.getText().toString());
        save_client_changes.put("due_date", due_date.getText().toString());
        save_client_changes.put("amt_paid", paid_amt.getText().toString());
        save_client_changes.put("amt_due", due_amt.getText().toString());
        if(profile_imageURL!=null)
        {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(c_uri);
            if(!(c_uri.equals("https://firebasestorage.googleapis.com/v0/b/gym-management-d98ff.appspot.com/o/profilePics%2Fdefault_photo.png?alt=media&token=4ebef1e3-55cc-47e4-acf9-3bb027c6b90c")))
                storageReference.delete();
            Log.d("TAG", "previous photo deleted");
            save_client_changes.put("uri", profile_imageURL);
        }
        else
            save_client_changes.put("uri", c_uri);

        documentReference.set(save_client_changes).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(getApplicationContext(), "Changes saved", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        finish();
        startActivity(fragment_intent);
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
        startActivity(fragment_intent);
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
        startActivity(fragment_intent);
        return super.onOptionsItemSelected(item);
    }
}