package com.winlator.samp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.winlator.XServerDisplayActivity;
import com.winlator.box64.Box64Preset;
import com.winlator.container.Container;
import com.winlator.container.ContainerManager;
import com.winlator.container.DXWrappers;
import com.winlator.container.GraphicsDrivers;
import com.winlator.core.Callback;
import com.winlator.core.GPUHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class SampLauncherManager {
    public static final String SAMP_CONTAINER_NAME = "GTA SA-MP";
    public static final String PREF_NICKNAME = "samp_nickname";
    public static final String DEFAULT_NICKNAME = "Player_Android";
    public static final String PREF_RENDERER = "samp_renderer"; // "auto", "turnip_dxvk", "wined3d_gles"
    public static final String PREF_SCREEN_SIZE = "samp_screen_size";
    public static final String DEFAULT_SCREEN_SIZE = "1280x720";
    public static final String PREF_FPS_LIMIT = "samp_fps_limit";
    public static final String DEFAULT_FPS_LIMIT = "60";

    public static String getNickname(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_NICKNAME, DEFAULT_NICKNAME);
    }

    public static void setNickname(Context context, String nickname) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(PREF_NICKNAME, nickname).apply();
    }

    public static void prepareAndLaunch(final Context context, final SampServer server, final String password, final Callback<Boolean> callback) {
        final ContainerManager containerManager = new ContainerManager(context);
        Container targetContainer = null;

        for (Container container : containerManager.getContainers()) {
            if (SAMP_CONTAINER_NAME.equals(container.getName())) {
                targetContainer = container;
                break;
            }
        }

        if (targetContainer != null) {
            configureContainerForGta(context, targetContainer);
            targetContainer.saveData();
            launchActivity(context, targetContainer, server, password);
            if (callback != null) callback.call(true);
        }
        else {
            JSONObject data = new JSONObject();
            try {
                data.put("name", SAMP_CONTAINER_NAME);
                data.put("screenSize", DEFAULT_SCREEN_SIZE);
            }
            catch (JSONException ignored) {}

            containerManager.createContainerAsync(data, (container) -> {
                if (container != null) {
                    configureContainerForGta(context, container);
                    container.saveData();
                    launchActivity(context, container, server, password);
                    if (callback != null) callback.call(true);
                }
                else {
                    if (callback != null) callback.call(false);
                }
            });
        }
    }

    private static void configureContainerForGta(Context context, Container container) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String rendererPref = preferences.getString(PREF_RENDERER, "auto");
        String screenSize = preferences.getString(PREF_SCREEN_SIZE, DEFAULT_SCREEN_SIZE);
        String fpsLimit = preferences.getString(PREF_FPS_LIMIT, DEFAULT_FPS_LIMIT);

        container.setName(SAMP_CONTAINER_NAME);
        container.setScreenSize(screenSize);
        container.setBox64Preset(Box64Preset.PERFORMANCE);
        container.setEnvVars("ZINK_DESCRIPTORS=lazy MESA_SHADER_CACHE_DISABLE=false MESA_SHADER_CACHE_MAX_SIZE=512MB mesa_glthread=true WINEESYNC=1 WINEFSYNC=1 TU_DEBUG=noconform MESA_NO_ERROR=1");

        // Graphics driver & DXWrapper setup for Mali vs Adreno
        if ("turnip_dxvk".equals(rendererPref)) {
            container.setGraphicsDriver("turnip,gladio");
            container.setDXWrapper(DXWrappers.DXVK);
        }
        else if ("wined3d_gles".equals(rendererPref)) {
            container.setGraphicsDriver(GraphicsDrivers.DEFAULT_VULKAN_DRIVER + "," + GraphicsDrivers.DEFAULT_OPENGL_DRIVER);
            container.setDXWrapper(DXWrappers.WINED3D);
        }
        else { // "auto"
            if (GPUHelper.isAdreno(context)) {
                container.setGraphicsDriver("turnip,gladio");
                container.setDXWrapper(DXWrappers.DXVK);
            }
            else {
                container.setGraphicsDriver(GraphicsDrivers.DEFAULT_VULKAN_DRIVER + "," + GraphicsDrivers.DEFAULT_OPENGL_DRIVER);
                container.setDXWrapper(DXWrappers.WINED3D);
            }
        }

        // Apply Wine registry tweaks for GTA SA RenderWare, Direct3D CSMT, and CLEO/ASI Overrides
        com.winlator.core.WineUtils.applyGtaOptimizations(container);

        // Setup custom DXVK configuration for GTA RenderWare
        try {
            File rootDir = com.winlator.xenvironment.RootFS.find(context).getRootDir();
            File userConfigDir = new File(rootDir, com.winlator.xenvironment.RootFS.USER_CONFIG_PATH);
            if (!userConfigDir.isDirectory()) userConfigDir.mkdirs();
            File dxvkConf = new File(userConfigDir, "dxvk.conf");

            StringBuilder dxvkConfig = new StringBuilder();
            dxvkConfig.append("d3d9.deferSurfaceCreation = True\n");
            dxvkConfig.append("d3d9.lenientClear = True\n");
            dxvkConfig.append("dxvk.enableAsync = True\n");
            dxvkConfig.append("dxvk.numCompilerThreads = 4\n");
            if (fpsLimit != null && !fpsLimit.isEmpty() && !"0".equals(fpsLimit)) {
                dxvkConfig.append("d3d9.maxFrameRate = ").append(fpsLimit).append("\n");
                dxvkConfig.append("dxgi.maxFrameRate = ").append(fpsLimit).append("\n");
            }
            com.winlator.core.FileUtils.writeString(dxvkConf, dxvkConfig.toString());
        }
        catch (Exception ignored) {}

        // Map Drive D to the GTA Game path
        String gamePath = SampGameValidator.getGamePath(context);
        container.setDrives("D:" + gamePath + "E:" + gamePath);
    }

    private static void launchActivity(Context context, Container container, SampServer server, String password) {
        String gamePath = SampGameValidator.getGamePath(context);
        String exeName = SampGameValidator.getGtaExecutableName(new File(gamePath));
        String nickname = getNickname(context);

        // Construct SA-MP command line: -c -h <ip> -p <port> -n <nick> [-z <pass>]
        StringBuilder args = new StringBuilder();
        args.append("-c -h ").append(server.getIp())
            .append(" -p ").append(server.getPort())
            .append(" -n ").append(nickname);

        if (password != null && !password.trim().isEmpty()) {
            args.append(" -z ").append(password.trim());
        }

        Intent intent = new Intent(context, XServerDisplayActivity.class);
        intent.putExtra("container_id", container.id);
        intent.putExtra("exec_path", "D:\\" + exeName);
        intent.putExtra("exec_args", args.toString());
        intent.putExtra("is_samp", true);
        intent.putExtra("samp_ip", server.getIp());
        intent.putExtra("samp_port", server.getPort());
        intent.putExtra("samp_hostname", server.getHostname());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
