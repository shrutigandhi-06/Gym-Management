package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class dashboard extends AppCompatActivity {

    TextView total_clients, client_membership_expiring;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        toolbar = findViewById(R.id.dashboard_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        total_clients = findViewById(R.id.dashboard_total_clients);
        client_membership_expiring = findViewById(R.id.dashboard_client_membership_expiring);
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