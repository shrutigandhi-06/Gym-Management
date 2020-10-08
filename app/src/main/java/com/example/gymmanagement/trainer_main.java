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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class trainer_main extends Fragment {

    LinearLayout ll;

    FirebaseFirestore firebaseFirestore;
    FirestoreRecyclerAdapter adapter;
    FirebaseAuth mAuth;
    RecyclerView recyclerView;
    FloatingActionButton add;

    byte flag = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ll= (LinearLayout) inflater.inflate(R.layout.activity_trainer_main, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

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
                return new trainerViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final trainerViewHolder holder, int position, @NonNull final trainer_list model) {

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
                        }
                        else
                        {
                            holder.relativeLayout.setVisibility(View.GONE);
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

                                flag = 0;
                                firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").document(model.getName().toLowerCase()).delete();

                                Log.d("TAG", "user deleted");

                                Snackbar.make(getActivity().findViewById(android.R.id.content), "Client deleted", Snackbar.LENGTH_LONG)
                                    .setAction("undo", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d("TAG", "undo");
                                            flag = 1;
                                            firebaseFirestore.collection(userID).document("user info").collection("trainers").document(model.getName().toLowerCase()).set(model);
                                        }
                                    }).show();

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(flag == 0)
                                        {
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
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        return ll;
    }

    private class trainerViewHolder extends RecyclerView.ViewHolder{

        TextView name, phone, address, blood_grp, email;
        Button delete,edit;
        CardView cardView;
        RelativeLayout relativeLayout;
        CircularImageView img_trainer_DP;

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

            delete = itemView.findViewById(R.id.btn_delete);
            edit = itemView.findViewById(R.id.btn_edit);
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

        Query query = firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers");
        FirestoreRecyclerOptions<trainer_list> options = new FirestoreRecyclerOptions.Builder<trainer_list>().setQuery(query.orderBy("name").startAt(s.toLowerCase()).endAt(s.toLowerCase()+"\uf8ff"), trainer_list.class).build();
        adapter = new FirestoreRecyclerAdapter<trainer_list, trainerViewHolder>(options) {
            @NonNull
            @Override
            public trainerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trainer_list, parent, false);
                return new trainerViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final trainerViewHolder holder, int position, @NonNull final trainer_list model) {

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
                        }
                        else
                        {
                            holder.relativeLayout.setVisibility(View.GONE);
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
                                flag = 0;
                                firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").document(model.getName().toLowerCase()).delete();

                                Log.d("TAG", "user deleted");

                                Snackbar.make(getActivity().findViewById(android.R.id.content), "Client deleted", Snackbar.LENGTH_LONG)
                                        .setAction("undo", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Log.d("TAG", "undo");
                                                flag = 1;
                                                firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("trainers").document(model.getName().toLowerCase()).set(model);
                                            }
                                        }).show();

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(flag == 0)
                                        {
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
            }
        };
        adapter.startListening();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }
}