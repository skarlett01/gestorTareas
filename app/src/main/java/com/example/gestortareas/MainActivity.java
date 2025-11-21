package com.example.gestortareas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
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
    private int userId = 1;

    private final ActivityResultLauncher<Intent> createTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String taskName = data.getStringExtra("taskName");
                    String taskDescription = data.getStringExtra("taskDescription");
                    String audioPath = data.getStringExtra("audioPath");
                    if (taskName != null) {
                        dbHelper.crearTarea(taskName, taskDescription, userId, audioPath);
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
                    String audioPath = data.getStringExtra("audioPath");
                    if (taskId != -1 && taskName != null) {
                        dbHelper.actualizarTarea(taskId, taskName, taskDescription, audioPath);
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

        tasksAdapter = new TareaAdapter(this, new ArrayList<>());
        listViewTasks.setAdapter(tasksAdapter);
        listViewTasks.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Changed to OnItemClickListener for toggling task completion
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
                    String audioPath = taskBundle.getString(TareaContract.TareaEntry.COLUMN_AUDIO_PATH);

                    Intent editTaskIntent = new Intent(MainActivity.this, PantallaCrearTarea.class);
                    editTaskIntent.putExtra("taskId", taskId);
                    editTaskIntent.putExtra("taskName", taskTitle);
                    editTaskIntent.putExtra("taskDescription", taskDescription);
                    editTaskIntent.putExtra("audioPath", audioPath);
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
                    String audioPath = taskBundle.getString(TareaContract.TareaEntry.COLUMN_AUDIO_PATH);

                    dbHelper.eliminarTarea(taskId);

                    if (audioPath != null && !audioPath.isEmpty()) {
                        File audioFile = new File(audioPath);
                        if (audioFile.exists()) {
                            audioFile.delete();
                        }
                    }
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
    }

    public class TareaAdapter extends ArrayAdapter<Bundle> {

        public TareaAdapter(Context context, List<Bundle> tasks) {
            super(context, R.layout.list_item_tarea, R.id.checkedTextView, tasks);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            CheckedTextView textView = view.findViewById(R.id.checkedTextView);

            Bundle taskBundle = getItem(position);

            if (taskBundle != null) {
                String title = taskBundle.getString(TareaContract.TareaEntry.COLUMN_TITULO);
                String description = taskBundle.getString(TareaContract.TareaEntry.COLUMN_DESCRIPCION);
                int status = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID_ESTADO);
                boolean isCompleted = (status == 2);
                String statusText = isCompleted ? "[Completada]" : "[Pendiente]";

                String taskText = title;
                if (description != null && !description.isEmpty()) {
                    taskText += ": " + description;
                }

                textView.setText(String.format("%s %s", statusText, taskText));

                // Explicitly set the checked state of the view
                textView.setChecked(isCompleted);

                if (isCompleted) {
                    textView.setCheckMarkTintList(ColorStateList.valueOf(Color.GREEN));
                } else {
                    textView.setCheckMarkTintList(ColorStateList.valueOf(Color.YELLOW));
                }
            }

            return view;
        }
    }
}
