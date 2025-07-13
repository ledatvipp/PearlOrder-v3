package org.nexus.leDatOrder.models;

import org.bukkit.Material;

public enum SortType {
    MOST_PAID(Material.GOLD_INGOT, "Most Paid"),
    MOST_DELIVERED(Material.CHEST, "Most Delivered"),
    RECENTLY_LISTED(Material.CLOCK, "Recently Listed"),
    MOST_MONEY_PER_ITEM(Material.EMERALD, "Most Money Per Item");

    private final Material icon;
    private final String displayName;

    SortType(Material icon, String displayName) {
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