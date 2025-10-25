package com.example.gestortareas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import Data.DBHelper;
import Data.TareaContract;

public class MainActivity extends AppCompatActivity {

    private TareaAdapter tasksAdapter;
    private ListView listViewTasks;
    private Button buttonAddTask;
    private Button buttonModifyTask;
    private Button buttonDeleteTask;
    private DBHelper dbHelper;
    private int userId = 1; // ID de usuario que esta asociado con las tareas

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
        listViewTasks = findViewById(R.id.listViewTasks);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        buttonModifyTask = findViewById(R.id.buttonModifyTask);
        buttonDeleteTask = findViewById(R.id.buttonDeleteTask);

        // Set up the adapter with an empty list to start.
        tasksAdapter = new TareaAdapter(this, new ArrayList<>());
        listViewTasks.setAdapter(tasksAdapter);
        listViewTasks.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listViewTasks.setOnItemClickListener((parent, view, position, id) -> {
            Bundle taskBundle = tasksAdapter.getItem(position);
            if (taskBundle != null) {
                int taskId = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID);
                boolean isCompleted = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID_ESTADO) == 2;
                dbHelper.actualizarEstadoTarea(taskId, !isCompleted);
                loadTasks();
            }
        });

        buttonAddTask.setOnClickListener(v -> {
            Intent createTaskIntent = new Intent(MainActivity.this, PantallaCrearTarea.class);
            createTaskLauncher.launch(createTaskIntent);
        });

        buttonModifyTask.setOnClickListener(v -> {
            SparseBooleanArray checked = listViewTasks.getCheckedItemPositions();
            ArrayList<Integer> selectedPositions = new ArrayList<>();
            for (int i = 0; i < checked.size(); i++) {
                if (checked.valueAt(i)) {
                    selectedPositions.add(checked.keyAt(i));
                }
            }

            if (selectedPositions.size() == 1) {
                int position = selectedPositions.get(0);
                Bundle taskBundle = tasksAdapter.getItem(position);
                if (taskBundle != null) {
                    int taskId = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID);
                    String taskTitle = taskBundle.getString(TareaContract.TareaEntry.COLUMN_TITULO);
                    String taskDescription = taskBundle.getString(TareaContract.TareaEntry.COLUMN_DESCRIPCION);

                    Intent editTaskIntent = new Intent(MainActivity.this, ModificarTarea.class);
                    editTaskIntent.putExtra("taskId", taskId);
                    editTaskIntent.putExtra("taskName", taskTitle);
                    editTaskIntent.putExtra("taskDescription", taskDescription);
                    editTaskLauncher.launch(editTaskIntent);
                }
            } else if (selectedPositions.isEmpty()) {
                Toast.makeText(MainActivity.this, "Selecciona una tarea para modificar", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Selecciona solo una tarea para modificar", Toast.LENGTH_SHORT).show();
            }
        });

        buttonDeleteTask.setOnClickListener(v -> {
            SparseBooleanArray checked = listViewTasks.getCheckedItemPositions();
            ArrayList<Bundle> tasksToDelete = new ArrayList<>();
            for (int i = 0; i < checked.size(); i++) {
                if (checked.valueAt(i)) {
                    int position = checked.keyAt(i);
                    Bundle taskBundle = tasksAdapter.getItem(position);
                    if (taskBundle != null) {
                        tasksToDelete.add(taskBundle);
                    }
                }
            }

            if (!tasksToDelete.isEmpty()) {
                for (Bundle taskBundle : tasksToDelete) {
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
        List<Bundle> newTasks = dbHelper.getTareasByUser(userId);
        tasksAdapter.clear();
        tasksAdapter.addAll(newTasks);

        // After updating adapter, re-check the completed items
        for (int i = 0; i < tasksAdapter.getCount(); i++) {
            Bundle bundle = tasksAdapter.getItem(i);
            if (bundle != null) {
                boolean isCompleted = bundle.getInt(TareaContract.TareaEntry.COLUMN_ID_ESTADO) == 2;
                listViewTasks.setItemChecked(i, isCompleted);
            }
        }
    }

    // Custom Adapter class for better data handling
    public static class TareaAdapter extends ArrayAdapter<Bundle> {

        public TareaAdapter(Context context, List<Bundle> tasks) {
            super(context, android.R.layout.simple_list_item_multiple_choice, tasks);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the standard checked text view
            View view = super.getView(position, convertView, parent);
            CheckedTextView textView = (CheckedTextView) view;

            Bundle taskBundle = getItem(position);

            if (taskBundle != null) {
                String title = taskBundle.getString(TareaContract.TareaEntry.COLUMN_TITULO);
                String description = taskBundle.getString(TareaContract.TareaEntry.COLUMN_DESCRIPCION);
                int status = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID_ESTADO);
                boolean isCompleted = (status == 2);
                String statusText = isCompleted ? "[Completada]" : "[Pendiente]";

                textView.setText(String.format("%s %s: %s", statusText, title, description));
                textView.setTextColor(Color.BLACK);

                if (isCompleted) {
                    textView.setCheckMarkTintList(ColorStateList.valueOf(Color.GREEN));
                } else {
                    textView.setCheckMarkTintList(ColorStateList.valueOf(Color.YELLOW));
                }
            }

            return textView;
        }
    }
}
