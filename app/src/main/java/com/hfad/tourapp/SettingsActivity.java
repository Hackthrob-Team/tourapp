package com.hfad.tourapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = "SettingsActivity";
    SwitchCompat swSummary; //Will only the location be announced or a summary from Wikipedia?
    SwitchCompat swTTS; //Will the service do TTS announcement or just show current city?
    SwitchCompat swNotify;  //Will notifications to announce when entering city be used?
    SwitchCompat swDarkMode;    //Will dark mode be enabled?
    SharedPreferences prefs;
    SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);

        //Initializing each of the switches
        swSummary = findViewById(R.id.swSummary);
        swTTS = findViewById(R.id.swTTS);
        swNotify = findViewById(R.id.swNotify);
        swDarkMode = findViewById(R.id.swDarkMode);
        prefs = getSharedPreferences("com.hfad.tourapp.preferences", Context.MODE_PRIVATE);

        if(prefs.getBoolean("summary", false))
            swSummary.setChecked(true);
        if(prefs.getBoolean("text-to-speech", false))
            swTTS.setChecked(true);
        if(prefs.getBoolean("notify", false))
            swNotify.setChecked(true);
        if(prefs.getBoolean("dark-mode", false))
            swDarkMode.setChecked(true);

        //Setting up listeners for switches

        swSummary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "swSummary listener");
                prefEditor = prefs.edit();
                prefEditor.putBoolean("summary", isChecked);
                prefEditor.apply();
            }
        });

        swTTS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "swTTS listener");
                prefEditor = prefs.edit();
                prefEditor.putBoolean("text-to-speech", isChecked);
                prefEditor.apply();
            }
        });

        swNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "swNotify listener");
                prefEditor = prefs.edit();
                prefEditor.putBoolean("notify", isChecked);
                prefEditor.apply();
            }
        });

        swDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "swDarkMode listener");
                prefEditor = prefs.edit();
                prefEditor.putBoolean("dark-mode", isChecked);
                prefEditor.apply();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}