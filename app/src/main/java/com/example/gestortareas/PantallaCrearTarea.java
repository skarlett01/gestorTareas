package com.example.gestortareas;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PantallaCrearTarea extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private EditText editTextTaskName;
    private EditText editTextTaskDescription;
    private Button buttonSaveTask;
    private Button buttonCancelTask;
    private Button buttonRecord;
    private Button buttonStop;
    private Button buttonPlay;
    private Button buttonPlayOldAudio;
    private Button buttonDeleteAudio;
    private LinearLayout existingAudioButtonsLayout;
    private TextView titleTextView;
    private SeekBar seekBarAudio;
    private TextView textCurrentTime, textTotalTime;
    private LinearLayout playbackSeekBarLayout;

    private int taskId = -1;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String currentAudioPath;
    private String originalAudioPath;
    private Handler seekBarHandler = new Handler();
    private Runnable updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_crear_tarea);

        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);
        buttonCancelTask = findViewById(R.id.buttonCancelTask);
        buttonRecord = findViewById(R.id.buttonRecord);
        buttonStop = findViewById(R.id.buttonStop);
        buttonPlay = findViewById(R.id.buttonPlay);
        buttonPlayOldAudio = findViewById(R.id.buttonPlayOldAudio);
        buttonDeleteAudio = findViewById(R.id.buttonDeleteAudio);
        existingAudioButtonsLayout = findViewById(R.id.existingAudioButtonsLayout);
        titleTextView = findViewById(R.id.textView2);
        seekBarAudio = findViewById(R.id.seekBarAudio);
        textCurrentTime = findViewById(R.id.textCurrentTime);
        textTotalTime = findViewById(R.id.textTotalTime);
        playbackSeekBarLayout = findViewById(R.id.playbackSeekBarLayout);

        buttonStop.setEnabled(false);
        buttonPlay.setEnabled(false);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("taskId")) {
            titleTextView.setText("Modificar Tarea");
            taskId = intent.getIntExtra("taskId", -1);
            String taskName = intent.getStringExtra("taskName");
            String taskDescription = intent.getStringExtra("taskDescription");
            currentAudioPath = intent.getStringExtra("audioPath");
            originalAudioPath = currentAudioPath;

            editTextTaskName.setText(taskName);
            editTextTaskDescription.setText(taskDescription);

            if (currentAudioPath != null && !currentAudioPath.isEmpty()) {
                existingAudioButtonsLayout.setVisibility(View.VISIBLE);
                playbackSeekBarLayout.setVisibility(View.VISIBLE);
            }
        } else {
            existingAudioButtonsLayout.setVisibility(View.GONE);
            playbackSeekBarLayout.setVisibility(View.GONE);
        }

        buttonSaveTask.setOnClickListener(v -> {
            String taskName = editTextTaskName.getText().toString();
            String taskDescription = editTextTaskDescription.getText().toString();

            if (!taskName.isEmpty()) {
                if (originalAudioPath != null && !originalAudioPath.equals(currentAudioPath)) {
                    File oldFile = new File(originalAudioPath);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                Intent resultIntent = new Intent();
                resultIntent.putExtra("taskName", taskName);
                resultIntent.putExtra("taskDescription", taskDescription);
                resultIntent.putExtra("audioPath", currentAudioPath);
                if (taskId != -1) {
                    resultIntent.putExtra("taskId", taskId);
                }
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        buttonCancelTask.setOnClickListener(v -> finish());

        buttonRecord.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                startRecording();
            }
        });

        buttonStop.setOnClickListener(v -> stopRecording());

        buttonPlay.setOnClickListener(v -> playRecording(currentAudioPath));
        buttonPlayOldAudio.setOnClickListener(v -> playRecording(originalAudioPath));

        buttonDeleteAudio.setOnClickListener(v -> {
            currentAudioPath = null;
            existingAudioButtonsLayout.setVisibility(View.GONE);
            playbackSeekBarLayout.setVisibility(View.GONE);
            Toast.makeText(this, "Audio eliminado. Guarda para confirmar.", Toast.LENGTH_SHORT).show();
        });

        seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void startRecording() {
        existingAudioButtonsLayout.setVisibility(View.GONE);
        playbackSeekBarLayout.setVisibility(View.GONE);
        File audioDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (audioDir != null) {
            if (!audioDir.exists()) {
                audioDir.mkdirs();
            }
            File audioFile = new File(audioDir, System.currentTimeMillis() + ".3gp");
            currentAudioPath = audioFile.getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(currentAudioPath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                buttonRecord.setEnabled(false);
                buttonStop.setEnabled(true);
                buttonPlay.setEnabled(false);
                Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            buttonRecord.setEnabled(true);
            buttonStop.setEnabled(false);
            buttonPlay.setEnabled(true);
            playbackSeekBarLayout.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Grabación detenida.", Toast.LENGTH_SHORT).show();
        }
    }

    private void playRecording(String path) {
        if (path == null || path.isEmpty()){
            Toast.makeText(this, "No hay audio para reproducir.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
            initializeSeekBar();
            Toast.makeText(this, "Reproduciendo...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "No se pudo reproducir el audio.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeSeekBar() {
        seekBarAudio.setMax(mediaPlayer.getDuration());
        textTotalTime.setText(formatTime(mediaPlayer.getDuration()));

        updateSeekBar = () -> {
            if (mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                seekBarAudio.setProgress(currentPosition);
                textCurrentTime.setText(formatTime(currentPosition));
                seekBarHandler.postDelayed(updateSeekBar, 1000);
            }
        };
        seekBarHandler.postDelayed(updateSeekBar, 1000);

        mediaPlayer.setOnCompletionListener(mp -> {
            seekBarHandler.removeCallbacks(updateSeekBar);
            seekBarAudio.setProgress(0);
            textCurrentTime.setText("0:00");
        });
    }

    private String formatTime(int millis) {
        return String.format(Locale.getDefault(), "%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, "Permiso de grabación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (seekBarHandler != null && updateSeekBar != null) {
            seekBarHandler.removeCallbacks(updateSeekBar);
        }
    }
}
