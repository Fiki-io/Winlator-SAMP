package com.winlator.samp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class SampServer implements Serializable {
    private String ip;
    private int port;
    private String hostname = "Retrieving info...";
    private int players = 0;
    private int maxPlayers = 0;
    private String gamemode = "-";
    private String map = "-";
    private boolean password = false;
    private int ping = -1;
    private boolean isOnline = false;
    private boolean isFavorite = false;

    public SampServer(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public SampServer(String ip, int port, String hostname) {
        this.ip = ip;
        this.port = port;
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return ip + ":" + port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getGamemode() {
        return gamemode;
    }

    public void setGamemode(String gamemode) {
        this.gamemode = gamemode;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public boolean hasPassword() {
        return password;
    }

    public void setPassword(boolean password) {
        this.password = password;
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("ip", ip);
            obj.put("port", port);
            obj.put("hostname", hostname);
            obj.put("players", players);
            obj.put("maxPlayers", maxPlayers);
            obj.put("gamemode", gamemode);
            obj.put("map", map);
            obj.put("password", password);
            obj.put("isFavorite", isFavorite);
        }
        catch (JSONException ignored) {}
        return obj;
    }

    public static SampServer fromJSONObject(JSONObject obj) {
        if (obj == null) return null;
        String ip = obj.optString("ip", "127.0.0.1");
        int port = obj.optInt("port", 7777);
        SampServer server = new SampServer(ip, port);
        server.setHostname(obj.optString("hostname", "SA-MP Server"));
        server.setPlayers(obj.optInt("players", 0));
        server.setMaxPlayers(obj.optInt("maxPlayers", 0));
        server.setGamemode(obj.optString("gamemode", "-"));
        server.setMap(obj.optString("map", "-"));
        server.setPassword(obj.optBoolean("password", false));
        server.setFavorite(obj.optBoolean("isFavorite", false));
        return server;
    }
}
