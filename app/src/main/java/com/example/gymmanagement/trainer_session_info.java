package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class trainer_session_info extends AppCompatActivity {

    Toolbar t_session_toolbar;
    RecyclerView t_session_recycler_view;

    Spinner t_name_spinner;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;
    FirestoreRecyclerAdapter adapter;

    Intent from_t_session;
    String selected_trainer;
    ArrayList<String> t_names= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_session_info);

        t_name_spinner = findViewById(R.id.t_name_spinner);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        t_session_toolbar = findViewById(R.id.trainer_sessions_toolbar);
        setSupportActionBar(t_session_toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        final String trainer_name = intent.getStringExtra("trainer name");
        selected_trainer = trainer_name;
        t_names.add(trainer_name);

        t_session_recycler_view = findViewById(R.id.trainer_session_recycler_view);
        t_adapter_class();

        from_t_session = new Intent(trainer_session_info.this, fragment_main.class);
        String i = "trainer";
        from_t_session.putExtra("flag", i);

        firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot documentSnapshot:task.getResult())
                    {
                        documentSnapshot.getData();
                        if(!(documentSnapshot.get("name").equals(trainer_name)))
                            t_names.add(documentSnapshot.get("name")+"");
                    }
                }
            }
        });


        t_name_spinner.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.custom_spinner, t_names));
        t_name_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selected_trainer = adapterView.getItemAtPosition(position).toString();
                t_adapter_class();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private class trainerViewHolder extends RecyclerView.ViewHolder{

        TextView date, time, client_attended;

        public trainerViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.t_session_date);
            time = itemView.findViewById(R.id.t_session_time);
            client_attended = itemView.findViewById(R.id.client_attended);
        }
    }

    public void t_adapter_class()
    {
        Query query = firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").document(selected_trainer).collection("sessions").orderBy("t_arrival_date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<trainer_sessions_getter_setter> options = new FirestoreRecyclerOptions.Builder<trainer_sessions_getter_setter>().setQuery(query, trainer_sessions_getter_setter.class).build();

        adapter = new FirestoreRecyclerAdapter<trainer_sessions_getter_setter, trainerViewHolder>(options) {

            @NonNull
            @Override
            public trainerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trainer_sessions, parent,false);
                return new trainerViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull trainerViewHolder holder, int position, @NonNull trainer_sessions_getter_setter model) {

                holder.date.setText(model.getT_arrival_date());
                holder.time.setText(model.getT_arrival_time());
                holder.client_attended.setText(model.getClient_attended());
            }
        };

        adapter.startListening();
        t_session_recycler_view.setLayoutManager(new LinearLayoutManager(trainer_session_info.this));
        t_session_recycler_view.setAdapter(adapter);
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(from_t_session);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        startActivity(from_t_session);
        return super.onOptionsItemSelected(item);

    }
}