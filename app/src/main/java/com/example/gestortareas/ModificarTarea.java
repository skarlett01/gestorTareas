package com.example.gestortareas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class ModificarTarea extends AppCompatActivity {

    private EditText editTextTaskName;
    private EditText editTextTaskDescription;
    private Button buttonSaveTask;
    private Button buttonCancelTask;
    private int taskId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_tarea);

        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);
        buttonCancelTask = findViewById(R.id.buttonCancelTask);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("taskId")) {
            taskId = intent.getIntExtra("taskId", -1);
            String taskName = intent.getStringExtra("taskName");
            String taskDescription = intent.getStringExtra("taskDescription");
            editTextTaskName.setText(taskName);
            editTextTaskDescription.setText(taskDescription);
        }

        buttonSaveTask.setOnClickListener(v -> {
            String taskName = editTextTaskName.getText().toString();
            String taskDescription = editTextTaskDescription.getText().toString();

            if (!taskName.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("taskName", taskName);
                resultIntent.putExtra("taskDescription", taskDescription);
                if (taskId != -1) {
                    resultIntent.putExtra("taskId", taskId);
                }
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        buttonCancelTask.setOnClickListener(v -> finish());
    }
}
