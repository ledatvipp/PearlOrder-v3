package org.nexus.leDatOrder.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.gui.MyOrderGUI;
import org.nexus.leDatOrder.gui.OrderGUI;
import org.nexus.leDatOrder.utils.ColorUtils;

public class OrderCommand implements CommandExecutor {
    private final LeDatOrder plugin;

    public OrderCommand(LeDatOrder plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize("&cThis command can only be used by players."));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ledatorder.use")) {
            player.sendMessage(ColorUtils.colorize("&cYou don't have permission to use this command."));
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
                player.sendMessage(ColorUtils.colorize("&cYou don't have permission to use this command."));
                return true;
            }

            plugin.getConfigManager().loadConfig();
            player.sendMessage(ColorUtils.colorize("&aConfig reloaded."));
            return true;
        }

        // Hiển thị trợ giúp
        player.sendMessage(ColorUtils.colorize("&6===== LeDatOrder Help ====="));
        player.sendMessage(ColorUtils.colorize("&e/order &7- Open the main order GUI"));
        player.sendMessage(ColorUtils.colorize("&e/order my &7- Open your orders GUI"));
        if (player.hasPermission("ledatorder.admin")) {
            player.sendMessage(ColorUtils.colorize("&e/order reload &7- Reload the config"));
        }

        return true;
    }
}