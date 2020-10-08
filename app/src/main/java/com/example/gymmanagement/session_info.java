package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class session_info extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;

    FirestoreRecyclerAdapter adapter;

    Toolbar toolbar;

    TextView c_name;

    Intent from_sessions;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_info);

        Intent intent = getIntent();
        String client_name = intent.getStringExtra("client name");

        c_name = findViewById(R.id.c_name);
        c_name.setText(client_name.toUpperCase());

        firebaseFirestore= FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        from_sessions = new Intent(session_info.this, fragment_main.class);
        String i = "client";
        from_sessions.putExtra("flag", i);

        toolbar = findViewById(R.id.sessions_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.session_recycler_view);

        String userID = mAuth.getUid();

        Query query = firebaseFirestore.collection(userID).document("user info").collection("clients").document(client_name).collection("sessions").orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<sessions_getter_setter> options = new FirestoreRecyclerOptions.Builder<sessions_getter_setter>().setQuery(query, sessions_getter_setter.class).build();

        adapter = new FirestoreRecyclerAdapter<sessions_getter_setter, sessionViewHolder>(options) {
            @NonNull
            @Override
            public sessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sessions, parent, false);
                return new sessionViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull sessionViewHolder holder, int position, @NonNull sessions_getter_setter model) {
                
                Log.d("TAG", "position");
                //holder.Sr_no.setText(position+1+"");
                holder.date.setText(model.getDate());
                holder.time.setText(model.getArrival_time());
                holder.trainer_attended.setText(model.getTrainer_attended());
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(session_info.this));
        recyclerView.setAdapter(adapter);
    }

    private class sessionViewHolder extends RecyclerView.ViewHolder{

        TextView Sr_no, trainer_attended, date, time;
        public sessionViewHolder(@NonNull View itemView) {
            super(itemView);

            //Sr_no = itemView.findViewById(R.id.sr_no);
            trainer_attended = itemView.findViewById(R.id.trainer_attended);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        startActivity(from_sessions);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(from_sessions);
    }
}