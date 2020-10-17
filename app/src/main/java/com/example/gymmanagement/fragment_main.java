package com.example.gymmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class fragment_main extends AppCompatActivity {

    private tab_adapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_main);

        Intent intent = getIntent();
        String flag = intent.getStringExtra("flag");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        adapter = new tab_adapter(getSupportFragmentManager(),BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new after_login(), "Home");
        adapter.addFragment(new client_main(), "Clients");
        adapter.addFragment(new trainer_main(), "Trainers");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        if(flag!=null)
        {
            if(flag.equals("trainer"))
                change_fragment(2);

            else if(flag.equals("client"))
                change_fragment(1);
        }
    }

    public void change_fragment(int position)
    {
        viewPager.setCurrentItem(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.drawer_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.item_dashboard:
            {
                Log.d("TAG", "into dashboard");
                finish();
                startActivity(new Intent(fragment_main.this, dashboard.class));
                break;
            }

            case R.id.item_logout:
            {
                client_main.c_cnt = 0;
                trainer_main.t_cnt = 0;
                FirebaseAuth.getInstance().signOut();
                MainActivity mainActivity = new MainActivity();
                Log.d("TAG", "user logout");
                mainActivity.new_user = false;
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}