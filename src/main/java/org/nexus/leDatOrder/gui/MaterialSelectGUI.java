package org.nexus.leDatOrder.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.models.OrderType;
import org.nexus.leDatOrder.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MaterialSelectGUI {
    private static final Map<UUID, String> playerSearchMap = new HashMap<>();
    
    private final LeDatOrder plugin;
    private final Player player;
    private Inventory inventory;
    private int currentPage = 0;
    private OrderType currentFilterType = OrderType.ALL;
    private boolean showingCommonOnly = false;

    private static final List<Material> COMMON_MATERIALS = Arrays.asList(
            Material.STONE, Material.GRASS_BLOCK, Material.DIRT, Material.COBBLESTONE,
            Material.OAK_LOG, Material.OAK_PLANKS, Material.DIAMOND, Material.IRON_INGOT,
            Material.GOLD_INGOT, Material.EMERALD, Material.COAL, Material.REDSTONE,
            Material.LAPIS_LAZULI, Material.QUARTZ, Material.NETHERITE_INGOT, Material.DIAMOND_SWORD,
            Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL,
            Material.COOKED_BEEF, Material.BREAD, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE,
            Material.OBSIDIAN, Material.ENDER_PEARL, Material.BLAZE_ROD, Material.NETHER_STAR,
            Material.ELYTRA, Material.SHULKER_BOX, Material.TRIDENT, Material.CROSSBOW,
            Material.NETHERITE_INGOT, Material.NETHERITE_SWORD, Material.NETHERITE_PICKAXE,
            Material.NETHERITE_AXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS,
            Material.NETHERITE_BOOTS, Material.BEACON, Material.DRAGON_EGG, Material.END_CRYSTAL,
            Material.TOTEM_OF_UNDYING, Material.EXPERIENCE_BOTTLE, Material.ENCHANTED_GOLDEN_APPLE
    );

    public MaterialSelectGUI(LeDatOrder plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        createInventory();
        updateInventory();
        player.openInventory(inventory);
    }

    private void createInventory() {
        String title = plugin.getConfigManager().getMaterialSelectGuiTitle();
        int size = plugin.getConfigManager().getMaterialSelectGuiSize();
        inventory = Bukkit.createInventory(null, size, title);
    }

    public static void setSearch(Player player, String search) {
        playerSearchMap.put(player.getUniqueId(), search.toUpperCase());
    }

    public static void clearSearch(Player player) {
        playerSearchMap.remove(player.getUniqueId());
    }

    public static String getSearch(Player player) {
        return playerSearchMap.getOrDefault(player.getUniqueId(), "");
    }

    private void updateInventory() {
        inventory.clear();

        // Lấy danh sách material đã lọc
        List<Material> filteredMaterials = getFilteredMaterials();

        // Tính toán phân trang
        int totalPages = (int) Math.ceil(filteredMaterials.size() / 45.0);
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }

        // Hiển thị các material
        int startIndex = currentPage * 45;
        int endIndex = Math.min(startIndex + 45, filteredMaterials.size());

        for (int i = startIndex; i < endIndex; i++) {
            Material material = filteredMaterials.get(i);
            int slot = i - startIndex;
            
            ItemStack materialItem = new ItemStack(material);
            ItemMeta meta = materialItem.getItemMeta();
            if (meta != null) {
                String displayName = plugin.getConfigManager().getMaterialSelectItemDisplayName()
                        .replace("%material%", material.name());
                meta.setDisplayName(ColorUtils.colorize(displayName));
                
                List<String> lore = new ArrayList<>();
                for (String line : plugin.getConfigManager().getMaterialSelectItemLore()) {
                    lore.add(ColorUtils.colorize(line));
                }
                meta.setLore(lore);
                materialItem.setItemMeta(meta);
            }
            inventory.setItem(slot, materialItem);
        }

        // Thêm các nút chức năng
        addFunctionButtons();
    }

    private List<Material> getFilteredMaterials() {
        List<Material> filtered = new ArrayList<>();
        String search = getSearch(player);
        
        // Xác định danh sách material ban đầu
        List<Material> initialList;
        if (showingCommonOnly) {
            initialList = new ArrayList<>(COMMON_MATERIALS);
        } else {
            initialList = Arrays.asList(Material.values());
        }
        
        // Lọc tất cả material hợp lệ trước
        for (Material material : initialList) {
            if (material.isLegacy() || !material.isItem()) continue;
            
            // Lọc theo tìm kiếm nếu có
            if (!search.isEmpty() && !material.name().contains(search)) {
                continue;
            }
            
            // Nếu là ALL thì thêm tất cả material hợp lệ
            if (currentFilterType == OrderType.ALL) {
                filtered.add(material);
                continue;
            }
            
            String name = material.name();
            
            switch (currentFilterType) {
                case BLOCKS:
                    if (material.isBlock()) filtered.add(material);
                    break;
                case TOOLS:
                    if (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || 
                        name.endsWith("_HOE") || name.equals("SHEARS") || name.equals("FLINT_AND_STEEL")) {
                        filtered.add(material);
                    }
                    break;
                case FOOD:
                    if (material.isEdible()) filtered.add(material);
                    break;
                case COMBAT:
                    if (name.endsWith("_SWORD") || name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || 
                        name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") || name.equals("BOW") || 
                        name.equals("CROSSBOW") || name.equals("TRIDENT") || name.equals("SHIELD") || 
                        name.endsWith("ARROW")) {
                        filtered.add(material);
                    }
                    break;
                case POTIONS:
                    if (name.contains("POTION") || name.equals("BREWING_STAND") || name.equals("CAULDRON")) {
                        filtered.add(material);
                    }
                    break;
                case BOOKS:
                    if (name.contains("BOOK") || name.equals("ENCHANTING_TABLE") || name.equals("BOOKSHELF")) {
                        filtered.add(material);
                    }
                    break;
                case INGREDIENTS:
                    if (name.endsWith("_ORE") || name.endsWith("_INGOT") || name.endsWith("_NUGGET") || 
                        name.equals("DIAMOND") || name.equals("EMERALD") || name.equals("COAL") || 
                        name.equals("REDSTONE") || name.equals("LAPIS_LAZULI") || name.equals("QUARTZ") || 
                        name.equals("ROTTEN_FLESH") || name.equals("BONE") || name.equals("STRING") || 
                        name.equals("SPIDER_EYE") || name.equals("GUNPOWDER") || name.equals("ENDER_PEARL")) {
                        filtered.add(material);
                    }
                    break;
                case UTILITIES:
                    if (name.contains("CHEST") || name.contains("SHULKER") || name.contains("HOPPER") || 
                        name.equals("DROPPER") || name.equals("DISPENSER") || name.equals("OBSERVER") || 
                        name.equals("COMPARATOR") || name.equals("REPEATER") || name.equals("TOTEM_OF_UNDYING")) {
                        filtered.add(material);
                    }
                    break;
            }
        }
        return filtered;
    }

    private void addFunctionButtons() {
        // Filter button
        ItemStack filterItem = new ItemStack(plugin.getConfigManager().getMaterialSelectFilterItemMaterial());
        ItemMeta filterMeta = filterItem.getItemMeta();
        if (filterMeta != null) {
            filterMeta.setDisplayName(plugin.getConfigManager().getMaterialSelectFilterItemDisplayName());
            List<String> filterLore = new ArrayList<>();
            for (OrderType type : OrderType.values()) {
                String prefix = (type == currentFilterType) ? "&a✓ " : "&7";
                filterLore.add(ColorUtils.colorize(prefix + type.getDisplayName()));
            }
            filterMeta.setLore(filterLore);
            filterItem.setItemMeta(filterMeta);
        }
        inventory.setItem(plugin.getConfigManager().getMaterialSelectFilterItemSlot(), filterItem);

        // Common materials button
        ItemStack commonItem = new ItemStack(plugin.getConfigManager().getMaterialSelectCommonItemMaterial());
        ItemMeta commonMeta = commonItem.getItemMeta();
        if (commonMeta != null) {
            commonMeta.setDisplayName(plugin.getConfigManager().getMaterialSelectCommonItemDisplayName());
            List<String> commonLore = new ArrayList<>();
            String status = showingCommonOnly ? "&a✓ " : "&7";
            for (String line : plugin.getConfigManager().getMaterialSelectCommonItemLore()) {
                commonLore.add(ColorUtils.colorize(status + line));
            }
            commonMeta.setLore(commonLore);
            commonItem.setItemMeta(commonMeta);
        }
        inventory.setItem(plugin.getConfigManager().getMaterialSelectCommonItemSlot(), commonItem);

        // Back button
        ItemStack backItem = new ItemStack(plugin.getConfigManager().getMaterialSelectBackItemMaterial());
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(plugin.getConfigManager().getMaterialSelectBackItemDisplayName());
            List<String> backLore = new ArrayList<>();
            for (String line : plugin.getConfigManager().getMaterialSelectBackItemLore()) {
                backLore.add(ColorUtils.colorize(line));
            }
            backMeta.setLore(backLore);
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(plugin.getConfigManager().getMaterialSelectBackItemSlot(), backItem);

        // Search button
        ItemStack searchItem = new ItemStack(plugin.getConfigManager().getMaterialSelectSearchItemMaterial());
        ItemMeta searchMeta = searchItem.getItemMeta();
        if (searchMeta != null) {
            searchMeta.setDisplayName(plugin.getConfigManager().getMaterialSelectSearchItemDisplayName());
            List<String> searchLore = new ArrayList<>();
            String search = getSearch(player);
            if (!search.isEmpty()) {
                searchLore.add(ColorUtils.colorize("&7Current: &a" + search));
                searchLore.add(ColorUtils.colorize("&7Click to clear search"));
            } else {
                for (String line : plugin.getConfigManager().getMaterialSelectSearchItemLore()) {
                    searchLore.add(ColorUtils.colorize(line));
                }
            }
            searchMeta.setLore(searchLore);
            searchItem.setItemMeta(searchMeta);
        }
        inventory.setItem(plugin.getConfigManager().getMaterialSelectSearchItemSlot(), searchItem);

        // Previous page button
        if (currentPage > 0) {
            ItemStack prevItem = new ItemStack(plugin.getConfigManager().getMaterialSelectPreviousPageItemMaterial());
            ItemMeta prevMeta = prevItem.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(plugin.getConfigManager().getMaterialSelectPreviousPageItemDisplayName());
                prevItem.setItemMeta(prevMeta);
            }
            inventory.setItem(plugin.getConfigManager().getMaterialSelectPreviousPageItemSlot(), prevItem);
        }

        // Next page button
        List<Material> filteredMaterials = getFilteredMaterials();
        int totalPages = (int) Math.ceil(filteredMaterials.size() / 45.0);
        if (currentPage < totalPages - 1) {
            ItemStack nextItem = new ItemStack(plugin.getConfigManager().getMaterialSelectNextPageItemMaterial());
            ItemMeta nextMeta = nextItem.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(plugin.getConfigManager().getMaterialSelectNextPageItemDisplayName());
                nextItem.setItemMeta(nextMeta);
            }
            inventory.setItem(plugin.getConfigManager().getMaterialSelectNextPageItemSlot(), nextItem);
        }

        // Hiển thị thông tin trang
        ItemStack pageInfoItem = new ItemStack(Material.PAPER);
        ItemMeta pageInfoMeta = pageInfoItem.getItemMeta();
        if (pageInfoMeta != null) {
            pageInfoMeta.setDisplayName(ColorUtils.colorize("&6Page Information"));
            List<String> pageInfoLore = new ArrayList<>();
            pageInfoLore.add(ColorUtils.colorize("&7Current Page: &a" + (currentPage + 1) + "&7/&a" + Math.max(1, totalPages)));
            pageInfoLore.add(ColorUtils.colorize("&7Total Items: &a" + filteredMaterials.size()));
            pageInfoMeta.setLore(pageInfoLore);
            pageInfoItem.setItemMeta(pageInfoMeta);
        }
        inventory.setItem(46, pageInfoItem);
    }

    public void nextPage() {
        List<Material> filteredMaterials = getFilteredMaterials();
        int totalPages = (int) Math.ceil(filteredMaterials.size() / 45.0);
        if (currentPage < totalPages - 1) {
            currentPage++;
            
            // Đảm bảo inventory đã được khởi tạo
            if (inventory == null) {
                createInventory();
            }
            
            updateInventory();
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            if (currentPage > 0) {
                currentPage--;
                
                // Đảm bảo inventory đã được khởi tạo
                if (inventory == null) {
                    createInventory();
                }
                
                updateInventory();
            }
        }
    }

    public void toggleCommonOnly() {
        showingCommonOnly = !showingCommonOnly;
        currentPage = 0; // Reset to first page
        
        // Đảm bảo inventory đã được khởi tạo
        if (inventory == null) {
            createInventory();
        }
        
        updateInventory();
    }

    public void setFilterType(OrderType type) {
        currentFilterType = type;
        currentPage = 0; // Reset to first page
        
        // Đảm bảo inventory đã được khởi tạo
        if (inventory == null) {
            createInventory();
        }
        
        updateInventory();
    }

    // Thêm các phương thức sau vào class MaterialSelectGUI
    public OrderType getCurrentFilterType() {
        return currentFilterType;
    }
    
    public void toggleCommonMaterials() {
        showingCommonOnly = !showingCommonOnly;
    }
}