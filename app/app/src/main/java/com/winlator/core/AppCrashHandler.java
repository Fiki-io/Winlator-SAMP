package com.winlator.core;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppCrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "AppCrashHandler";
    private static final String CRASH_LOG_FILENAME = "crash_log.txt";
    private static final String LATEST_LOG_FILENAME = "latest_log.txt";
    private static AppCrashHandler instance;
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    private AppCrashHandler(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new AppCrashHandler(context);
            Thread.setDefaultUncaughtExceptionHandler(instance);
            Log.i(TAG, "Global AppCrashHandler initialized successfully.");
        }
    }

    public static synchronized AppCrashHandler getInstance() {
        return instance;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            saveCrashReport(thread, throwable);
        }
        catch (Exception e) {
            Log.e(TAG, "Failed to save crash report", e);
        }
        finally {
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
            else {
                System.exit(1);
            }
        }
    }

    public void saveCrashReport(Thread thread, Throwable throwable) {
        File logsDir = getLogsDir();
        if (!logsDir.exists()) logsDir.mkdirs();

        File crashFile = new File(logsDir, CRASH_LOG_FILENAME);
        File latestFile = new File(logsDir, LATEST_LOG_FILENAME);

        StringBuilder sb = new StringBuilder();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());

        sb.append("=====================================================\n");
        sb.append("           WINLATOR SA-MP CRASH REPORT               \n");
        sb.append("=====================================================\n");
        sb.append("Crash Time: ").append(timestamp).append("\n");
        sb.append("Thread: ").append(thread != null ? thread.getName() : "Unknown").append("\n\n");

        sb.append("--- DEVICE INFORMATION ---\n");
        sb.append("Manufacturer : ").append(Build.MANUFACTURER).append("\n");
        sb.append("Model        : ").append(Build.MODEL).append("\n");
        sb.append("Device/Board : ").append(Build.DEVICE).append(" / ").append(Build.BOARD).append("\n");
        sb.append("Android SDK  : ").append(Build.VERSION.SDK_INT).append(" (Android ").append(Build.VERSION.RELEASE).append(")\n");
        sb.append("Supported ABIs: ");
        if (Build.SUPPORTED_ABIS != null) {
            for (String abi : Build.SUPPORTED_ABIS) sb.append(abi).append(" ");
        }
        sb.append("\n\n");

        sb.append("--- EXCEPTION & STACK TRACE ---\n");
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            sb.append(sw.toString()).append("\n");
        }
        else {
            sb.append("No Exception Throwable object provided.\n");
        }

        sb.append("\n--- LOGCAT SNAPSHOT (LAST 250 LINES) ---\n");
        sb.append(getRecentLogcat(250)).append("\n");
        sb.append("=====================================================\n");

        String report = sb.toString();

        // Write to crash_log.txt and latest_log.txt
        writeToFile(crashFile, report);
        writeToFile(latestFile, report);
    }

    public String getCrashLog() {
        File crashFile = new File(getLogsDir(), CRASH_LOG_FILENAME);
        if (crashFile.exists()) {
            return readFromFile(crashFile);
        }
        return "No previous crash log found. System is running cleanly!";
    }

    public String getRecentLogcat(int maxLines) {
        StringBuilder logcat = new StringBuilder();
        Process process = null;
        try {
            // logcat -d dumps logcat and exits
            process = Runtime.getRuntime().exec(new String[]{"logcat", "-d", "-v", "time"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            java.util.LinkedList<String> buffer = new java.util.LinkedList<>();
            while ((line = reader.readLine()) != null) {
                buffer.add(line);
                if (maxLines > 0 && buffer.size() > maxLines) {
                    buffer.removeFirst();
                }
            }
            for (String l : buffer) {
                logcat.append(l).append("\n");
            }
        }
        catch (Exception e) {
            logcat.append("Error capturing logcat: ").append(e.getMessage());
        }
        finally {
            if (process != null) {
                process.destroy();
            }
        }
        return logcat.length() > 0 ? logcat.toString() : "No logcat output available.";
    }

    public String getDeviceDiagnostics() {
        StringBuilder sb = new StringBuilder();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());

        sb.append("=== SYSTEM & ENGINE DIAGNOSTICS ===\n");
        sb.append("Timestamp   : ").append(timestamp).append("\n");
        sb.append("Device      : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        sb.append("Brand/Board : ").append(Build.BRAND).append(" / ").append(Build.BOARD).append("\n");
        sb.append("Hardware    : ").append(Build.HARDWARE).append("\n");
        sb.append("Android OS  : Android ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("CPU Cores   : ").append(Runtime.getRuntime().availableProcessors()).append("\n");
        sb.append("Max Memory  : ").append(Runtime.getRuntime().maxMemory() / (1024 * 1024)).append(" MB\n");
        sb.append("Free Memory : ").append(Runtime.getRuntime().freeMemory() / (1024 * 1024)).append(" MB\n");
        sb.append("GPU Type    : ");
        if (GPUHelper.isAdreno(context)) {
            sb.append("Qualcomm Adreno (Turnip Vulkan + DXVK Async Enabled)\n");
        }
        else if (GPUHelper.isMali(context)) {
            sb.append("ARM Mali (WineD3D + Native OpenGL ES 3.2 Enabled)\n");
        }
        else if (GPUHelper.isPowerVR(context)) {
            sb.append("PowerVR (WineD3D + OpenGL ES Enabled)\n");
        }
        else {
            sb.append("Generic / Other GPU\n");
        }

        sb.append("\n=== RECENT ERROR LOGS (FILTERED) ===\n");
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"logcat", "-d", "-v", "time", "*:E"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < 100) {
                sb.append(line).append("\n");
                count++;
            }
        }
        catch (Exception e) {
            sb.append("Could not filter error logs: ").append(e.getMessage()).append("\n");
        }

        return sb.toString();
    }

    public void clearAllLogs() {
        try {
            Runtime.getRuntime().exec(new String[]{"logcat", "-c"});
            File logsDir = getLogsDir();
            if (logsDir.exists()) {
                File[] files = logsDir.listFiles();
                if (files != null) {
                    for (File f : files) f.delete();
                }
            }
        }
        catch (Exception ignored) {}
    }

    private File getLogsDir() {
        return new File(context.getFilesDir(), "logs");
    }

    private void writeToFile(File file, String content) {
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(content);
            writer.flush();
        }
        catch (Exception e) {
            Log.e(TAG, "Failed writing to log file: " + file.getPath(), e);
        }
    }

    private String readFromFile(File file) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        catch (Exception e) {
            return "Error reading log file: " + e.getMessage();
        }
        return sb.toString();
    }
}
