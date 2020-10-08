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
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class client_main extends Fragment {

    LinearLayout ll;

    FloatingActionButton client_add;
    RecyclerView client_recyclerView;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;
    FirestoreRecyclerAdapter adapter;
    String userID;

    byte flag = 0;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ll = (LinearLayout) inflater.inflate(R.layout.activity_client_main, container, false);

        client_recyclerView = ll.findViewById(R.id.client_recycler_view);
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        userID = mAuth.getUid();

        client_add = ll.findViewById(R.id.btn_client_add);

        client_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getActivity().finish();
                startActivity(new Intent(getActivity(), add_client.class));
            }
        });

        Query query = firebaseFirestore.collection(userID).document("user info").collection("clients");

        FirestoreRecyclerOptions<client_list> options = new FirestoreRecyclerOptions.Builder<client_list>().setQuery(query, client_list.class).build();

        adapter = new FirestoreRecyclerAdapter<client_list, clientViewHolder>(options)
        {
            @NonNull
            @Override
            public clientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list, parent,false);
                return new clientViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final clientViewHolder holder, final int position, @NonNull final client_list model) {

                Picasso.get().load(model.getUri()).into(holder.imageView);

                holder.name.setText(model.getName().toUpperCase());
                holder.phone.setText(model.getPhone());
                holder.email.setText(model.getEmail());
                holder.join_date.setText(model.getJoin_date());
                holder.due_date.setText(model.getDue_date());
                holder.plan.setText(model.getPlan());
                holder.amount.setText(model.getAmount());
                holder.amt_paid.setText(model.getAmt_paid());
                holder.amt_due.setText(model.getAmt_due());

                holder.client_cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    if(holder.client_relative_layout.getVisibility()==View.GONE)
                    {
                        TransitionManager.beginDelayedTransition(holder.client_cardView, new AutoTransition());
                        holder.client_relative_layout.setVisibility(View.VISIBLE);
                        holder.buttons.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        holder.client_relative_layout.setVisibility(View.GONE);
                        holder.buttons.setVisibility(View.GONE);
                    }
                    }
                });

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setMessage("Are you sure you want to delete?");
                    builder.setTitle("Delete Client");
                    builder.setCancelable(true);

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            flag = 0;
                            firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("clients").document(model.getName().toLowerCase()).delete();

                            Log.d("TAG", "user deleted");

                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Client deleted", Snackbar.LENGTH_LONG)
                                    .setAction("undo", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d("TAG", "undo");
                                            flag = 1;
                                            firebaseFirestore.collection(userID).document("user info").collection("clients").document(model.getName().toLowerCase()).set(model);
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

                            holder.client_relative_layout.setVisibility(View.GONE);
                            holder.buttons.setVisibility(View.GONE);
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
                    Intent intent = new Intent(getActivity(), edit_client.class);
                    intent.putExtra("name", holder.name.getText().toString());
                    intent.putExtra("phone", holder.phone.getText().toString());
                    intent.putExtra("email", holder.email.getText().toString());
                    intent.putExtra("plan", holder.plan.getText().toString());
                    intent.putExtra("join_date", holder.join_date.getText().toString());
                    intent.putExtra("due_date", holder.due_date.getText().toString());
                    intent.putExtra("amount", holder.amount.getText().toString());
                    intent.putExtra("paid_amt", holder.amt_paid.getText().toString());
                    intent.putExtra("due_amt", holder.amt_due.getText().toString());
                    intent.putExtra("uri", model.getUri());
                    getActivity().finish();
                    startActivity(intent);
                    }
                });

                holder.sessions_info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), session_info.class);
                    intent.putExtra("client name", holder.name.getText().toString().toLowerCase());
                    getActivity().finish();
                    startActivity(intent);
                    }
                });
            }
        };

        client_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        client_recyclerView.setAdapter(adapter);
        return ll;
    }

    private class clientViewHolder extends RecyclerView.ViewHolder{

        TextView name, phone, email, join_date, due_date, plan, amount, amt_paid, amt_due;
        Button edit, delete, sessions_info;
        CardView client_cardView;
        RelativeLayout client_relative_layout;
        GridLayout buttons;

        CircularImageView imageView;

        public clientViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.card_client_DP);

            name = itemView.findViewById(R.id.txt_client_name);
            phone = itemView.findViewById(R.id.txt_client_phone);
            join_date = itemView.findViewById(R.id.client_join_date);
            due_date = itemView.findViewById(R.id.client_due_date);
            plan = itemView.findViewById(R.id.client_plan);
            amount = itemView.findViewById(R.id.client_amount);
            amt_paid = itemView.findViewById(R.id.client_paid);
            amt_due = itemView.findViewById(R.id.client_due);
            email = itemView.findViewById(R.id.client_email);

            buttons = itemView.findViewById(R.id.buttons);
            edit = itemView.findViewById(R.id.btn_client_edit);
            delete = itemView.findViewById(R.id.btn_client_delete);
            sessions_info = itemView.findViewById(R.id.btn_sessions_info);
            client_cardView = itemView.findViewById(R.id.client_card_view);
            client_relative_layout = itemView.findViewById(R.id.client_relative_layout);
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.client_search_view, menu);
        MenuItem menuItem = menu.findItem(R.id.client_bar);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                onClientSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                onClientSearch(s);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void onClientSearch(String s) {

        Query query = firebaseFirestore.collection(userID).document("user info").collection("clients");
        FirestoreRecyclerOptions<client_list> options = new FirestoreRecyclerOptions.Builder<client_list>().setQuery(query.orderBy("name").startAt(s.toLowerCase()).endAt(s.toLowerCase()+"\uf8ff"), client_list.class).build();

        adapter = new FirestoreRecyclerAdapter<client_list, clientViewHolder>(options) {
            @NonNull
            @Override
            public clientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list, parent,false);
                return new clientViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final clientViewHolder holder, int position, @NonNull final client_list model) {

                Picasso.get().load(model.getUri()).into(holder.imageView);

                holder.name.setText(model.getName().toUpperCase());
                holder.phone.setText(model.getPhone());
                holder.email.setText(model.getEmail());
                holder.join_date.setText(model.getJoin_date());
                holder.due_date.setText(model.getDue_date());
                holder.plan.setText(model.getPlan());
                holder.amount.setText(model.getAmount());
                holder.amt_paid.setText(model.getAmt_paid());
                holder.amt_due.setText(model.getAmt_due());

                holder.client_cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(holder.client_relative_layout.getVisibility()==View.GONE)
                        {
                            TransitionManager.beginDelayedTransition(holder.client_cardView, new AutoTransition());
                            holder.client_relative_layout.setVisibility(View.VISIBLE);
                            holder.buttons.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            holder.client_relative_layout.setVisibility(View.GONE);
                            holder.buttons.setVisibility(View.GONE);
                        }
                    }
                });

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setMessage("Are you sure you want to delete?");
                        builder.setTitle("Delete Client");
                        builder.setCancelable(true);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                flag = 0;
                                firebaseFirestore.collection(mAuth.getUid()).document("user info").collection("clients").document(model.getName().toLowerCase()).delete();

                                Log.d("TAG", "user deleted");

                                Snackbar.make(getActivity().findViewById(android.R.id.content), "Client deleted", Snackbar.LENGTH_LONG)
                                        .setAction("undo", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Log.d("TAG", "undo");
                                                flag = 1;
                                                firebaseFirestore.collection(userID).document("user info").collection("clients").document(model.getName().toLowerCase()).set(model);
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

                                holder.client_relative_layout.setVisibility(View.GONE);
                                holder.buttons.setVisibility(View.GONE);
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
                        Intent intent = new Intent(getActivity(), edit_client.class);
                        intent.putExtra("name", holder.name.getText().toString());
                        intent.putExtra("phone", holder.phone.getText().toString());
                        intent.putExtra("email", holder.email.getText().toString());
                        intent.putExtra("plan", holder.plan.getText().toString());
                        intent.putExtra("join_date", holder.join_date.getText().toString());
                        intent.putExtra("due_date", holder.due_date.getText().toString());
                        intent.putExtra("amount", holder.amount.getText().toString());
                        intent.putExtra("paid_amt", holder.amt_paid.getText().toString());
                        intent.putExtra("due_amt", holder.amt_due.getText().toString());
                        intent.putExtra("uri",model.getUri());

                        getActivity().finish();
                        startActivity(intent);
                    }
                });

                holder.sessions_info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), session_info.class);
                        intent.putExtra("client name", holder.name.getText().toString().toLowerCase());
                        getActivity().finish();
                        startActivity(intent);
                    }
                });
            }
        };

        adapter.startListening();
        client_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        client_recyclerView.setAdapter(adapter);
    }
}
