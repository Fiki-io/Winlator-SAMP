package com.winlator.samp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import androidx.preference.PreferenceManager;

import java.io.File;

public class SampGameValidator {
    public static final String PREF_GAME_PATH = "samp_game_path";
    public static final String DEFAULT_GAME_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GTASA";

    public static String getGamePath(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_GAME_PATH, DEFAULT_GAME_PATH);
    }

    public static void setGamePath(Context context, String path) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(PREF_GAME_PATH, path).apply();
    }

    public static boolean isGameInstalled(Context context) {
        String path = getGamePath(context);
        return isValidGtaFolder(new File(path));
    }

    public static boolean isValidGtaFolder(File folder) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) return false;

        File exe1 = new File(folder, "gta_sa.exe");
        File exe2 = new File(folder, "gta-sa.exe");
        File exe3 = new File(folder, "GTA_SA.EXE");

        boolean hasExe = exe1.exists() || exe2.exists() || exe3.exists();
        if (!hasExe) return false;

        File models = new File(folder, "models");
        File data = new File(folder, "data");

        return (models.exists() && models.isDirectory()) || (data.exists() && data.isDirectory());
    }

    public static String getGtaExecutableName(File folder) {
        if (new File(folder, "gta_sa.exe").exists()) return "gta_sa.exe";
        if (new File(folder, "gta-sa.exe").exists()) return "gta-sa.exe";
        if (new File(folder, "GTA_SA.EXE").exists()) return "GTA_SA.EXE";
        return "gta_sa.exe";
    }
}
