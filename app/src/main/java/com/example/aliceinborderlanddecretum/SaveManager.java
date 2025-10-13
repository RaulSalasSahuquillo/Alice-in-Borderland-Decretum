package com.example.aliceinborderlanddecretum;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class SaveManager {
    private static final String SAVE_FILE = "savegame.json";

    public static void save(Context ctx, GameState state) {
        try {
            JSONObject json = state.toJson();
            try (FileWriter fw = new FileWriter(new File(ctx.getFilesDir(), SAVE_FILE), false)) {
                fw.write(json.toString());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static GameState load(Context ctx) {
        File f = new File(ctx.getFilesDir(), SAVE_FILE);
        if (!f.exists()) return null;
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
            JSONObject json = new JSONObject(sb.toString());
            return GameState.fromJson(json);
        } catch (Exception e) {
            // si está corrupto, lo ignoramos y empezamos nuevo
            e.printStackTrace();
            return null;
        }
    }

    public static void clear(Context ctx) {
        File f = new File(ctx.getFilesDir(), SAVE_FILE);
        if (f.exists()) f.delete();
    }
}
