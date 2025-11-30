package com.example.aliceinborderlanddecretum;

import org.json.JSONException;
import org.json.JSONObject;

public class GameState {

    // 1. VARIABLE QUE FALTABA (La causa de tu error rojo)
    public String currentSceneId = "scene_001";

    // 2. VARIABLE DEL SALTO (La que añadimos antes)
    public boolean hasJumped = false;

    // Constructor vacío
    public GameState() {}

    // Método para convertir a JSON (Guardar)
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        // Guardamos la escena
        json.put("currentSceneId", currentSceneId);
        // Guardamos si saltó
        json.put("hasJumped", hasJumped);
        return json;
    }

    // Método para leer desde JSON (Cargar)
    public static GameState fromJson(JSONObject json) {
        GameState state = new GameState();

        // Recuperamos la escena (si no existe, por defecto "scene_001")
        state.currentSceneId = json.optString("currentSceneId", "scene_001");

        // Recuperamos si saltó (si no existe, por defecto false)
        state.hasJumped = json.optBoolean("hasJumped", false);

        return state;
    }
}