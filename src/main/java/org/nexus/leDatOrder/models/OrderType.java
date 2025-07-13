package org.nexus.leDatOrder.models;

import org.bukkit.Material;

public enum OrderType {
    ALL(Material.HOPPER, "Tất cả"),
    BLOCKS(Material.GRASS_BLOCK, "Blocks"),
    TOOLS(Material.DIAMOND_PICKAXE, "Tools"),
    FOOD(Material.COOKED_BEEF, "Food"),
    COMBAT(Material.DIAMOND_SWORD, "Combat"),
    POTIONS(Material.POTION, "Potions"),
    BOOKS(Material.ENCHANTED_BOOK, "Books"),
    INGREDIENTS(Material.DIAMOND, "Ingredients"),
    UTILITIES(Material.CHEST, "Utilities");

    private final Material icon;
    private final String displayName;

    OrderType(Material icon, String displayName) {
        this.icon = icon;
        this.displayName = displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }
}