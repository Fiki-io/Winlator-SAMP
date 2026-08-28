package com.winlator.samp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.winlator.R;

import java.util.ArrayList;

public class SampServerAdapter extends RecyclerView.Adapter<SampServerAdapter.ViewHolder> {
    private final ArrayList<SampServer> servers = new ArrayList<>();
    private OnServerClickListener listener;

    public interface OnServerClickListener {
        void onServerClick(SampServer server);
    }

    public void setOnServerClickListener(OnServerClickListener listener) {
        this.listener = listener;
    }

    public void setServers(ArrayList<SampServer> newServers) {
        this.servers.clear();
        if (newServers != null) {
            this.servers.addAll(newServers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.samp_server_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final SampServer server = servers.get(position);
        holder.tvHostname.setText(server.getHostname());
        holder.tvAddress.setText(server.getAddress());
        holder.tvPlayers.setText(server.getPlayers() + "/" + server.getMaxPlayers());
        holder.tvGamemode.setText("Gamemode: " + server.getGamemode());

        if (server.isOnline()) {
            holder.tvStatus.setText("Online");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));

            int ping = server.getPing();
            if (ping > 0) {
                holder.tvPing.setText(ping + " ms");
                if (ping < 90) holder.tvPing.setTextColor(Color.parseColor("#4CAF50"));
                else if (ping < 180) holder.tvPing.setTextColor(Color.parseColor("#FFC107"));
                else holder.tvPing.setTextColor(Color.parseColor("#FF9800"));
            }
            else holder.tvPing.setText("-- ms");
        }
        else {
            holder.tvStatus.setText("Offline / Checking");
            holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
            holder.tvPing.setText("-- ms");
            holder.tvPing.setTextColor(Color.parseColor("#9E9E9E"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onServerClick(server);
        });
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHostname;
        TextView tvAddress;
        TextView tvPlayers;
        TextView tvPing;
        TextView tvGamemode;
        TextView tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHostname = itemView.findViewById(R.id.TVHostname);
            tvAddress = itemView.findViewById(R.id.TVAddress);
            tvPlayers = itemView.findViewById(R.id.TVPlayers);
            tvPing = itemView.findViewById(R.id.TVPing);
            tvGamemode = itemView.findViewById(R.id.TVGamemode);
            tvStatus = itemView.findViewById(R.id.TVStatus);
        }
    }
}
