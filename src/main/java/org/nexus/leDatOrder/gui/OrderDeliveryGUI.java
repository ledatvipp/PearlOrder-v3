package org.nexus.leDatOrder.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
        int actualDelivered = deliveredAmount;

        if (deliveredAmount > remainingNeeded) {
            excessAmount = deliveredAmount - remainingNeeded;
            actualDelivered = remainingNeeded;
        }

        // Items sẽ được lưu trữ trong order để người tạo order có thể collect sau
        // Không cần gửi trực tiếp cho người tạo order nữa

        // Thông báo cho người tạo order (nếu đang online)
        Player orderOwner = plugin.getServer().getPlayer(order.getPlayerUUID());
        if (orderOwner != null && orderOwner.isOnline()) {
            final String deliveryMessage = "&a" + player.getName() + " đã giao &e" + actualDelivered + " &a" +
                    order.getMaterial().name() + " cho đơn hàng của bạn! Vào /order để nhận items.";
            final Player finalOrderOwner = orderOwner;

            plugin.getFoliaLib().getScheduler().runAtEntity(orderOwner, (task) -> {
                finalOrderOwner.sendMessage(ColorUtils.colorize(deliveryMessage));
            });
        }

        // Cập nhật order và trả tiền cho người chơi
        double paymentAmount = order.getPricePerItem() * actualDelivered;
        plugin.getOrderManager().deliverItems(player, order, actualDelivered);

        // Trả tiền cho người giao hàng dựa trên loại tiền tệ
        if (order.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT) {
            if (plugin.getVaultManager().isEnabled()) {
                plugin.getVaultManager().deposit(player, paymentAmount);
                player.sendMessage(ColorUtils.colorize("&aĐã nhận &e$" + String.format("%.2f", paymentAmount) + " &acho việc giao hàng!"));
            }
        } else {
            if (plugin.getPlayerPointsManager().isEnabled()) {
                plugin.getPlayerPointsManager().deposit(player, (int)paymentAmount);
                player.sendMessage(ColorUtils.colorize("&aĐã nhận &e" + (int)paymentAmount + " Points &acho việc giao hàng!"));
            }
        }

        // Kiểm tra nếu đơn hàng đã hoàn thành
        if (order.getReceivedAmount() >= order.getRequiredAmount()) {
            player.sendMessage(ColorUtils.colorize("&aĐơn hàng đã nhận đủ số lượng yêu cầu."));

            if (orderOwner != null && orderOwner.isOnline()) {
                plugin.getFoliaLib().getScheduler().runAtEntity(orderOwner, (task) -> {
                    orderOwner.sendMessage(ColorUtils.colorize("&aĐơn hàng " + order.getMaterial().name() + " đã được hoàn thành! Sử dụng /order để collect items."));
                });
            }
        }

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

        // Hiển thị thông báo thành công
        showDeliveryAnimation(plugin, player, paymentAmount);
    }

    private static void showDeliveryAnimation(LeDatOrder plugin, Player player, double amount) {
        // Mở lại GUI Order ngay lập tức
        plugin.getFoliaLib().getScheduler().runAtEntity(player, (task) -> {
            new OrderGUI(plugin, player).open();
        });
    }
}