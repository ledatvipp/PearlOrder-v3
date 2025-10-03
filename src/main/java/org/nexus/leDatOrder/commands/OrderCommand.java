package org.nexus.leDatOrder.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.gui.MyOrderGUI;
import org.nexus.leDatOrder.gui.OrderGUI;

public class OrderCommand implements CommandExecutor {
    private final LeDatOrder plugin;

    public OrderCommand(LeDatOrder plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("command.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ledatorder.use")) {
            player.sendMessage(plugin.getConfigManager().getMessage("command.no-permission"));
            return true;
        }

        if (args.length == 0) {
            // Mở GUI Order chính
            new OrderGUI(plugin, player).open();
            return true;
        }

        if (args[0].equalsIgnoreCase("my") || args[0].equalsIgnoreCase("me")) {
            // Mở GUI My Order
            new MyOrderGUI(plugin, player).open();
            return true;
        }

        // Các lệnh admin
        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("ledatorder.admin")) {
                player.sendMessage(plugin.getConfigManager().getMessage("command.no-permission"));
                return true;
            }

            plugin.getConfigManager().loadConfig();
            player.sendMessage(plugin.getConfigManager().getMessage("command.reload.success"));
            return true;
        }

        // Hiển thị trợ giúp
        player.sendMessage(plugin.getConfigManager().getMessage("command.help.header"));
        player.sendMessage(plugin.getConfigManager().getMessage("command.help.order"));
        player.sendMessage(plugin.getConfigManager().getMessage("command.help.my"));
        if (player.hasPermission("ledatorder.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("command.help.reload"));
        }

        return true;
    }
}