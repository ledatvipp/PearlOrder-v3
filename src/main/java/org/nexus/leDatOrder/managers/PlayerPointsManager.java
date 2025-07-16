package org.nexus.leDatOrder.managers;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.nexus.leDatOrder.LeDatOrder;

public class PlayerPointsManager {
    private final LeDatOrder plugin;
    private PlayerPointsAPI ppAPI;
    private boolean enabled = false;

    public PlayerPointsManager(LeDatOrder plugin) {
        this.plugin = plugin;
        setupPlayerPoints();
    }

    private void setupPlayerPoints() {
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) {
            plugin.getLogger().warning("PlayerPoints not found! PlayerPoints features will be disabled.");
            return;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            plugin.getLogger().warning("PlayerPoints is not enabled! PlayerPoints features will be disabled.");
            return;
        }

        try {
            this.ppAPI = PlayerPoints.getInstance().getAPI();
            this.enabled = true;
            plugin.getLogger().info("PlayerPoints integration enabled!");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to setup PlayerPoints: " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public PlayerPointsAPI getAPI() {
        return ppAPI;
    }

    public boolean has(Player player, int amount) {
        if (!enabled || ppAPI == null) return false;
        return ppAPI.look(player.getUniqueId()) >= amount;
    }

    public boolean withdraw(Player player, int amount) {
        if (!enabled || ppAPI == null) return false;
        return ppAPI.take(player.getUniqueId(), amount);
    }

    public boolean deposit(Player player, int amount) {
        if (!enabled || ppAPI == null) return false;
        return ppAPI.give(player.getUniqueId(), amount);
    }

    public int getBalance(Player player) {
        if (!enabled || ppAPI == null) return 0;
        return ppAPI.look(player.getUniqueId());
    }
}