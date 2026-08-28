package com.winlator.samp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.winlator.R;

import java.io.File;

public class SampSettingsFragment extends Fragment {
    private EditText etNickname;
    private EditText etGamePath;
    private TextView tvPathStatus;
    private Spinner spRenderer;
    private Spinner spScreenSize;
    private Spinner spFpsLimit;

    private static final String[] RENDERER_LABELS = {
        "Auto (Adreno: Turnip+DXVK | Mali: WineD3D+GLES)",
        "Turnip + DXVK (Snapdragon / Adreno)",
        "WineD3D + OpenGL ES (MediaTek / Mali / Exynos)"
    };
    private static final String[] RENDERER_VALUES = {
        "auto",
        "turnip_dxvk",
        "wined3d_gles"
    };

    private static final String[] SCREEN_LABELS = {
        "960x540 (Low-end / Fast FPS)",
        "1280x720 (Standard HD 720p)",
        "1600x900 (Balanced 900p)",
        "1920x1080 (Full HD 1080p)"
    };
    private static final String[] SCREEN_VALUES = {
        "960x540",
        "1280x720",
        "1600x900",
        "1920x1080"
    };

    private static final String[] FPS_LABELS = {
        "30 FPS (Power Saver)",
        "60 FPS (Standard Smooth)",
        "90 FPS (High Refresh)",
        "120 FPS (Ultra)"
    };
    private static final String[] FPS_VALUES = {
        "30",
        "60",
        "90",
        "120"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_samp_settings, container, false);

        etNickname = root.findViewById(R.id.ETNickname);
        etGamePath = root.findViewById(R.id.ETGamePath);
        tvPathStatus = root.findViewById(R.id.TVPathStatus);
        spRenderer = root.findViewById(R.id.SPRenderer);
        spScreenSize = root.findViewById(R.id.SPScreenSize);
        spFpsLimit = root.findViewById(R.id.SPFpsLimit);

        setupSpinners();
        loadSettings();

        etGamePath.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePathValidation(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        root.findViewById(R.id.BTSelectFolder).setOnClickListener(v -> showFolderSuggestDialog());
        root.findViewById(R.id.BTSaveSettings).setOnClickListener(v -> saveSettings());

        return root;
    }

    private void setupSpinners() {
        if (getContext() == null) return;
        spRenderer.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, RENDERER_LABELS));
        spScreenSize.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, SCREEN_LABELS));
        spFpsLimit.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, FPS_LABELS));
    }

    private void loadSettings() {
        if (getContext() == null) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        etNickname.setText(SampLauncherManager.getNickname(getContext()));
        String path = SampGameValidator.getGamePath(getContext());
        etGamePath.setText(path);
        updatePathValidation(path);

        String currentRenderer = prefs.getString(SampLauncherManager.PREF_RENDERER, "auto");
        for (int i = 0; i < RENDERER_VALUES.length; i++) {
            if (RENDERER_VALUES[i].equals(currentRenderer)) {
                spRenderer.setSelection(i);
                break;
            }
        }

        String currentScreen = prefs.getString(SampLauncherManager.PREF_SCREEN_SIZE, SampLauncherManager.DEFAULT_SCREEN_SIZE);
        for (int i = 0; i < SCREEN_VALUES.length; i++) {
            if (SCREEN_VALUES[i].equals(currentScreen)) {
                spScreenSize.setSelection(i);
                break;
            }
        }

        String currentFps = prefs.getString(SampLauncherManager.PREF_FPS_LIMIT, SampLauncherManager.DEFAULT_FPS_LIMIT);
        for (int i = 0; i < FPS_VALUES.length; i++) {
            if (FPS_VALUES[i].equals(currentFps)) {
                spFpsLimit.setSelection(i);
                break;
            }
        }
    }

    private void updatePathValidation(String path) {
        boolean valid = SampGameValidator.isValidGtaFolder(new File(path));
        if (valid) {
            tvPathStatus.setText("✓ GTA SA Folder Valid (gta_sa.exe found)");
            tvPathStatus.setTextColor(Color.parseColor("#4CAF50"));
        }
        else {
            tvPathStatus.setText("✗ Invalid Folder (gta_sa.exe / models missing)");
            tvPathStatus.setTextColor(Color.parseColor("#EF5350"));
        }
    }

    private void showFolderSuggestDialog() {
        if (getContext() == null) return;
        String extStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
        String[] suggestions = {
            extStorage + "/GTASA",
            extStorage + "/GTA_SA",
            extStorage + "/Download/GTASA",
            extStorage + "/Android/data/com.winlator/files/GTASA",
            extStorage + "/GTA San Andreas"
        };

        new AlertDialog.Builder(getContext())
            .setTitle("Pilih Lokasi Folder GTA SA")
            .setItems(suggestions, (dialog, which) -> {
                etGamePath.setText(suggestions[which]);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void saveSettings() {
        if (getContext() == null) return;
        String nickname = etNickname.getText().toString().trim();
        String path = etGamePath.getText().toString().trim();

        if (nickname.isEmpty()) {
            etNickname.setError("Nickname cannot be empty");
            return;
        }

        SampLauncherManager.setNickname(getContext(), nickname);
        SampGameValidator.setGamePath(getContext(), path);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit()
            .putString(SampLauncherManager.PREF_RENDERER, RENDERER_VALUES[spRenderer.getSelectedItemPosition()])
            .putString(SampLauncherManager.PREF_SCREEN_SIZE, SCREEN_VALUES[spScreenSize.getSelectedItemPosition()])
            .putString(SampLauncherManager.PREF_FPS_LIMIT, FPS_VALUES[spFpsLimit.getSelectedItemPosition()])
            .apply();

        Toast.makeText(getContext(), "Settings saved successfully!", Toast.LENGTH_SHORT).show();
    }
}
