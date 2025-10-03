package org.nexus.leDatOrder.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.enums.CurrencyType;
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
        // Use configurable title and size for the create order GUI.  The title may contain colour codes and
        // placeholders that are already colourised by ConfigManager.  The size defaults to 27 if not set.
        String title = plugin.getConfigManager().getCreateOrderGuiTitle();
        int size = plugin.getConfigManager().getCreateOrderGuiSize();
        inventory = Bukkit.createInventory(null, size, title);
    }

    private void updateInventory() {
        inventory.clear();

        CreateOrderData data = playerCreateMap.get(player.getUniqueId());

        // Back
        int backSlot = plugin.getConfigManager().getItemSlot("gui.create-order.back-item", 10);
        Material backMat = plugin.getConfigManager().getItemMaterial("gui.create-order.back-item", Material.RED_STAINED_GLASS_PANE);
        String backName = plugin.getConfigManager().getItemDisplayName("gui.create-order.back-item", "&cBack");
        List<String> backLore = plugin.getConfigManager().getItemLore("gui.create-order.back-item");
        ItemStack backItem = new ItemStack(backMat);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(backName);
            if (backLore != null && !backLore.isEmpty()) backMeta.setLore(backLore);
            backItem.setItemMeta(backMeta);
        }
        if (backSlot >= 0 && backSlot < inventory.getSize()) inventory.setItem(backSlot, backItem);

        // Material select
        int materialSlot = plugin.getConfigManager().getItemSlot("gui.create-order.material-item", 12);
        Material materialMat = plugin.getConfigManager().getItemMaterial("gui.create-order.material-item", Material.STONE);
        String materialItemName = plugin.getConfigManager().getItemDisplayName("gui.create-order.material-item", "&6Select Material");
        List<String> materialLore = plugin.getConfigManager().getItemLore("gui.create-order.material-item");
        ItemStack materialItem = new ItemStack(materialMat);
        ItemMeta materialMeta = materialItem.getItemMeta();
        if (materialMeta != null) {
            materialMeta.setDisplayName(ColorUtils.colorize(materialItemName.replace("%material%", data.getMaterial().name())));
            List<String> lore = new ArrayList<>();
            if (materialLore != null && !materialLore.isEmpty()) {
                for (String line : materialLore) {
                    lore.add(ColorUtils.colorize(line.replace("%material%", data.getMaterial().name())));
                }
            }
            materialMeta.setLore(lore);
            materialItem.setItemMeta(materialMeta);
        }
        if (materialSlot >= 0 && materialSlot < inventory.getSize()) inventory.setItem(materialSlot, materialItem);

        // Amount
        int amountSlot = plugin.getConfigManager().getItemSlot("gui.create-order.amount-item", 13);
        Material amountMat = plugin.getConfigManager().getItemMaterial("gui.create-order.amount-item", Material.CHEST);
        String amountName = plugin.getConfigManager().getItemDisplayName("gui.create-order.amount-item", "&6Set Amount");
        List<String> amountLore = plugin.getConfigManager().getItemLore("gui.create-order.amount-item");
        ItemStack amountItem = new ItemStack(amountMat);
        ItemMeta amountMeta = amountItem.getItemMeta();
        if (amountMeta != null) {
            amountMeta.setDisplayName(amountName.replace("%amount%", String.valueOf(data.getAmount())));
            List<String> lore = new ArrayList<>();
            if (amountLore != null && !amountLore.isEmpty()) {
                for (String line : amountLore) {
                    lore.add(ColorUtils.colorize(line.replace("%amount%", String.valueOf(data.getAmount()))));
                }
            }
            amountMeta.setLore(lore);
            amountItem.setItemMeta(amountMeta);
        }
        if (amountSlot >= 0 && amountSlot < inventory.getSize()) inventory.setItem(amountSlot, amountItem);

        // Price
        int priceSlot = plugin.getConfigManager().getItemSlot("gui.create-order.price-item", 14);
        Material priceMat = plugin.getConfigManager().getItemMaterial("gui.create-order.price-item", Material.SUNFLOWER);
        String priceName = plugin.getConfigManager().getItemDisplayName("gui.create-order.price-item", "&6Set Price Per Item");
        List<String> priceLore = plugin.getConfigManager().getItemLore("gui.create-order.price-item");
        ItemStack priceItem = new ItemStack(priceMat);
        ItemMeta priceMeta = priceItem.getItemMeta();
        if (priceMeta != null) {
            priceMeta.setDisplayName(priceName);
            List<String> lore = new ArrayList<>();
            String priceString = (data.getCurrencyType() == CurrencyType.VAULT)
                    ? "$" + String.format("%.2f", data.getPricePerItem())
                    : ((int) data.getPricePerItem()) + " Points";
            if (priceLore != null && !priceLore.isEmpty()) {
                for (String line : priceLore) {
                    lore.add(ColorUtils.colorize(line.replace("%price%", priceString)));
                }
            }
            priceMeta.setLore(lore);
            priceItem.setItemMeta(priceMeta);
        }
        if (priceSlot >= 0 && priceSlot < inventory.getSize()) inventory.setItem(priceSlot, priceItem);

        // Confirm
        int confirmSlot = plugin.getConfigManager().getItemSlot("gui.create-order.confirm-item", 16);
        Material confirmMat = plugin.getConfigManager().getItemMaterial("gui.create-order.confirm-item", Material.LIME_STAINED_GLASS_PANE);
        String confirmName = plugin.getConfigManager().getItemDisplayName("gui.create-order.confirm-item", "&aConfirm");
        List<String> confirmLore = plugin.getConfigManager().getItemLore("gui.create-order.confirm-item");
        ItemStack confirmItem = new ItemStack(confirmMat);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName(confirmName);
            List<String> lore = new ArrayList<>();
            String matName = data.getMaterial().name();
            String amountString = String.valueOf(data.getAmount());
            String priceString;
            String totalString;
            String currencyName;
            if (data.getCurrencyType() == CurrencyType.VAULT) {
                priceString = "$" + String.format("%.2f", data.getPricePerItem());
                totalString = "$" + String.format("%.2f", data.getAmount() * data.getPricePerItem());
                currencyName = "Vault Money";
            } else {
                priceString = ((int) data.getPricePerItem()) + " Points";
                totalString = ((int) (data.getAmount() * data.getPricePerItem())) + " Points";
                currencyName = "PlayerPoints";
            }
            if (confirmLore != null && !confirmLore.isEmpty()) {
                for (String line : confirmLore) {
                    String processed = line
                            .replace("%material%", matName)
                            .replace("%amount%", amountString)
                            .replace("%price%", priceString)
                            .replace("%total%", totalString)
                            .replace("%currency%", currencyName);
                    lore.add(ColorUtils.colorize(processed));
                }
            } else {
                lore.add(ColorUtils.colorize("&7Material: &a" + matName));
                lore.add(ColorUtils.colorize("&7Amount: &a" + amountString));
                lore.add(ColorUtils.colorize("&7Price per item: &a" + priceString));
                lore.add(ColorUtils.colorize("&7Total cost: &a" + totalString));
                lore.add(ColorUtils.colorize("&7Currency: &e" + currencyName));
            }
            confirmMeta.setLore(lore);
            confirmItem.setItemMeta(confirmMeta);
        }
        if (confirmSlot >= 0 && confirmSlot < inventory.getSize()) inventory.setItem(confirmSlot, confirmItem);

        // Currency toggle
        int currencySlot = plugin.getConfigManager().getCreateOrderCurrencyItemSlot();
        Material currencyMat = plugin.getConfigManager().getCreateOrderCurrencyItemMaterial();
        Material configCurrencyMat = plugin.getConfigManager().getItemMaterial("gui.create-order.currency-item", Material.GOLD_INGOT);
        if (currencyMat == configCurrencyMat) {
            currencyMat = (data.getCurrencyType() == CurrencyType.VAULT) ? Material.GOLD_INGOT : Material.EXPERIENCE_BOTTLE;
        }
        String currencyNameDisplay = plugin.getConfigManager().getCreateOrderCurrencyItemDisplayName();
        List<String> currencyLoreCfg = plugin.getConfigManager().getCreateOrderCurrencyItemLore();
        ItemStack currencyItem = new ItemStack(currencyMat);
        ItemMeta currencyMeta = currencyItem.getItemMeta();
        if (currencyMeta != null) {
            String curName = ColorUtils.colorize(
                    currencyNameDisplay.replace("%currency%", data.getCurrencyType() == CurrencyType.VAULT ? "Vault Money" : "PlayerPoints"));
            currencyMeta.setDisplayName(curName);

            List<String> lore = new ArrayList<>();
            if (currencyLoreCfg != null && !currencyLoreCfg.isEmpty()) {
                for (String line : currencyLoreCfg) {
                    String processed = line
                            .replace("%currency%", data.getCurrencyType() == CurrencyType.VAULT ? "Vault Money" : "PlayerPoints")
                            .replace("%vault_status%", plugin.getVaultManager().isEnabled() ? ColorUtils.colorize("&aAvailable") : ColorUtils.colorize("&cUnavailable"))
                            .replace("%points_status%", plugin.getPlayerPointsManager().isEnabled() ? ColorUtils.colorize("&aAvailable") : ColorUtils.colorize("&cUnavailable"));
                    lore.add(ColorUtils.colorize(processed));
                }
            } else {
                lore.add(ColorUtils.colorize("&7Current: &a" + (data.getCurrencyType() == CurrencyType.VAULT ? "Vault Money" : "PlayerPoints")));
                lore.add(ColorUtils.colorize("&7Click to switch currency"));
            }
            currencyMeta.setLore(lore);
            currencyItem.setItemMeta(currencyMeta);
        }
        if (currencySlot >= 0 && currencySlot < inventory.getSize()) inventory.setItem(currencySlot, currencyItem);
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
            player.sendMessage(plugin.getConfigManager().getMessage("order.creation.amount-invalid"));
            return;
        }

        if (data.getPricePerItem() <= 0) {
            player.sendMessage(plugin.getConfigManager().getMessage("order.creation.price-invalid"));
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
            player.sendMessage(plugin.getConfigManager().getMessage(
                    "order.creation.limit-reached",
                    "%max%",
                    String.valueOf(maxOrders)
            ));
            return;
        }

        // Tính tổng chi phí để tạo order
        double fee = 0; // Có thể thêm phí tạo order nếu muốn
        
        boolean paymentSuccess = false;
        String currencyName = "";
        
        if (data.getCurrencyType() == CurrencyType.VAULT) {
            // Sử dụng Vault
            if (!plugin.getVaultManager().isEnabled()) {
                player.sendMessage(plugin.getConfigManager().getMessage("currency.vault-unavailable"));
                return;
            }

            double totalCost = data.getAmount() * data.getPricePerItem();

            if (!plugin.getVaultManager().has(player, totalCost + fee)) {
                String needed = plugin.getConfigManager().formatCurrencyAmount(totalCost + fee, CurrencyType.VAULT);
                player.sendMessage(plugin.getConfigManager().getMessage("order.creation.insufficient-money", "%amount%", needed));
                return;
            }

            paymentSuccess = plugin.getVaultManager().withdraw(player, totalCost + fee);
            currencyName = plugin.getConfigManager().formatCurrencyAmount(totalCost, CurrencyType.VAULT);

        } else {
            // Sử dụng PlayerPoints
            if (!plugin.getPlayerPointsManager().isEnabled()) {
                player.sendMessage(plugin.getConfigManager().getMessage("currency.playerpoints-unavailable"));
                return;
            }

            int totalCost = (int) Math.round(data.getAmount() * data.getPricePerItem());
            int totalFee = (int) Math.round(fee);

            if (!plugin.getPlayerPointsManager().has(player, totalCost + totalFee)) {
                String needed = plugin.getConfigManager().formatCurrencyAmount(totalCost + totalFee, CurrencyType.PLAYERPOINTS);
                player.sendMessage(plugin.getConfigManager().getMessage("order.creation.insufficient-points", "%amount%", needed));
                return;
            }

            paymentSuccess = plugin.getPlayerPointsManager().withdraw(player, totalCost + totalFee);
            currencyName = plugin.getConfigManager().formatCurrencyAmount(totalCost, CurrencyType.PLAYERPOINTS);
        }

        if (!paymentSuccess) {
            player.sendMessage(plugin.getConfigManager().getMessage("order.creation.payment-failed"));
            return;
        }

        if (fee > 0) {
            if (data.getCurrencyType() == CurrencyType.VAULT) {
                String feeString = plugin.getConfigManager().formatCurrencyAmount(fee, CurrencyType.VAULT);
                player.sendMessage(plugin.getConfigManager().getMessage("order.creation.fee-charged-vault", "%fee%", feeString));
            } else {
                String feeString = plugin.getConfigManager().formatCurrencyAmount(fee, CurrencyType.PLAYERPOINTS);
                player.sendMessage(plugin.getConfigManager().getMessage("order.creation.fee-charged-points", "%fee%", feeString));
            }
        }
        player.sendMessage(plugin.getConfigManager().getMessage("order.creation.payment-success", "%amount%", currencyName));

        // Tạo order mới - SỬA DỤNG ENUM ĐÚNG
        Order order = new Order(
                player.getUniqueId(),
                player.getName(),
                data.getMaterial(),
                data.getPricePerItem(),
                data.getAmount(),
                data.getCurrencyType()  // Sử dụng CurrencyType từ enum riêng biệt
        );

        // Thêm vào danh sách order
        plugin.getOrderManager().addOrder(order);

        // Thông báo
        player.sendMessage(plugin.getConfigManager().getMessage(
                "order.creation.success",
                "%current%",
                String.valueOf(playerOrders.size() + 1),
                "%max%",
                String.valueOf(maxOrders)
        ));

        // Mở lại GUI My Order
        new MyOrderGUI(plugin, player).open();
        
        // Xóa dữ liệu tạo order
        removePlayer(player);
    }

    public static class CreateOrderData {
        private Material material = Material.STONE;
        private int amount = 1;
        private double pricePerItem = 1.0;
        private CurrencyType currencyType = CurrencyType.VAULT;

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

        public CurrencyType getCurrencyType() {
            return currencyType;
        }

        public void setCurrencyType(CurrencyType currencyType) {
            this.currencyType = currencyType;
        }
        
        public void toggleCurrencyType() {
            this.currencyType = (this.currencyType == CurrencyType.VAULT) ? CurrencyType.PLAYERPOINTS : CurrencyType.VAULT;
        }
    }
}