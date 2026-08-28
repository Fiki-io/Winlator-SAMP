package com.winlator.samp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class SampServerStorage {
    private static final String PREF_SERVERS_FAVORITES = "samp_servers_favorites";
    private static final String PREF_SERVERS_HISTORY = "samp_servers_history";

    public static ArrayList<SampServer> getFavoriteServers(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String json = preferences.getString(PREF_SERVERS_FAVORITES, null);
        ArrayList<SampServer> list = new ArrayList<>();

        if (json == null || json.isEmpty()) {
            // Add default starter servers
            list.add(new SampServer("185.169.134.67", 7777, "Arizona RP | Scottdale"));
            list.add(new SampServer("185.169.134.68", 7777, "Arizona RP | Chandler"));
            list.add(new SampServer("51.77.68.54", 7777, "Freeroam / Stunt Server"));
            list.add(new SampServer("play.valrise-indo.com", 7777, "Valrise Roleplay Indonesia"));
            list.add(new SampServer("127.0.0.1", 7777, "Localhost Test Server"));
            saveFavoriteServers(context, list);
            return list;
        }

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                SampServer server = SampServer.fromJSONObject(array.getJSONObject(i));
                if (server != null) list.add(server);
            }
        }
        catch (JSONException ignored) {}
        return list;
    }

    public static void saveFavoriteServers(Context context, ArrayList<SampServer> servers) {
        JSONArray array = new JSONArray();
        for (SampServer server : servers) {
            array.put(server.toJSONObject());
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(PREF_SERVERS_FAVORITES, array.toString()).apply();
    }

    public static void addFavoriteServer(Context context, SampServer newServer) {
        ArrayList<SampServer> list = getFavoriteServers(context);
        for (SampServer s : list) {
            if (s.getIp().equalsIgnoreCase(newServer.getIp()) && s.getPort() == newServer.getPort()) {
                return;
            }
        }
        list.add(0, newServer);
        saveFavoriteServers(context, list);
    }

    public static void removeFavoriteServer(Context context, SampServer target) {
        ArrayList<SampServer> list = getFavoriteServers(context);
        for (int i = 0; i < list.size(); i++) {
            SampServer s = list.get(i);
            if (s.getIp().equalsIgnoreCase(target.getIp()) && s.getPort() == target.getPort()) {
                list.remove(i);
                break;
            }
        }
        saveFavoriteServers(context, list);
    }

    public static ArrayList<SampServer> getHostedServers() {
        ArrayList<SampServer> list = new ArrayList<>();
        list.add(new SampServer("185.169.134.67", 7777, "Arizona RP [Server 1]"));
        list.add(new SampServer("185.169.134.68", 7777, "Arizona RP [Server 2]"));
        list.add(new SampServer("51.77.68.54", 7777, "Ultra Cops and Robbers"));
        list.add(new SampServer("54.37.142.75", 7777, "Next-Gen Roleplay"));
        list.add(new SampServer("185.169.134.3", 7777, "Diamond RP Emerald"));
        return list;
    }
}
