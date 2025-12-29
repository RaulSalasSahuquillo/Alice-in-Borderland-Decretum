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

    private enum FaseJuego { FASE_1_PAREJAS, FASE_2_BATALLA }
    private enum TipoEfecto { NINGUNO, ACIDO, ASFIXIA, FLECHA, ELECTROCUTADO, JUMPSCARE }

    class Carta {
        int resId;
        int peso;
        TipoEfecto efecto;

        Carta(int resId, int peso) { // Para Fase 1
            this.resId = resId;
            this.peso = peso;
            this.efecto = TipoEfecto.NINGUNO;
        }

        Carta(int resId, TipoEfecto efecto) { // Para Fase 2
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

    // --- CICLO INICIAL ---

    private void inicializarMazo() {
        mazo.clear();
        // Simulación de 6 cartas del Tarot (Arcanos) repetidos 4 veces
        int[] drawables = {R.drawable.ascorazon, R.drawable.doscorazon, R.drawable.trescorazon,
                R.drawable.cuatrocorazon, R.drawable.cincocorazon, R.drawable.seiscorazon};
        for (int i = 0; i < drawables.length; i++) {
            for (int j = 0; j < 4; j++) {
                mazo.add(new Carta(drawables[i], i + 1));
            }
        }
        Collections.shuffle(mazo);
    }

    private void robarCarta() {
        if (!turnoJugador || esperandoDescarte) return;

        if (faseActual == FaseJuego.FASE_1_PAREJAS && manoJugador.size() < 3) {
            manoJugador.add(mazo.remove(0));
            esperandoDescarte = true;
            actualizarUI("Descarta una para igualar");
        } else if (faseActual == FaseJuego.FASE_2_BATALLA && manoJugador.size() < 3) {
            manoJugador.add(mazoEspeciales.remove(0));
            actualizarUI("¡Tira tu carta especial!");
        }
    }

    private void clickEnCartaJugador(int index) {
        if (index >= manoJugador.size()) return;

        if (faseActual == FaseJuego.FASE_1_PAREJAS && esperandoDescarte) {
            Carta descartada = manoJugador.remove(index);
            ivMontoDescarte.setImageResource(descartada.resId);
            esperandoDescarte = false;
            verificarParejaJugador();
        } else if (faseActual == FaseJuego.FASE_2_BATALLA && index == 2) {
            // En batalla, la carta 3 es la especial
            resolverTurnoBatalla(manoJugador.remove(2));
        }
    }

    // --- REGLA: LOS 5 TURNOS DE PÁNICO ---

    private void verificarParejaJugador() {
        if (manoJugador.get(0).peso == manoJugador.get(1).peso) {
            iniciarModoPanico(true); // Rival entra en pánico
        } else {
            turnoJugador = false;
            ejecutarTurnoRival();
        }
        actualizarUI(null);
    }

    private void iniciarModoPanico(boolean esRival) {
        final int[] intentos = {5};
        tvStatusInfo.setText(esRival ? "¡PAREJA! RIVAL TIENE 5 TURNOS" : "¡IGUALÓ! TIENES 5 TURNOS");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (intentos[0] > 0) {
                    intentos[0]--;
                    // Lógica del Rival intentando igualar
                    if (esRival) {
                        manoRival.add(mazo.remove(0));
                        // IA simple: descarta la que no hace pareja
                        int d = (manoRival.get(0).peso == manoRival.get(2).peso) ? 1 : (manoRival.get(1).peso == manoRival.get(2).peso ? 0 : 2);
                        manoRival.remove(d);
                        actualizarUI("Intento Rival: " + (5 - intentos[0]));

                        if (manoRival.get(0).peso == manoRival.get(1).peso) {
                            Toast.makeText(GamePlay2.this, "¡El Rival igualó!", Toast.LENGTH_SHORT).show();
                            iniciarFaseBatalla();
                            return;
                        }
                    }
                    handler.postDelayed(this, 1200);
                } else {
                    if (esRival) ganarPorMuerte();
                }
            }
        }, 1500);
    }

    // --- FASE 2: BATALLA ESPECIAL ---

    private void iniciarFaseBatalla() {
        faseActual = FaseJuego.FASE_2_BATALLA;
        mazoEspeciales.clear();
        // Llenar mazo especial (no tarot)
        for(int i=0; i<3; i++) {
            mazoEspeciales.add(new Carta(R.drawable.acidocarta, TipoEfecto.ACIDO));
            mazoEspeciales.add(new Carta(R.drawable.flechacarta, TipoEfecto.FLECHA));
            mazoEspeciales.add(new Carta(R.drawable.jumpscarecarta, TipoEfecto.JUMPSCARE));
        }
        Collections.shuffle(mazoEspeciales);
        actualizarUI("Fase de Batalla: Roba una carta");
    }

    private void resolverTurnoBatalla(Carta especialJugador) {
        Carta especialRival = mazoEspeciales.remove(0);

        // Efecto visual: mostrar brevemente la carta del rival
        ivRivalCards[2].setImageResource(especialRival.resId);

        // Ambas se tiran a la vez (lógica simultánea)
        aplicarEfecto(especialJugador, false);
        aplicarEfecto(especialRival, true);

        handler.postDelayed(() -> {
            ivRivalCards[2].setImageResource(R.drawable.reverso);
            if (hpJugador > 0 && hpRival > 0) {
                actualizarUI("Siguiente ronda de ataque");
            } else {
                checkFinal();
            }
        }, 2000);
    }

    private void aplicarEfecto(Carta c, boolean alJugador) {
        int daño = 0;
        switch (c.efecto) {
            case ACIDO: daño = 35; break;
            case FLECHA: daño = 20; break;
            case JUMPSCARE: daño = 100; break; // Muere instantáneamente
        }
        if (alJugador) hpJugador -= daño; else hpRival -= daño;
    }

    // --- TURNOS IA Y UI ---

    private void ejecutarTurnoRival() {
        actualizarUI("Turno del Rival...");
        handler.postDelayed(() -> {
            manoRival.add(mazo.remove(0));
            int d = (manoRival.get(0).peso == manoRival.get(2).peso) ? 1 : 2;
            manoRival.remove(d);

            if (manoRival.get(0).peso == manoRival.get(1).peso) {
                iniciarModoPanico(false); // Jugador entra en pánico
            } else {
                turnoJugador = true;
                actualizarUI("Tu turno: Roba");
            }
        }, 1500);
    }

    private void actualizarUI(String status) {
        if (status != null) tvStatusInfo.setText(status + "\n[HP " + hpJugador + " VS " + hpRival + "]");

        for (int i = 0; i < 3; i++) {
            if (i < manoJugador.size()) {
                ivPlayerCards[i].setImageResource(manoJugador.get(i).resId);
                ivPlayerCards[i].setVisibility(View.VISIBLE);
            } else {
                ivPlayerCards[i].setVisibility(View.INVISIBLE);
            }

            if (i < manoRival.size()) {
                ivRivalCards[i].setVisibility(View.VISIBLE);
                // Solo mostrar la carta del rival si estamos en animación de batalla
            } else {
                ivRivalCards[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    private void ganarPorMuerte() {
        tvStatusInfo.setText("¡EL RIVAL HA MUERTO!");
        ivMontoCoger.setEnabled(false);
    }

    private void checkFinal() {
        if (hpJugador <= 0) tvStatusInfo.setText("GAME OVER - HAS MUERTO");
        else if (hpRival <= 0) tvStatusInfo.setText("VICTORIA - RIVAL ELIMINADO");
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
        inicializarMazo();
        manoJugador.add(mazo.remove(0)); manoJugador.add(mazo.remove(0));
        manoRival.add(mazo.remove(0)); manoRival.add(mazo.remove(0));
        actualizarUI("Roba una carta");
    }
}