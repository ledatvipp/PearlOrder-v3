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
import org.nexus.leDatOrder.utils.ColorUtils;

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

                    Order order = new Order(playerUUID, playerName, material, pricePerItem, requiredAmount);
                    // Set additional properties
                    order.addReceivedAmount(orderSection.getInt("receivedAmount"));
                    order.addPaidAmount(orderSection.getDouble("paidAmount"));

                    // Đọc collectedAmount nếu có, mặc định là 0
                    if (orderSection.contains("collectedAmount")) {
                        order.addCollectedAmount(orderSection.getInt("collectedAmount"));
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
            config.set(path + ".collectedAmount", order.getCollectedAmount()); // Lưu collectedAmount
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

    public void deliverItems(Player player, Order order, int amount) {
        order.addReceivedAmount(amount);

        double paymentAmount = order.getPricePerItem() * amount;
        order.addPaidAmount(paymentAmount);

        Player orderOwner = plugin.getServer().getPlayer(order.getPlayerUUID());
        if (orderOwner != null && orderOwner.isOnline()) {
            String message = "&a" + player.getName() + " đã giao &e" + amount + " &a" + 
                    order.getMaterial().name() + " cho đơn hàng của bạn và bạn đã trả &e$" + 
                    String.format("%.2f", paymentAmount);
            
            plugin.getFoliaLib().getScheduler().runAtEntity(orderOwner, (task) -> {
                orderOwner.sendMessage(ColorUtils.colorize(message));
            });
        }
        
        saveOrders();
    }

    public void payOrder(Player player, Order order, double amount) {
        order.addPaidAmount(amount);
        saveOrders();
    }
}