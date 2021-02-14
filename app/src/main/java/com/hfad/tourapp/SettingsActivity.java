package com.hfad.tourapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = "SettingsActivity";
    public static final boolean DEFAULT_WELCOME = false;
    public static final boolean DEFAULT_NOTIFY = true;
    public static final boolean DEFAULT_SPEECH_LIMIT = false;
    public static final int DEFAULT_WORD_COUNT = 100;
    private SwitchCompat swWelcome; //Will only the location be announced or a summary from Wikipedia?
    private SwitchCompat swNotify;  //Will notifications to announce when entering city be used?
    private SwitchCompat swSpeechLimit;    //Will dark mode be enabled?
    private SeekBar sbWords;    //Regulates the words to limit to
    private TextView tvWordCount;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);

        //Initializing each of the switches
        swWelcome = findViewById(R.id.swWelcome);
        swNotify = findViewById(R.id.swNotify);
        swSpeechLimit = findViewById(R.id.swSpeechLimit);
        sbWords = findViewById(R.id.sbWords);
        tvWordCount = findViewById(R.id.tvWordCount);
        prefs = getSharedPreferences("com.hfad.tourapp.preferences", Context.MODE_PRIVATE);

        if(prefs.getBoolean("welcome", DEFAULT_WELCOME))
            swWelcome.setChecked(true);
        if(prefs.getBoolean("notify", DEFAULT_NOTIFY))
            swNotify.setChecked(true);
        if(prefs.getBoolean("speech-limit", DEFAULT_SPEECH_LIMIT)) {
            swSpeechLimit.setChecked(true);
            sbWords.setEnabled(true);
        } else {
            swSpeechLimit.setChecked(false);
            sbWords.setEnabled(false);
        }
        sbWords.setProgress(prefs.getInt("word-count", DEFAULT_WORD_COUNT));

        tvWordCount.setText(sbWords.getProgress() + " words");

        //Setting up listeners for switches

        swWelcome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefEditor = prefs.edit();
                prefEditor.putBoolean("welcome", isChecked);
                prefEditor.apply();

                if (isChecked) {
                    swSpeechLimit.setEnabled(false);
                    sbWords.setEnabled(false);
                }
                else {
                    swSpeechLimit.setEnabled(true);
                    sbWords.setEnabled(true);
                }
            }
        });

        swNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefEditor = prefs.edit();
                prefEditor.putBoolean("notify", isChecked);
                prefEditor.apply();
            }
        });

        swSpeechLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefEditor = prefs.edit();
                prefEditor.putBoolean("speech-limit", isChecked);
                prefEditor.apply();

                if (isChecked) sbWords.setEnabled(true);
                else sbWords.setEnabled(false);
            }
        });

        sbWords.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefEditor = prefs.edit();
                tvWordCount.setText(sbWords.getProgress() + " words");
                prefEditor.putInt("word-count", sbWords.getProgress());
                prefEditor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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