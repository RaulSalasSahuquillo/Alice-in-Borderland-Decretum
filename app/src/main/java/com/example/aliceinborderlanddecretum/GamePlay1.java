package com.example.aliceinborderlanddecretum;

import android.os.Bundle;
import android.os.Handler; // <<< IMPORTA ESTO
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

// 1. Implementa la interface que creamos en JoystickView
public class GamePlay1 extends AppCompatActivity implements JoystickView.JoystickListener {

    private ImageView ivCharacter;
    private JoystickView joystick;

    // Variables para el Game Loop
    private Handler gameLoopHandler = new Handler();
    private final long loopDelay = 16; // 1000ms / 60fps = ~16ms
    private float moveSpeed = 10.0f; // Píxeles por "tick" de movimiento

    // Variables para guardar la entrada del joystick
    private float joystickAngle = 0;
    private float joystickStrength = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game1);

        ivCharacter = findViewById(R.id.ivCharacter);
        joystick = findViewById(R.id.joystick);

        // 2. Conecta la Activity al Joystick
        joystick.setJoystickListener(this);

        // 3. Inicia el Game Loop
        gameLoopHandler.postDelayed(gameLoop, loopDelay);
    }

    // 4. Este es el Game Loop
    private Runnable gameLoop = new Runnable() {
        @Override
        public void run() {
            // Solo nos movemos si el joystick tiene "fuerza"
            if (joystickStrength > 0) {

                // Calcula el movimiento en X e Y usando trigonometría
                // Math.cos(angle) nos da el movimiento X (-1 a 1)
                // Math.sin(angle) nos da el movimiento Y (-1 a 1)
                float deltaX = (float) (Math.cos(joystickAngle) * moveSpeed * joystickStrength);
                float deltaY = (float) (Math.sin(joystickAngle) * moveSpeed * joystickStrength);

                // Actualiza la posición del personaje
                // Usamos setX y setY para moverlo a una posición absoluta
                ivCharacter.setX(ivCharacter.getX() + deltaX);
                ivCharacter.setY(ivCharacter.getY() + deltaY);
            }

            // Vuelve a ejecutar este mismo código después del "delay"
            gameLoopHandler.postDelayed(this, loopDelay);
        }
    };

    // 5. Este método se llama CADA VEZ que el joystick se mueve
    @Override
    public void onMove(float angle, float strength) {
        // Simplemente guardamos los valores.
        // El "Game Loop" se encargará de usarlos.
        this.joystickAngle = angle;
        this.joystickStrength = strength;
    }

    // Limpia el loop cuando la actividad se pausa
    @Override
    protected void onPause() {
        super.onPause();
        gameLoopHandler.removeCallbacks(gameLoop);
    }

    // Vuelve a iniciar el loop cuando la actividad se reanuda
    @Override
    protected void onResume() {
        super.onResume();
        gameLoopHandler.postDelayed(gameLoop, loopDelay);
    }
}