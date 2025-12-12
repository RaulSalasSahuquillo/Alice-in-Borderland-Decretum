package com.example.aliceinborderlanddecretum;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private GameState currentState;
    private TextView tvSceneTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. CARGAMOS EL ESTADO GUARDADO
        currentState = SaveManager.load(this);

        // --- ▼▼▼ BLOQUE NUEVO: REDIRECCIÓN AUTOMÁTICA ▼▼▼ ---
        // Preguntamos: ¿Hay partida guardada Y vamos por la escena 2?
        if (currentState != null && "scene_002".equals(currentState.currentSceneId)) {
            // SÍ -> Abrimos directamente el Juego 2
            Intent intent = new Intent(this, GamePlay2.class);
            startActivity(intent);
            finish(); // Cerramos el menú para que no se vea
            return;   // ¡IMPORTANTE! Detenemos el código aquí.
        }
        // --- ▲▲▲ FIN DEL BLOQUE NUEVO ▲▲▲ ---

        // ... (Tu código de intro y save sigue aquí igual que antes) ...
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

        // ... (Tu código de UI) ...
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

        // BOTÓN JUGAR:
        // Como ya filtramos arriba la escena 2, si el usuario está viendo este botón
        // es OBLIGATORIO que esté en la Escena 1 (o partida nueva).
        // Por eso, aquí siempre lanzamos GamePlay1.
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GamePlay1.class);
                startActivity(intent);
            }
        });

        // BOTÓN RENDIRSE (NUEVA PARTIDA):
        buttonGiveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame();
            }
        });

        // 7) Cargar el texto de la escena actual
        goToScene(currentState.currentSceneId, false);
    }

    // === NAVEGACIÓN DE ESCENAS (TEXTOS) ===
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

    // ... (El resto de tus métodos auxiliares se quedan igual) ...
    private void setFlag(String flag) { /*...*/ }
    private boolean hasFlag(String flag) { /*...*/ return false; }
    private void addVar(String key, int delta) { /*...*/ }
    private int getVar(String key) { /*...*/ return 0; }
    private void addItem(String id) { /*...*/ }
    private boolean hasItem(String id) { /*...*/ return false; }
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
        // IMPORTANTE: Resetear también el salto si empiezas de cero
        currentState.hasJumped = false;

        SaveManager.save(this, currentState);
        goToScene("scene_001", false);
    }
}