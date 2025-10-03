package org.nexus.leDatOrder.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.enums.CurrencyType;
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
        if (config.contains("gui.title") && !config.contains("gui.order.title")) {
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

        setDefaultMessages();
        setDefaultOrderGUI();
        setDefaultMyOrderGUI();
        setDefaultOrderManageGUI();
        setDefaultCreateOrderGUI();
        setDefaultMaterialSelectGUI();
        setDefaultOrderDeliveryGUI();

        saveConfig();
    }

    private void setDefaultMessages() {
        String basePath = "messages";

        if (!config.contains(basePath + ".command.player-only")) {
            config.set(basePath + ".command.player-only", "&cThis command can only be used by players.");
        }
        if (!config.contains(basePath + ".command.no-permission")) {
            config.set(basePath + ".command.no-permission", "&cYou don't have permission to use this command.");
        }
        if (!config.contains(basePath + ".command.reload.success")) {
            config.set(basePath + ".command.reload.success", "&aConfig reloaded.");
        }
        if (!config.contains(basePath + ".command.help.header")) {
            config.set(basePath + ".command.help.header", "&6===== LeDatOrder Help =====");
        }
        if (!config.contains(basePath + ".command.help.order")) {
            config.set(basePath + ".command.help.order", "&e/order &7- Open the main order GUI");
        }
        if (!config.contains(basePath + ".command.help.my")) {
            config.set(basePath + ".command.help.my", "&e/order my &7- Open your orders GUI");
        }
        if (!config.contains(basePath + ".command.help.reload")) {
            config.set(basePath + ".command.help.reload", "&e/order reload &7- Reload the config");
        }

        // Order creation messages
        if (!config.contains(basePath + ".order.creation.success")) {
            config.set(basePath + ".order.creation.success", "&aOrder created successfully! (&e%current%&a/&e%max%&a)");
        }
        if (!config.contains(basePath + ".order.creation.amount-invalid")) {
            config.set(basePath + ".order.creation.amount-invalid", "&cAmount must be greater than 0.");
        }
        if (!config.contains(basePath + ".order.creation.price-invalid")) {
            config.set(basePath + ".order.creation.price-invalid", "&cPrice must be greater than 0.");
        }
        if (!config.contains(basePath + ".order.creation.limit-reached")) {
            config.set(basePath + ".order.creation.limit-reached", "&cBạn đã đạt giới hạn số lượng order (&e%max%&c). Hãy hoàn thành hoặc hủy một số order hiện tại.");
        }
        if (!config.contains(basePath + ".order.creation.insufficient-money")) {
            config.set(basePath + ".order.creation.insufficient-money", "&cYou don't have enough money to create this order. You need %amount%");
        }
        if (!config.contains(basePath + ".order.creation.insufficient-points")) {
            config.set(basePath + ".order.creation.insufficient-points", "&cYou don't have enough points to create this order. You need %amount% points");
        }
        if (!config.contains(basePath + ".order.creation.payment-failed")) {
            config.set(basePath + ".order.creation.payment-failed", "&cPayment failed! Please try again.");
        }
        if (!config.contains(basePath + ".order.creation.fee-charged-vault")) {
            config.set(basePath + ".order.creation.fee-charged-vault", "&aYou have been charged &e%fee% &afor creating this order.");
        }
        if (!config.contains(basePath + ".order.creation.fee-charged-points")) {
            config.set(basePath + ".order.creation.fee-charged-points", "&aYou have been charged &e%fee% &afor creating this order.");
        }
        if (!config.contains(basePath + ".order.creation.payment-success")) {
            config.set(basePath + ".order.creation.payment-success", "&aYou have paid &e%amount% &afor this order.");
        }

        // Currency messages
        if (!config.contains(basePath + ".currency.vault-unavailable")) {
            config.set(basePath + ".currency.vault-unavailable", "&cVault is not available!");
        }
        if (!config.contains(basePath + ".currency.playerpoints-unavailable")) {
            config.set(basePath + ".currency.playerpoints-unavailable", "&cPlayerPoints is not available!");
        }
        if (!config.contains(basePath + ".currency.no-system-available")) {
            config.set(basePath + ".currency.no-system-available", "&cNo currency system is available!");
        }
        if (!config.contains(basePath + ".currency.vault-unavailable-switch")) {
            config.set(basePath + ".currency.vault-unavailable-switch", "&cVault is not available! Using PlayerPoints instead.");
        }
        if (!config.contains(basePath + ".currency.playerpoints-unavailable-switch")) {
            config.set(basePath + ".currency.playerpoints-unavailable-switch", "&cPlayerPoints is not available! Using Vault instead.");
        }
        if (!config.contains(basePath + ".currency.type-changed")) {
            config.set(basePath + ".currency.type-changed", "&aCurrency type changed to: &e%type%");
        }
        
        // Input messages
        if (!config.contains(basePath + ".input.amount-prompt")) {
            config.set(basePath + ".input.amount-prompt", "&aPlease enter the amount in chat:");
        }
        if (!config.contains(basePath + ".input.price-prompt")) {
            config.set(basePath + ".input.price-prompt", "&aPlease enter the price per item in chat:");
        }
        if (!config.contains(basePath + ".input.search-prompt")) {
            config.set(basePath + ".input.search-prompt", "&aPlease enter search term in chat:");
        }
        if (!config.contains(basePath + ".input.amount-set")) {
            config.set(basePath + ".input.amount-set", "&aAmount set to &e%amount%");
        }
        if (!config.contains(basePath + ".input.price-set")) {
            config.set(basePath + ".input.price-set", "&aPrice set to &e%price%");
        }
        if (!config.contains(basePath + ".input.material-set")) {
            config.set(basePath + ".input.material-set", "&aMaterial set to &e%material%");
        }
        if (!config.contains(basePath + ".input.search-result")) {
            config.set(basePath + ".input.search-result", "&aKết quả tìm kiếm: &e%search%");
        }
        if (!config.contains(basePath + ".input.search-cleared")) {
            config.set(basePath + ".input.search-cleared", "&aSearch cleared");
        }
        if (!config.contains(basePath + ".input.invalid-number")) {
            config.set(basePath + ".input.invalid-number", "&cInvalid number format. Please enter a valid number.");
        }
        if (!config.contains(basePath + ".input.amount-invalid")) {
            config.set(basePath + ".input.amount-invalid", "&cAmount must be greater than 0.");
        }
        if (!config.contains(basePath + ".input.price-invalid")) {
            config.set(basePath + ".input.price-invalid", "&cPrice must be greater than 0.");
        }

        // Sort and filter messages
        if (!config.contains(basePath + ".sort.changed")) {
            config.set(basePath + ".sort.changed", "&aSắp xếp theo: &e%type%");
        }
        if (!config.contains(basePath + ".filter.changed")) {
            config.set(basePath + ".filter.changed", "&aLọc theo: &e%type%");
        }

        if (!config.contains(basePath + ".material-select.selected")) {
            config.set(basePath + ".material-select.selected", "&aSelected material: &e%material%");
        }

        // Delivery messages
        if (!config.contains(basePath + ".delivery.self-delivery")) {
            config.set(basePath + ".delivery.self-delivery", "&cBạn không thể giao đồ cho chính mình!");
        }
        if (!config.contains(basePath + ".delivery.success")) {
            config.set(basePath + ".delivery.success", "&aSuccessfully delivered %amount% %material% to %player%'s order!");
        }
        if (!config.contains(basePath + ".delivery.payment-received")) {
            config.set(basePath + ".delivery.payment-received", "&aYou received %amount% for delivering items!");
        }
        if (!config.contains(basePath + ".delivery.owner-update")) {
            config.set(basePath + ".delivery.owner-update", "&a%player% đã giao &e%amount% %material%&a. Đã chi: &e%payment%&a.");
        }
        if (!config.contains(basePath + ".delivery.order-filled")) {
            config.set(basePath + ".delivery.order-filled", "&aĐơn hàng đã nhận đủ số lượng yêu cầu.");
        }
        if (!config.contains(basePath + ".delivery.owner-complete")) {
            config.set(basePath + ".delivery.owner-complete", "&aĐơn hàng %material% của bạn đã hoàn thành! Hãy vào /order để nhận.");
        }
        if (!config.contains(basePath + ".delivery.excess-returned")) {
            config.set(basePath + ".delivery.excess-returned", "&aĐã trả lại &e%amount% %material%&a vượt quá số lượng yêu cầu.");
        }

        if (!config.contains(basePath + ".order.cancel.cannot")) {
            config.set(basePath + ".order.cancel.cannot", "&cKhông thể hủy đơn hàng này. Có thể đơn đã hoàn thành hoặc không tồn tại.");
        }
        if (!config.contains(basePath + ".order.cancel.refund")) {
            config.set(basePath + ".order.cancel.refund", "&aĐã hoàn trả &e%amount% &avề tài khoản của bạn.");
        }
        if (!config.contains(basePath + ".order.cancel.items-returned")) {
            config.set(basePath + ".order.cancel.items-returned", "&aĐã trả lại &e%amount% %material%&a cho những người đóng góp.");
        }
        if (!config.contains(basePath + ".order.cancel.items-missing")) {
            config.set(basePath + ".order.cancel.items-missing", "&eCòn &6%amount% %material%&e không thể hoàn trả do thiếu dữ liệu đóng góp.");
        }
        if (!config.contains(basePath + ".order.cancel.success")) {
            config.set(basePath + ".order.cancel.success", "&aOrder has been cancelled.");
        }
        if (!config.contains(basePath + ".order.cancel.contributor-refund")) {
            config.set(basePath + ".order.cancel.contributor-refund", "&aBạn đã nhận lại &e%amount% %material%&a từ một đơn hàng bị hủy.");
        }

        if (!config.contains(basePath + ".order.collect.none")) {
            config.set(basePath + ".order.collect.none", "&cNo items available to collect.");
        }
        if (!config.contains(basePath + ".order.collect.partial")) {
            config.set(basePath + ".order.collect.partial", "&aĐã nhận &e%collected% %material%&a. Phần còn lại đã rơi xuống đất do túi đồ đầy.");
        }
        if (!config.contains(basePath + ".order.collect.success")) {
            config.set(basePath + ".order.collect.success", "&aĐã nhận &e%amount% %material%!");
        }
        if (!config.contains(basePath + ".order.collect.completed")) {
            config.set(basePath + ".order.collect.completed", "&aĐơn hàng đã hoàn thành và được xóa khỏi hệ thống.");
        }

        if (!config.contains(basePath + ".order.completed-owner")) {
            config.set(basePath + ".order.completed-owner", "&aĐơn hàng %material% đã hoàn thành và được xóa khỏi hệ thống!");
        }

        if (!config.contains(basePath + ".refund.received")) {
            config.set(basePath + ".refund.received", "&aBạn đã nhận lại &e%amount% &avật phẩm từ các đơn hàng bị hủy.");
        }
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
        setDefaultItem(basePath + ".price-item", "SUNFLOWER", "&6Set Price Per Item", 14, Arrays.asList("&7Current: &a%price%", "&7Click to set price"));
        setDefaultItem(basePath + ".confirm-item", "LIME_STAINED_GLASS_PANE", "&aConfirm", 16, Arrays.asList(
                "&7Material: &a%material%",
                "&7Amount: &a%amount%",
                "&7Price per item: &a%price%",
                "&7Total cost: &a%total%",
                "&7Currency: &e%currency%"
        ));
        
        // Thêm currency type button
        setDefaultItem(basePath + ".currency-item", "GOLD_INGOT", "&6Currency Type", 22, Arrays.asList(
                "&7Current: &a%currency%",
                "&7Click to switch currency",
                "",
                "&a✓ &7Vault Money Available: %vault_status%",
                "&a✓ &7PlayerPoints Available: %points_status%"
        ));
    }

    private void setDefaultOrderDeliveryGUI() {
        String basePath = "gui.order-delivery";
        
        if (!config.contains(basePath + ".title")) {
            config.set(basePath + ".title", "&6Order -> %player%");
        }
        
        if (!config.contains(basePath + ".size")) {
            config.set(basePath + ".size", 45);
        }
        
        setDefaultItem(basePath + ".back-item", "BARRIER", "&cBack to Orders", 40, Arrays.asList("&7Return to order list"));
        
        if (!config.contains(basePath + ".order-info.display-name")) {
            config.set(basePath + ".order-info.display-name", "&6%player%'s Order");
        }
        
        if (!config.contains(basePath + ".order-info.lore")) {
            config.set(basePath + ".order-info.lore", Arrays.asList(
                    "&7Required: &a%required% %material%",
                    "&7Price per item: &a%price%",
                    "&7Currency: &e%currency%",
                    "",
                    "&7Progress: &e%received%/%required%",
                    "&7Remaining: &c%remaining%",
                    "",
                    "&7Put items in the inventory to deliver"
            ));
        }
        
        if (!config.contains(basePath + ".order-info.slot")) {
            config.set(basePath + ".order-info.slot", 4);
        }
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

    // Getter methods cho messages
    public String getMessage(String path) {
        return ColorUtils.colorize(config.getString("messages." + path, "&cMessage not found: " + path));
    }
    
    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }

    // Getter methods cho CreateOrderGUI currency button
    public Material getCreateOrderCurrencyItemMaterial() {
        return getItemMaterial("gui.create-order.currency-item", Material.GOLD_INGOT);
    }
    
    public String getCreateOrderCurrencyItemDisplayName() {
        return getItemDisplayName("gui.create-order.currency-item", "&6Currency Type");
    }
    
    public List<String> getCreateOrderCurrencyItemLore() {
        List<String> lore = getItemLore("gui.create-order.currency-item");
        if (lore.isEmpty()) {
            lore = ColorUtils.colorize(Arrays.asList(
                    "&7Current: &a%currency%",
                    "&7Click to switch currency",
                    "",
                    "&a✓ &7Vault Money Available: %vault_status%",
                    "&a✓ &7PlayerPoints Available: %points_status%"
            ));
        }
        return lore;
    }
    
    public int getCreateOrderCurrencyItemSlot() {
        return getItemSlot("gui.create-order.currency-item", 22);
    }

    // Getter methods cho OrderDeliveryGUI
    public String getOrderDeliveryGuiTitle() {
        return ColorUtils.colorize(config.getString("gui.order-delivery.title", "&6Order -> %player%"));
    }

    public int getOrderDeliveryGuiSize() {
        return config.getInt("gui.order-delivery.size", 45);
    }
    
    public String getOrderDeliveryOrderInfoDisplayName() {
        return ColorUtils.colorize(config.getString("gui.order-delivery.order-info.display-name", "&6%player%'s Order"));
    }
    
    public List<String> getOrderDeliveryOrderInfoLore() {
        List<String> lore = config.getStringList("gui.order-delivery.order-info.lore");
        if (lore.isEmpty()) {
            lore = Arrays.asList(
                    "&7Required: &a%required% %material%",
                    "&7Price per item: &a%price%",
                    "&7Currency: &e%currency%",
                    "",
                    "&7Progress: &e%received%/%required%",
                    "&7Remaining: &c%remaining%",
                    "",
                    "&7Put items in the inventory to deliver"
            );
        }
        return ColorUtils.colorize(lore);
    }
    
    public int getOrderDeliveryOrderInfoSlot() {
        return config.getInt("gui.order-delivery.order-info.slot", 4);
    }
    
    public Material getOrderDeliveryBackItemMaterial() {
        return getItemMaterial("gui.order-delivery.back-item", Material.BARRIER);
    }
    
    public String getOrderDeliveryBackItemDisplayName() {
        return getItemDisplayName("gui.order-delivery.back-item", "&cBack to Orders");
    }
    
    public List<String> getOrderDeliveryBackItemLore() {
        List<String> lore = getItemLore("gui.order-delivery.back-item");
        if (lore.isEmpty()) {
            lore = ColorUtils.colorize(Arrays.asList("&7Return to order list"));
        }
        return lore;
    }

    public int getOrderDeliveryBackItemSlot() {
        return getItemSlot("gui.order-delivery.back-item", 40);
    }

    public String formatCurrencyAmount(double amount, CurrencyType currencyType) {
        if (currencyType == CurrencyType.VAULT) {
            return "$" + String.format("%.2f", amount);
        }
        return String.valueOf((int) Math.round(amount)) + " Points";
    }

    public String getCurrencyDisplayName(CurrencyType currencyType) {
        return currencyType == CurrencyType.VAULT ? "Vault Money" : "PlayerPoints";
    }
}