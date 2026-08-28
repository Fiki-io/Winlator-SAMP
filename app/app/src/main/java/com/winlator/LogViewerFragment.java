package com.winlator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.winlator.core.AppCrashHandler;

import java.util.concurrent.Executors;

public class LogViewerFragment extends Fragment {
    private TextView tvLogContent;
    private TextView tvLogStatus;
    private EditText etLogFilter;
    private Spinner spinnerLogType;
    private ScrollView scrollVertical;
    private String currentFullLog = "";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final String[] LOG_TYPES = {
        "Box64 & Wine Live Engine Console",
        "Recent Logcat (All System/Engine)",
        "Previous Crash Report",
        "System & Engine Diagnostics"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_viewer, container, false);

        tvLogContent = view.findViewById(R.id.tv_log_content);
        tvLogStatus = view.findViewById(R.id.tv_log_status);
        etLogFilter = view.findViewById(R.id.et_log_filter);
        spinnerLogType = view.findViewById(R.id.spinner_log_type);
        scrollVertical = view.findViewById(R.id.scroll_vertical);

        Button btnCopy = view.findViewById(R.id.btn_copy_log);
        Button btnRefresh = view.findViewById(R.id.btn_refresh_log);
        Button btnShare = view.findViewById(R.id.btn_share_log);
        Button btnClear = view.findViewById(R.id.btn_clear_log);

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, LOG_TYPES);
        spinnerLogType.setAdapter(adapter);

        spinnerLogType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadLogs(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Copy Button
        btnCopy.setOnClickListener(v -> copyLogToClipboard());

        // Refresh Button
        btnRefresh.setOnClickListener(v -> loadLogs(spinnerLogType.getSelectedItemPosition()));

        // Share Button
        btnShare.setOnClickListener(v -> shareLogText());

        // Clear Button
        btnClear.setOnClickListener(v -> {
            AppCrashHandler.getInstance().clearAllLogs();
            Toast.makeText(requireContext(), "Logs cleared", Toast.LENGTH_SHORT).show();
            loadLogs(spinnerLogType.getSelectedItemPosition());
        });

        // Filter text listener
        etLogFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadLogs(0);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity)requireActivity()).getSupportActionBar().setTitle("System & Crash Logs");
    }

    private void loadLogs(int mode) {
        tvLogStatus.setText("Fetching logs...");
        Executors.newSingleThreadExecutor().execute(() -> {
            String log;
            AppCrashHandler crashHandler = AppCrashHandler.getInstance();
            if (crashHandler == null) {
                AppCrashHandler.init(requireContext());
                crashHandler = AppCrashHandler.getInstance();
            }

            switch (mode) {
                case 0:
                    log = crashHandler.getEngineLog();
                    break;
                case 1:
                    log = crashHandler.getRecentLogcat(500);
                    break;
                case 2:
                    log = crashHandler.getCrashLog();
                    break;
                case 3:
                    log = crashHandler.getDeviceDiagnostics();
                    break;
                default:
                    log = crashHandler.getEngineLog();
                    break;
            }

            final String finalLog = log;
            mainHandler.post(() -> {
                currentFullLog = finalLog;
                applyFilter(etLogFilter.getText().toString());
            });
        });
    }

    private void applyFilter(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            tvLogContent.setText(currentFullLog);
            int lineCount = currentFullLog.split("\n").length;
            tvLogStatus.setText("Total: " + lineCount + " lines");
        }
        else {
            String lowerFilter = filter.trim().toLowerCase();
            String[] lines = currentFullLog.split("\n");
            StringBuilder filtered = new StringBuilder();
            int matchCount = 0;

            for (String line : lines) {
                if (line.toLowerCase().contains(lowerFilter)) {
                    filtered.append(line).append("\n");
                    matchCount++;
                }
            }

            if (matchCount == 0) {
                tvLogContent.setText("No log lines match the filter: \"" + filter + "\"");
            }
            else {
                tvLogContent.setText(filtered.toString());
            }
            tvLogStatus.setText("Filtered: " + matchCount + " of " + lines.length + " lines matching \"" + filter + "\"");
        }

        // Scroll to bottom
        scrollVertical.post(() -> scrollVertical.fullScroll(View.FOCUS_DOWN));
    }

    private void copyLogToClipboard() {
        CharSequence text = tvLogContent.getText();
        if (text == null || text.length() == 0) {
            Toast.makeText(requireContext(), "No log to copy", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Winlator_SAMP_Log", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "📋 Log copied to clipboard! (" + text.length() + " chars)", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareLogText() {
        CharSequence text = tvLogContent.getText();
        if (text == null || text.length() == 0) {
            Toast.makeText(requireContext(), "No log to share", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Winlator SA-MP System Log");
        intent.putExtra(Intent.EXTRA_TEXT, text.toString());
        startActivity(Intent.createChooser(intent, "Share Log Via"));
    }
}
