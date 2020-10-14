package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class add_client extends AppCompatActivity {

    EditText edt_name, edt_phone, edt_join_date, edt_due_date, edt_amount, edt_amt_paid, edt_amt_due, edt_email;
    Spinner plan;
    Button client_save;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;
    ProgressBar progressBar,DP_progressbar;
    String userID, client_plan = null;

    ImageView imageView;
    Uri uriProfile_image;
    String profile_imageURL;
    private static final int CHOOSE_IMAGE = 101;

    ArrayList<String> client_names;

    Toolbar toolbar;

    Intent intent;

    static int m2, d2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_client);

        intent = new Intent(add_client.this, fragment_main.class);
        String i = "client";
        intent.putExtra("flag", i);

        toolbar = findViewById(R.id.add_client_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.client_DP);
        DP_progressbar = findViewById(R.id.add_c_progress_bar);

        edt_name = findViewById(R.id.edt_client_name);
        edt_phone = findViewById(R.id.edt_client_phone);
        edt_email = findViewById(R.id.edt_client_email);
        edt_join_date = findViewById(R.id.edt_client_join_date);
        edt_due_date = findViewById(R.id.edt_client_due_date);
        edt_amount = findViewById(R.id.edt_client_amount);
        edt_amt_paid = findViewById(R.id.edt_client_amtpaid);
        edt_amt_due = findViewById(R.id.edt_client_amtdue);

        plan = findViewById(R.id.spinner_client_plan);
        final ArrayList<String> plans = new ArrayList<>();
        plans.add("Select plan");
        plans.add("1 Month");
        plans.add("3 Months");
        plans.add("6 Months");
        plans.add("1 Year");
        plans.add("3 Years");
        plans.add("5 Years");

        client_save = findViewById(R.id.btn_client_save);
        progressBar = findViewById(R.id.client_progress_bar);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        userID = mAuth.getUid();

        client_names = new ArrayList<>();
        firebaseFirestore.collection(userID).document("user info").collection("clients").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot documentSnapshot:task.getResult())
                    {
                        documentSnapshot.getData();
                        client_names.add(documentSnapshot.get("name")+"");
                    }
                }
            }
        });

        edt_amt_paid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String amt = edt_amount.getText().toString();
                String paid = edt_amt_paid.getText().toString();
                String due;
                if(charSequence.length() != 0)
                {
                    due = (Long.parseLong(amt) - Long.parseLong(paid))+"";
                    if(Long.parseLong(due)>=0)
                        edt_amt_due.setText(due+"");
                    else
                       edt_amt_due.setText("-");
                }
                else
                    edt_amt_due.setText(amt);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        plan.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, plans));
        plan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                client_plan = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        client_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addClient();
            }
        });

        final Calendar calendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener j_date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "dd/MM/yy";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(myFormat, Locale.US);

                edt_join_date.setText(simpleDateFormat.format(calendar.getTime()));

                int d ,m ,y, x;
                String c_plan = client_plan;

                String date = simpleDateFormat.format(calendar.getTime());

                d = Integer.parseInt(date.substring(0,2));
                m = Integer.parseInt(date.substring(3,5));
                y = Integer.parseInt(date.substring(6,8));

                Log.d("TAG", date);
                Log.d("TAG", "date: "+d);
                Log.d("TAG", "month: "+m);
                Log.d("TAG", "year: "+y);


                if(c_plan.equals("1 Month"))
                    x = 30;
                else if(c_plan.equals("3 Months"))
                    x = 90;
                else if(c_plan.equals("6 Months"))
                    x = 180;
                else if(c_plan.equals("1 Year"))
                    x = 365;
                else if(c_plan.equals("3 Years"))
                    x = 1095;
                else
                    x = 1825;

                    Log.d("TAG",addDays(d,m,y,x));
                    edt_due_date.setText(addDays(d,m,y,x));
            }
        };

        edt_join_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(client_plan.equals("Select plan"))
                {
                    TextView errorText = (TextView)plan.getSelectedView();
                    errorText.setError("");
                    errorText.setTextColor(Color.RED);//just to highlight that this is an error
                    errorText.setText("Select plan");
                }
                else
                    new DatePickerDialog(add_client.this, j_date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });
    }

    private void showImageChooser() {

        Intent img_choose_intent = new Intent();
        img_choose_intent.setType("image/*");
        img_choose_intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(img_choose_intent,"Select profile image"),CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CHOOSE_IMAGE && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            uriProfile_image = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uriProfile_image);
                imageView.setImageBitmap(bitmap);
                uploadImage();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {

        DP_progressbar.setVisibility(View.VISIBLE);
        client_save.setEnabled(false);
        client_save.setAlpha(0.5f);

        final StorageReference profile_imageREF = FirebaseStorage.getInstance().getReference("profilePics/clients_DP/"+System.currentTimeMillis()+".jpg");
        if(uriProfile_image!=null)
        {
            profile_imageREF.putFile(uriProfile_image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(getApplicationContext(),"Looking nice",Toast.LENGTH_SHORT).show();
                    DP_progressbar.setVisibility(View.GONE);
                    client_save.setEnabled(true);
                    client_save.setAlpha(1);

                    profile_imageREF.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            profile_imageURL = String.valueOf(uri);
                            Log.d("TAG", profile_imageURL);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast toast = Toast.makeText(add_client.this,e.getMessage(),Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }
    }

    private void addClient() {

        String c_name = edt_name.getText().toString().toLowerCase();
        String c_phone = edt_phone.getText().toString();
        String c_email = edt_email.getText().toString().trim();
        String c_amount = edt_amount.getText().toString();
        String c_amt_paid = edt_amt_paid.getText().toString();
        String c_amt_due = edt_amt_due.getText().toString();
        String c_join_date = edt_join_date.getText().toString();
        String c_due_date = edt_due_date.getText().toString();
        boolean client_collision = false;

        if(progressBar.getVisibility()==View.GONE)
            progressBar.setVisibility(View.VISIBLE);

        if(edt_name.getText().toString().isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            edt_name.setError("Please enter name");
            edt_name.requestFocus();
            return;
        }
        try
        {
            for(int i=0;i<client_names.size();i++)
            {
                String c_names = client_names.get(i);
                if(edt_name.getText().toString().toLowerCase().equals(c_names))
                {
                    client_collision = true;
                }
            }
            if(client_collision)
            {
                progressBar.setVisibility(View.GONE);
                edt_name.setError("Client already exists");
                edt_name.requestFocus();
                return;
            }
        }
        catch (Exception e)
        {

        }

        if(edt_phone.getText().toString().isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            edt_phone.setError("Please enter phone no.");
            edt_phone.requestFocus();
            return;
        }
        if(edt_phone.getText().toString().length() < 10)
        {
            progressBar.setVisibility(View.GONE);
            edt_phone.setError("Invalid contact number");
            edt_phone.requestFocus();
            return;
        }
        if(edt_email.getText().toString().isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            edt_email.setError("Please enter name");
            edt_email.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(edt_email.getText().toString().trim()).matches())
        {
            progressBar.setVisibility(View.GONE);
            edt_email.setError("Invalid email address");
            edt_email.requestFocus();
            return;
        }
        if(edt_join_date.getText().toString().isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            edt_join_date.setError("Please enter join date");
            edt_join_date.requestFocus();
            return;
        }
        if(edt_due_date.getText().toString().isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            edt_due_date.setError("Please enter due date");
            edt_due_date.requestFocus();
            return;
        }
        if(client_plan.equals("Select plan"))
        {
            progressBar.setVisibility(View.GONE);
            TextView errorText = (TextView)plan.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText("Please select plan");
            return;
        }
        if(edt_amount.getText().toString().isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            edt_amount.setError("Please enter amount");
            edt_amount.requestFocus();
            return;
        }
        if(edt_amt_paid.getText().toString().isEmpty())
        {
            progressBar.setVisibility(View.GONE);
            edt_amt_paid.setError("Please enter paid amount");
            edt_amt_paid.requestFocus();
            return;
        }


        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if(profile_imageURL!=null)
        {

            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(profile_imageURL)).build();
            firebaseUser.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast toast = Toast.makeText(add_client.this, "You're good to go!!!",Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });
        }

        DocumentReference documentReference = firebaseFirestore.collection(userID).document("user info").collection("clients").document(c_name.trim());
        HashMap<Object, String> save_client = new HashMap<>();
        save_client.put("name", c_name);
        save_client.put("phone", c_phone);
        save_client.put("plan", client_plan);
        save_client.put("amount", c_amount);
        save_client.put("amt_paid", c_amt_paid);
        save_client.put("amt_due", c_amt_due);
        save_client.put("email", c_email);
        save_client.put("join_date", c_join_date);
        save_client.put("due_date", c_due_date);
        if(profile_imageURL!=null)
            save_client.put("uri", profile_imageURL);
        else
            save_client.put("uri", "https://firebasestorage.googleapis.com/v0/b/gym-management-d98ff.appspot.com/o/profilePics%2Fdefault_photo.png?alt=media&token=4ebef1e3-55cc-47e4-acf9-3bb027c6b90c");

        documentReference.set(save_client);
        Toast.makeText(getApplicationContext(),"Client Added Successfully",Toast.LENGTH_SHORT).show();
        finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        StorageReference storageReference;
        try {
            if(!(profile_imageURL.equals(null)))
            {
                if(!(profile_imageURL.equals("https://firebasestorage.googleapis.com/v0/b/gym-management-d98ff.appspot.com/o/profilePics%2Fdefault_photo.png?alt=media&token=4ebef1e3-55cc-47e4-acf9-3bb027c6b90c")))
                {
                    Log.d("TAG", "photo deleted (on back pressed)");
                    storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(profile_imageURL);
                    storageReference.delete();
                }
            }
        }
        catch (Exception e)
        {
            Log.d("TAG", e.getMessage());
        }

        finish();
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        StorageReference storageReference;
        try {
            if(!(profile_imageURL.equals(null)))
            {
                if(!(profile_imageURL.equals("https://firebasestorage.googleapis.com/v0/b/gym-management-d98ff.appspot.com/o/profilePics%2Fdefault_photo.png?alt=media&token=4ebef1e3-55cc-47e4-acf9-3bb027c6b90c")))
                {
                    Log.d("TAG", "photo deleted (on back pressed)");
                    storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(profile_imageURL);
                    storageReference.delete();
                }
            }
        }
        catch (Exception e)
        {
            Log.d("TAG", e.getMessage());
        }

        finish();
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    static boolean isLeap(int y) {
        if (y % 100 != 0 && y % 4 == 0 || y % 400 == 0)
            return true;

        return false;
    }

    static int offsetDays(int d, int m, int y){
        int offset = d;

        if(m - 1 == 11)
            offset += 335;
        if(m - 1 == 10)
            offset += 304;
        if(m - 1 == 9)
            offset += 273;
        if(m - 1 == 8)
            offset += 243;
        if(m - 1 == 7)
            offset += 212;
        if(m - 1 == 6)
            offset += 181;
        if(m - 1 == 5)
            offset += 151;
        if(m - 1 == 4)
            offset += 120;
        if(m - 1 == 3)
            offset += 90;
        if(m - 1 == 2)
            offset += 59;
        if(m - 1 == 1)
            offset += 31;

        if (isLeap(y) && m > 2)
            offset += 1;

        return offset;
    }

    static void revoffsetDays(int offset, int y){
        int []month={ 0, 31, 28, 31, 30, 31, 30,
                31, 31, 30, 31, 30, 31 };

        if (isLeap(y))
            month[2] = 29;

        int i;
        for (i = 1; i <= 12; i++)
        {
            if (offset <= month[i])
                break;
            offset = offset - month[i];
        }

        d2 = offset;
        m2 = i;
    }

    static String addDays(int d1, int m1, int y1, int x){
        int offset1 = offsetDays(d1, m1, y1);
        int remDays = isLeap(y1) ? (366 - offset1) : (365 - offset1);
        int y2, offset2 = 0;
        if (x <= remDays)
        {
            y2 = y1;
            offset2 =offset1 + x;
        }
        else
        {
            x -= remDays;
            y2 = y1 + 1;
            int y2days = isLeap(y2) ? 366 : 365;
            while (x >= y2days)
            {
                x -= y2days;
                y2++;
                y2days = isLeap(y2) ? 366 : 365;
            }
            offset2 = x;
        }
        revoffsetDays(offset2, y2);
        String date2;
        if(d2<10)
            date2 = "0"+d2+"/"+m2+"/"+y2;
        else
            date2 = d2+"/"+m2+"/"+y2;

        return date2;
    }

}