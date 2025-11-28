package com.example.gestortareas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import Data.DBHelper;
import Data.TareaContract;

public class ModificarUsuarioActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText lastNameEditText;
    private EditText usernameEditText;
    private Button saveButton;
    private Button selectImageButton;
    private ImageView profileImageView;
    private DBHelper dbHelper;
    private int userId = 1;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    profileImageView.setImageURI(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_usuario);

        dbHelper = new DBHelper(this);

        nameEditText = findViewById(R.id.nameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        saveButton = findViewById(R.id.saveButton);
        selectImageButton = findViewById(R.id.selectImageButton);
        profileImageView = findViewById(R.id.profileImageView);

        loadUserProfile();

        selectImageButton.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String lastName = lastNameEditText.getText().toString();
            String username = usernameEditText.getText().toString();

            if (name.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            String imagePath = (selectedImageUri != null) ? selectedImageUri.toString() : getImagePathFromDatabase();
            
            try {
                dbHelper.actualizarUsuario(userId, name, lastName, username, imagePath);
                Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                finish();
            } catch (Exception e) {
                Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfile() {
        Bundle user = dbHelper.getUsuario(userId);
        if (user != null) {
            nameEditText.setText(user.getString(TareaContract.UsuarioEntry.COLUMN_NOMBRE));
            lastNameEditText.setText(user.getString(TareaContract.UsuarioEntry.COLUMN_APELLIDO));
            usernameEditText.setText(user.getString(TareaContract.UsuarioEntry.COLUMN_USERNAME));

            String imagePath = user.getString(TareaContract.UsuarioEntry.COLUMN_PROFILE_IMAGE_PATH);
            if (imagePath != null) {
                selectedImageUri = Uri.parse(imagePath);
                Glide.with(this).load(selectedImageUri).into(profileImageView);
            }
        }
    }

    private String getImagePathFromDatabase() {
        Bundle user = dbHelper.getUsuario(userId);
        if (user != null) {
            return user.getString(TareaContract.UsuarioEntry.COLUMN_PROFILE_IMAGE_PATH);
        }
        return null;
    }
}
