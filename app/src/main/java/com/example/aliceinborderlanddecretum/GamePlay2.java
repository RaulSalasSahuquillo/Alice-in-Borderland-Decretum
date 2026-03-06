package com.example.aliceinborderlanddecretum;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GamePlay2 extends AppCompatActivity {

    private enum FaseJuego { FASE_1_PAREJAS, MODO_PANICO_JUGADOR, FASE_2_BATALLA }
    private enum TipoEfecto { NINGUNO, ACIDO, FLECHA, JUMPSCARE }

    class Carta {
        int resId;
        int peso;
        TipoEfecto efecto;

        Carta(int resId, int peso) { // Fase 1
            this.resId = resId;
            this.peso = peso;
            this.efecto = TipoEfecto.NINGUNO;
        }

        Carta(int resId, TipoEfecto efecto) { // Fase 2
            this.resId = resId;
            this.peso = 0;
            this.efecto = efecto;
        }
    }

    // --- UI ---
    private VideoView videoIntro;
    private ConstraintLayout layoutCardGame;
    private ImageView ivMontoCoger, ivMontoDescarte;
    private ImageView[] ivPlayerCards = new ImageView[3];
    private ImageView[] ivRivalCards = new ImageView[3];
    private TextView tvStatusInfo;

    // --- LÓGICA ---
    private List<Carta> mazo = new ArrayList<>();
    private List<Carta> mazoEspeciales = new ArrayList<>();
    private List<Carta> manoJugador = new ArrayList<>();
    private List<Carta> manoRival = new ArrayList<>();

    private FaseJuego faseActual = FaseJuego.FASE_1_PAREJAS;
    private boolean turnoJugador = true;
    private boolean esperandoDescarte = false;
    private int hpJugador = 100, hpRival = 100;
    private int intentosPanicoRestantes = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game2);
        vincularVistas();
        configurarVideo();
    }

    private void vincularVistas() {
        videoIntro = findViewById(R.id.videoIntro);
        layoutCardGame = findViewById(R.id.layoutCardGame);
        ivMontoCoger = findViewById(R.id.ivMontoCoger);
        ivMontoDescarte = findViewById(R.id.ivMontoDescarte);
        tvStatusInfo = findViewById(R.id.tvStatusInfo);

        ivPlayerCards[0] = findViewById(R.id.ivPlayer1);
        ivPlayerCards[1] = findViewById(R.id.ivPlayer2);
        ivPlayerCards[2] = findViewById(R.id.ivPlayer3);

        ivRivalCards[0] = findViewById(R.id.ivRival1);
        ivRivalCards[1] = findViewById(R.id.ivRival2);
        ivRivalCards[2] = findViewById(R.id.ivRival3);

        ivMontoCoger.setOnClickListener(v -> robarCarta());

        for (int i = 0; i < 3; i++) {
            final int index = i;
            ivPlayerCards[i].setOnClickListener(v -> clickEnCartaJugador(index));
        }
    }

    private void inicializarMazos() {
        mazo.clear();
        int[] drawables = {R.drawable.ascorazon, R.drawable.doscorazon, R.drawable.trescorazon,
                R.drawable.cuatrocorazon, R.drawable.cincocorazon, R.drawable.seiscorazon};
        for (int d : drawables) {
            for (int j = 0; j < 4; j++) mazo.add(new Carta(d, d));
        }
        Collections.shuffle(mazo);

        mazoEspeciales.clear();
        for (int i = 0; i < 10; i++) {
            mazoEspeciales.add(new Carta(R.drawable.acidocarta, TipoEfecto.ACIDO));
            mazoEspeciales.add(new Carta(R.drawable.flechacarta, TipoEfecto.FLECHA));
            if (i % 5 == 0) mazoEspeciales.add(new Carta(R.drawable.jumpscarecarta, TipoEfecto.JUMPSCARE));
        }
        Collections.shuffle(mazoEspeciales);
    }

    private void robarCarta() {
        if (!turnoJugador || esperandoDescarte || hpJugador <= 0 || hpRival <= 0) return;

        if ((faseActual == FaseJuego.FASE_1_PAREJAS || faseActual == FaseJuego.MODO_PANICO_JUGADOR) && manoJugador.size() < 3) {
            manoJugador.add(mazo.remove(0));
            esperandoDescarte = true;
            actualizarUI("Descarta una carta");
        } else if (faseActual == FaseJuego.FASE_2_BATALLA && manoJugador.size() < 3) {
            manoJugador.add(mazoEspeciales.remove(0));
            actualizarUI("¡Tira tu carta especial!");
        }
    }

    private void clickEnCartaJugador(int index) {
        if (index >= manoJugador.size() || !turnoJugador) return;

        if (esperandoDescarte) {
            Carta descartada = manoJugador.remove(index);
            ivMontoDescarte.setImageResource(descartada.resId);
            esperandoDescarte = false;

            if (faseActual == FaseJuego.MODO_PANICO_JUGADOR) {
                verificarParejaPanicoJugador();
            } else {
                verificarParejaNormalJugador();
            }
        } else if (faseActual == FaseJuego.FASE_2_BATALLA && index == 2) {
            // En batalla, la carta 3 (index 2) es la especial
            resolverTurnoBatalla(manoJugador.remove(2));
        }
    }

    private void verificarParejaNormalJugador() {
        if (manoJugador.get(0).peso == manoJugador.get(1).peso) {
            iniciarModoPanicoRival();
        } else {
            turnoJugador = false;
            ejecutarTurnoRival();
        }
        actualizarUI(null);
    }

    private void verificarParejaPanicoJugador() {
        intentosPanicoRestantes--;
        if (manoJugador.get(0).peso == manoJugador.get(1).peso) {
            Toast.makeText(this, "¡Igualaste!", Toast.LENGTH_SHORT).show();
            iniciarFaseBatalla();
        } else if (intentosPanicoRestantes <= 0) {
            hpJugador = 0;
            checkFinal("¡Tiempo agotado! Has muerto.");
        } else {
            actualizarUI("Pánico: Quedan " + intentosPanicoRestantes + " intentos");
            // Sigue siendo turno del jugador hasta que se acaben los intentos o iguale
        }
    }

    private void iniciarModoPanicoRival() {
        turnoJugador = false;
        final int[] intentos = {5};
        tvStatusInfo.setText("¡PAREJA! RIVAL TIENE 5 TURNOS");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (intentos[0] > 0 && hpRival > 0) {
                    intentos[0]--;
                    manoRival.add(mazo.remove(0));
                    // IA: Descarta la que no ayude a la pareja
                    int d = (manoRival.get(0).peso == manoRival.get(2).peso) ? 1 : (manoRival.get(1).peso == manoRival.get(2).peso ? 0 : 2);
                    manoRival.remove(d);
                    actualizarUI("Rival intentando... (" + intentos[0] + ")");

                    if (manoRival.get(0).peso == manoRival.get(1).peso) {
                        iniciarFaseBatalla();
                    } else if (intentos[0] == 0) {
                        hpRival = 0;
                        checkFinal("El rival no igualó y ha muerto.");
                    } else {
                        handler.postDelayed(this, 1200);
                    }
                }
            }
        }, 1500);
    }

    private void ejecutarTurnoRival() {
        actualizarUI("Turno del Rival...");
        handler.postDelayed(() -> {
            manoRival.add(mazo.remove(0));
            int d = (manoRival.get(0).peso == manoRival.get(2).peso) ? 1 : 2;
            manoRival.remove(d);
            actualizarUI(null);

            if (manoRival.get(0).peso == manoRival.get(1).peso) {
                faseActual = FaseJuego.MODO_PANICO_JUGADOR;
                intentosPanicoRestantes = 5;
                turnoJugador = true;
                actualizarUI("¡EL RIVAL IGUALÓ! Tienes 5 turnos.");
            } else {
                turnoJugador = true;
                actualizarUI("Tu turno");
            }
        }, 1500);
    }

    private void iniciarFaseBatalla() {
        faseActual = FaseJuego.FASE_2_BATALLA;
        turnoJugador = true;
        esperandoDescarte = false;
        // Roban la primera carta especial automáticamente
        manoJugador.add(mazoEspeciales.remove(0));
        manoRival.add(mazoEspeciales.remove(0));
        actualizarUI("¡FASE DE BATALLA! Tira tu carta especial");
    }

    private void resolverTurnoBatalla(Carta especialJugador) {
        turnoJugador = false;
        Carta especialRival = manoRival.remove(2);

        // Revelar carta rival
        ivRivalCards[2].setImageResource(especialRival.resId);
        ivRivalCards[2].setVisibility(View.VISIBLE);

        // Aplicar daño simultáneo
        aplicarEfecto(especialJugador, false);
        aplicarEfecto(especialRival, true);

        handler.postDelayed(() -> {
            ivRivalCards[2].setImageResource(R.drawable.reverso);
            if (hpJugador > 0 && hpRival > 0) {
                // Robar siguiente especial
                manoJugador.add(mazoEspeciales.remove(0));
                manoRival.add(mazoEspeciales.remove(0));
                turnoJugador = true;
                actualizarUI("Siguiente ronda");
            } else {
                checkFinal(null);
            }
        }, 2000);
    }

    private void aplicarEfecto(Carta c, boolean alJugador) {
        int daño = 0;
        switch (c.efecto) {
            case ACIDO: daño = 35; break;
            case FLECHA: daño = 20; break;
            case JUMPSCARE: daño = 100; break;
        }
        if (alJugador) hpJugador = Math.max(0, hpJugador - daño);
        else hpRival = Math.max(0, hpRival - daño);
    }

    private void actualizarUI(String status) {
        if (status != null) tvStatusInfo.setText(status + "\n[HP " + hpJugador + " - " + hpRival + "]");
        else tvStatusInfo.setText("HP " + hpJugador + " - " + hpRival);

        for (int i = 0; i < 3; i++) {
            // Jugador
            if (i < manoJugador.size()) {
                ivPlayerCards[i].setImageResource(manoJugador.get(i).resId);
                ivPlayerCards[i].setVisibility(View.VISIBLE);
            } else {
                ivPlayerCards[i].setVisibility(View.INVISIBLE);
            }
            // Rival
            if (i < manoRival.size()) {
                ivRivalCards[i].setVisibility(View.VISIBLE);
                if (faseActual == FaseJuego.FASE_2_BATALLA && i < 2) {
                    ivRivalCards[i].setImageResource(manoRival.get(i).resId); // Revelar pareja en batalla
                } else if (i == 2 && !turnoJugador && faseActual == FaseJuego.FASE_2_BATALLA) {
                    // Se muestra en resolverTurnoBatalla
                } else {
                    ivRivalCards[i].setImageResource(R.drawable.reverso);
                }
            } else {
                ivRivalCards[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    private void checkFinal(String mensaje) {
        if (mensaje != null) tvStatusInfo.setText(mensaje);
        else if (hpJugador <= 0 && hpRival <= 0) tvStatusInfo.setText("¡EMPATE MORTAL!");
        else if (hpJugador <= 0) tvStatusInfo.setText("GAME OVER - HAS MUERTO");
        else if (hpRival <= 0) tvStatusInfo.setText("VICTORIA - RIVAL ELIMINADO");

        ivMontoCoger.setEnabled(false);
    }

    private void configurarVideo() {
        try {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.the_fool;
            videoIntro.setVideoURI(Uri.parse(videoPath));
            videoIntro.setOnCompletionListener(mp -> {
                videoIntro.setVisibility(View.GONE);
                layoutCardGame.setVisibility(View.VISIBLE);
                comenzarPartida();
            });
            videoIntro.start();
        } catch (Exception e) { comenzarPartida(); }
    }

    private void comenzarPartida() {
        inicializarMazos();
        manoJugador.add(mazo.remove(0)); manoJugador.add(mazo.remove(0));
        manoRival.add(mazo.remove(0)); manoRival.add(mazo.remove(0));
        actualizarUI("Roba para empezar");
    }
}