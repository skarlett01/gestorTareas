package com.example.gestortareas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> tasks;
    private ArrayAdapter<String> tasksAdapter;
    private ListView listViewTasks;
    private Button buttonAddTask;

    private final ActivityResultLauncher<Intent> createTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String taskName = data.getStringExtra("taskName");
                    String taskDescription = data.getStringExtra("taskDescription");
                    if (taskName != null) {
                        tasks.add(taskName + ": " + taskDescription);
                        tasksAdapter.notifyDataSetChanged();
                    }
                }
            });

    // Logica para redirigir a la pantalla de crear tarea
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tasks = new ArrayList<>();
        listViewTasks = findViewById(R.id.listViewTasks);
        buttonAddTask = findViewById(R.id.buttonAddTask);

        tasksAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tasks);
        listViewTasks.setAdapter(tasksAdapter);

        buttonAddTask.setOnClickListener(v -> {
            Intent createTaskIntent = new Intent(MainActivity.this, PantallaCrearTarea.class);
            createTaskLauncher.launch(createTaskIntent);
        });
    }
}
