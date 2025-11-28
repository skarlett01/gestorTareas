package com.example.gestortareas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class configuracion extends AppCompatActivity {

    //Nueva vista de configuracion ya configurada con talkback
    private Switch switchTalkback;
    private ImageButton backButton;
    private static final String PREFS_NAME = "prefs";
    private static final String TALKBACK_ENABLED = "talkback_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        switchTalkback = findViewById(R.id.switchTalkback);
        backButton = findViewById(R.id.backButton);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean talkbackEnabled = prefs.getBoolean(TALKBACK_ENABLED, false);
        switchTalkback.setChecked(talkbackEnabled);

        switchTalkback.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
            editor.putBoolean(TALKBACK_ENABLED, isChecked);
            editor.apply();
        });

        backButton.setOnClickListener(v -> {
            finish();
        });
    }
}
