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
import java.util.stream.Collectors;

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
        // Use configurable title and size for the player's orders GUI
        String title = plugin.getConfigManager().getMyOrderGuiTitle();
        int size = plugin.getConfigManager().getMyOrderGuiSize();
        inventory = Bukkit.createInventory(null, size, title);
    }

    private void updateInventory() {
        inventory.clear();

        // Lấy danh sách order của người chơi và lọc ra những order chưa hoàn thành hoặc còn items để collect
        List<Order> playerOrders = plugin.getOrderManager().getOrdersByPlayer(player.getUniqueId())
                .stream()
                .filter(order -> !order.isCompleted() || order.getCollectedAmount() < order.getReceivedAmount())
                .collect(Collectors.toList());

        // Hiển thị các order của người chơi (slot 0-26)
        for (int i = 0; i < Math.min(playerOrders.size(), 27); i++) {
            Order order = playerOrders.get(i);
            ItemStack orderItem = createMyOrderItem(order);
            inventory.setItem(i, orderItem);
        }

        // Lấy thông tin border item và create item từ cấu hình
        // Border items fill the last row of the inventory (from size-9 to size-1)
        Material borderMat = plugin.getConfigManager().getItemMaterial("gui.my-order.border-item", Material.BLACK_STAINED_GLASS_PANE);
        String borderName = plugin.getConfigManager().getItemDisplayName("gui.my-order.border-item", " ");
        List<String> borderLore = plugin.getConfigManager().getItemLore("gui.my-order.border-item");
        ItemStack borderItem = new ItemStack(borderMat);
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(ColorUtils.colorize(borderName));
            if (borderLore != null && !borderLore.isEmpty()) {
                List<String> lore = new ArrayList<>();
                for (String l : borderLore) {
                    lore.add(ColorUtils.colorize(l));
                }
                borderMeta.setLore(lore);
            }
            borderItem.setItemMeta(borderMeta);
        }
        // Fill the last row with border items
        int invSize = inventory.getSize();
        for (int i = invSize - 9; i < invSize; i++) {
            inventory.setItem(i, borderItem);
        }
        // Create button from config
        int createSlot = plugin.getConfigManager().getItemSlot("gui.my-order.create-item", 31);
        Material createMat = plugin.getConfigManager().getItemMaterial("gui.my-order.create-item", Material.MAP);
        String createName = plugin.getConfigManager().getItemDisplayName("gui.my-order.create-item", "&6Create New Order");
        List<String> createLore = plugin.getConfigManager().getItemLore("gui.my-order.create-item");
        ItemStack createItem = new ItemStack(createMat);
        ItemMeta createMeta = createItem.getItemMeta();
        if (createMeta != null) {
            createMeta.setDisplayName(ColorUtils.colorize(createName));
            if (createLore != null && !createLore.isEmpty()) {
                List<String> lore = new ArrayList<>();
                for (String l : createLore) {
                    lore.add(ColorUtils.colorize(l));
                }
                createMeta.setLore(lore);
            }
            createItem.setItemMeta(createMeta);
        }
        // Place the create button
        if (createSlot >= 0 && createSlot < inventory.getSize()) {
            inventory.setItem(createSlot, createItem);
        }
    }

    private ItemStack createMyOrderItem(Order order) {
        // Create an item representing the player's order using configurable display name and lore
        ItemStack item = new ItemStack(order.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Display name
            String displayName = plugin.getConfigManager()
                    .getItemDisplayName("gui.my-order.order-item", "&6%player%'s Order")
                    .replace("%player%", order.getPlayerName());
            meta.setDisplayName(ColorUtils.colorize(displayName));

            // Build lore with placeholder replacements
            List<String> configLore = plugin.getConfigManager().getItemLore("gui.my-order.order-item");
            if (configLore == null || configLore.isEmpty()) {
                configLore = new ArrayList<>();
            }
            List<String> lore = new ArrayList<>();
            // Prepare values
            String priceString = plugin.getConfigManager().formatCurrencyAmount(order.getPricePerItem(), order.getCurrencyType());
            double totalCost = order.getPricePerItem() * order.getRequiredAmount();
            String totalString = plugin.getConfigManager().formatCurrencyAmount(totalCost, order.getCurrencyType());
            String currencyName = plugin.getConfigManager().getCurrencyDisplayName(order.getCurrencyType());
            String paidString = plugin.getConfigManager().formatCurrencyAmount(order.getPaidAmount(), order.getCurrencyType());
            for (String line : configLore) {
                String processed = line
                        .replace("%player%", order.getPlayerName())
                        .replace("%amount%", String.valueOf(order.getRequiredAmount()))
                        .replace("%material%", order.getMaterial().name())
                        .replace("%price%", priceString)
                        .replace("%currency%", currencyName)
                        .replace("%received%", String.valueOf(order.getReceivedAmount()))
                        .replace("%required%", String.valueOf(order.getRequiredAmount()))
                        .replace("%paid%", paidString)
                        .replace("%total%", totalString);
                lore.add(ColorUtils.colorize(processed));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}