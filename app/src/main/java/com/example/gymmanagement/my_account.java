package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class my_account extends AppCompatActivity {

    EditText user_name, user_phone, user_email;
    ImageButton edit, save;
    Button reset_password, delete_account;

    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    FirebaseAuth mAuth;

    Toolbar toolbar;

    String u_name, u_phone, u_email;
    boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        documentReference = firebaseFirestore.collection(mAuth.getUid()).document("user info");

        toolbar = findViewById(R.id.my_acc_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user_name = findViewById(R.id.user_name);
        user_phone = findViewById(R.id.user_phone);
        user_email = findViewById(R.id.user_email);

        edit = findViewById(R.id.btn_user_edit);
        save = findViewById(R.id.btn_user_save);
        reset_password = findViewById(R.id.btn_user_reset_password);

        delete_account = findViewById(R.id.delete_acc);
        delete_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(my_account.this);

                builder.setTitle("Are you sure you want to delete your account?");
                builder.setMessage("Once the account is deleted the data will be deleted permanently...");
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                delete_account();
                            }
                        });

                builder.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                try
                {
                    user_name.setText(documentSnapshot.getString("name"));
                    user_phone.setText(documentSnapshot.getString("phone"));
                    user_email.setText(documentSnapshot.getString("email"));
                }
                catch (Exception e)
                {

                }
            }
        });


        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                flag = true;
                user_name.setEnabled(true);
                user_phone.setEnabled(true);
                user_email.setEnabled(true);
                save.setVisibility(View.VISIBLE);
                edit.setVisibility(View.GONE);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                flag = false;
                u_name = user_name.getText().toString();
                u_phone = user_phone.getText().toString();
                u_email = user_email.getText().toString();
                HashMap<String, Object> update_info = new HashMap<>();
                update_info.put("name", u_name);
                update_info.put("phone", u_phone);
                update_info.put("email", u_email);
                documentReference.set(update_info).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(my_account.this, "Changes saved", Toast.LENGTH_SHORT).show();
                        user_name.setEnabled(false);
                        user_phone.setEnabled(false);
                        user_email.setEnabled(false);
                        save.setVisibility(View.GONE);
                        edit.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(my_account.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        reset_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.sendPasswordResetEmail(user_email.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Reset Password link sent",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(my_account.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void delete_account()
    {

        firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("clients").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot documentSnapshot:task.getResult())
                    {
                        documentSnapshot.getData();
                        String name = documentSnapshot.get("name")+"";
                        firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("clients").document(name).delete();
                    }
                    Log.d("TAG", "clients deleted");
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful())
                            {
                                for(QueryDocumentSnapshot documentSnapshot:task.getResult())
                                {
                                    documentSnapshot.getData();
                                    String name = documentSnapshot.get("name")+"";
                                    firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").document(name).delete();
                                }
                                Log.d("TAG", "trainers deleted");
                            }
                        }
                    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful())
                            {
                                firebaseFirestore.collection(mAuth.getUid()).document("user info").delete();
                                mAuth.getCurrentUser().delete();
                                //mAuth.signOut();
                                Log.d("TAG","User deleted");
                                finish();
                                startActivity(new Intent(my_account.this, signUp.class));
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        if(flag)
        {
            user_name.setEnabled(false);
            user_phone.setEnabled(false);
            user_email.setEnabled(false);
            save.setVisibility(View.GONE);
            edit.setVisibility(View.VISIBLE);
            flag = false;
        }
        else
        {
            finish();
            startActivity(new Intent(my_account.this, dashboard.class));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        startActivity(new Intent(my_account.this, dashboard.class));
        return super.onOptionsItemSelected(item);
    }
}