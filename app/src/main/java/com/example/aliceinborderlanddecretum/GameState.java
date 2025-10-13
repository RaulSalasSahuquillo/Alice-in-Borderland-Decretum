package com.example.aliceinborderlanddecretum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class GameState {
    public static final int SAVE_VERSION = 1;

    public String currentSceneId = "intro";     // escena actual
    public Set<String> flags = new HashSet<>(); // banderas (decisiones tomadas)
    public Map<String, Integer> vars = new HashMap<>(); // variables (p.ej. puntuación, llaves, etc.)
    public List<String> inventory = new ArrayList<>();  // inventario simple
    public long lastSavedAt = 0L;                // marca de tiempo último guardado

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("version", SAVE_VERSION);
        o.put("currentSceneId", currentSceneId);
        o.put("flags", new JSONArray(flags));
        JSONObject jvars = new JSONObject();
        for (Map.Entry<String,Integer> e : vars.entrySet()) jvars.put(e.getKey(), e.getValue());
        o.put("vars", jvars);
        o.put("inventory", new JSONArray(inventory));
        o.put("lastSavedAt", System.currentTimeMillis());
        return o;
    }

    public static GameState fromJson(JSONObject o) throws JSONException {
        GameState s = new GameState();
        int version = o.optInt("version", 1);
        // si cambias la estructura en el futuro, haz migraciones por version aquí

        s.currentSceneId = o.optString("currentSceneId", "intro");

        JSONArray jflags = o.optJSONArray("flags");
        if (jflags != null) for (int i=0;i<jflags.length();i++) s.flags.add(jflags.getString(i));

        JSONObject jvars = o.optJSONObject("vars");
        if (jvars != null) {
            Iterator<String> it = jvars.keys();
            while (it.hasNext()) {
                String k = it.next();
                s.vars.put(k, jvars.optInt(k, 0));
            }
        }

        JSONArray jinv = o.optJSONArray("inventory");
        if (jinv != null) for (int i=0;i<jinv.length();i++) s.inventory.add(jinv.getString(i));

        s.lastSavedAt = o.optLong("lastSavedAt", 0L);
        return s;
    }
}

