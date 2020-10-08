package com.example.gymmanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class signUp extends AppCompatActivity implements View.OnClickListener{

    FirebaseAuth mAuth;

    EditText signUp_email,signUp_password,confirm_password;
    Button signUp_button;
    TextView back_to_login;

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        imageView = findViewById(R.id.imageView);

        signUp_button = findViewById(R.id.btn_signUp);
        signUp_email = findViewById(R.id.edt_Semail);
        signUp_password = findViewById(R.id.edt_Spassword);
        back_to_login = findViewById(R.id.txt_back2login);
        confirm_password = findViewById(R.id.edt_confirm_Spassword);

        findViewById(R.id.txt_back2login).setOnClickListener(this);
        findViewById(R.id.btn_signUp).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_signUp:{
                registerUser();
                break;
            }
            case R.id.txt_back2login:{

                finish();
                startActivity(new Intent(signUp.this, MainActivity.class));
                break;
            }
        }
    }

    private void registerUser() {

        String email = signUp_email.getText().toString().trim();
        String password = signUp_password.getText().toString().trim();
        String c_password = confirm_password.getText().toString();

        if(email.isEmpty()){

            signUp_email.setError("email address is required");
            signUp_email.requestFocus();
            return;
        }
        if(password.isEmpty()){

            signUp_password.setError("password is required");
            signUp_password.requestFocus();
            return;
        }
        if(c_password.isEmpty()){

            signUp_password.setError("confirm your password");
            signUp_password.requestFocus();
            return;
        }
        if(!(password.equals(c_password)))
        {
            confirm_password.setError("password doesn't match");
            confirm_password.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            signUp_email.setError("Invalid email address");
            signUp_email.requestFocus();
            return;
        }
        if(password.length()<6){

            signUp_password.setError("Minimum password length should be six");
            signUp_password.requestFocus();
            return;
        }

        Intent intent = new Intent(signUp.this, set_profile.class);
        intent.putExtra("set_email",signUp_email.getText().toString());
        intent.putExtra("set_password",signUp_password.getText().toString());
        finish();
        startActivity(intent);
    }
}