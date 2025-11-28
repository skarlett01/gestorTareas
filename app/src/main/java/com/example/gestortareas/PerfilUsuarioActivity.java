package com.example.gestortareas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import Data.DBHelper;
import Data.TareaContract;

public class PerfilUsuarioActivity extends AppCompatActivity {

    private ImageButton backButton;
    private ImageButton profileImageButton;
    private TextView nameTextView;
    private TextView usernameTextView;
    private Button modifyProfileButton;
    private DBHelper dbHelper;
    private int userId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_usuario);

        dbHelper = new DBHelper(this);

        backButton = findViewById(R.id.backButton);
        profileImageButton = findViewById(R.id.profileImageButton);
        nameTextView = findViewById(R.id.nameTextView);
        usernameTextView = findViewById(R.id.usernameTextView);
        modifyProfileButton = findViewById(R.id.modifyProfileButton);

        loadUserProfile();

        backButton.setOnClickListener(v -> {
            finish();
        });

        View.OnClickListener listener = v -> {
            Intent intent = new Intent(PerfilUsuarioActivity.this, ModificarUsuarioActivity.class);
            startActivity(intent);
        };

        profileImageButton.setOnClickListener(listener);
        modifyProfileButton.setOnClickListener(listener);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        Bundle user = dbHelper.getUsuario(userId);
        if (user != null) {
            String name = user.getString(TareaContract.UsuarioEntry.COLUMN_NOMBRE);
            String lastName = user.getString(TareaContract.UsuarioEntry.COLUMN_APELLIDO);
            String username = user.getString(TareaContract.UsuarioEntry.COLUMN_USERNAME);
            String imagePath = user.getString(TareaContract.UsuarioEntry.COLUMN_PROFILE_IMAGE_PATH);

            StringBuilder fullName = new StringBuilder();
            if (name != null && !name.isEmpty()) {
                fullName.append(name);
            }
            if (lastName != null && !lastName.isEmpty()) {
                if (fullName.length() > 0) {
                    fullName.append(" ");
                }
                fullName.append(lastName);
            }

            if (fullName.length() > 0) {
                nameTextView.setText(fullName.toString());
            } else {
                nameTextView.setText("Nombre y Apellido");
            }

            if (username != null && !username.isEmpty()) {
                usernameTextView.setText(username);
            } else {
                usernameTextView.setText("Nombre de usuario");
            }

            if (imagePath != null && !imagePath.isEmpty()) {
                Glide.with(this).load(Uri.parse(imagePath)).into(profileImageButton);
            } else {
                profileImageButton.setImageResource(android.R.drawable.ic_menu_myplaces);
            }
        }
    }
}
