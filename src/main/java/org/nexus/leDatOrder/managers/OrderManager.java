package org.nexus.leDatOrder.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.models.Order;
import org.nexus.leDatOrder.models.OrderType;
import org.nexus.leDatOrder.models.SortType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OrderManager {
    private final LeDatOrder plugin;
    private final File ordersFile;
    private final Map<UUID, Order> orders;

    public OrderManager(LeDatOrder plugin) {
        this.plugin = plugin;
        this.ordersFile = new File(plugin.getDataFolder(), "orders.yml");
        this.orders = new HashMap<>();
        loadOrders();
        // Dọn dẹp đơn hàng đã hoàn thành khi khởi động
        cleanupCompletedOrders();
    }

    public void loadOrders() {
        if (!ordersFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                ordersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create orders.yml file: " + e.getMessage());
                return;
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(ordersFile);
        ConfigurationSection ordersSection = config.getConfigurationSection("orders");

        if (ordersSection != null) {
            for (String key : ordersSection.getKeys(false)) {
                ConfigurationSection orderSection = ordersSection.getConfigurationSection(key);
                if (orderSection != null) {
                    UUID id = UUID.fromString(key);
                    UUID playerUUID = UUID.fromString(orderSection.getString("playerUUID"));
                    String playerName = orderSection.getString("playerName");
                    Material material = Material.valueOf(orderSection.getString("material"));
                    double pricePerItem = orderSection.getDouble("pricePerItem");
                    int requiredAmount = orderSection.getInt("requiredAmount");

                    // Đọc CurrencyType nếu có, mặc định là VAULT
                    org.nexus.leDatOrder.enums.CurrencyType currencyType = org.nexus.leDatOrder.enums.CurrencyType.VAULT;
                    if (orderSection.contains("currencyType")) {
                        try {
                            currencyType = org.nexus.leDatOrder.enums.CurrencyType.valueOf(orderSection.getString("currencyType"));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid currency type in order " + key + ", defaulting to VAULT");
                        }
                    }

                    Order order = new Order(playerUUID, playerName, material, pricePerItem, requiredAmount, currencyType);
                    order.setId(id);
                    // Set additional properties
                    order.addReceivedAmount(orderSection.getInt("receivedAmount"));
                    order.addPaidAmount(orderSection.getDouble("paidAmount"));

                    // Đọc collectedAmount nếu có, mặc định là 0
                    if (orderSection.contains("collectedAmount")) {
                        order.addCollectedAmount(orderSection.getInt("collectedAmount"));
                    }

                    if (orderSection.isConfigurationSection("contributions")) {
                        ConfigurationSection contributionSection = orderSection.getConfigurationSection("contributions");
                        for (String contributorKey : contributionSection.getKeys(false)) {
                            try {
                                UUID contributorId = UUID.fromString(contributorKey);
                                int contributedAmount = contributionSection.getInt(contributorKey);
                                order.addContribution(contributorId, contributedAmount);
                            } catch (IllegalArgumentException ex) {
                                plugin.getLogger().warning("Invalid contributor UUID " + contributorKey + " in order " + key);
                            }
                        }
                    }

                    orders.put(id, order);
                }
            }
        }
    }

    public void saveOrders() {
        FileConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, Order> entry : orders.entrySet()) {
            Order order = entry.getValue();
            String path = "orders." + order.getId().toString();

            config.set(path + ".playerUUID", order.getPlayerUUID().toString());
            config.set(path + ".playerName", order.getPlayerName());
            config.set(path + ".material", order.getMaterial().toString());
            config.set(path + ".pricePerItem", order.getPricePerItem());
            config.set(path + ".requiredAmount", order.getRequiredAmount());
            config.set(path + ".receivedAmount", order.getReceivedAmount());
            config.set(path + ".paidAmount", order.getPaidAmount());
            config.set(path + ".totalPaid", order.getTotalPaid());
            config.set(path + ".createdAt", order.getCreatedAt().getTime());
            config.set(path + ".collectedAmount", order.getCollectedAmount());
            config.set(path + ".currencyType", order.getCurrencyType().name()); // Lưu CurrencyType

            config.set(path + ".contributions", null);
            if (!order.getContributions().isEmpty()) {
                for (Map.Entry<UUID, Integer> contributionEntry : order.getContributions().entrySet()) {
                    config.set(path + ".contributions." + contributionEntry.getKey().toString(), contributionEntry.getValue());
                }
            }
        }

        try {
            config.save(ordersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save orders to file: " + e.getMessage());
        }
    }

    public void addOrder(Order order) {
        orders.put(order.getId(), order);
        saveOrders();
    }

    public void removeOrder(UUID orderId) {
        orders.remove(orderId);

        FileConfiguration config = YamlConfiguration.loadConfiguration(ordersFile);
        config.set("orders." + orderId.toString(), null);

        try {
            config.save(ordersFile);
            plugin.getLogger().info("Đã xóa đơn hàng " + orderId + " khỏi hệ thống và file orders.yml");
        } catch (IOException e) {
            plugin.getLogger().severe("Không thể lưu file orders.yml sau khi xóa đơn hàng: " + e.getMessage());
            // Nếu không lưu được, gọi saveOrders() như một phương án dự phòng
            saveOrders();
        }
    }

    public Order getOrder(UUID orderId) {
        return orders.get(orderId);
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public List<Order> getOrdersByPlayer(UUID playerUUID) {
        return orders.values().stream()
                .filter(order -> order.getPlayerUUID().equals(playerUUID))
                .collect(Collectors.toList());
    }

    public List<Order> getOrdersByType(OrderType type) {
        if (type == OrderType.ALL) {
            return getAllOrders();
        }
        return orders.values().stream()
                .filter(order -> order.getType() == type)
                .collect(Collectors.toList());
    }

    public List<Order> getSortedOrders(SortType sortType, OrderType filterType) {
        List<Order> filteredOrders = getOrdersByType(filterType);

        switch (sortType) {
            case MOST_PAID:
                filteredOrders.sort(Comparator.comparing(Order::getPaidAmount).reversed());
                break;
            case MOST_DELIVERED:
                filteredOrders.sort(Comparator.comparing(Order::getReceivedAmount).reversed());
                break;
            case RECENTLY_LISTED:
                filteredOrders.sort(Comparator.comparing(Order::getCreatedAt).reversed());
                break;
            case MOST_MONEY_PER_ITEM:
                filteredOrders.sort(Comparator.comparing(Order::getPricePerItem).reversed());
                break;
        }

        return filteredOrders;
    }

    public void deliverItems(Player deliveryPlayer, Order order, int amount) {
        // Cập nhật số lượng đã nhận
        order.addReceivedAmount(amount);

        if (amount > 0) {
            order.addContribution(deliveryPlayer.getUniqueId(), amount);
        }

        // Tính toán số tiền phải trả
        double paymentAmount = order.getPricePerItem() * amount;
        order.addPaidAmount(paymentAmount);

        // Thông báo cho người tạo order (nếu đang online)
        Player orderOwner = plugin.getServer().getPlayer(order.getPlayerUUID());
        if (orderOwner != null && orderOwner.isOnline()) {
            String paymentString = plugin.getConfigManager().formatCurrencyAmount(paymentAmount, order.getCurrencyType());
            String ownerMessage = plugin.getConfigManager().getMessage(
                    "delivery.owner-update",
                    "%player%", deliveryPlayer.getName(),
                    "%amount%", String.valueOf(amount),
                    "%material%", order.getMaterial().name(),
                    "%payment%", paymentString
            );
            final Player finalOrderOwner = orderOwner;
            plugin.getFoliaLib().getScheduler().runAtEntity(orderOwner, (task) -> finalOrderOwner.sendMessage(ownerMessage));
        }

        // Không tự động xóa order ở đây nữa, để người tạo order có thể collect items
        saveOrders();
    }

    public void checkAndRemoveCompletedOrder(Order order) {
        // Chỉ xóa order khi đã nhận đủ items VÀ đã collect hết
        if (order.getReceivedAmount() >= order.getRequiredAmount() &&
                order.getCollectedAmount() >= order.getReceivedAmount()) {

            removeOrder(order.getId());

            Player orderOwner = plugin.getServer().getPlayer(order.getPlayerUUID());
            if (orderOwner != null && orderOwner.isOnline()) {
                String completionMessage = plugin.getConfigManager().getMessage(
                        "order.completed-owner",
                        "%material%",
                        order.getMaterial().name()
                );
                final Player finalOrderOwner = orderOwner;
                plugin.getFoliaLib().getScheduler().runAtEntity(orderOwner, (task) -> finalOrderOwner.sendMessage(completionMessage));
            }
        }
    }

    public void cleanupCompletedOrders() {
        List<UUID> completedOrderIds = new ArrayList<>();

        // Chỉ xóa những order đã hoàn thành và đã collect hết items
        for (Order order : orders.values()) {
            if (order.getReceivedAmount() >= order.getRequiredAmount() &&
                    order.getCollectedAmount() >= order.getReceivedAmount()) {
                completedOrderIds.add(order.getId());
            }
        }

        for (UUID orderId : completedOrderIds) {
            orders.remove(orderId);
        }

        if (!completedOrderIds.isEmpty()) {
            saveOrders();
            plugin.getLogger().info("Đã dọn dẹp " + completedOrderIds.size() + " đơn hàng đã hoàn thành.");
        }
    }

    public List<Order> getActiveOrders() {
        return orders.values().stream()
                .filter(order -> order.getReceivedAmount() < order.getRequiredAmount())
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách đơn hàng chưa hoàn thành theo loại
     */
    public List<Order> getActiveOrdersByType(OrderType type) {
        if (type == OrderType.ALL) {
            return getActiveOrders();
        }
        return getActiveOrders().stream()
                .filter(order -> order.getType() == type)
                .collect(Collectors.toList());
    }

    public List<Order> getSortedActiveOrders(SortType sortType, OrderType filterType) {
        List<Order> filteredOrders = getActiveOrdersByType(filterType);

        switch (sortType) {
            case MOST_PAID:
                filteredOrders.sort(Comparator.comparing(Order::getPaidAmount).reversed());
                break;
            case MOST_DELIVERED:
                filteredOrders.sort(Comparator.comparing(Order::getReceivedAmount).reversed());
                break;
            case RECENTLY_LISTED:
                filteredOrders.sort(Comparator.comparing(Order::getCreatedAt).reversed());
                break;
            case MOST_MONEY_PER_ITEM:
                filteredOrders.sort(Comparator.comparing(Order::getPricePerItem).reversed());
                break;
        }

        return filteredOrders;
    }

    public void payOrder(Player player, Order order, double amount) {
        order.addPaidAmount(amount);
        saveOrders();
    }
}