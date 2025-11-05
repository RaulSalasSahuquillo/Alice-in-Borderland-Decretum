package com.example.aliceinborderlanddecretum;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class GamePlay1 extends AppCompatActivity implements JoystickView.JoystickListener {

    private ImageView ivCharacter;
    private JoystickView joystick;

    // Game loop
    private final Handler gameLoopHandler = new Handler(Looper.getMainLooper());
    private final long loopDelay = 16; // ~60 FPS
    private float moveSpeed = 10.0f;
    private float joystickAngle = 0f;
    private float joystickStrength = 0f;

    // Fondo que cambia cada minuto
    private ConstraintLayout mainLayout;
    private final int[] backgrounds = new int[]{
            R.drawable.bg1,
            R.drawable.bg2,
            R.drawable.bg3
    };
    private int currentIndex = 0;
    private final Handler bgHandler = new Handler(Looper.getMainLooper());

    private final Runnable gameLoop = new Runnable() {
        @Override
        public void run() {
            if (joystickStrength > 0f) {
                float deltaX = (float) (Math.cos(joystickAngle) * moveSpeed * joystickStrength);
                float deltaY = (float) (Math.sin(joystickAngle) * moveSpeed * joystickStrength);
                ivCharacter.setX(ivCharacter.getX() + deltaX);
                ivCharacter.setY(ivCharacter.getY() + deltaY);
            }
            gameLoopHandler.postDelayed(this, loopDelay);
        }
    };

    private final Runnable backgroundChanger = new Runnable() {
        @Override
        public void run() {
            currentIndex = (currentIndex + 1) % backgrounds.length;
            mainLayout.setBackgroundResource(backgrounds[currentIndex]);
            bgHandler.postDelayed(this, 60_000); // cada 1 minuto
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game1); // asegúrate de que este es tu layout

        ivCharacter = findViewById(R.id.ivCharacter);
        joystick = findViewById(R.id.joystick);
        mainLayout = findViewById(R.id.main); // el ConstraintLayout raíz debe tener android:id="@+id/main"

        joystick.setJoystickListener(this);

        // Fondo inicial
        mainLayout.setBackgroundResource(backgrounds[currentIndex]);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Arrancar bucles
        gameLoopHandler.postDelayed(gameLoop, loopDelay);
        bgHandler.postDelayed(backgroundChanger, 60_000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Parar bucles para evitar fugas y CPU innecesaria
        gameLoopHandler.removeCallbacks(gameLoop);
        bgHandler.removeCallbacks(backgroundChanger);
    }

    @Override
    public void onMove(float angle, float strength) {
        this.joystickAngle = angle;
        this.joystickStrength = strength;
    }
}
