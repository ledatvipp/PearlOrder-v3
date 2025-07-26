package org.nexus.leDatOrder.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.models.Order;
import org.nexus.leDatOrder.models.OrderType;
import org.nexus.leDatOrder.models.SortType;
import org.nexus.leDatOrder.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderGUI {
    private final LeDatOrder plugin;
    private final Player player;
    private Inventory inventory;
    private int currentPage = 0;
    private SortType currentSortType = SortType.RECENTLY_LISTED;
    private OrderType currentFilterType = OrderType.ALL;

    public OrderGUI(LeDatOrder plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        createInventory();
        updateInventory();
        player.openInventory(inventory);
    }

    private void createInventory() {
        String title = plugin.getConfigManager().getOrderGuiTitle().replace("%page%", String.valueOf(currentPage + 1));
        int size = plugin.getConfigManager().getOrderGuiSize();
        inventory = Bukkit.createInventory(null, size, title);
    }

    private void updateInventory() {
        inventory.clear();

        // Lấy danh sách order đã sắp xếp và lọc (chỉ những đơn chưa hoàn thành)
        List<Order> filteredOrders = plugin.getOrderManager().getSortedActiveOrders(currentSortType, currentFilterType);

        // Tính toán phân trang
        int totalPages = (int) Math.ceil(filteredOrders.size() / 45.0);
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }

        // Hiển thị các order
        int startIndex = currentPage * 45;
        int endIndex = Math.min(startIndex + 45, filteredOrders.size());

        for (int i = startIndex; i < endIndex; i++) {
            Order order = filteredOrders.get(i);
            int slot = i - startIndex;
            
            ItemStack orderItem = createOrderItem(order);
            inventory.setItem(slot, orderItem);
        }

        // Thêm các nút chức năng
        addFunctionButtons();
    }

    private ItemStack createOrderItem(Order order) {
        ItemStack item = new ItemStack(order.getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            String displayName = plugin.getConfigManager().getOrderItemDisplayName()
                    .replace("%player%", order.getPlayerName());
            meta.setDisplayName(ColorUtils.colorize(displayName));
            
            // Set lore
            List<String> lore = new ArrayList<>();
            for (String line : plugin.getConfigManager().getOrderItemLore()) {
                line = line.replace("%type%", order.getType().getDisplayName())
                        .replace("%price%", String.format("%.2f", order.getPricePerItem()))
                        .replace("%revived%", String.valueOf(order.getReceivedAmount()))
                        .replace("%required%", String.valueOf(order.getRequiredAmount()))
                        .replace("%paid%", String.format("%.2f", order.getPaidAmount()))
                        .replace("%total_paid%", String.format("%.2f", order.getTotalPaid()))
                        .replace("%player%", order.getPlayerName());
                lore.add(ColorUtils.colorize(line));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private void addFunctionButtons() {
        // Sort button (CAULDRON)
        ItemStack sortItem = new ItemStack(Material.CAULDRON);
        ItemMeta sortMeta = sortItem.getItemMeta();
        if (sortMeta != null) {
            sortMeta.setDisplayName(ColorUtils.colorize("&6Sort"));
            List<String> sortLore = new ArrayList<>();
            for (SortType type : SortType.values()) {
                String prefix = (type == currentSortType) ? "&a✓ " : "&7";
                sortLore.add(ColorUtils.colorize(prefix + type.getDisplayName()));
            }
            sortMeta.setLore(sortLore);
            sortItem.setItemMeta(sortMeta);
        }
        inventory.setItem(plugin.getConfigManager().getSortItemSlot(), sortItem);

        // Filter button (HOPPER)
        ItemStack filterItem = new ItemStack(Material.HOPPER);
        ItemMeta filterMeta = filterItem.getItemMeta();
        if (filterMeta != null) {
            filterMeta.setDisplayName(ColorUtils.colorize("&6Filter"));
            List<String> filterLore = new ArrayList<>();
            for (OrderType type : OrderType.values()) {
                String prefix = (type == currentFilterType) ? "&a✓ " : "&7";
                filterLore.add(ColorUtils.colorize(prefix + type.getDisplayName()));
            }
            filterMeta.setLore(filterLore);
            filterItem.setItemMeta(filterMeta);
        }
        inventory.setItem(plugin.getConfigManager().getFilterItemSlot(), filterItem);

        // Refresh button (MAP)
        ItemStack refreshItem = new ItemStack(Material.MAP);
        ItemMeta refreshMeta = refreshItem.getItemMeta();
        if (refreshMeta != null) {
            refreshMeta.setDisplayName(ColorUtils.colorize("&6Refresh"));
            List<String> refreshLore = new ArrayList<>();
            refreshLore.add(ColorUtils.colorize("&7Làm mới GUI"));
            refreshMeta.setLore(refreshLore);
            refreshItem.setItemMeta(refreshMeta);
        }
        inventory.setItem(plugin.getConfigManager().getRefreshItemSlot(), refreshItem);

        // Search button (OAK_SIGN)
        ItemStack searchItem = new ItemStack(Material.OAK_SIGN);
        ItemMeta searchMeta = searchItem.getItemMeta();
        if (searchMeta != null) {
            searchMeta.setDisplayName(ColorUtils.colorize("&6Tìm kiếm"));
            List<String> searchLore = new ArrayList<>();
            searchLore.add(ColorUtils.colorize("&7Tìm kiếm material nhanh"));
            searchMeta.setLore(searchLore);
            searchItem.setItemMeta(searchMeta);
        }
        inventory.setItem(plugin.getConfigManager().getSearchItemSlot(), searchItem);

        // Create Order button (DROPPER)
        ItemStack createItem = new ItemStack(Material.DROPPER);
        ItemMeta createMeta = createItem.getItemMeta();
        if (createMeta != null) {
            createMeta.setDisplayName(ColorUtils.colorize("&6Tạo Order"));
            List<String> createLore = new ArrayList<>();
            createLore.add(ColorUtils.colorize("&7Mở GUI tạo order mới"));
            createMeta.setLore(createLore);
            createItem.setItemMeta(createMeta);
        }
        inventory.setItem(plugin.getConfigManager().getCreateItemSlot(), createItem);

        // Previous page button (ARROW)
        if (currentPage > 0) {
            ItemStack prevItem = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevItem.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(ColorUtils.colorize("&6Trang trước"));
                prevItem.setItemMeta(prevMeta);
            }
            inventory.setItem(plugin.getConfigManager().getPreviousPageItemSlot(), prevItem);
        }

        // Next page button (ARROW)
        List<Order> filteredOrders = plugin.getOrderManager().getSortedOrders(currentSortType, currentFilterType);
        int totalPages = (int) Math.ceil(filteredOrders.size() / 45.0);
        if (currentPage < totalPages - 1) {
            ItemStack nextItem = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextItem.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ColorUtils.colorize("&6Trang sau"));
                nextItem.setItemMeta(nextMeta);
            }
            inventory.setItem(plugin.getConfigManager().getNextPageItemSlot(), nextItem);
        }
    }

    public void nextPage() {
        List<Order> filteredOrders = plugin.getOrderManager().getSortedActiveOrders(currentSortType, currentFilterType);
        int totalPages = (int) Math.ceil(filteredOrders.size() / 45.0);
        if (currentPage < totalPages - 1) {
            currentPage++;
            
            // Đảm bảo inventory đã được khởi tạo
            if (inventory == null) {
                createInventory();
            } else {
                // Tạo lại inventory với tiêu đề mới
                String title = plugin.getConfigManager().getOrderGuiTitle().replace("%page%", String.valueOf(currentPage + 1));
                int size = plugin.getConfigManager().getOrderGuiSize();
                inventory = Bukkit.createInventory(null, size, title);
            }
            
            updateInventory();
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            
            // Đảm bảo inventory đã được khởi tạo
            if (inventory == null) {
                createInventory();
            } else {
                // Tạo lại inventory với tiêu đề mới
                String title = plugin.getConfigManager().getOrderGuiTitle().replace("%page%", String.valueOf(currentPage + 1));
                int size = plugin.getConfigManager().getOrderGuiSize();
                inventory = Bukkit.createInventory(null, size, title);
            }
            
            updateInventory();
        }
    }

    // Thêm các getter và setter
    public SortType getCurrentSortType() {
        return currentSortType;
    }
    
    public void setCurrentSortType(SortType sortType) {
        this.currentSortType = sortType;
    }
    
    public OrderType getCurrentFilterType() {
        return currentFilterType;
    }
    
    public void setCurrentFilterType(OrderType filterType) {
        this.currentFilterType = filterType;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
}