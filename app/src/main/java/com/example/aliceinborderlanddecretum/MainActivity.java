package com.example.aliceinborderlanddecretum;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Button;
import android.widget.TextView;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private GameState currentState;
    private TextView tvSceneTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentState = SaveManager.load(this);

        // ... (Tu código de intro y save está perfecto) ...
        boolean seen = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("intro_seen", false);
        if (currentState == null && !seen) {
            startActivity(new Intent(this, IntroActivity.class));
            finish();
            return;
        }

        if (currentState == null) {
            currentState = new GameState();
            currentState.currentSceneId = "scene_001";
            SaveManager.save(this, currentState);
        }

        // ... (Tu código de UI está perfecto) ...
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 5) CONECTA LOS ELEMENTOS DEL XML
        tvSceneTextView = findViewById(R.id.tvScene);
        Button buttonPlay = findViewById(R.id.btnNext);
        Button buttonGiveUp = findViewById(R.id.btnNewGame);

        // 6) ASIGNA LOS CLICKS (LISTENERS)

        // --- ▼▼▼ AQUÍ ESTÁ EL CAMBIO ▼▼▼ ---
        // Al presionar PLAY, inicia la Activity GamePlay1
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Prepara la "intención" de ir de MainActivity a GamePlay1
                Intent intent = new Intent(MainActivity.this, GamePlay1.class);

                // 2. Ejecuta la intención (¡Esto cambia la pantalla!)
                startActivity(intent);
            }
        });
        // --- ▲▲▲ AQUÍ TERMINA EL CAMBIO ▲▲▲ ---

        // Al presionar GIVE UP, llama a newGame()
        buttonGiveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame(); // Llama a tu función de nueva partida
            }
        });

        // 7) Cargar la escena guardada
        goToScene(currentState.currentSceneId, false);
    }

    // === NAVEGACIÓN DE ESCENAS (ejemplos) ===

    // Este método AHORA solo se usa para cargar la escena
    // inicial en el TextView cuando se abre la app.
    private void goToScene(String sceneId, boolean autosave) {
        currentState.currentSceneId = sceneId;
        if (autosave) SaveManager.save(this, currentState);

        if (tvSceneTextView != null) {
            if (sceneId.equals("scene_001")) {
                tvSceneTextView.setText("GAME: おおあめ (Escena 1)");
            } else if (sceneId.equals("scene_002")) {
                tvSceneTextView.setText("GAME: おおあめ [CARD: 1]");
            } else {
                tvSceneTextView.setText("Escena: " + sceneId);
            }
        }
    }

    // ... (El resto de tu código está perfecto) ...
    private void setFlag(String flag) { /*...*/ }
    private boolean hasFlag(String flag) { /*...*/ return false; }
    private void addVar(String key, int delta) { /*...*/ }
    private int getVar(String key) { /*...*/ return 0; }
    private void addItem(String id) { /*...*/ }
    private boolean hasItem(String id) { /*...*/ return false; }

    // Este método "nextScene" ya no se usa con el botón PLAY,
    // pero puedes usarlo si quieres cambiar el texto en el futuro.
    private void nextScene(String id) { goToScene(id, true); }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentState != null) SaveManager.save(this, currentState);
    }

    private void newGame() {
        SaveManager.clear(this);
        currentState = new GameState();
        currentState.currentSceneId = "scene_001";
        SaveManager.save(this, currentState);
        goToScene("scene_001", false);
    }
}