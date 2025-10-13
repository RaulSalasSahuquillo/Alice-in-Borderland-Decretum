package com.example.aliceinborderlanddecretum;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

public class IntroActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    @Nullable private Long resumePositionMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        playerView = findViewById(R.id.player_view);
        Button btnSkip = findViewById(R.id.btn_skip);
        btnSkip.setOnClickListener(v -> finishIntro());

        // Bloquear botón atrás con el API nuevo
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                // no hacemos nada (bloqueado)
            }
        });

        enterImmersive();
    }

    @Override protected void onStart() {
        super.onStart();
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);

            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.intro);
            player.setMediaItem(MediaItem.fromUri(uri));
            player.prepare();
            if (resumePositionMs != null) player.seekTo(resumePositionMs);
            player.setPlayWhenReady(true);

            player.addListener(new Player.Listener() {
                @Override public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_ENDED) finishIntro();
                }
            });
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (player != null) player.play();
        enterImmersive();
    }

    @Override protected void onPause() {
        super.onPause();
        if (player != null) {
            resumePositionMs = player.getCurrentPosition();
            player.pause();
        }
    }

    @Override protected void onStop() {
        super.onStop();
        if (player != null) { player.release(); player = null; }
    }

    private void finishIntro() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit().putBoolean("intro_seen", true).apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void enterImmersive() {
        View d = getWindow().getDecorView();
        d.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
}

