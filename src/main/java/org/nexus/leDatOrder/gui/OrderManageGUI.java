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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderManageGUI {
    private static final Map<UUID, Order> playerManageMap = new HashMap<>();

    private final LeDatOrder plugin;
    private final Player player;
    private final Order order;
    private Inventory inventory;

    public OrderManageGUI(LeDatOrder plugin, Player player, Order order) {
        this.plugin = plugin;
        this.player = player;
        this.order = order;
        playerManageMap.put(player.getUniqueId(), order);
    }

    public void open() {
        createInventory();
        updateInventory();
        player.openInventory(inventory);
    }

    private void createInventory() {
        String title = ColorUtils.colorize("&6Manage Order");
        inventory = Bukkit.createInventory(null, 27, title);
    }

    private void updateInventory() {
        inventory.clear();

        // Hiển thị thông tin order ở giữa
        ItemStack orderItem = new ItemStack(order.getMaterial());
        ItemMeta orderMeta = orderItem.getItemMeta();
        if (orderMeta != null) {
            orderMeta.setDisplayName(ColorUtils.colorize("&6Your Order: " + order.getMaterial().name()));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.colorize("&7Required: &a" + order.getRequiredAmount()));

            // Hiển thị giá theo loại tiền tệ
            if (order.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT) {
                lore.add(ColorUtils.colorize("&7Price per item: &a$" + String.format("%.2f", order.getPricePerItem())));
            } else {
                lore.add(ColorUtils.colorize("&7Price per item: &a" + (int)order.getPricePerItem() + " Points"));
            }

            lore.add(ColorUtils.colorize("&7Currency: &e" + (order.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT ? "Vault Money" : "PlayerPoints")));
            lore.add(ColorUtils.colorize(""));
            lore.add(ColorUtils.colorize("&7Delivered: &e" + order.getReceivedAmount() + "/" + order.getRequiredAmount()));
            lore.add(ColorUtils.colorize("&7Collected: &e" + order.getCollectedAmount() + "/" + order.getReceivedAmount()));

            int availableToCollect = order.getReceivedAmount() - order.getCollectedAmount();
            if (availableToCollect > 0) {
                lore.add(ColorUtils.colorize("&aAvailable to collect: &e" + availableToCollect));
            } else {
                lore.add(ColorUtils.colorize("&7No items to collect"));
            }

            lore.add(ColorUtils.colorize(""));

            // Hiển thị thông tin thanh toán
            double totalCost = order.getRequiredAmount() * order.getPricePerItem();
            if (order.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT) {
                lore.add(ColorUtils.colorize("&7Paid: &e$" + String.format("%.2f", order.getPaidAmount()) + "/" + String.format("%.2f", totalCost)));
            } else {
                lore.add(ColorUtils.colorize("&7Paid: &e" + (int)order.getPaidAmount() + "/" + (int)totalCost + " Points"));
            }

            orderMeta.setLore(lore);
            orderItem.setItemMeta(orderMeta);
        }
        inventory.setItem(13, orderItem);

        // Nút hủy bỏ order (slot 12)
        ItemStack cancelItem = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(ColorUtils.colorize("&cCancel Order"));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.colorize("&7Click to cancel this order"));

            // Thông tin về refund
            int itemsNotReceived = order.getRequiredAmount() - order.getReceivedAmount();
            if (itemsNotReceived > 0) {
                double refundAmount = itemsNotReceived * order.getPricePerItem();
                if (order.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT) {
                    lore.add(ColorUtils.colorize("&aRefund: &e$" + String.format("%.2f", refundAmount)));
                } else {
                    lore.add(ColorUtils.colorize("&aRefund: &e" + (int)refundAmount + " Points"));
                }
            }

            int availableItems = order.getReceivedAmount() - order.getCollectedAmount();
            if (availableItems > 0) {
                lore.add(ColorUtils.colorize("&aItems returned: &e" + availableItems));
            }

            cancelMeta.setLore(lore);
            cancelItem.setItemMeta(cancelMeta);
        }
        inventory.setItem(12, cancelItem);

        // Nút thu thập items (slot 14)
        ItemStack collectItem = new ItemStack(Material.CHEST);
        ItemMeta collectMeta = collectItem.getItemMeta();
        if (collectMeta != null) {
            collectMeta.setDisplayName(ColorUtils.colorize("&aCollect Items"));
            List<String> lore = new ArrayList<>();
            int availableToCollect = order.getReceivedAmount() - order.getCollectedAmount();

            if (availableToCollect > 0) {
                lore.add(ColorUtils.colorize("&7Click to collect delivered items"));
                lore.add(ColorUtils.colorize("&aAvailable: &e" + availableToCollect + " " + order.getMaterial().name()));
            } else {
                lore.add(ColorUtils.colorize("&7No items available to collect"));
                lore.add(ColorUtils.colorize("&7Wait for delivery or cancel order"));
            }

            collectMeta.setLore(lore);
            collectItem.setItemMeta(collectMeta);
        }
        inventory.setItem(14, collectItem);
    }

    public static Order getOrderByPlayer(Player player) {
        return playerManageMap.get(player.getUniqueId());
    }

    public static void removePlayer(Player player) {
        playerManageMap.remove(player.getUniqueId());
    }

    public static void cancelOrder(LeDatOrder plugin, Player player) {
        Order order = getOrderByPlayer(player);
        if (order == null) {
            player.sendMessage(ColorUtils.colorize("&cKhông thể hủy đơn hàng. Đơn hàng đã hoàn thành hoặc không tồn tại."));
            new MyOrderGUI(plugin, player).open();
            return;
        }

        // Tính toán số tiền cần refund: chỉ refund phần chưa nhận được hàng
        int itemsNotReceived = order.getRequiredAmount() - order.getReceivedAmount();
        double refundAmount = itemsNotReceived * order.getPricePerItem();

        // Refund theo loại tiền tệ
        if (refundAmount > 0) {
            if (order.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT) {
                if (plugin.getVaultManager().isEnabled()) {
                    plugin.getVaultManager().deposit(player, refundAmount);
                    player.sendMessage(ColorUtils.colorize("&aRefunded &e$" + String.format("%.2f", refundAmount) + " &ato your account."));
                }
            } else {
                if (plugin.getPlayerPointsManager().isEnabled()) {
                    plugin.getPlayerPointsManager().deposit(player, (int)refundAmount);
                    player.sendMessage(ColorUtils.colorize("&aRefunded &e" + (int)refundAmount + " Points &ato your account."));
                }
            }
        }

        // Trả lại đồ chưa nhận
        int availableItems = order.getReceivedAmount() - order.getCollectedAmount();
        if (availableItems > 0) {
            ItemStack items = new ItemStack(order.getMaterial(), availableItems);
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(items);

            if (!leftover.isEmpty()) {
                // Nếu không đủ chỗ trong inventory, thả xuống đất
                for (ItemStack item : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
                player.sendMessage(ColorUtils.colorize("&aSome items were dropped on the ground because your inventory is full."));
            }

            player.sendMessage(ColorUtils.colorize("&aReturned &e" + availableItems + " &aitems to you."));
        }

        // Xóa order
        plugin.getOrderManager().removeOrder(order.getId());
        player.sendMessage(ColorUtils.colorize("&aOrder has been cancelled."));

        // Mở lại GUI My Order
        new MyOrderGUI(plugin, player).open();

        removePlayer(player);
    }

    public static void collectItems(LeDatOrder plugin, Player player) {
        Order order = getOrderByPlayer(player);
        if (order == null) return;

        int availableItems = order.getReceivedAmount() - order.getCollectedAmount();
        if (availableItems <= 0) {
            player.sendMessage(ColorUtils.colorize("&cNo items available to collect."));
            return;
        }

        // Tạo ItemStack để gửi cho người chơi
        ItemStack items = new ItemStack(order.getMaterial(), availableItems);
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(items);

        // Nếu không đủ chỗ trong inventory
        if (!leftover.isEmpty()) {
            int collected = availableItems - leftover.values().iterator().next().getAmount();

            // Thả items còn lại xuống đất
            for (ItemStack leftoverItem : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
            }

            player.sendMessage(ColorUtils.colorize("&aCollected &e" + collected + " &aitems. Remaining items dropped on ground due to full inventory."));

            // Cập nhật số lượng đã thu thập (tất cả items đều được "collect", một số rơi xuống đất)
            order.addCollectedAmount(availableItems);
        } else {
            // Lưu lại số lượng đã nhận
            player.sendMessage(ColorUtils.colorize("&aCollected &e" + availableItems + " &a" + order.getMaterial().name() + "!"));

            // Cập nhật số lượng đã thu thập
            order.addCollectedAmount(availableItems);
        }

        // Kiểm tra nếu đơn hàng đã hoàn thành và tất cả items đã được collect
        if (order.isCompleted() && order.getCollectedAmount() >= order.getReceivedAmount()) {
            plugin.getOrderManager().removeOrder(order.getId());
            player.sendMessage(ColorUtils.colorize("&aĐơn hàng đã hoàn thành và được xóa khỏi hệ thống."));
            removePlayer(player);
            new MyOrderGUI(plugin, player).open();
            return;
        }

        // Lưu thay đổi
        plugin.getOrderManager().saveOrders();

        // Mở lại GUI quản lý order
        new OrderManageGUI(plugin, player, order).open();
    }
}