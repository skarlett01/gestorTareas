package com.example.gestortareas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class PantallaCrearTarea extends AppCompatActivity {

    private EditText editTextTaskName;
    private EditText editTextTaskDescription;
    private Button buttonSaveTask;
    private Button buttonCancelTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_crear_tarea);

        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);
        buttonCancelTask = findViewById(R.id.buttonCancelTask);

        buttonSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = editTextTaskName.getText().toString();
                String taskDescription = editTextTaskDescription.getText().toString();

                if (!taskName.isEmpty()) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("taskName", taskName);
                    resultIntent.putExtra("taskDescription", taskDescription);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        buttonCancelTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
