package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AdditionalUserInfo;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;

    ProgressBar progressBar_MainActivity;
    TextView signUp, forgot_password;
    EditText login_email,login_password;
    Button login_button;

    public static boolean new_user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        signUp = findViewById(R.id.txt_signUp);
        login_email = findViewById(R.id.edt_email);
        login_password = findViewById(R.id.edt_password);
        login_button = findViewById(R.id.btn_login);
        forgot_password = findViewById(R.id.txt_forgot_password);
        progressBar_MainActivity = findViewById(R.id.progress_bar_MainActivity);

        findViewById(R.id.txt_signUp).setOnClickListener(this);
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.txt_forgot_password).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.txt_signUp:
            {
                finish();
                startActivity(new Intent(MainActivity.this, signUp.class));
                break;
            }

            case R.id.btn_login:
            {
                loginUser();
                break;
            }

            case R.id.txt_forgot_password:
            {

                reset_password();
                //startActivity(new Intent(getApplicationContext(), reset_password.class));
            }
        }
    }

    private void loginUser() {

        String email = login_email.getText().toString().trim();
        String password = login_password.getText().toString().trim();

        if(progressBar_MainActivity.getVisibility()==View.GONE)
            progressBar_MainActivity.setVisibility(View.VISIBLE);

        if(email.isEmpty())
        {
            progressBar_MainActivity.setVisibility(View.GONE);
            login_email.setError("email address is required");
            login_email.requestFocus();
            return;
        }
        if(password.isEmpty())
        {
            progressBar_MainActivity.setVisibility(View.GONE);
            login_password.setError("password is required");
            login_password.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            progressBar_MainActivity.setVisibility(View.GONE);
            login_email.setError("Invalid email address");
            login_email.requestFocus();
            return;
        }
        if(password.length()<6)
        {
            progressBar_MainActivity.setVisibility(View.GONE);
            login_password.setError("Minimum password length should be six");
            login_password.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    FirebaseUser user = mAuth.getCurrentUser();

                    Log.d("TAG",user.getEmail());
                    if (user.isEmailVerified())
                    {
                        Log.d("TAG", new_user+"");
                        finish();
                        Toast.makeText(getApplicationContext(), "LogIn Successful", Toast.LENGTH_SHORT).show();
                        if(new_user)
                            startActivity(new Intent(MainActivity.this, app_intro.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        else
                            startActivity(new Intent(MainActivity.this, fragment_main.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }
                    else
                    {
                        progressBar_MainActivity.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Your email was not verified. Please try again with different email address.", Toast.LENGTH_LONG).show();
                        user.delete();
                    }
                }
                else
                {
                    progressBar_MainActivity.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Invalid email or password", Toast.LENGTH_LONG).show();
                    forgot_password.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void reset_password() {

        if(TextUtils.isEmpty(login_email.getText().toString()))
        {
            Toast.makeText(getApplicationContext(),"please enter valid email address", Toast.LENGTH_LONG).show();
        }
        else
        {
            mAuth.sendPasswordResetEmail(login_email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(), "reset password link sent", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null)
        {
            if(mAuth.getCurrentUser().isEmailVerified()) {
                finish();
                startActivity(new Intent(MainActivity.this, fragment_main.class));
            }
        }
    }
}