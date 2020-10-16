package com.example.gymmanagement;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class after_login extends Fragment {

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    String userID;
    String client, trainer_attended;
    String no_total_clients;

    ArrayList<String> clients, trainers;

    Spinner client_spinner, trainer_spinner;

    Button save_record;
    TextView total_clients;
    ProgressBar progress_client;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout rl = (RelativeLayout)inflater.inflate(R.layout.activity_after_login, container, false);

        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        client_spinner = rl.findViewById(R.id.spinner_client_name);
        trainer_spinner = rl.findViewById(R.id.spinner_trainer_attended);
        save_record = rl.findViewById(R.id.btn_record_save);
        //total_clients = rl.findViewById(R.id.txt_no_total_clients);
        //progress_client = rl.findViewById(R.id.progress_client);

        userID = mAuth.getUid();

        clients = new ArrayList<>();
        clients.add("Client Name");

        firebaseFirestore.collection(userID).document("user info").collection("clients").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if(task.isSuccessful())
            {
                for(QueryDocumentSnapshot documentSnapshot:task.getResult())
                {
                    documentSnapshot.getData();
                    clients.add(documentSnapshot.get("name")+"");
                }
            }
            }
        });

        client_spinner.setAdapter(new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, clients));
        client_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                    client = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        trainers = new ArrayList<>();
        trainers.add("Trainer attendant");

        firebaseFirestore.collection(userID).document("user info").collection("trainers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot documentSnapshot:task.getResult())
                    {
                        documentSnapshot.getData();
                        trainers.add(documentSnapshot.get("name")+"");
                    }
                }
            }
        });

        trainer_spinner.setAdapter(new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, trainers));
        trainer_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                    trainer_attended = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        save_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if(client.equals("Client Name"))
            {
                TextView errorText = (TextView)client_spinner.getSelectedView();
                errorText.setError("");
                errorText.setTextColor(Color.RED);//just to highlight that this is an error
                errorText.setText("Client name");
            }
            if(trainer_attended.equals("Trainer attendant"))
            {
                TextView errorText = (TextView)trainer_spinner.getSelectedView();
                errorText.setError("");
                errorText.setTextColor(Color.RED);//just to highlight that this is an error
                errorText.setText("Trainer attendant");
            }
            if(!(trainer_attended.equals("Trainer attendant")) && !(client.equals("Client Name")))
            {
                Log.d("TAG", "temp");
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat= new SimpleDateFormat("dd-MM-yy");
                String date=simpleDateFormat.format(calendar.getTime());
                String time = DateFormat.getTimeInstance().format(calendar.getTime());

                DocumentReference documentReference = firebaseFirestore.collection(userID).document("user info").collection("clients").document(client).collection("sessions").document(time);

                HashMap<String, Object> session_data = new HashMap<>();
                session_data.put("trainer_attended", trainer_attended);
                session_data.put("arrival_time", time);
                session_data.put("date", date);

                documentReference.set(session_data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Data saved", Toast.LENGTH_SHORT).show();
                        client_spinner.setSelection(0);
                        trainer_spinner.setSelection(0);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        client_spinner.setSelection(0);
                        trainer_spinner.setSelection(0);
                    }
                });

                DocumentReference documentReference1 = firebaseFirestore.collection(userID).document("user info").collection("trainers").document(trainer_attended).collection("sessions").document(time);

                HashMap<String, Object> trainer_session_data = new HashMap<>();
                trainer_session_data.put("client_attended", client);
                trainer_session_data.put("t_arrival_time", time);
                trainer_session_data.put("t_arrival_date",date);

                documentReference1.set(trainer_session_data);

            }
            }
        });
        return rl;
    }
}

