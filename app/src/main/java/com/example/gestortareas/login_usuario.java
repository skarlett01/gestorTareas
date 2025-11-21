package com.example.gestortareas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class login_usuario extends AppCompatActivity {

    //Logica de inicio de sesion de usuario
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_usuario);

        Button buttonLogin = findViewById(R.id.buttonLogin);
        Switch switchTalkBack = findViewById(R.id.switchTalkBack);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isTalkBackEnabled = switchTalkBack.isChecked();
                Intent intent = new Intent(login_usuario.this, MainActivity.class);
                intent.putExtra("talkBackEnabled", isTalkBackEnabled);
                startActivity(intent);
            }
        });
    }
}
