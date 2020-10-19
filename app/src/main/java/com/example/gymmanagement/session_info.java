package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfBody;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


public class session_info extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;

    FirestoreRecyclerAdapter adapter;

    Toolbar toolbar;

    Spinner c_name_spinner;
    ArrayList<String> c_names = new ArrayList<>();

    Intent from_sessions;

    RecyclerView recyclerView;

    String selected_client;

    ArrayList<String> sessions_information;
    int cnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_info);

        Intent intent = getIntent();
        final String client_name = intent.getStringExtra("client name");
        selected_client = client_name;

        c_name_spinner = findViewById(R.id.c_name_spinner);
        c_names.add(client_name);

        firebaseFirestore= FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String userID = mAuth.getUid();

        recyclerView = findViewById(R.id.session_recycler_view);
        adapter_class();

        firebaseFirestore.collection(userID).document("user info").collection("clients").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot documentSnapshot:task.getResult())
                    {
                        documentSnapshot.getData();
                        if(!(documentSnapshot.get("name").equals(client_name)))
                            c_names.add(documentSnapshot.get("name").toString()+"");
                    }
                }
            }
        });

        c_name_spinner.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.custom_spinner, c_names));
        c_name_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selected_client = adapterView.getItemAtPosition(position).toString();
                sessions_information.clear();
                adapter_class();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        from_sessions = new Intent(session_info.this, fragment_main.class);
        String i = "client";
        from_sessions.putExtra("flag", i);

        sessions_information = new ArrayList<>();

        toolbar = findViewById(R.id.sessions_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void savePDF() {
        String pdf_format = "Date               Title                   Trainer attendant";
        String new_line = "\n";
        Document doc = new Document();
        String mfile = selected_client+"'s sessions info";
        String mfilepath = Environment.getExternalStorageDirectory().getPath()+"/Download"+"/"+mfile+".pdf";
        Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN,12,Font.BOLD);
        Font bigBold = new Font(Font.FontFamily.TIMES_ROMAN,20,Font.BOLD);
        try
        {
            PdfWriter.getInstance(doc,new FileOutputStream(mfilepath));
            doc.open();
            doc.add(new Paragraph(selected_client.toUpperCase(),bigBold));
            doc.add(new Paragraph(new_line,smallBold));
            doc.add(new Paragraph(pdf_format,smallBold));
            doc.add(new Paragraph(new_line,smallBold));
            for(int i = 0; i<sessions_information.size();i++)
            {
                doc.add(new Paragraph(sessions_information.get(i),smallBold));
                cnt++;
            }
            doc.close();
            Log.d("sessions", cnt + "after for loop");
            Toast.makeText(this, ""+mfile+".pdf"+" is saved to "+mfilepath, Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            Toast.makeText(this,"This is Error msg : " +e.getMessage(), Toast.LENGTH_LONG).show();
        }

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

    public void adapter_class() {
        Log.d("TAG", selected_client);
        Query query = firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("clients").document(selected_client).collection("sessions").orderBy("date", Query.Direction.DESCENDING);

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
                String per_session = model.getDate() + "         " + model.getArrival_time() + "         " + model.getTrainer_attended()+"\n";
                sessions_information.add(per_session);
                Log.d("TAG", "position");
                //holder.Sr_no.setText(position+1+"");
                holder.date.setText(model.getDate());
                holder.time.setText(model.getArrival_time());
                holder.trainer_attended.setText(model.getTrainer_attended());
            }
        };

        adapter.startListening();
        recyclerView.setLayoutManager(new LinearLayoutManager(session_info.this));
        recyclerView.setAdapter(adapter);
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
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.download_session_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.download_session_info)
        {
            adapter_class();
            savePDF();
            Log.d("sessions", sessions_information.toString());
            sessions_information.clear();
            Log.d("sessions", sessions_information.toString()+"removed all "+cnt);
            cnt = 0;
        }
        else
        {
            finish();
            startActivity(from_sessions);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(from_sessions);
    }
}