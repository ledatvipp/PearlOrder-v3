package org.nexus.leDatOrder;

import com.tcoded.folialib.FoliaLib;
import org.bukkit.plugin.java.JavaPlugin;
import org.nexus.leDatOrder.commands.OrderCommand;
import org.nexus.leDatOrder.listeners.OrderListener;
import org.nexus.leDatOrder.managers.ConfigManager;
import org.nexus.leDatOrder.managers.OrderManager;
import org.nexus.leDatOrder.managers.VaultManager;

public final class LeDatOrder extends JavaPlugin {

    private FoliaLib foliaLib;
    private static LeDatOrder instance;
    private ConfigManager configManager;
    private OrderManager orderManager;
    private VaultManager vaultManager;

    @Override
    public void onEnable() {

        instance = this;
        foliaLib = new FoliaLib(this);

        configManager = new ConfigManager(this);
        orderManager = new OrderManager(this);
        vaultManager = new VaultManager(this);

        getCommand("order").setExecutor(new OrderCommand(this));

        getServer().getPluginManager().registerEvents(new OrderListener(this), this);

        getLogger().info("PearlOrder has been enabled!");
    }

    @Override
    public void onDisable() {
        if (orderManager != null) {
            orderManager.saveOrders();
        }
        
        getLogger().info("PearlOrder has been disabled!");
    }

    public static LeDatOrder getInstance() {
        return instance;
    }

    public FoliaLib getFoliaLib() {
        return foliaLib;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public OrderManager getOrderManager() {
        return orderManager;
    }
    
    public VaultManager getVaultManager() {
        return vaultManager;
    }
}
