package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class reset_password extends AppCompatActivity {

    EditText reset_email;
    FirebaseAuth mAuth;
    Button reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();
        reset_email = findViewById(R.id.edt_email_reset);
        reset = findViewById(R.id.btn_reset);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            if(TextUtils.isEmpty(reset_email.getText().toString()))
            {
                Toast.makeText(getApplicationContext(),"please enter valid email address", Toast.LENGTH_SHORT).show();
            }
            else
            {
                mAuth.sendPasswordResetEmail(reset_email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(getApplicationContext(), "reset password link sent", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            }
        });
    }
}