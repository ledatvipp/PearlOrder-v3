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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderGUI {
    private final LeDatOrder plugin;
    private final Player player;
    private Inventory inventory;
    private int currentPage = 0;
    private SortType currentSortType = SortType.RECENTLY_LISTED;
    private OrderType currentFilterType = OrderType.ALL;
    private static final Map<UUID, OrderGUIState> STATES = new HashMap<>();

    private static class OrderGUIState {
        private SortType sortType = SortType.RECENTLY_LISTED;
        private OrderType filterType = OrderType.ALL;
        private int page = 0;
    }

    public OrderGUI(LeDatOrder plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        OrderGUIState state = STATES.get(player.getUniqueId());
        if (state != null) {
            this.currentSortType = state.sortType;
            this.currentFilterType = state.filterType;
            this.currentPage = state.page;
        } else {
            saveState();
        }
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
        if (currentPage < 0) {
            currentPage = 0;
        }

        saveState();

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
        boolean showPrev = currentPage > 0;
        boolean showNext = currentPage < totalPages - 1;
        addFunctionButtons(showPrev, showNext);
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
            String priceString = plugin.getConfigManager().formatCurrencyAmount(order.getPricePerItem(), order.getCurrencyType());
            String paidString = plugin.getConfigManager().formatCurrencyAmount(order.getPaidAmount(), order.getCurrencyType());
            String totalPaidString = plugin.getConfigManager().formatCurrencyAmount(order.getTotalPaid(), order.getCurrencyType());
            for (String line : plugin.getConfigManager().getOrderItemLore()) {
                line = line.replace("%type%", order.getType().getDisplayName())
                        .replace("%price%", priceString)
                        .replace("%revived%", String.valueOf(order.getReceivedAmount()))
                        .replace("%required%", String.valueOf(order.getRequiredAmount()))
                        .replace("%paid%", paidString)
                        .replace("%total_paid%", totalPaidString)
                        .replace("%player%", order.getPlayerName());
                lore.add(ColorUtils.colorize(line));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private void addFunctionButtons(boolean showPrevPage, boolean showNextPage) {
        // Sort button using configuration
        {
            Material sortMat = plugin.getConfigManager().getItemMaterial("gui.order.sort-item", Material.CAULDRON);
            String sortName = plugin.getConfigManager().getItemDisplayName("gui.order.sort-item", "&6Sort");
            List<String> sortLoreCfg = plugin.getConfigManager().getItemLore("gui.order.sort-item");
            ItemStack sortItem = new ItemStack(sortMat);
            ItemMeta sortMeta = sortItem.getItemMeta();
            if (sortMeta != null) {
                sortMeta.setDisplayName(ColorUtils.colorize(sortName));
                List<String> lore = new ArrayList<>();
                if (sortLoreCfg != null && !sortLoreCfg.isEmpty()) {
                    for (String line : sortLoreCfg) {
                        lore.add(ColorUtils.colorize(line));
                    }
                }
                for (SortType type : SortType.values()) {
                    String prefix = (type == currentSortType) ? "&a✓ " : "&7";
                    lore.add(ColorUtils.colorize(prefix + type.getDisplayName()));
                }
                sortMeta.setLore(lore);
                sortItem.setItemMeta(sortMeta);
            }
            inventory.setItem(plugin.getConfigManager().getSortItemSlot(), sortItem);
        }

        // Filter button using configuration
        {
            Material filterMat = plugin.getConfigManager().getItemMaterial("gui.order.filter-item", Material.HOPPER);
            String filterName = plugin.getConfigManager().getItemDisplayName("gui.order.filter-item", "&6Filter");
            List<String> filterLoreCfg = plugin.getConfigManager().getItemLore("gui.order.filter-item");
            ItemStack filterItem = new ItemStack(filterMat);
            ItemMeta filterMeta = filterItem.getItemMeta();
            if (filterMeta != null) {
                filterMeta.setDisplayName(ColorUtils.colorize(filterName));
                List<String> lore = new ArrayList<>();
                if (filterLoreCfg != null && !filterLoreCfg.isEmpty()) {
                    for (String line : filterLoreCfg) {
                        lore.add(ColorUtils.colorize(line));
                    }
                }
                for (OrderType type : OrderType.values()) {
                    String prefix = (type == currentFilterType) ? "&a✓ " : "&7";
                    lore.add(ColorUtils.colorize(prefix + type.getDisplayName()));
                }
                filterMeta.setLore(lore);
                filterItem.setItemMeta(filterMeta);
            }
            inventory.setItem(plugin.getConfigManager().getFilterItemSlot(), filterItem);
        }

        // Refresh button using configuration
        {
            Material refreshMat = plugin.getConfigManager().getItemMaterial("gui.order.refresh-item", Material.MAP);
            String refreshName = plugin.getConfigManager().getItemDisplayName("gui.order.refresh-item", "&6Refresh");
            List<String> refreshLoreCfg = plugin.getConfigManager().getItemLore("gui.order.refresh-item");
            ItemStack refreshItem = new ItemStack(refreshMat);
            ItemMeta refreshMeta = refreshItem.getItemMeta();
            if (refreshMeta != null) {
                refreshMeta.setDisplayName(ColorUtils.colorize(refreshName));
                List<String> lore = new ArrayList<>();
                if (refreshLoreCfg != null && !refreshLoreCfg.isEmpty()) {
                    for (String line : refreshLoreCfg) {
                        lore.add(ColorUtils.colorize(line));
                    }
                }
                refreshMeta.setLore(lore);
                refreshItem.setItemMeta(refreshMeta);
            }
            inventory.setItem(plugin.getConfigManager().getRefreshItemSlot(), refreshItem);
        }

        // Search button using configuration
        {
            Material searchMat = plugin.getConfigManager().getItemMaterial("gui.order.search-item", Material.OAK_SIGN);
            String searchName = plugin.getConfigManager().getItemDisplayName("gui.order.search-item", "&6Tìm kiếm");
            List<String> searchLoreCfg = plugin.getConfigManager().getItemLore("gui.order.search-item");
            ItemStack searchItem = new ItemStack(searchMat);
            ItemMeta searchMeta = searchItem.getItemMeta();
            if (searchMeta != null) {
                searchMeta.setDisplayName(ColorUtils.colorize(searchName));
                List<String> lore = new ArrayList<>();
                if (searchLoreCfg != null && !searchLoreCfg.isEmpty()) {
                    for (String line : searchLoreCfg) {
                        lore.add(ColorUtils.colorize(line));
                    }
                }
                searchMeta.setLore(lore);
                searchItem.setItemMeta(searchMeta);
            }
            inventory.setItem(plugin.getConfigManager().getSearchItemSlot(), searchItem);
        }

        // Create Order button using configuration
        {
            Material createMat = plugin.getConfigManager().getItemMaterial("gui.order.create-item", Material.DROPPER);
            String createName = plugin.getConfigManager().getItemDisplayName("gui.order.create-item", "&6Tạo Order");
            List<String> createLoreCfg = plugin.getConfigManager().getItemLore("gui.order.create-item");
            ItemStack createItem = new ItemStack(createMat);
            ItemMeta createMeta = createItem.getItemMeta();
            if (createMeta != null) {
                createMeta.setDisplayName(ColorUtils.colorize(createName));
                List<String> lore = new ArrayList<>();
                if (createLoreCfg != null && !createLoreCfg.isEmpty()) {
                    for (String line : createLoreCfg) {
                        lore.add(ColorUtils.colorize(line));
                    }
                }
                createMeta.setLore(lore);
                createItem.setItemMeta(createMeta);
            }
            inventory.setItem(plugin.getConfigManager().getCreateItemSlot(), createItem);
        }

        // Previous page button using configuration (only show if currentPage > 0)
        if (showPrevPage) {
            Material prevMat = plugin.getConfigManager().getItemMaterial("gui.order.previous-page-item", Material.ARROW);
            String prevName = plugin.getConfigManager().getItemDisplayName("gui.order.previous-page-item", "&6Trang trước");
            List<String> prevLoreCfg = plugin.getConfigManager().getItemLore("gui.order.previous-page-item");
            ItemStack prevItem = new ItemStack(prevMat);
            ItemMeta prevMeta = prevItem.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(ColorUtils.colorize(prevName));
                List<String> lore = new ArrayList<>();
                if (prevLoreCfg != null && !prevLoreCfg.isEmpty()) {
                    for (String line : prevLoreCfg) {
                        lore.add(ColorUtils.colorize(line));
                    }
                }
                prevMeta.setLore(lore);
                prevItem.setItemMeta(prevMeta);
            }
            inventory.setItem(plugin.getConfigManager().getPreviousPageItemSlot(), prevItem);
        }

        // Next page button using configuration (only show if there are more pages)
        if (showNextPage) {
            Material nextMat = plugin.getConfigManager().getItemMaterial("gui.order.next-page-item", Material.ARROW);
            String nextName = plugin.getConfigManager().getItemDisplayName("gui.order.next-page-item", "&6Trang sau");
            List<String> nextLoreCfg = plugin.getConfigManager().getItemLore("gui.order.next-page-item");
            ItemStack nextItem = new ItemStack(nextMat);
            ItemMeta nextMeta = nextItem.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ColorUtils.colorize(nextName));
                List<String> lore = new ArrayList<>();
                if (nextLoreCfg != null && !nextLoreCfg.isEmpty()) {
                    for (String line : nextLoreCfg) {
                        lore.add(ColorUtils.colorize(line));
                    }
                }
                nextMeta.setLore(lore);
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
        saveState();
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
        saveState();
    }

    // Thêm các getter và setter
    public SortType getCurrentSortType() {
        return currentSortType;
    }
    
    public void setCurrentSortType(SortType sortType) {
        this.currentSortType = sortType;
        this.currentPage = 0;
        saveState();
    }

    public OrderType getCurrentFilterType() {
        return currentFilterType;
    }

    public void setCurrentFilterType(OrderType filterType) {
        this.currentFilterType = filterType;
        this.currentPage = 0;
        saveState();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    private void saveState() {
        OrderGUIState state = STATES.computeIfAbsent(player.getUniqueId(), ignored -> new OrderGUIState());
        state.sortType = currentSortType;
        state.filterType = currentFilterType;
        state.page = currentPage;
    }

    public static void clearState(Player player) {
        if (player != null) {
            STATES.remove(player.getUniqueId());
        }
    }
}