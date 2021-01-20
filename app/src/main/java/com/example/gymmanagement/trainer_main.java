package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.cketti.mailto.EmailIntentBuilder;

import static com.example.gymmanagement.client_main.c_cnt;

public class trainer_main extends Fragment {

    LinearLayout ll;

    FirebaseFirestore firebaseFirestore;
    FirestoreRecyclerAdapter adapter, s_adapter;
    FirebaseAuth mAuth;
    RecyclerView recyclerView;
    FloatingActionButton add;

    TextView add_trainers;
    ImageView img_add_trainers;
    ArrayList<String> total_trainers;

    static int t_cnt = 0, s_t_cnt = 0;

    byte flag = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ll= (LinearLayout) inflater.inflate(R.layout.activity_trainer_main, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        total_trainers = new ArrayList<>();
        firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot documentSnapshot:task.getResult())
                    {
                        documentSnapshot.getData();
                        total_trainers.add(documentSnapshot.get("name")+"");
                    }
                }
            }
        });


        add_trainers = ll.findViewById(R.id.txt_add_trainers);
        img_add_trainers = ll.findViewById(R.id.img_add_trainers);

        recyclerView = ll.findViewById(R.id.trainer_recycler_view);
        add = ll.findViewById(R.id.btn_trainer_add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), add_trainer.class));
                getActivity().finish();
            }
        });

        final String userID = mAuth.getUid();

        Query query = firebaseFirestore.collection(userID).document("user info").collection("trainers");
        FirestoreRecyclerOptions<trainer_list> options = new FirestoreRecyclerOptions.Builder<trainer_list>().setQuery(query, trainer_list.class).build();

        adapter = new FirestoreRecyclerAdapter<trainer_list, trainerViewHolder>(options) {
            @NonNull
            @Override
            public trainerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trainer_list, parent, false);
                if(!(adapter.getItemCount()==0))
                {
                    add_trainers.setVisibility(View.GONE);
                    img_add_trainers.setVisibility(View.GONE);
                }
                return new trainerViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final trainerViewHolder holder, int position, @NonNull final trainer_list model) {

                t_cnt=adapter.getItemCount();
                Log.d("TAG",t_cnt+"");

                Picasso.get().load(model.getUri()).into(holder.img_trainer_DP);

                holder.name.setText(model.getName().toUpperCase());
                holder.phone.setText(model.getPhone());
                holder.address.setText(model.getAddress());
                holder.blood_grp.setText(model.getBlood_grp());
                holder.email.setText(model.getEmail());

                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (holder.relativeLayout.getVisibility() == View.GONE)
                        {
                            TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                            holder.relativeLayout.setVisibility(View.VISIBLE);
                            holder.trainer_buttons.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            holder.relativeLayout.setVisibility(View.GONE);
                            holder.trainer_buttons.setVisibility(View.GONE);
                        }
                    }
                });

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setMessage("Are you sure you want to delete?");
                        builder.setTitle("Delete Trainer");
                        builder.setCancelable(true);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                                t_cnt--;
                                flag = 0;
                                firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").document(model.getName().toLowerCase()).delete();

                                Log.d("TAG", "user deleted");

                                Snackbar.make(getActivity().findViewById(android.R.id.content), "Trainer deleted", Snackbar.LENGTH_LONG)
                                    .setAction("undo", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d("TAG", "undo");
                                            flag = 1;
                                            t_cnt++;
                                            add_trainers.setVisibility(View.GONE);
                                            img_add_trainers.setVisibility(View.GONE);
                                            firebaseFirestore.collection(userID).document("user info").collection("trainers").document(model.getName().toLowerCase()).set(model);
                                        }
                                    }).show();

                                Log.d("TAG",t_cnt+"");
                                if(t_cnt == 0)
                                {
                                    add_trainers.setVisibility(View.VISIBLE);
                                    img_add_trainers.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    add_trainers.setVisibility(View.GONE);
                                    img_add_trainers.setVisibility(View.GONE);
                                }

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(flag == 0)
                                        {
                                            firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").document(model.getName()).collection("sessions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        for(QueryDocumentSnapshot documentSnapshot1 : task.getResult())
                                                        {
                                                            documentSnapshot1.getData();
                                                            String time = documentSnapshot1.get("t_arrival_time")+"";
                                                            firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").document(model.getName()).collection("sessions").document(time).delete();
                                                        }
                                                        Log.d("TAG", "sessions deleted");
                                                    }
                                                }
                                            });
                                            StorageReference profile_imageREF = FirebaseStorage.getInstance().getReferenceFromUrl(model.getUri());
                                            if(!(model.getUri().equals("https://firebasestorage.googleapis.com/v0/b/gym-management-d98ff.appspot.com/o/profilePics%2Fdefault_photo.png?alt=media&token=4ebef1e3-55cc-47e4-acf9-3bb027c6b90c")))
                                            {
                                                profile_imageREF.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("TAG", "photo deleted");
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }, 3500);

                                holder.relativeLayout.setVisibility(View.GONE);
                                holder.trainer_buttons.setVisibility(View.GONE);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });

                holder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), edit_trainer.class);
                        intent.putExtra("name",holder.name.getText().toString());
                        intent.putExtra("phone",holder.phone.getText().toString());
                        intent.putExtra("email",holder.email.getText().toString());
                        intent.putExtra("address",holder.address.getText().toString());
                        intent.putExtra("blood_grp",holder.blood_grp.getText().toString());
                        intent.putExtra("uri",model.getUri());
                        getActivity().finish();
                        startActivity(intent);
                    }
                });

                holder.trainer_session_info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent s_intent = new Intent(getActivity(),trainer_session_info.class);
                        s_intent.putExtra("trainer name", holder.name.getText().toString().toLowerCase());
                        getActivity().finish();
                        startActivity(s_intent);
                    }
                });
                holder.img_phone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent phone_intent = new Intent(Intent.ACTION_DIAL);
                        phone_intent.setData(Uri.parse("tel:"+model.getPhone()));
                        startActivity(phone_intent);
                    }
                });
                holder.img_email.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent emailIntent = EmailIntentBuilder.from(getActivity())
                                .to(model.getEmail())
                                .build();
                        startActivity(emailIntent);
                    }
                });

                holder.img_share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String trainer_details = "Name: "+model.getName() + "\n" + "Email: "+model.getEmail() + "\n" +"Contact no: "+ model.getPhone()
                                + "\n" + "Blood Group: "+model.getBlood_grp() + "\n" + "Address: "+ model.getAddress();
                        Intent share_intent = new Intent(Intent.ACTION_VIEW);
                        share_intent.setData(Uri.parse("https://api.whatsapp.com/send?phone="+"&text="+trainer_details));
                        startActivity(share_intent);
                    }
                });
            }
        };

        if(t_cnt==0)
        {
            add_trainers.setVisibility(View.VISIBLE);
            img_add_trainers.setVisibility(View.VISIBLE);
        }
        else
        {
            add_trainers.setVisibility(View.GONE);
            img_add_trainers.setVisibility(View.GONE);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        return ll;
    }

    private class trainerViewHolder extends RecyclerView.ViewHolder{

        TextView name, phone, address, blood_grp, email;
        Button delete,edit,trainer_session_info;
        CardView cardView;
        RelativeLayout relativeLayout;
        CircularImageView img_trainer_DP;
        GridLayout trainer_buttons;
        ImageView img_phone, img_email, img_share;

        public trainerViewHolder(@NonNull View itemView) {
            super(itemView);

            img_trainer_DP = itemView.findViewById(R.id.trainer_profile_pic);

            name = itemView.findViewById(R.id.txt_trainer_name);
            phone = itemView.findViewById(R.id.txt_trainer_phoneNo);
            address = itemView.findViewById(R.id.txt_trainer_address);
            blood_grp = itemView.findViewById(R.id.txt_trainer_blood_grp);
            email = itemView.findViewById(R.id.txt_trainer_email);

            cardView = itemView.findViewById(R.id.trainer_card_view);
            relativeLayout = itemView.findViewById(R.id.trainer_relative_layout);
            trainer_buttons = itemView.findViewById(R.id.trainer_buttons);

            delete = itemView.findViewById(R.id.btn_delete);
            edit = itemView.findViewById(R.id.btn_edit);
            trainer_session_info = itemView.findViewById(R.id.btn_trainer_sessions_info);
            img_email = itemView.findViewById(R.id.icon_t_email);
            img_phone = itemView.findViewById(R.id.icon_t_phone);
            img_share = itemView.findViewById(R.id.icon_t_share);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        recyclerView.setAdapter(adapter);
        inflater.inflate(R.menu.search_view, menu);
        MenuItem menuItem = menu.findItem(R.id.bar);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                onSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                onSearch(s);
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void onSearch(String s) {

        TextView no_trainers;
        ImageView img_no_trainers;

        no_trainers = ll.findViewById(R.id.txt_no_trainers);
        img_no_trainers = ll.findViewById(R.id.img_no_trainers);


        String trainer;
        boolean found=false;
        for(int i=0; i<total_trainers.size(); i++)
        {
            trainer = total_trainers.get(i);
            if(trainer.startsWith(s.toLowerCase()))
                found = true;
        }
        if(found)
        {
            Log.d("TAG", "clients found");
            no_trainers.setVisibility(View.GONE);
            img_no_trainers.setVisibility(View.GONE);
        }
        else
        {
            if (t_cnt == 0) {
                add_trainers.setVisibility(View.VISIBLE);
                img_add_trainers.setVisibility(View.VISIBLE);
            }
            else
            {
                Log.d("TAG", "no clients found");
                no_trainers.setVisibility(View.VISIBLE);
                img_no_trainers.setVisibility(View.VISIBLE);
            }
        }

        Query s_query = firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers");
        FirestoreRecyclerOptions<trainer_list> options = new FirestoreRecyclerOptions.Builder<trainer_list>().setQuery(s_query.orderBy("name").startAt(s.toLowerCase()).endAt(s.toLowerCase()+"\uf8ff"), trainer_list.class).build();
        s_adapter = new FirestoreRecyclerAdapter<trainer_list, trainerViewHolder>(options) {
            @NonNull
            @Override
            public trainerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trainer_list, parent, false);
                /*if(!(adapter.getItemCount()==0))
                    add_trainers.setVisibility(View.GONE);*/
                return new trainerViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final trainerViewHolder holder, int position, @NonNull final trainer_list model) {

                s_t_cnt=adapter.getItemCount();
                Log.d("TAG",s_t_cnt+"");

                Picasso.get().load(model.getUri()).into(holder.img_trainer_DP);

                holder.name.setText(model.getName().toUpperCase());
                holder.phone.setText(model.getPhone());
                holder.address.setText(model.getAddress());
                holder.blood_grp.setText(model.getBlood_grp());
                holder.email.setText(model.getEmail());

                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (holder.relativeLayout.getVisibility() == View.GONE)
                        {
                            TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                            holder.relativeLayout.setVisibility(View.VISIBLE);
                            holder.trainer_buttons.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            holder.relativeLayout.setVisibility(View.GONE);
                            holder.trainer_buttons.setVisibility(View.GONE);
                        }
                    }
                });

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setMessage("Are you sure you want to delete?");
                        builder.setTitle("Delete Trainer");
                        builder.setCancelable(true);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                                s_t_cnt--;
                                flag = 0;
                                firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").document(model.getName().toLowerCase()).delete();

                                Log.d("TAG", "user deleted");

                                Snackbar.make(getActivity().findViewById(android.R.id.content), "Trainer deleted", Snackbar.LENGTH_LONG)
                                        .setAction("undo", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Log.d("TAG", "undo");
                                                flag = 1;
                                                s_t_cnt--;
                                                add_trainers.setVisibility(View.GONE);
                                                img_add_trainers.setVisibility(View.GONE);
                                                firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").document(model.getName().toLowerCase()).set(model);
                                            }
                                        }).show();

                                Log.d("TAG",s_t_cnt+"");
                                if(s_t_cnt == 0)
                                {
                                    add_trainers.setVisibility(View.VISIBLE);
                                    img_add_trainers.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    add_trainers.setVisibility(View.GONE);
                                    img_add_trainers.setVisibility(View.GONE);
                                }

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(flag == 0)
                                        {
                                            firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").document(model.getName()).collection("sessions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        for(QueryDocumentSnapshot documentSnapshot1 : task.getResult())
                                                        {
                                                            documentSnapshot1.getData();
                                                            String time = documentSnapshot1.get("t_arrival_time")+"";
                                                            firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").document(model.getName()).collection("sessions").document(time).delete();
                                                        }
                                                        Log.d("TAG", "sessions deleted");
                                                    }
                                                }
                                            });
                                            StorageReference profile_imageREF = FirebaseStorage.getInstance().getReferenceFromUrl(model.getUri());
                                            if(!(model.getUri().equals("https://firebasestorage.googleapis.com/v0/b/gym-management-d98ff.appspot.com/o/profilePics%2Fdefault_photo.png?alt=media&token=4ebef1e3-55cc-47e4-acf9-3bb027c6b90c")))
                                            {
                                                profile_imageREF.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("TAG", "photo deleted");
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }, 3500);

                                holder.relativeLayout.setVisibility(View.GONE);
                                holder.trainer_buttons.setVisibility(View.GONE);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });

                holder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), edit_trainer.class);
                        intent.putExtra("name",holder.name.getText().toString());
                        intent.putExtra("phone",holder.phone.getText().toString());
                        intent.putExtra("email",holder.email.getText().toString());
                        intent.putExtra("address",holder.address.getText().toString());
                        intent.putExtra("blood_grp",holder.blood_grp.getText().toString());
                        intent.putExtra("uri", model.getUri());

                        startActivity(intent);
                        getActivity().finish();
                    }
                });

                holder.trainer_session_info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent s_intent = new Intent(getActivity(),trainer_session_info.class);
                        s_intent.putExtra("trainer name", holder.name.getText().toString().toLowerCase());
                        getActivity().finish();
                        startActivity(s_intent);
                    }
                });
                holder.img_phone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent phone_intent = new Intent(Intent.ACTION_DIAL);
                        phone_intent.setData(Uri.parse("tel:"+model.getPhone()));
                        startActivity(phone_intent);
                    }
                });
                holder.img_email.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent emailIntent = EmailIntentBuilder.from(getActivity())
                                .to(model.getEmail())
                                .build();
                        startActivity(emailIntent);
                    }
                });
                holder.img_share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String trainer_details = "Name: "+model.getName() + "\n" + "Email: "+model.getEmail() + "\n" +"Contact no: "+ model.getPhone()
                                + "\n" + "Blood Group: "+model.getBlood_grp() + "\n" + "Address: "+ model.getAddress();
                        Intent share_intent = new Intent(Intent.ACTION_VIEW);
                        share_intent.setData(Uri.parse("https://api.whatsapp.com/send?phone="+"&text="+trainer_details));
                        startActivity(share_intent);
                    }
                });
            }
        };

        s_adapter.startListening();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(s_adapter);
    }
}