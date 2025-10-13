package com.example.gestortareas;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

import Data.DBHelper;
import Data.TareaContract;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> tasks;
    private ArrayAdapter<String> tasksAdapter;
    private ListView listViewTasks;
    private Button buttonAddTask;
    private Button buttonModifyTask;
    private Button buttonDeleteTask;
    private DBHelper dbHelper;
    private int userId = 1; // ID de usuario que esta asociado con las tareas
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

    private final ActivityResultLauncher<Intent> editTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    int taskId = data.getIntExtra("taskId", -1);
                    String taskName = data.getStringExtra("taskName");
                    String taskDescription = data.getStringExtra("taskDescription");
                    if (taskId != -1 && taskName != null) {
                        dbHelper.actualizarTarea(taskId, taskName, taskDescription);
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
        tasksBundle = new ArrayList<>();
        listViewTasks = findViewById(R.id.listViewTasks);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        buttonModifyTask = findViewById(R.id.buttonModifyTask);
        buttonDeleteTask = findViewById(R.id.buttonDeleteTask);

        tasksAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, tasks) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                CheckedTextView textView = (CheckedTextView) view;
                textView.setTextColor(Color.BLACK);

                Bundle taskBundle = tasksBundle.get(position);
                boolean isCompleted = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID_ESTADO) == 2;
                if(isCompleted) {
                    textView.setCheckMarkTintList(ColorStateList.valueOf(Color.GREEN));
                } else {
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

        buttonModifyTask.setOnClickListener(v -> {
            SparseBooleanArray checked = listViewTasks.getCheckedItemPositions();
            ArrayList<Integer> selectedPositions = new ArrayList<>();
            for (int i = 0; i < checked.size(); i++) {
                int key = checked.keyAt(i);
                if (checked.get(key)) {
                    selectedPositions.add(key);
                }
            }

            if (selectedPositions.size() == 1) {
                int position = selectedPositions.get(0);
                Bundle taskBundle = tasksBundle.get(position);
                int taskId = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID);
                String taskTitle = taskBundle.getString(TareaContract.TareaEntry.COLUMN_TITULO);
                String taskDescription = taskBundle.getString(TareaContract.TareaEntry.COLUMN_DESCRIPCION);

                Intent editTaskIntent = new Intent(MainActivity.this, PantallaCrearTarea.class);
                editTaskIntent.putExtra("taskId", taskId);
                editTaskIntent.putExtra("taskName", taskTitle);
                editTaskIntent.putExtra("taskDescription", taskDescription);
                editTaskLauncher.launch(editTaskIntent);
            } else if (selectedPositions.size() == 0) {
                Toast.makeText(MainActivity.this, "Selecciona una tarea para modificar", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Selecciona solo una tarea para modificar", Toast.LENGTH_SHORT).show();
            }
        });

        buttonDeleteTask.setOnClickListener(v -> {
            SparseBooleanArray checked = listViewTasks.getCheckedItemPositions();
            ArrayList<Integer> selectedPositions = new ArrayList<>();
            for (int i = 0; i < checked.size(); i++) {
                int key = checked.keyAt(i);
                if (checked.get(key)) {
                    selectedPositions.add(key);
                }
            }

            if (selectedPositions.size() > 0) {
                for (int position : selectedPositions) {
                    Bundle taskBundle = tasksBundle.get(position);
                    int taskId = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID);
                    dbHelper.eliminarTarea(taskId);
                }
                loadTasks();
                Toast.makeText(MainActivity.this, "Tareas eliminadas", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Selecciona una o más tareas para eliminar", Toast.LENGTH_SHORT).show();
            }
        });

        loadTasks();
    }

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

        for (int i = 0; i < tasksBundle.size(); i++) {
            Bundle bundle = tasksBundle.get(i);
            boolean isCompleted = bundle.getInt(TareaContract.TareaEntry.COLUMN_ID_ESTADO) == 2;
            listViewTasks.setItemChecked(i, isCompleted);
        }
    }
}
