package com.example.gestortareas;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

// Importaciones necesarias
import Data.DBHelper;
import Data.TareaContract;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> tasks;
    private ArrayAdapter<String> tasksAdapter;
    private ListView listViewTasks;
    private Button buttonAddTask;
    private DBHelper dbHelper;
    private int userId = 1; // ID de usuario harcodeado
    private List<Bundle> tasksBundle;

    private final ActivityResultLauncher<Intent> createTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String taskName = data.getStringExtra("taskName");
                    String taskDescription = data.getStringExtra("taskDescription");
                    if (taskName != null) {
                        dbHelper.crearTarea(taskName, taskDescription, userId);
                        loadTasks();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);
        tasks = new ArrayList<>();
        listViewTasks = findViewById(R.id.listViewTasks);
        buttonAddTask = findViewById(R.id.buttonAddTask);

        tasksAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, tasks) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                CheckedTextView textView = (CheckedTextView) view;
                textView.setTextColor(Color.BLACK);

                Bundle taskBundle = tasksBundle.get(position);
                boolean isCompleted = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID_ESTADO) == 2;
                listViewTasks.setItemChecked(position, isCompleted);
                if(isCompleted) {
                    textView.setCheckMarkTintList(ColorStateList.valueOf(Color.GREEN));
                }else {
                    textView.setCheckMarkTintList(ColorStateList.valueOf(Color.YELLOW));
                }

                return view;
            }
        };
        listViewTasks.setAdapter(tasksAdapter);
        listViewTasks.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listViewTasks.setOnItemClickListener((parent, view, position, id) -> {
            Bundle taskBundle = tasksBundle.get(position);
            int taskId = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID);
            boolean isCompleted = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID_ESTADO) == 2;
            dbHelper.actualizarEstadoTarea(taskId, !isCompleted);
            loadTasks();
        });

        buttonAddTask.setOnClickListener(v -> {
            Intent createTaskIntent = new Intent(MainActivity.this, PantallaCrearTarea.class);
            createTaskLauncher.launch(createTaskIntent);
        });

        loadTasks();
    }

    // Cargar tareas desde la base de datos
    private void loadTasks() {
        tasksBundle = dbHelper.getTareasByUser(userId);
        tasks.clear();
        for (Bundle bundle : tasksBundle) {
            String title = bundle.getString(TareaContract.TareaEntry.COLUMN_TITULO);
            String description = bundle.getString(TareaContract.TareaEntry.COLUMN_DESCRIPCION);
            int status = bundle.getInt(TareaContract.TareaEntry.COLUMN_ID_ESTADO);
            String statusText = status == 2 ? "[Completada]" : "[Pendiente]";
            tasks.add(statusText + " " + title + ": " + description);
        }
        tasksAdapter.notifyDataSetChanged();
    }
}
