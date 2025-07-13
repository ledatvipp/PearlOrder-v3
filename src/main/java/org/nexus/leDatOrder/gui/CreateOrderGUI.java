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

public class CreateOrderGUI {
    private static final Map<UUID, CreateOrderData> playerCreateMap = new HashMap<>();
    
    private final LeDatOrder plugin;
    private final Player player;
    private Inventory inventory;

    public CreateOrderGUI(LeDatOrder plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        
        // Khởi tạo dữ liệu mặc định nếu chưa có
        if (!playerCreateMap.containsKey(player.getUniqueId())) {
            playerCreateMap.put(player.getUniqueId(), new CreateOrderData());
        }
    }

    public void open() {
        createInventory();
        updateInventory();
        player.openInventory(inventory);
    }

    private void createInventory() {
        String title = ColorUtils.colorize("&6Create Order");
        inventory = Bukkit.createInventory(null, 27, title);
    }

    private void updateInventory() {
        inventory.clear();
        
        CreateOrderData data = playerCreateMap.get(player.getUniqueId());

        // Nút Back (RED_STAINED_GLASS_PANE) ở slot 10
        ItemStack backItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ColorUtils.colorize("&cBack"));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.colorize("&7Return to Your Orders"));
            backMeta.setLore(lore);
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(10, backItem);

        // Item yêu cầu order (slot 12)
        ItemStack materialItem = new ItemStack(data.getMaterial());
        ItemMeta materialMeta = materialItem.getItemMeta();
        if (materialMeta != null) {
            materialMeta.setDisplayName(ColorUtils.colorize("&6Select Material"));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.colorize("&7Current: &a" + data.getMaterial().name()));
            lore.add(ColorUtils.colorize("&7Click to change material"));
            materialMeta.setLore(lore);
            materialItem.setItemMeta(materialMeta);
        }
        inventory.setItem(12, materialItem);

        // Số lượng (CHEST) ở slot 13
        ItemStack amountItem = new ItemStack(Material.CHEST);
        ItemMeta amountMeta = amountItem.getItemMeta();
        if (amountMeta != null) {
            amountMeta.setDisplayName(ColorUtils.colorize("&6Set Amount"));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.colorize("&7Current: &a" + data.getAmount()));
            lore.add(ColorUtils.colorize("&7Click to set amount"));
            amountMeta.setLore(lore);
            amountItem.setItemMeta(amountMeta);
        }
        inventory.setItem(13, amountItem);

        // Giá tiền (SUNFLOWER) ở slot 14
        ItemStack priceItem = new ItemStack(Material.SUNFLOWER);
        ItemMeta priceMeta = priceItem.getItemMeta();
        if (priceMeta != null) {
            priceMeta.setDisplayName(ColorUtils.colorize("&6Set Price Per Item"));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.colorize("&7Current: &a$" + String.format("%.2f", data.getPricePerItem())));
            lore.add(ColorUtils.colorize("&7Click to set price"));
            priceMeta.setLore(lore);
            priceItem.setItemMeta(priceMeta);
        }
        inventory.setItem(14, priceItem);

        // Nút xác nhận (LIME_STAINED_GLASS_PANE) ở slot 16
        ItemStack confirmItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName(ColorUtils.colorize("&aConfirm"));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.colorize("&7Material: &a" + data.getMaterial().name()));
            lore.add(ColorUtils.colorize("&7Amount: &a" + data.getAmount()));
            lore.add(ColorUtils.colorize("&7Price per item: &a$" + String.format("%.2f", data.getPricePerItem())));
            lore.add(ColorUtils.colorize("&7Total cost: &a$" + String.format("%.2f", data.getAmount() * data.getPricePerItem())));
            confirmMeta.setLore(lore);
            confirmItem.setItemMeta(confirmMeta);
        }
        inventory.setItem(16, confirmItem);
    }

    public static CreateOrderData getCreateData(Player player) {
        return playerCreateMap.get(player.getUniqueId());
    }

    public static void removePlayer(Player player) {
        playerCreateMap.remove(player.getUniqueId());
    }

    public static void createOrder(LeDatOrder plugin, Player player) {
        CreateOrderData data = getCreateData(player);
        if (data == null) return;

        // Kiểm tra số lượng và giá tiền
        if (data.getAmount() <= 0) {
            player.sendMessage(ColorUtils.colorize("&cAmount must be greater than 0."));
            return;
        }

        if (data.getPricePerItem() <= 0) {
            player.sendMessage(ColorUtils.colorize("&cPrice must be greater than 0."));
            return;
        }
        
        // Kiểm tra giới hạn số lượng order dựa trên quyền
        int maxOrders = 5; // Giới hạn mặc định
        
        // Kiểm tra các quyền từ cao đến thấp
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission("ledatorder.amount." + i)) {
                maxOrders = i;
                break;
            }
        }
        
        // Lấy số lượng order hiện tại của người chơi
        List<Order> playerOrders = plugin.getOrderManager().getOrdersByPlayer(player.getUniqueId());
        if (playerOrders.size() >= maxOrders) {
            player.sendMessage(ColorUtils.colorize("&cBạn đã đạt giới hạn số lượng order (&e" + maxOrders + "&c). Hãy hoàn thành hoặc hủy một số order hiện tại."));
            return;
        }

        // Tính tổng chi phí để tạo order
        double fee = 0; // Có thể thêm phí tạo order nếu muốn
        double totalCost = data.getAmount() * data.getPricePerItem();

        // Kiểm tra xem người chơi có đủ tiền không
        if (!plugin.getVaultManager().has(player, totalCost + fee)) {
            player.sendMessage(ColorUtils.colorize("&cYou don't have enough money to create this order. You need $" + String.format("%.2f", totalCost + fee)));
            return;
        }

        // Trừ tiền
        plugin.getVaultManager().withdraw(player, totalCost + fee);
        if (fee > 0) {
            player.sendMessage(ColorUtils.colorize("&aYou have been charged &e$" + String.format("%.2f", fee) + " &afor creating this order."));
        }
        player.sendMessage(ColorUtils.colorize("&aYou have paid &e$" + String.format("%.2f", totalCost) + " &afor this order."));

        // Tạo order mới
        Order order = new Order(
                player.getUniqueId(),
                player.getName(),
                data.getMaterial(),
                data.getPricePerItem(),
                data.getAmount()
        );

        // Thêm vào danh sách order
        plugin.getOrderManager().addOrder(order);

        // Thông báo
        player.sendMessage(ColorUtils.colorize("&aOrder created successfully! (&e" + (playerOrders.size() + 1) + "&a/&e" + maxOrders + "&a)"));

        // Mở lại GUI My Order
        new MyOrderGUI(plugin, player).open();
        
        // Xóa dữ liệu tạo order
        removePlayer(player);
    }

    public static class CreateOrderData {
        private Material material = Material.STONE;
        private int amount = 1;
        private double pricePerItem = 1.0;

        public Material getMaterial() {
            return material;
        }

        public void setMaterial(Material material) {
            this.material = material;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public double getPricePerItem() {
            return pricePerItem;
        }

        public void setPricePerItem(double pricePerItem) {
            this.pricePerItem = pricePerItem;
        }
    }
}