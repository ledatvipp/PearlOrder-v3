package org.nexus.leDatOrder.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.nexus.leDatOrder.enums.CurrencyType;

import java.util.UUID;
import java.util.Date;

public class Order {
    private UUID id;
    private UUID playerUUID;
    private String playerName;
    private Material material;
    private double pricePerItem;
    private int requiredAmount;
    private int receivedAmount;
    private double totalPaid;
    private double paidAmount;
    private Date createdAt;
    private OrderType type;
    private int collectedAmount;
    private CurrencyType currencyType;

    // Constructor cũ (để tương thích ngược)
    public Order(UUID playerUUID, String playerName, Material material, double pricePerItem, int requiredAmount) {
        this.id = UUID.randomUUID();
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.material = material;
        this.pricePerItem = pricePerItem;
        this.requiredAmount = requiredAmount;
        this.receivedAmount = 0;
        this.totalPaid = pricePerItem * requiredAmount;
        this.paidAmount = 0;
        this.createdAt = new Date();
        this.type = getOrderTypeFromMaterial(material);
        this.collectedAmount = 0;
        this.currencyType = CurrencyType.VAULT; // Mặc định là Vault
    }

    // Constructor mới với CurrencyType
    public Order(UUID playerUUID, String playerName, Material material, double pricePerItem, int requiredAmount, CurrencyType currencyType) {
        this.id = UUID.randomUUID();
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.material = material;
        this.pricePerItem = pricePerItem;
        this.requiredAmount = requiredAmount;
        this.receivedAmount = 0;
        this.totalPaid = pricePerItem * requiredAmount;
        this.paidAmount = 0;
        this.createdAt = new Date();
        this.type = getOrderTypeFromMaterial(material);
        this.collectedAmount = 0;
        this.currencyType = currencyType;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Material getMaterial() {
        return material;
    }

    public double getPricePerItem() {
        return pricePerItem;
    }

    public int getRequiredAmount() {
        return requiredAmount;
    }

    public int getReceivedAmount() {
        return receivedAmount;
    }

    public void addReceivedAmount(int amount) {
        this.receivedAmount += amount;
        if (this.receivedAmount > this.requiredAmount) {
            this.receivedAmount = this.requiredAmount;
        }
    }

    public double getTotalPaid() {
        return totalPaid;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void addPaidAmount(double amount) {
        this.paidAmount += amount;
        if (this.paidAmount > this.totalPaid) {
            this.paidAmount = this.totalPaid;
        }
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public OrderType getType() {
        return type;
    }

    public int getCollectedAmount() {
        return collectedAmount;
    }

    public void addCollectedAmount(int amount) {
        this.collectedAmount += amount;
        if (this.collectedAmount > this.requiredAmount) {
            this.collectedAmount = this.requiredAmount;
        }
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public boolean isCompleted() {
        return receivedAmount >= requiredAmount;
    }

    public boolean isFullyCompleted() {
        return receivedAmount >= requiredAmount && collectedAmount >= receivedAmount;
    }

    public ItemStack createItemStack() {
        // TODO: OrderGUI
        return null;
    }

    private OrderType getOrderTypeFromMaterial(Material material) {
        String name = material.name();

        // Blocks
        if (material.isBlock()) {
            return OrderType.BLOCKS;
        }

        // Tools
        if (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") ||
                name.endsWith("_HOE") || name.equals("SHEARS") || name.equals("FLINT_AND_STEEL")) {
            return OrderType.TOOLS;
        }

        // Food
        if (material.isEdible()) {
            return OrderType.FOOD;
        }

        // Combat
        if (name.endsWith("_SWORD") || name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
                name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") || name.equals("BOW") ||
                name.equals("CROSSBOW") || name.equals("TRIDENT") || name.equals("SHIELD") ||
                name.endsWith("ARROW")) {
            return OrderType.COMBAT;
        }

        // Potions
        if (name.contains("POTION") || name.equals("BREWING_STAND") || name.equals("CAULDRON")) {
            return OrderType.POTIONS;
        }

        // Books
        if (name.contains("BOOK") || name.equals("ENCHANTING_TABLE") || name.equals("BOOKSHELF")) {
            return OrderType.BOOKS;
        }

        // Ingredients
        if (name.endsWith("_ORE") || name.endsWith("_INGOT") || name.endsWith("_NUGGET") ||
                name.equals("DIAMOND") || name.equals("EMERALD") || name.equals("COAL") ||
                name.equals("REDSTONE") || name.equals("LAPIS_LAZULI") || name.equals("QUARTZ") ||
                name.equals("ROTTEN_FLESH") || name.equals("BONE") || name.equals("STRING") ||
                name.equals("SPIDER_EYE") || name.equals("GUNPOWDER") || name.equals("ENDER_PEARL")) {
            return OrderType.INGREDIENTS;
        }

        // Utilities
        if (name.contains("CHEST") || name.contains("SHULKER") || name.contains("HOPPER") ||
                name.equals("DROPPER") || name.equals("DISPENSER") || name.equals("OBSERVER") ||
                name.equals("COMPARATOR") || name.equals("REPEATER") || name.equals("TOTEM_OF_UNDYING")) {
            return OrderType.UTILITIES;
        }

        // Default
        return OrderType.ALL;
    }
}