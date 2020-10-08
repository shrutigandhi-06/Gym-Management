package com.example.gymmanagement;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

import org.jetbrains.annotations.Nullable;

public class app_intro extends AppIntro {

    String title, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(AppIntroFragment.newInstance(
                title = "Welcome...",
                description = "This is the first slide of the example"
        ));
        addSlide(AppIntroFragment.newInstance(
                title = "...Let's get started!",
                description = "This is the last slide, I won't annoy you more :)"
        ));
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