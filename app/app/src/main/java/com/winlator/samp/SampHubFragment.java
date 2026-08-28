package com.winlator.samp;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.winlator.MainActivity;
import com.winlator.R;

import java.io.File;
import java.util.ArrayList;

public class SampHubFragment extends Fragment {
    private enum Tab { FAVORITES, HOSTED }
    private Tab currentTab = Tab.FAVORITES;
    private RecyclerView rvServers;
    private SampServerAdapter adapter;
    private TextView tvPlayerNickname;
    private TextView tvGtaStatus;
    private TextView tvEmpty;
    private Button btnTabFavorites;
    private Button btnTabHosted;
    private ArrayList<SampServer> currentServers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_samp_hub, container, false);

        tvPlayerNickname = root.findViewById(R.id.TVPlayerNickname);
        tvGtaStatus = root.findViewById(R.id.TVGtaStatus);
        tvEmpty = root.findViewById(R.id.TVEmpty);
        btnTabFavorites = root.findViewById(R.id.BTTabFavorites);
        btnTabHosted = root.findViewById(R.id.BTTabHosted);
        rvServers = root.findViewById(R.id.RVServers);

        rvServers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SampServerAdapter();
        rvServers.setAdapter(adapter);

        adapter.setOnServerClickListener(this::showServerDetailsDialog);

        btnTabFavorites.setOnClickListener(v -> switchTab(Tab.FAVORITES));
        btnTabHosted.setOnClickListener(v -> switchTab(Tab.HOSTED));

        root.findViewById(R.id.BTAddServer).setOnClickListener(v -> showAddServerDialog());

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHeaderInfo();
        loadServers();
    }

    private void updateHeaderInfo() {
        if (getContext() == null) return;
        String nick = SampLauncherManager.getNickname(getContext());
        tvPlayerNickname.setText(nick);

        String gamePath = SampGameValidator.getGamePath(getContext());
        boolean installed = SampGameValidator.isValidGtaFolder(new File(gamePath));
        if (installed) {
            tvGtaStatus.setText("GTA SA: Ready (" + new File(gamePath).getName() + ")");
            tvGtaStatus.setTextColor(Color.parseColor("#4CAF50"));
        }
        else {
            tvGtaStatus.setText("GTA SA: Not Found (Go to Settings)");
            tvGtaStatus.setTextColor(Color.parseColor("#EF5350"));
        }
    }

    private void switchTab(Tab tab) {
        currentTab = tab;
        if (tab == Tab.FAVORITES) {
            btnTabFavorites.setBackgroundColor(Color.parseColor("#56677A"));
            btnTabFavorites.setTextColor(Color.WHITE);
            btnTabHosted.setBackgroundColor(Color.parseColor("#2D3238"));
            btnTabHosted.setTextColor(Color.parseColor("#9E9E9E"));
        }
        else {
            btnTabHosted.setBackgroundColor(Color.parseColor("#56677A"));
            btnTabHosted.setTextColor(Color.WHITE);
            btnTabFavorites.setBackgroundColor(Color.parseColor("#2D3238"));
            btnTabFavorites.setTextColor(Color.parseColor("#9E9E9E"));
        }
        loadServers();
    }

    private void loadServers() {
        if (getContext() == null) return;
        if (currentTab == Tab.FAVORITES) {
            currentServers = SampServerStorage.getFavoriteServers(getContext());
        }
        else {
            currentServers = SampServerStorage.getHostedServers();
        }

        adapter.setServers(currentServers);
        tvEmpty.setVisibility(currentServers.isEmpty() ? View.VISIBLE : View.GONE);

        // Asynchronously query each server for live ping and player count
        for (SampServer server : currentServers) {
            SampServerQuery.query(server, (s, success) -> {
                if (isAdded()) {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void showAddServerDialog() {
        if (getContext() == null) return;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_samp_server, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
            .setView(dialogView)
            .create();

        EditText etIp = dialogView.findViewById(R.id.ETServerIp);
        EditText etPort = dialogView.findViewById(R.id.ETServerPort);

        dialogView.findViewById(R.id.BTCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.BTAdd).setOnClickListener(v -> {
            String ip = etIp.getText().toString().trim();
            String portStr = etPort.getText().toString().trim();
            if (ip.isEmpty()) {
                etIp.setError("Please enter server IP");
                return;
            }
            int port = 7777;
            try {
                if (!portStr.isEmpty()) port = Integer.parseInt(portStr);
            }
            catch (NumberFormatException ignored) {}

            SampServer newServer = new SampServer(ip, port, "Querying server...");
            SampServerStorage.addFavoriteServer(getContext(), newServer);
            dialog.dismiss();
            switchTab(Tab.FAVORITES);
        });

        dialog.show();
    }

    private void showServerDetailsDialog(final SampServer server) {
        if (getContext() == null) return;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_samp_server_details, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
            .setView(dialogView)
            .create();

        TextView tvHostname = dialogView.findViewById(R.id.TVDetailHostname);
        TextView tvAddress = dialogView.findViewById(R.id.TVDetailAddress);
        TextView tvPlayers = dialogView.findViewById(R.id.TVDetailPlayers);
        TextView tvPing = dialogView.findViewById(R.id.TVDetailPing);
        TextView tvGamemode = dialogView.findViewById(R.id.TVDetailGamemode);
        TextView tvMap = dialogView.findViewById(R.id.TVDetailMap);
        EditText etPassword = dialogView.findViewById(R.id.ETServerPassword);
        Button btnDelete = dialogView.findViewById(R.id.BTDeleteServer);
        Button btnConnect = dialogView.findViewById(R.id.BTConnect);

        tvHostname.setText(server.getHostname());
        tvAddress.setText(server.getAddress());
        tvPlayers.setText(server.getPlayers() + " / " + server.getMaxPlayers());
        tvPing.setText(server.getPing() > 0 ? server.getPing() + " ms" : "-- ms");
        tvGamemode.setText(server.getGamemode());
        tvMap.setText(server.getMap());

        etPassword.setVisibility(server.hasPassword() ? View.VISIBLE : View.GONE);
        btnDelete.setVisibility(currentTab == Tab.FAVORITES ? View.VISIBLE : View.GONE);

        btnDelete.setOnClickListener(v -> {
            SampServerStorage.removeFavoriteServer(getContext(), server);
            dialog.dismiss();
            loadServers();
        });

        dialogView.findViewById(R.id.BTDetailCancel).setOnClickListener(v -> dialog.dismiss());

        btnConnect.setOnClickListener(v -> {
            String gamePath = SampGameValidator.getGamePath(getContext());
            if (!SampGameValidator.isValidGtaFolder(new File(gamePath))) {
                Toast.makeText(getContext(), "GTA SA files not found! Set game directory in Settings tab.", Toast.LENGTH_LONG).show();
                return;
            }

            dialog.dismiss();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).preloaderDialog.show(R.string.starting_up);
            }

            String password = etPassword.getText().toString().trim();
            SampLauncherManager.prepareAndLaunch(getContext(), server, password, (success) -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).preloaderDialog.closeOnUiThread();
                }
            });
        });

        dialog.show();
    }
}
