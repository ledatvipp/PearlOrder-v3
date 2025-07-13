package org.nexus.leDatOrder.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.models.Order;
import org.nexus.leDatOrder.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class MyOrderGUI {
    private final LeDatOrder plugin;
    private final Player player;
    private Inventory inventory;

    public MyOrderGUI(LeDatOrder plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        createInventory();
        updateInventory();
        player.openInventory(inventory);
    }

    private void createInventory() {
        String title = ColorUtils.colorize("&6Your Orders");
        inventory = Bukkit.createInventory(null, 36, title);
    }

    private void updateInventory() {
        inventory.clear();

        // Lấy danh sách order của người chơi
        List<Order> playerOrders = plugin.getOrderManager().getOrdersByPlayer(player.getUniqueId());

        // Hiển thị các order của người chơi (slot 0-26)
        for (int i = 0; i < Math.min(playerOrders.size(), 27); i++) {
            Order order = playerOrders.get(i);
            ItemStack orderItem = createMyOrderItem(order);
            inventory.setItem(i, orderItem);
        }

        // Thêm border (BLACK_STAINED_GLASS_PANE) cho slot 27-35
        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            borderItem.setItemMeta(borderMeta);
        }

        for (int i = 27; i < 36; i++) {
            inventory.setItem(i, borderItem);
        }

        // Thêm nút Create Order (MAP) ở slot 31
        ItemStack createItem = new ItemStack(Material.MAP);
        ItemMeta createMeta = createItem.getItemMeta();
        if (createMeta != null) {
            createMeta.setDisplayName(ColorUtils.colorize("&6Create New Order"));
            List<String> createLore = new ArrayList<>();
            createLore.add(ColorUtils.colorize("&7Click to create a new order"));
            createMeta.setLore(createLore);
            createItem.setItemMeta(createMeta);
        }
        inventory.setItem(31, createItem);
    }

    private ItemStack createMyOrderItem(Order order) {
        ItemStack item = new ItemStack(order.getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            String displayName = ColorUtils.colorize("&6" + player.getName() + "'s Order");
            meta.setDisplayName(displayName);
            
            // Set lore
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.colorize("&a" + order.getRequiredAmount() + " &f" + order.getMaterial().name()));
            lore.add(ColorUtils.colorize("&a$" + String.format("%.2f", order.getPricePerItem()) + " per item"));
            lore.add(" ");
            lore.add(ColorUtils.colorize("&e" + order.getReceivedAmount() + "/" + order.getRequiredAmount() + " &7Delivered"));
            lore.add(ColorUtils.colorize("&e" + String.format("%.2f", order.getPaidAmount()) + "/" + 
                    String.format("%.2f", order.getRequiredAmount() * order.getPricePerItem()) + " &7Paid"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
}