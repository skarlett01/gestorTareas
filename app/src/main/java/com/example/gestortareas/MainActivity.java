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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
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
    private ImageButton profileButton;
    private DBHelper dbHelper;
    private int userId = 1;
    private SparseBooleanArray selectedItems;

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

        selectedItems = new SparseBooleanArray();
        dbHelper = new DBHelper(this);
        listViewTasks = findViewById(R.id.listViewTasks);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        buttonModifyTask = findViewById(R.id.buttonModifyTask);
        buttonDeleteTask = findViewById(R.id.buttonDeleteTask);
        profileButton = findViewById(R.id.profileButton);

        tasksAdapter = new TareaAdapter(this, new ArrayList<>());
        listViewTasks.setAdapter(tasksAdapter);

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PerfilUsuarioActivity.class);
            startActivity(intent);
        });

        listViewTasks.setOnItemClickListener((parent, view, position, id) -> {
            if (selectedItems.get(position, false)) {
                selectedItems.delete(position);
            } else {
                selectedItems.put(position, true);
            }
            tasksAdapter.notifyDataSetChanged();
        });

        listViewTasks.setOnItemLongClickListener((parent, view, position, id) -> {
            Bundle taskBundle = tasksAdapter.getItem(position);
            if (taskBundle != null) {
                int taskId = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID);
                boolean isCompleted = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID_ESTADO) == 2;
                dbHelper.actualizarEstadoTarea(taskId, !isCompleted);
                loadTasks();
                return true;
            }
            return false;
        });

        buttonAddTask.setOnClickListener(v -> {
            Intent createTaskIntent = new Intent(MainActivity.this, PantallaCrearTarea.class);
            createTaskLauncher.launch(createTaskIntent);
        });

        buttonModifyTask.setOnClickListener(v -> {
            ArrayList<Integer> selectedPositions = new ArrayList<>();
            for (int i = 0; i < selectedItems.size(); i++) {
                if (selectedItems.valueAt(i)) {
                    selectedPositions.add(selectedItems.keyAt(i));
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
            ArrayList<Bundle> tasksToDelete = new ArrayList<>();
            for (int i = 0; i < selectedItems.size(); i++) {
                if (selectedItems.valueAt(i)) {
                    int position = selectedItems.keyAt(i);
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
        selectedItems.clear();
    }

    private static class ViewHolder {
        CheckBox checkBox;
        TextView textView;
    }

    public class TareaAdapter extends ArrayAdapter<Bundle> {

        public TareaAdapter(Context context, List<Bundle> tasks) {
            super(context, 0, tasks);
        }

        @Override
        public long getItemId(int position) {
            Bundle taskBundle = getItem(position);
            if (taskBundle != null) {
                return taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID);
            }
            return -1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_tarea, parent, false);
                holder = new ViewHolder();
                holder.checkBox = convertView.findViewById(R.id.task_checkbox);
                holder.textView = convertView.findViewById(R.id.task_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

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

                holder.textView.setText(String.format("%s %s", statusText, taskText));
                holder.checkBox.setChecked(isCompleted);

                // This ensures the checkbox has a visible color for its state.
                if (isCompleted) {
                    holder.checkBox.setButtonTintList(ColorStateList.valueOf(Color.GREEN));
                } else {
                    holder.checkBox.setButtonTintList(ColorStateList.valueOf(Color.GRAY));
                }

                if (selectedItems.get(position, false)) {
                    convertView.setBackgroundColor(Color.LTGRAY);
                } else {
                    convertView.setBackgroundColor(Color.TRANSPARENT);
                }
            }

            return convertView;
        }
    }
}
