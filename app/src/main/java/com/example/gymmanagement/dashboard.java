package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class dashboard extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;

    ArrayList<String> clients, trainers;
    String no_total_clients, no_total_trainers;
    ProgressBar progress_client,progress_trainer;
    TextView total_clients,total_trainers;
    CardView card_membership_expiry,total_clients_card, total_trainers_card, my_account_card;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        progress_client = findViewById(R.id.progress_client);
        progress_trainer = findViewById(R.id.progress_trainer);
        total_clients = findViewById(R.id.total_clients);
        total_trainers = findViewById(R.id.total_trainers);

        total_clients_card = findViewById(R.id.total_clients_card);
        total_clients_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(dashboard.this, fragment_main.class);
                String i = "client";
                intent.putExtra("flag", i);
                startActivity(intent);
            }
        });

        total_trainers_card = findViewById(R.id.total_trainers_card);
        total_trainers_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(dashboard.this, fragment_main.class);
                String i = "trainer";
                intent.putExtra("flag", i);
                startActivity(intent);
            }
        });

        card_membership_expiry = findViewById(R.id.membership_expiry_card);
        card_membership_expiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(dashboard.this, membership_expiry.class));
            }
        });

        progress_client.setVisibility(View.VISIBLE);
        progress_trainer.setVisibility(View.VISIBLE);
        total_clients.setVisibility(View.GONE);
        total_trainers.setVisibility(View.GONE);

        toolbar = findViewById(R.id.dashboard_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        clients = new ArrayList<>();

        firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("clients").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot documentSnapshot:task.getResult())
                    {
                        documentSnapshot.getData();
                        clients.add(documentSnapshot.get("name")+"");
                    }
                    progress_client.setVisibility(View.GONE);
                    total_clients.setVisibility(View.VISIBLE);
                    no_total_clients = "(" + (clients.size()) + ")";
                    total_clients.setText(no_total_clients);
                }
            }
        });

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
                    progress_trainer.setVisibility(View.GONE);
                    total_trainers.setVisibility(View.VISIBLE);
                    no_total_trainers = "(" + (trainers.size()) + ")";
                    total_trainers.setText(no_total_trainers);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(dashboard.this, fragment_main.class));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        startActivity(new Intent(dashboard.this, fragment_main.class));
        return super.onOptionsItemSelected(item);
    }
}