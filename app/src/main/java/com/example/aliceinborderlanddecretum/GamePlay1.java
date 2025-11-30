package com.example.aliceinborderlanddecretum;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class GamePlay1 extends AppCompatActivity implements JoystickView.JoystickListener {

    private ImageView ivCharacter;
    private JoystickView joystick;
    private Button btnJump;
    private final Handler gameLoopHandler = new Handler(Looper.getMainLooper());
    private final Handler bgHandler = new Handler(Looper.getMainLooper());

    private final long loopDelay = 16; // ~60 FPS
    private float moveSpeed = 5.0f;
    private float joystickAngle = 0f;
    private float joystickStrength = 0f;

    // --- CONFIGURACIÓN DE LA ZONA DE SALTO ---
    // Ajusta TARGET_X e Y según la posición real en tu pantalla
    private float targetX_px;
    private float targetY_px;
    private final float ACTION_RADIUS = 150f;

    // Fondo
    private ConstraintLayout mainLayout;
    private final int[] backgrounds = new int[]{
            R.drawable.bg1,
            R.drawable.bg2,
            R.drawable.bg3
    };
    private int currentIndex = 0;

    // Runnable del juego (Movimiento y chequeo de zona)
    private final Runnable gameLoop = new Runnable() {
        @Override
        public void run() {
            // 1. Lógica de movimiento
            if (joystickStrength > 0f && ivCharacter.getVisibility() == View.VISIBLE) {
                float deltaX = (float) (Math.cos(joystickAngle) * moveSpeed * joystickStrength);
                float deltaY = (float) (Math.sin(joystickAngle) * moveSpeed * joystickStrength);
                ivCharacter.setX(ivCharacter.getX() + deltaX);
                ivCharacter.setY(ivCharacter.getY() + deltaY);
            }

            // 2. Comprobar si estamos cerca del punto de salto
            checkDistanceToTarget();

            // 3. Repetir loop
            gameLoopHandler.postDelayed(this, loopDelay);
        }
    };

    // Runnable del cambio de fondo
    private final Runnable backgroundChanger = new Runnable() {
        @Override
        public void run() {
            currentIndex = (currentIndex + 1) % backgrounds.length;
            mainLayout.setBackgroundResource(backgrounds[currentIndex]);
            bgHandler.postDelayed(this, 60_000); // 1 minuto
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game1);

        targetX_px = dpToPx(568f); // 556dp (marginStart)  DEBE CORREGIRSE LA COORDENADA PARA QUE SALGA EXACTAMENTE POR LA VENTANA. ESTA ES APROXIMADA
        targetY_px = dpToPx(28f);  // 36dp (marginTop)     DEBE CORREGIRSE LA COORDENADA PARA QUE SALGA EXACTAMENTE POR LA VENTANA. ESTA ES APROXIMADA
            // -----------------------------------------------

        ivCharacter = findViewById(R.id.ivCharacter);

        // Vincular Vistas
        ivCharacter = findViewById(R.id.ivCharacter);
        joystick = findViewById(R.id.joystick);
        mainLayout = findViewById(R.id.main);
        btnJump = findViewById(R.id.btnJump);

        joystick.setJoystickListener(this);
        mainLayout.setBackgroundResource(backgrounds[currentIndex]);

        // --- LÓGICA DE CARGA (SAVE MANAGER) ---
        // 1. Cargamos el estado actual desde el JSON
        GameState currentState = SaveManager.load(this);

        // 2. Verificamos si existe el estado y si 'hasJumped' es true
        if (currentState != null && currentState.hasJumped) {
            // CASO: YA SALTÓ ANTES
            ivCharacter.setVisibility(View.GONE);  // Personaje oculto
            btnJump.setVisibility(View.GONE);      // Botón oculto
            joystick.setEnabled(false);            // Joystick desactivado
        } else {
            // CASO: AÚN NO HA SALTADO
            btnJump.setOnClickListener(v -> performJumpAction());
        }
    }

    // Método que calcula la distancia entre el personaje y el punto objetivo
    private void checkDistanceToTarget() {
        if (ivCharacter.getVisibility() != View.VISIBLE) return;

        // Usamos las variables convertidas a píxeles
        float dx = ivCharacter.getX() - targetX_px;
        float dy = ivCharacter.getY() - targetY_px;

        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= ACTION_RADIUS) {
            if (btnJump.getVisibility() != View.VISIBLE) {
                btnJump.setVisibility(View.VISIBLE);
            }
        } else {
            if (btnJump.getVisibility() == View.VISIBLE) {
                btnJump.setVisibility(View.GONE);
            }
        }
    }

    // Acción al pulsar el botón
    private void performJumpAction() {
        btnJump.setEnabled(false);

        ivCharacter.animate()
                .translationYBy(-300f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .alpha(0f)
                .setDuration(600)
                .withEndAction(() -> {
                    // 1. Ocultar cosas
                    ivCharacter.setVisibility(View.GONE);
                    btnJump.setVisibility(View.GONE);

                    // 2. Guardar que hemos pasado al nivel 2
                    saveGameJumpState();

                    // 3. IR DIRECTAMENTE A GAMEPLAY 2
                    // ▼▼▼ CAMBIO AQUÍ ▼▼▼
                    android.content.Intent intent = new android.content.Intent(GamePlay1.this, GamePlay2.class);
                    startActivity(intent);
                    finish(); // Cierra el nivel 1 para no poder volver atrás
                })
                .start();
    }

    // Método auxiliar para guardar en JSON
    private void saveGameJumpState() {
        GameState state = SaveManager.load(this);
        if (state == null) state = new GameState();

        state.hasJumped = true;
        state.currentSceneId = "scene_002"; // <--- Importante para saber que estamos en el nivel 2

        SaveManager.save(this, state);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Solo iniciamos el loop de juego si el personaje es visible (no ha saltado)
        if (ivCharacter.getVisibility() == View.VISIBLE) {
            gameLoopHandler.postDelayed(gameLoop, loopDelay);
        }

        bgHandler.postDelayed(backgroundChanger, 60_000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameLoopHandler.removeCallbacks(gameLoop);
        bgHandler.removeCallbacks(backgroundChanger);
    }

    @Override
    public void onMove(float angle, float strength) {
        this.joystickAngle = angle;
        this.joystickStrength = strength;
    }
    // Método auxiliar para pasar de dp (XML) a px (Java)
    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}