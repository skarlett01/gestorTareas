package com.example.gestortareas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Data.DBHelper;
import Data.TareaContract;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TareaAdapter tasksAdapter;
    private ListView listViewTasks;
    private Button buttonAddTask;
    private Button buttonModifyTask;
    private Button buttonDeleteTask;
    private DBHelper dbHelper;
    private int userId = 1;
    private TextToSpeech tts;
    private boolean talkBackEnabled;

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
                        if (talkBackEnabled && tts != null) {
                            tts.speak("Tarea " + taskName + " creada.", TextToSpeech.QUEUE_ADD, null, null);
                        }
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
                        if (talkBackEnabled && tts != null) {
                            tts.speak("Tarea " + taskName + " actualizada.", TextToSpeech.QUEUE_ADD, null, null);
                        }
                        loadTasks();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        talkBackEnabled = getIntent().getBooleanExtra("talkBackEnabled", false);
        if (talkBackEnabled) {
            tts = new TextToSpeech(this, this);
        }

        dbHelper = new DBHelper(this);
        listViewTasks = findViewById(R.id.listViewTasks);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        buttonModifyTask = findViewById(R.id.buttonModifyTask);
        buttonDeleteTask = findViewById(R.id.buttonDeleteTask);

        tasksAdapter = new TareaAdapter(this, new ArrayList<>());
        listViewTasks.setAdapter(tasksAdapter);
        listViewTasks.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listViewTasks.setOnItemClickListener((parent, view, position, id) -> {
            if (talkBackEnabled && tts != null) {
                Bundle taskBundle = tasksAdapter.getItem(position);
                if (taskBundle != null) {
                    String taskTitle = taskBundle.getString(TareaContract.TareaEntry.COLUMN_TITULO);
                    boolean isSelected = listViewTasks.isItemChecked(position);
                    String selectionMessage = isSelected ? " seleccionada." : " deseleccionada.";
                    tts.speak(taskTitle + selectionMessage, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });

        listViewTasks.setOnItemLongClickListener((parent, view, position, id) -> {
            Bundle taskBundle = tasksAdapter.getItem(position);
            if (taskBundle != null) {
                int taskId = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID);
                boolean isCompleted = taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID_ESTADO) == 2;
                dbHelper.actualizarEstadoTarea(taskId, !isCompleted);

                if (talkBackEnabled && tts != null) {
                    String statusMessage = !isCompleted ? " marcada como completada." : " marcada como pendiente.";
                    String title = taskBundle.getString(TareaContract.TareaEntry.COLUMN_TITULO);
                    tts.speak(title + statusMessage, TextToSpeech.QUEUE_FLUSH, null, null);
                }

                loadTasks();
                return true;
            }
            return false;
        });

        buttonAddTask.setOnClickListener(v -> {
            if (talkBackEnabled && tts != null) {
                tts.speak("Abriendo pantalla para crear tarea", TextToSpeech.QUEUE_FLUSH, null, null);
            }
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
                    if (talkBackEnabled && tts != null) {
                        tts.speak("Abriendo pantalla para modificar tarea", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                    Intent editTaskIntent = new Intent(MainActivity.this, PantallaCrearTarea.class);
                    editTaskIntent.putExtra("taskId", taskBundle.getInt(TareaContract.TareaEntry.COLUMN_ID));
                    editTaskIntent.putExtra("taskName", taskBundle.getString(TareaContract.TareaEntry.COLUMN_TITULO));
                    editTaskIntent.putExtra("taskDescription", taskBundle.getString(TareaContract.TareaEntry.COLUMN_DESCRIPCION));
                    editTaskIntent.putExtra("audioPath", taskBundle.getString(TareaContract.TareaEntry.COLUMN_AUDIO_PATH));
                    editTaskLauncher.launch(editTaskIntent);
                }
            } else if (selectedPositions.isEmpty()) {
                if (talkBackEnabled && tts != null) {
                    tts.speak("Selecciona una tarea para modificar", TextToSpeech.QUEUE_FLUSH, null, null);
                }
            } else {
                if (talkBackEnabled && tts != null) {
                    tts.speak("Selecciona solo una tarea para modificar", TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });

        buttonDeleteTask.setOnClickListener(v -> {
            SparseBooleanArray checked = listViewTasks.getCheckedItemPositions();
            ArrayList<Bundle> tasksToDelete = new ArrayList<>();
            for (int i = 0; i < checked.size(); i++) {
                if (checked.valueAt(i)) {
                    tasksToDelete.add(tasksAdapter.getItem(checked.keyAt(i)));
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
                String message = tasksToDelete.size() == 1 ? "1 tarea eliminada" : tasksToDelete.size() + " tareas eliminadas";
                if (talkBackEnabled && tts != null) {
                    tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            } else {
                if (talkBackEnabled && tts != null) {
                    tts.speak("Selecciona una o más tareas para eliminar", TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });

        loadTasks();
    }

    private void loadTasks() {
        List<Bundle> newTasks = dbHelper.getTareasByUser(userId);
        tasksAdapter.clear();
        tasksAdapter.addAll(newTasks);
        listViewTasks.clearChoices();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale spanish = new Locale("es", "ES");
            int result = tts.setLanguage(spanish);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "El lenguaje español no es soportado", Toast.LENGTH_SHORT).show();
            } else {
                tts.speak("Bienvenido al gestor de tareas", TextToSpeech.QUEUE_FLUSH, null, null);
            }
        } else {
            Toast.makeText(this, "Fallo al inicializar TextToSpeech", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
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
