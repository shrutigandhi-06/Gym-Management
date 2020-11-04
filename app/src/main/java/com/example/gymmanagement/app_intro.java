package com.example.gymmanagement;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

import org.jetbrains.annotations.Nullable;

public class app_intro extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance("Welcome to Gymster","Helping hand for Gym Owners",R.drawable.app_logo_full_final,
                Color.rgb(44,145,253),Color.WHITE, Color.WHITE));

        addSlide(AppIntroFragment.newInstance("Add your new clients and trainers with a single touch","",R.drawable.final_add,
                Color.rgb(44,145,253),Color.WHITE, Color.WHITE));

        addSlide(AppIntroFragment.newInstance("Quickly add client's workouts","Keep track of your clients and trainers workout sessions via Session Info ",
                R.drawable.input_session, Color.rgb(44,145,253),Color.WHITE, Color.WHITE));

        addSlide(AppIntroFragment.newInstance("Get in touch with your clients and trainers", "Make a call, Compose an email and Share on WhatsApp with an one touch", R.drawable.get_in_touch, Color.rgb(44,145,253),Color.WHITE, Color.WHITE));

        addSlide(AppIntroFragment.newInstance("Let's get started!", "", Color.rgb(44,145,253),Color.WHITE, Color.WHITE));

    }

    @Override
    protected void onSkipPressed(@Nullable Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
        startActivity(new Intent(app_intro.this, fragment_main.class));
    }

    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
        startActivity(new Intent(app_intro.this, fragment_main.class));
    }
}