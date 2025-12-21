package com.example.aliceinborderlanddecretum;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;

public class GamePlay2 extends AppCompatActivity {

    private VideoView videoIntro;
    private ConstraintLayout layoutCardGame;
    private Button btnDrawCard;
    private ImageView ivMyCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game2);

        // 1. VINCULAR VISTAS
        videoIntro = findViewById(R.id.videoIntro);
        layoutCardGame = findViewById(R.id.layoutCardGame);
        btnDrawCard = findViewById(R.id.btnDrawCard);
        ivMyCard = findViewById(R.id.ivMyCard);

        // 2. CONFIGURAR EL VIDEO
        // "android.resource://" + paquete + "/" + R.raw.nombre_video
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.the_fool;
        Uri uri = Uri.parse(videoPath);
        videoIntro.setVideoURI(uri);

        // Listener: Qué pasa cuando el video termina
        videoIntro.setOnCompletionListener(mp -> startGame());

        // Opcional: Tocar el video para saltarlo (útil para pruebas)
        videoIntro.setOnTouchListener((v, event) -> {
            startGame();
            return true;
        });

        // Arrancar video
        videoIntro.start();

        // 3. LÓGICA DEL JUEGO DE CARTAS (Ejemplo simple)
        btnDrawCard.setOnClickListener(v -> {
            // Aquí pondrás tu lógica de cartas más adelante
            // Por ejemplo, cambiar la imagen de la carta:
            // ivMyCard.setImageResource(R.drawable.as_de_corazones);
        });

        // 4. GUARDAR QUE ESTAMOS AQUÍ
        saveLevelProgress();
    }

    private void startGame() {
        // Detener video si sigue sonando
        if (videoIntro.isPlaying()) {
            videoIntro.stopPlayback();
        }

        // Ocultar video y mostrar juego
        videoIntro.setVisibility(View.GONE);
        layoutCardGame.setVisibility(View.VISIBLE);
    }

    private void saveLevelProgress() {
        GameState state = SaveManager.load(this);
        if (state == null) state = new GameState();

        // Aseguramos que si cierran la app, vuelvan a esta escena
        if (!"scene_002".equals(state.currentSceneId)) {
            state.currentSceneId = "scene_002";
            state.hasJumped = true;
            SaveManager.save(this, state);
        }
    }
}