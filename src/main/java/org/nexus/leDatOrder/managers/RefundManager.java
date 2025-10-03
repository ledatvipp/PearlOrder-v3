package org.nexus.leDatOrder.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nexus.leDatOrder.LeDatOrder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RefundManager {
    private final LeDatOrder plugin;
    private final File refundsFile;
    private final Map<UUID, Map<Material, Integer>> pendingRefunds = new HashMap<>();

    public RefundManager(LeDatOrder plugin) {
        this.plugin = plugin;
        this.refundsFile = new File(plugin.getDataFolder(), "refunds.yml");
        loadRefunds();
    }

    private void loadRefunds() {
        if (!refundsFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                refundsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create refunds.yml file: " + e.getMessage());
                return;
            }
        }

        FileConfiguration configuration = YamlConfiguration.loadConfiguration(refundsFile);
        ConfigurationSection refundsSection = configuration.getConfigurationSection("refunds");
        if (refundsSection == null) {
            return;
        }

        for (String playerKey : refundsSection.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(playerKey);
                ConfigurationSection playerSection = refundsSection.getConfigurationSection(playerKey);
                if (playerSection == null) {
                    continue;
                }

                Map<Material, Integer> materials = new HashMap<>();
                for (String materialKey : playerSection.getKeys(false)) {
                    try {
                        Material material = Material.valueOf(materialKey);
                        int amount = playerSection.getInt(materialKey);
                        if (amount > 0) {
                            materials.merge(material, amount, Integer::sum);
                        }
                    } catch (IllegalArgumentException ignored) {
                        plugin.getLogger().warning("Invalid material " + materialKey + " in refunds.yml for player " + playerKey);
                    }
                }

                if (!materials.isEmpty()) {
                    pendingRefunds.put(playerId, materials);
                }
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Invalid player UUID in refunds.yml: " + playerKey);
            }
        }
    }

    public void queueRefund(UUID playerId, Material material, int amount) {
        if (amount <= 0 || material == Material.AIR) {
            return;
        }

        pendingRefunds.computeIfAbsent(playerId, ignored -> new HashMap<>())
                .merge(material, amount, Integer::sum);
        saveRefunds();
    }

    public void deliverRefunds(Player player) {
        Map<Material, Integer> refunds = pendingRefunds.remove(player.getUniqueId());
        if (refunds == null || refunds.isEmpty()) {
            return;
        }

        int totalItems = 0;
        for (Map.Entry<Material, Integer> entry : refunds.entrySet()) {
            Material material = entry.getKey();
            int amount = entry.getValue();
            if (amount <= 0) {
                continue;
            }

            totalItems += amount;
            giveItems(player, material, amount);
        }

        saveRefunds();

        if (totalItems > 0) {
            player.sendMessage(plugin.getConfigManager().getMessage("refund.received", "%amount%", String.valueOf(totalItems)));
        }
    }

    private void giveItems(Player player, Material material, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            int stackSize = Math.min(material.getMaxStackSize(), remaining);
            ItemStack stack = new ItemStack(material, stackSize);
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
            if (!leftovers.isEmpty()) {
                for (ItemStack leftover : leftovers.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                }
            }
            remaining -= stackSize;
        }
    }

    private void saveRefunds() {
        FileConfiguration configuration = new YamlConfiguration();
        for (Map.Entry<UUID, Map<Material, Integer>> entry : pendingRefunds.entrySet()) {
            UUID playerId = entry.getKey();
            Map<Material, Integer> materials = entry.getValue();
            if (materials == null || materials.isEmpty()) {
                continue;
            }

            for (Map.Entry<Material, Integer> materialEntry : materials.entrySet()) {
                int amount = materialEntry.getValue();
                if (amount <= 0) {
                    continue;
                }
                configuration.set("refunds." + playerId + "." + materialEntry.getKey().name(), amount);
            }
        }

        try {
            configuration.save(refundsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save refunds.yml: " + e.getMessage());
        }
    }
}
