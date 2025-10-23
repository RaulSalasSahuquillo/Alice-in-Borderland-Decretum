package com.example.aliceinborderlanddecretum;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private GameState currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Cargar estado existente (si lo hay)
        currentState = SaveManager.load(this);

        // 2) Mostrar intro sólo si NO hay save y no se ha visto la intro
        boolean seen = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("intro_seen", false);
        if (currentState == null && !seen) {
            startActivity(new Intent(this, IntroActivity.class));
            finish();
            return;
        }

        // 3) Si no hay save, crea uno nuevo
        if (currentState == null) {
            currentState = new GameState();
            currentState.currentSceneId = "scene_001"; // tu escena inicial jugable
            SaveManager.save(this, currentState);
        }

        // 4) Monta UI
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 5) Cargar la escena guardada
        goToScene(currentState.currentSceneId, false);
    }

    // === NAVEGACIÓN DE ESCENAS (ejemplos) ===
    private void goToScene(String sceneId, boolean autosave) {
        currentState.currentSceneId = sceneId;
        if (autosave) SaveManager.save(this, currentState);
        // Puedes usar Fragments o simplemente cambiar vistas/estado.
        // Ejemplo mínimo (placeholder):
        Toast.makeText(this, "Escena: " + sceneId, Toast.LENGTH_SHORT).show();

        // Si usas Fragments:
        // getSupportFragmentManager().beginTransaction()
        //     .replace(R.id.main, SceneFragment.newInstance(sceneId))
        //     .commit();
    }

    // === Helpers típicos para decisiones, variables e inventario ===
    private void setFlag(String flag) {
        currentState.flags.add(flag);
        SaveManager.save(this, currentState);
    }

    private boolean hasFlag(String flag) { return currentState.flags.contains(flag); }

    private void addVar(String key, int delta) {
        int v = currentState.vars.getOrDefault(key, 0) + delta;
        currentState.vars.put(key, v);
        SaveManager.save(this, currentState);
    }

    private int getVar(String key) { return currentState.vars.getOrDefault(key, 0); }

    private void addItem(String id) {
        if (!currentState.inventory.contains(id)) currentState.inventory.add(id);
        SaveManager.save(this, currentState);
    }

    private boolean hasItem(String id) { return currentState.inventory.contains(id); }

    private void nextScene(String id) { goToScene(id, true); }

    // === Autoguardado de seguridad al salir de la Activity ===
    @Override
    protected void onPause() {
        super.onPause();
        if (currentState != null) SaveManager.save(this, currentState);
    }

    // === Nueva partida (por si añades un botón en ajustes) ===
    private void newGame() {
        SaveManager.clear(this);
        currentState = new GameState();
        currentState.currentSceneId = "scene_001";
        SaveManager.save(this, currentState);
        goToScene("scene_001", false);
    }
}

