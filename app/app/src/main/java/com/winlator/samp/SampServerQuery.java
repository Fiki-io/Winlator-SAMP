package com.winlator.samp;

import android.os.Handler;
import android.os.Looper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampServerQuery {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final int TIMEOUT_MS = 2500;

    public interface QueryCallback {
        void onQueryResult(SampServer server, boolean success);
    }

    public static void query(final SampServer server, final QueryCallback callback) {
        executor.execute(() -> {
            boolean success = false;
            long startTime = System.currentTimeMillis();
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(TIMEOUT_MS);
                InetAddress address = InetAddress.getByName(server.getIp());
                int port = server.getPort();

                byte[] ipBytes = address.getAddress();
                if (ipBytes.length != 4) {
                    // IPv6 fallback or ignore
                    ipBytes = new byte[]{127, 0, 0, 1};
                }

                // Packet construction: "SAMP" (4 bytes) + IP (4 bytes) + Port (2 bytes LE) + Opcode 'i' (1 byte)
                ByteBuffer buffer = ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN);
                buffer.put((byte) 'S');
                buffer.put((byte) 'A');
                buffer.put((byte) 'M');
                buffer.put((byte) 'P');
                buffer.put(ipBytes);
                buffer.putShort((short) port);
                buffer.put((byte) 'i'); // Info opcode

                byte[] sendData = buffer.array();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
                socket.send(sendPacket);

                byte[] receiveData = new byte[2048];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                long ping = System.currentTimeMillis() - startTime;
                server.setPing((int) ping);

                if (receivePacket.getLength() > 11) {
                    ByteBuffer response = ByteBuffer.wrap(receiveData, 0, receivePacket.getLength()).order(ByteOrder.LITTLE_ENDIAN);
                    response.position(11); // Skip SAMP + IP + Port + Opcode

                    byte password = response.get();
                    short players = response.getShort();
                    short maxPlayers = response.getShort();

                    int hostnameLen = response.getInt();
                    if (hostnameLen > 0 && hostnameLen <= response.remaining()) {
                        byte[] hostnameBytes = new byte[hostnameLen];
                        response.get(hostnameBytes);
                        server.setHostname(new String(hostnameBytes, Charset.forName("windows-1252")));
                    }

                    int gamemodeLen = response.getInt();
                    if (gamemodeLen > 0 && gamemodeLen <= response.remaining()) {
                        byte[] gamemodeBytes = new byte[gamemodeLen];
                        response.get(gamemodeBytes);
                        server.setGamemode(new String(gamemodeBytes, Charset.forName("windows-1252")));
                    }

                    int mapLen = response.getInt();
                    if (mapLen > 0 && mapLen <= response.remaining()) {
                        byte[] mapBytes = new byte[mapLen];
                        response.get(mapBytes);
                        server.setMap(new String(mapBytes, Charset.forName("windows-1252")));
                    }

                    server.setPassword(password == 1);
                    server.setPlayers(players);
                    server.setMaxPlayers(maxPlayers);
                    server.setOnline(true);
                    success = true;
                }
            }
            catch (Exception e) {
                server.setOnline(false);
                server.setPing(-1);
            }

            final boolean finalSuccess = success;
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onQueryResult(server, finalSuccess);
                }
            });
        });
    }
}
