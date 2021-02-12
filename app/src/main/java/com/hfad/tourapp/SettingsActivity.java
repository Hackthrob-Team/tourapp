package com.hfad.tourapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = "SettingsActivity";
    SwitchCompat swTTS;
    SwitchCompat swNotify;
    SwitchCompat swDarkMode;
    SharedPreferences prefs;
    SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        swTTS = findViewById(R.id.swTTS);
        swNotify = findViewById(R.id.swNotify);
        swDarkMode = findViewById(R.id.swDarkMode);
        prefs = this.getPreferences(Context.MODE_PRIVATE);

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


}