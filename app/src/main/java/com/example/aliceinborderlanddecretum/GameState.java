package com.example.aliceinborderlanddecretum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class GameState {

    // --- VARIABLES DE NOVELA VISUAL (Historia) ---
    public String currentSceneId = "scene_001";
    public boolean hasJumped = false;

    // --- VARIABLES DE JUEGO DE CARTAS (GamePlay2) ---
    public String fase; // "FASE_1_PAREJAS" o "FASE_2_BATALLA"
    public int hpJugador;
    public int hpRival;
    public boolean esTuTurnoBatalla;
    public List<Integer> manoJugadorResIds; // Lista de IDs de las cartas (R.drawable.xxx)
    public List<Integer> manoRivalResIds;

    // Constructor: Inicializamos listas para evitar errores nulos
    public GameState() {
        manoJugadorResIds = new ArrayList<>();
        manoRivalResIds = new ArrayList<>();

        // Valores por defecto para cartas
        fase = "FASE_1_PAREJAS";
        hpJugador = 100;
        hpRival = 100;
    }

    // Método para guardar TODO en JSON
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        // 1. Guardar Novela Visual
        json.put("currentSceneId", currentSceneId);
        json.put("hasJumped", hasJumped);

        // 2. Guardar Estado del Juego de Cartas
        json.put("fase", fase);
        json.put("hpJugador", hpJugador);
        json.put("hpRival", hpRival);
        json.put("esTuTurnoBatalla", esTuTurnoBatalla);

        // Convertir lista de cartas jugador a JSONArray
        JSONArray jsonManoJ = new JSONArray();
        for (Integer id : manoJugadorResIds) jsonManoJ.put(id);
        json.put("manoJugador", jsonManoJ);

        // Convertir lista de cartas rival a JSONArray
        JSONArray jsonManoR = new JSONArray();
        for (Integer id : manoRivalResIds) jsonManoR.put(id);
        json.put("manoRival", jsonManoR);

        return json;
    }

    // Método para cargar TODO desde JSON
    public static GameState fromJson(JSONObject json) {
        GameState state = new GameState();

        // 1. Cargar Novela Visual
        state.currentSceneId = json.optString("currentSceneId", "scene_001");
        state.hasJumped = json.optBoolean("hasJumped", false);

        // 2. Cargar Estado del Juego de Cartas
        state.fase = json.optString("fase", "FASE_1_PAREJAS");
        state.hpJugador = json.optInt("hpJugador", 100);
        state.hpRival = json.optInt("hpRival", 100);
        state.esTuTurnoBatalla = json.optBoolean("esTuTurnoBatalla", false);

        // Recuperar lista de cartas jugador
        JSONArray jsonManoJ = json.optJSONArray("manoJugador");
        if (jsonManoJ != null) {
            for (int i = 0; i < jsonManoJ.length(); i++) {
                state.manoJugadorResIds.add(jsonManoJ.optInt(i));
            }
        }

        // Recuperar lista de cartas rival
        JSONArray jsonManoR = json.optJSONArray("manoRival");
        if (jsonManoR != null) {
            for (int i = 0; i < jsonManoR.length(); i++) {
                state.manoRivalResIds.add(jsonManoR.optInt(i));
            }
        }

        return state;
    }
}