package org.nexus.leDatOrder.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.utils.ColorUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    private final LeDatOrder plugin;
    private final File configFile;
    private FileConfiguration config;

    public ConfigManager(LeDatOrder plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        setDefaults();
    }

    private void setDefaultMaterialSelectGUI() {
        String basePath = "gui.material-select";
        
        if (!config.contains(basePath + ".title")) {
            config.set(basePath + ".title", "&6Select Material");
        }
        
        if (!config.contains(basePath + ".size")) {
            config.set(basePath + ".size", 54);
        }
        
        if (!config.contains(basePath + ".item.display-name")) {
            config.set(basePath + ".item.display-name", "&6%material%");
        }
        
        if (!config.contains(basePath + ".item.lore")) {
            config.set(basePath + ".item.lore", Arrays.asList("&7Click to select this material"));
        }
        
        setDefaultItem(basePath + ".filter-item", "HOPPER", "&6Filter Materials", 47);
        setDefaultItem(basePath + ".common-item", "CHEST", "&6Common Materials", 48, Arrays.asList("&7Show only common materials"));
        setDefaultItem(basePath + ".back-item", "RED_STAINED_GLASS_PANE", "&cBack", 49, Arrays.asList("&7Return to Create Order"));
        setDefaultItem(basePath + ".search-item", "OAK_SIGN", "&6Search Materials", 50, Arrays.asList("&7Click to search for materials"));
        setDefaultItem(basePath + ".previous-page-item", "ARROW", "&6Previous Page", 45);
        setDefaultItem(basePath + ".next-page-item", "ARROW", "&6Next Page", 53);
    }

    private void setDefaults() {
        // Chuyển cấu hình cũ sang cấu trúc mới nếu cần
        if (config.contains("gui.title") && !config.contains("gui.order.title")) {
            // Di chuyển cấu hình cũ sang cấu trúc mới
            config.set("gui.order.title", config.getString("gui.title"));
            config.set("gui.order.size", config.getInt("gui.size"));
            config.set("gui.order.order-item.display-name", config.getString("gui.order-item.display-name"));
            config.set("gui.order.order-item.lore", config.getStringList("gui.order-item.lore"));
            config.set("gui.order.sort-item.slot", config.getInt("gui.sort-item.slot"));
            config.set("gui.order.filter-item.slot", config.getInt("gui.filter-item.slot"));
            config.set("gui.order.refresh-item.slot", config.getInt("gui.refresh-item.slot"));
            config.set("gui.order.search-item.slot", config.getInt("gui.search-item.slot"));
            config.set("gui.order.create-item.slot", config.getInt("gui.create-item.slot"));
            config.set("gui.order.previous-page-item.slot", config.getInt("gui.previous-page-item.slot"));
            config.set("gui.order.next-page-item.slot", config.getInt("gui.next-page-item.slot"));
            
            // Xóa cấu hình cũ
            config.set("gui.title", null);
            config.set("gui.size", null);
            config.set("gui.order-item", null);
            config.set("gui.sort-item", null);
            config.set("gui.filter-item", null);
            config.set("gui.refresh-item", null);
            config.set("gui.search-item", null);
            config.set("gui.create-item", null);
            config.set("gui.previous-page-item", null);
            config.set("gui.next-page-item", null);
        }

        // Thiết lập các giá trị mặc định cho các GUI
        setDefaultOrderGUI();
        setDefaultMyOrderGUI();
        setDefaultOrderManageGUI();
        setDefaultCreateOrderGUI();
        setDefaultMaterialSelectGUI(); // Thêm dòng này

        saveConfig();
    }

    public String getMaterialSelectGuiTitle() {
        return ColorUtils.colorize(config.getString("gui.material-select.title", "&6Select Material"));
    }

    public int getMaterialSelectGuiSize() {
        return config.getInt("gui.material-select.size", 54);
    }
    
    public String getMaterialSelectItemDisplayName() {
        return ColorUtils.colorize(config.getString("gui.material-select.item.display-name", "&6%material%"));
    }
    
    public List<String> getMaterialSelectItemLore() {
        List<String> lore = config.getStringList("gui.material-select.item.lore");
        if (lore.isEmpty()) {
            lore = Arrays.asList("&7Click to select this material");
        }
        return ColorUtils.colorize(lore);
    }

    public Material getMaterialSelectFilterItemMaterial() {
        return getItemMaterial("gui.material-select.filter-item", Material.HOPPER);
    }
    
    public String getMaterialSelectFilterItemDisplayName() {
        return getItemDisplayName("gui.material-select.filter-item", "&6Filter Materials");
    }
    
    public int getMaterialSelectFilterItemSlot() {
        return getItemSlot("gui.material-select.filter-item", 47);
    }

    public Material getMaterialSelectCommonItemMaterial() {
        return getItemMaterial("gui.material-select.common-item", Material.CHEST);
    }
    
    public String getMaterialSelectCommonItemDisplayName() {
        return getItemDisplayName("gui.material-select.common-item", "&6Common Materials");
    }
    
    public List<String> getMaterialSelectCommonItemLore() {
        List<String> lore = getItemLore("gui.material-select.common-item");
        if (lore.isEmpty()) {
            lore = ColorUtils.colorize(Arrays.asList("&7Show only common materials"));
        }
        return lore;
    }
    
    public int getMaterialSelectCommonItemSlot() {
        return getItemSlot("gui.material-select.common-item", 48);
    }

    public Material getMaterialSelectBackItemMaterial() {
        return getItemMaterial("gui.material-select.back-item", Material.RED_STAINED_GLASS_PANE);
    }
    
    public String getMaterialSelectBackItemDisplayName() {
        return getItemDisplayName("gui.material-select.back-item", "&cBack");
    }
    
    public List<String> getMaterialSelectBackItemLore() {
        List<String> lore = getItemLore("gui.material-select.back-item");
        if (lore.isEmpty()) {
            lore = ColorUtils.colorize(Arrays.asList("&7Return to Create Order"));
        }
        return lore;
    }
    
    public int getMaterialSelectBackItemSlot() {
        return getItemSlot("gui.material-select.back-item", 49);
    }

    public Material getMaterialSelectSearchItemMaterial() {
        return getItemMaterial("gui.material-select.search-item", Material.OAK_SIGN);
    }
    
    public String getMaterialSelectSearchItemDisplayName() {
        return getItemDisplayName("gui.material-select.search-item", "&6Search Materials");
    }
    
    public List<String> getMaterialSelectSearchItemLore() {
        List<String> lore = getItemLore("gui.material-select.search-item");
        if (lore.isEmpty()) {
            lore = ColorUtils.colorize(Arrays.asList("&7Click to search for materials"));
        }
        return lore;
    }
    
    public int getMaterialSelectSearchItemSlot() {
        return getItemSlot("gui.material-select.search-item", 50);
    }

    public Material getMaterialSelectPreviousPageItemMaterial() {
        return getItemMaterial("gui.material-select.previous-page-item", Material.ARROW);
    }
    
    public String getMaterialSelectPreviousPageItemDisplayName() {
        return getItemDisplayName("gui.material-select.previous-page-item", "&6Previous Page");
    }
    
    public int getMaterialSelectPreviousPageItemSlot() {
        return getItemSlot("gui.material-select.previous-page-item", 45);
    }

    public Material getMaterialSelectNextPageItemMaterial() {
        return getItemMaterial("gui.material-select.next-page-item", Material.ARROW);
    }
    
    public String getMaterialSelectNextPageItemDisplayName() {
        return getItemDisplayName("gui.material-select.next-page-item", "&6Next Page");
    }
    
    public int getMaterialSelectNextPageItemSlot() {
        return getItemSlot("gui.material-select.next-page-item", 53);
    }

    private void setDefaultOrderGUI() {
        String basePath = "gui.order";
        
        if (!config.contains(basePath + ".title")) {
            config.set(basePath + ".title", "&6Orders (Page %page%)");
        }
        
        if (!config.contains(basePath + ".size")) {
            config.set(basePath + ".size", 54);
        }
        
        if (!config.contains(basePath + ".order-item.display-name")) {
            config.set(basePath + ".order-item.display-name", "&6%player%'s Order");
        }
        
        if (!config.contains(basePath + ".order-item.lore")) {
            config.set(basePath + ".order-item.lore", Arrays.asList(
                    "%type%",
                    "%price%/1 item",
                    " ",
                    "%revived%/%required% &7Delivered",
                    "%paid%/%total_paid% &7Paid",
                    " ",
                    "&fNhấn để giao đồ cho &a%player%"
            ));
        }
        
        setDefaultItem(basePath + ".sort-item", "CAULDRON", "&6Sort", 47);
        setDefaultItem(basePath + ".filter-item", "HOPPER", "&6Filter", 48);
        setDefaultItem(basePath + ".refresh-item", "MAP", "&6Refresh", 49, Arrays.asList("&7Làm mới GUI"));
        setDefaultItem(basePath + ".search-item", "OAK_SIGN", "&6Tìm kiếm", 50, Arrays.asList("&7Tìm kiếm material nhanh"));
        setDefaultItem(basePath + ".create-item", "DROPPER", "&6Tạo Order", 51, Arrays.asList("&7Mở GUI tạo order mới"));
        setDefaultItem(basePath + ".previous-page-item", "ARROW", "&6Trang trước", 45);
        setDefaultItem(basePath + ".next-page-item", "ARROW", "&6Trang sau", 53);
    }

    private void setDefaultMyOrderGUI() {
        String basePath = "gui.my-order";
        
        if (!config.contains(basePath + ".title")) {
            config.set(basePath + ".title", "&6Your Orders");
        }
        
        if (!config.contains(basePath + ".size")) {
            config.set(basePath + ".size", 36);
        }
        
        if (!config.contains(basePath + ".order-item.display-name")) {
            config.set(basePath + ".order-item.display-name", "&6%player%'s Order");
        }
        
        if (!config.contains(basePath + ".order-item.lore")) {
            config.set(basePath + ".order-item.lore", Arrays.asList(
                    "&a%amount% &f%material%",
                    "&a$%price% per item",
                    " ",
                    "&e%received%/%required% &7Delivered",
                    "&e%paid%/%total% &7Paid"
            ));
        }
        
        setDefaultItem(basePath + ".border-item", "BLACK_STAINED_GLASS_PANE", " ", -1);
        setDefaultItem(basePath + ".create-item", "MAP", "&6Create New Order", 31, Arrays.asList("&7Click to create a new order"));
    }

    private void setDefaultOrderManageGUI() {
        String basePath = "gui.order-manage";
        
        if (!config.contains(basePath + ".title")) {
            config.set(basePath + ".title", "&6Manage Order");
        }
        
        if (!config.contains(basePath + ".size")) {
            config.set(basePath + ".size", 27);
        }
        
        if (!config.contains(basePath + ".order-info-item.display-name")) {
            config.set(basePath + ".order-info-item.display-name", "&6Your Order: %material%");
        }
        
        if (!config.contains(basePath + ".order-info-item.lore")) {
            config.set(basePath + ".order-info-item.lore", Arrays.asList(
                    "&7Required: &a%required%",
                    "&7Price per item: &a$%price%",
                    " ",
                    "&7Delivered: &e%received%/%required%",
                    "&7Paid: &e$%paid%/%total%"
            ));
        }
        
        if (!config.contains(basePath + ".order-info-item.slot")) {
            config.set(basePath + ".order-info-item.slot", 13);
        }
        
        setDefaultItem(basePath + ".cancel-item", "BARRIER", "&cCancel Order", 12, Arrays.asList("&7Click to cancel this order"));
        setDefaultItem(basePath + ".collect-item", "CHEST", "&aCollect Items", 14, Arrays.asList("&7Click to collect delivered items", "&7Available: &e%received%"));
    }

    private void setDefaultCreateOrderGUI() {
        String basePath = "gui.create-order";
        
        if (!config.contains(basePath + ".title")) {
            config.set(basePath + ".title", "&6Create Order");
        }
        
        if (!config.contains(basePath + ".size")) {
            config.set(basePath + ".size", 27);
        }
        
        setDefaultItem(basePath + ".back-item", "RED_STAINED_GLASS_PANE", "&cBack", 10, Arrays.asList("&7Return to Your Orders"));
        setDefaultItem(basePath + ".material-item", "STONE", "&6Select Material", 12, Arrays.asList("&7Current: &a%material%", "&7Click to change material"));
        setDefaultItem(basePath + ".amount-item", "CHEST", "&6Set Amount", 13, Arrays.asList("&7Current: &a%amount%", "&7Click to set amount"));
        setDefaultItem(basePath + ".price-item", "SUNFLOWER", "&6Set Price Per Item", 14, Arrays.asList("&7Current: &a$%price%", "&7Click to set price"));
        setDefaultItem(basePath + ".confirm-item", "LIME_STAINED_GLASS_PANE", "&aConfirm", 16, Arrays.asList(
                "&7Material: &a%material%",
                "&7Amount: &a%amount%",
                "&7Price per item: &a$%price%",
                "&7Total cost: &a$%total%"
        ));
    }

    private void setDefaultItem(String path, String material, String displayName, int slot) {
        setDefaultItem(path, material, displayName, slot, null);
    }

    private void setDefaultItem(String path, String material, String displayName, int slot, List<String> lore) {
        if (!config.contains(path + ".material")) {
            config.set(path + ".material", material);
        }
        
        if (!config.contains(path + ".display-name")) {
            config.set(path + ".display-name", displayName);
        }
        
        if (slot >= 0 && !config.contains(path + ".slot")) {
            config.set(path + ".slot", slot);
        }
        
        if (lore != null && !config.contains(path + ".lore")) {
            config.set(path + ".lore", lore);
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config to file: " + e.getMessage());
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getOrderGuiTitle() {
        return ColorUtils.colorize(config.getString("gui.order.title", "&6Orders (Page %page%)"));
    }

    public int getOrderGuiSize() {
        return config.getInt("gui.order.size", 54);
    }

    public String getOrderItemDisplayName() {
        return config.getString("gui.order.order-item.display-name", "&6%player%'s Order");
    }

    public List<String> getOrderItemLore() {
        return config.getStringList("gui.order.order-item.lore");
    }

    public Material getItemMaterial(String path, Material defaultMaterial) {
        String materialName = config.getString(path + ".material");
        if (materialName == null) return defaultMaterial;
        
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material name in config: " + materialName);
            return defaultMaterial;
        }
    }

    public String getItemDisplayName(String path, String defaultName) {
        return ColorUtils.colorize(config.getString(path + ".display-name", defaultName));
    }

    public List<String> getItemLore(String path) {
        List<String> lore = config.getStringList(path + ".lore");
        return ColorUtils.colorize(lore);
    }

    public int getItemSlot(String path, int defaultSlot) {
        return config.getInt(path + ".slot", defaultSlot);
    }

    // Các phương thức getter cho OrderGUI buttons
    public int getSortItemSlot() {
        return getItemSlot("gui.order.sort-item", 47);
    }

    public int getFilterItemSlot() {
        return getItemSlot("gui.order.filter-item", 48);
    }

    public int getRefreshItemSlot() {
        return getItemSlot("gui.order.refresh-item", 49);
    }

    public int getSearchItemSlot() {
        return getItemSlot("gui.order.search-item", 50);
    }

    public int getCreateItemSlot() {
        return getItemSlot("gui.order.create-item", 51);
    }

    public int getPreviousPageItemSlot() {
        return getItemSlot("gui.order.previous-page-item", 45);
    }

    public int getNextPageItemSlot() {
        return getItemSlot("gui.order.next-page-item", 53);
    }

    // Các phương thức getter cho MyOrderGUI
    public String getMyOrderGuiTitle() {
        return ColorUtils.colorize(config.getString("gui.my-order.title", "&6Your Orders"));
    }

    public int getMyOrderGuiSize() {
        return config.getInt("gui.my-order.size", 36);
    }

    public String getOrderManageGuiTitle() {
        return ColorUtils.colorize(config.getString("gui.order-manage.title", "&6Manage Order"));
    }

    public int getOrderManageGuiSize() {
        return config.getInt("gui.order-manage.size", 27);
    }

    public String getCreateOrderGuiTitle() {
        return ColorUtils.colorize(config.getString("gui.create-order.title", "&6Create Order"));
    }

    public int getCreateOrderGuiSize() {
        return config.getInt("gui.create-order.size", 27);
    }
}