package org.nexus.leDatOrder.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.models.Order;
import org.nexus.leDatOrder.utils.ColorUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrderDeliveryGUI {
    private static final Map<UUID, Order> playerDeliveryMap = new HashMap<>();
    
    private final LeDatOrder plugin;
    private final Player player;
    private final Order order;
    private Inventory inventory;

    public OrderDeliveryGUI(LeDatOrder plugin, Player player, Order order) {
        this.plugin = plugin;
        this.player = player;
        this.order = order;
        playerDeliveryMap.put(player.getUniqueId(), order);
    }

    public void open() {
        createInventory();
        player.openInventory(inventory);
    }

    private void createInventory() {
        String title = ColorUtils.colorize("&6Order -> " + order.getPlayerName());
        inventory = Bukkit.createInventory(null, 45, title);
    }

    public static Order getOrderByPlayer(Player player) {
        return playerDeliveryMap.get(player.getUniqueId());
    }

    public static void removePlayer(Player player) {
        playerDeliveryMap.remove(player.getUniqueId());
    }

    public static void processDelivery(LeDatOrder plugin, Player player, Inventory inventory) {
        Order order = getOrderByPlayer(player);
        if (order == null) return;
    
        // Kiểm tra các item trong inventory
        int deliveredAmount = 0;
        Map<Integer, ItemStack> returnItems = new HashMap<>();
    
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                if (item.getType() == order.getMaterial()) {
                    // Item đúng loại
                    deliveredAmount += item.getAmount();
                } else {
                    // Item không đúng loại, trả lại cho người chơi
                    returnItems.put(i, item);
                }
            }
        }
    
        // Nếu không có item nào đúng loại, trả lại tất cả và đóng GUI
        if (deliveredAmount == 0) {
            for (Map.Entry<Integer, ItemStack> entry : returnItems.entrySet()) {
                player.getInventory().addItem(entry.getValue());
            }
            removePlayer(player);
            return;
        }
    
        // Tính toán số lượng thừa cần trả lại
        int remainingNeeded = order.getRequiredAmount() - order.getReceivedAmount();
        int excessAmount = 0;
        
        if (deliveredAmount > remainingNeeded) {
            excessAmount = deliveredAmount - remainingNeeded;
            deliveredAmount = remainingNeeded;
        }
        
        // Cập nhật order và trả tiền cho người chơi
        double paymentAmount = order.getPricePerItem() * deliveredAmount;
        plugin.getOrderManager().deliverItems(player, order, deliveredAmount);
    
        // Trả tiền cho người chơi
        plugin.getVaultManager().deposit(player, paymentAmount);
    
        // Kiểm tra nếu đơn hàng đã hoàn thành thì xóa
        if (order.getReceivedAmount() >= order.getRequiredAmount()) {
            player.sendMessage(ColorUtils.colorize("&aĐơn hàng đã nhận đủ số lượng yêu cầu."));
            
            // Nếu đã thu thập hết items thì xóa order
            if (order.getCollectedAmount() >= order.getRequiredAmount()) {
                plugin.getOrderManager().removeOrder(order.getId());
                player.sendMessage(ColorUtils.colorize("&aĐơn hàng đã hoàn thành và được xóa khỏi hệ thống."));
            }
        }
    
        // Hiển thị actionbar và thông báo
        showDeliveryAnimation(plugin, player, paymentAmount);
    
        // Trả lại các item không đúng loại
        for (Map.Entry<Integer, ItemStack> entry : returnItems.entrySet()) {
            player.getInventory().addItem(entry.getValue());
        }
    
        // Trả lại số lượng thừa nếu có
        if (excessAmount > 0) {
            ItemStack excessItems = new ItemStack(order.getMaterial(), excessAmount);
            player.getInventory().addItem(excessItems);
            player.sendMessage(ColorUtils.colorize("&aĐã trả lại &e" + excessAmount + " &a" + order.getMaterial().name() + " thừa."));
        }
    
        removePlayer(player);
    }
    
    // Phương thức đếm số slot trống trong inventory
    private static int countFreeSlots(Player player) {
        int freeSlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null) {
                freeSlots++;
            }
        }
        return freeSlots;
    }
    
    // Phương thức tính số slot cần thiết cho một số lượng item
    private static int calculateRequiredSlots(Material material, int amount) {
        int maxStackSize = material.getMaxStackSize();
        return (int) Math.ceil((double) amount / maxStackSize);
    }

    private static void showDeliveryAnimation(LeDatOrder plugin, Player player, double amount) {
        // Hiển thị thẳng actionbar thông báo thành công
        String successMessage = "&7Đã gửi đồ thành công và nhận được &6" + String.format("%.2f", amount);
        
        // Sử dụng entity scheduler để gửi action bar đến player
        plugin.getFoliaLib().getScheduler().runAtEntity(player, (task) -> {
            player.sendActionBar(ColorUtils.colorize(successMessage));
        });
        
        // Mở lại GUI Order ngay lập tức
        plugin.getFoliaLib().getScheduler().runAtEntity(player, (task) -> {
            new OrderGUI(plugin, player).open();
        });
    }
}