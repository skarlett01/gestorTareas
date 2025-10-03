package com.example.gestortareas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    private static final int CREATE_TASK_REQUEST = 1;
    private ArrayList<String> tasks;
    private ArrayAdapter<String> tasksAdapter;
    private ListView listViewTasks;
    private Button buttonAddTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tasks = new ArrayList<>();
        listViewTasks = findViewById(R.id.listViewTasks);
        buttonAddTask = findViewById(R.id.buttonAddTask);

        tasksAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tasks);
        listViewTasks.setAdapter(tasksAdapter);

        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PantallaCrearTarea.class);
                startActivityForResult(intent, CREATE_TASK_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_TASK_REQUEST && resultCode == RESULT_OK && data != null) {
            String taskName = data.getStringExtra("taskName");
            String taskDescription = data.getStringExtra("taskDescription");
            if (taskName != null) {
                tasks.add(taskName + ": " + taskDescription);
                tasksAdapter.notifyDataSetChanged();
            }
        }
    }
}
