package org.nexus.leDatOrder.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.models.Order;
import org.nexus.leDatOrder.utils.ColorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderManageGUI {
    private static final Map<UUID, Order> playerManageMap = new HashMap<>();

    private final LeDatOrder plugin;
    private final Player player;
    private final Order order;
    private Inventory inventory;

    public OrderManageGUI(LeDatOrder plugin, Player player, Order order) {
        this.plugin = plugin;
        this.player = player;
        this.order = order;
        playerManageMap.put(player.getUniqueId(), order);
    }

    public void open() {
        createInventory();
        updateInventory();
        player.openInventory(inventory);
    }

    private void createInventory() {
        // Use configurable title and size for order manage GUI
        String title = plugin.getConfigManager().getOrderManageGuiTitle();
        int size = plugin.getConfigManager().getOrderManageGuiSize();
        inventory = Bukkit.createInventory(null, size, title);
    }

    private void updateInventory() {
        inventory.clear();

        // Hiển thị thông tin order (order-info item) dựa trên cấu hình
        int infoSlot = plugin.getConfigManager().getItemSlot("gui.order-manage.order-info-item", 13);
        String infoName = plugin.getConfigManager().getItemDisplayName("gui.order-manage.order-info-item", "&6Your Order: %material%");
        List<String> infoLoreCfg = plugin.getConfigManager().getItemLore("gui.order-manage.order-info-item");
        ItemStack orderItem = new ItemStack(order.getMaterial());
        ItemMeta orderMeta = orderItem.getItemMeta();
        if (orderMeta != null) {
            orderMeta.setDisplayName(ColorUtils.colorize(infoName.replace("%material%", order.getMaterial().name())));
            List<String> lore = new ArrayList<>();
            if (infoLoreCfg != null) {
                double total = order.getPricePerItem() * order.getRequiredAmount();
                String priceString = order.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT ? "$" + String.format("%.2f", order.getPricePerItem()) : ((int) order.getPricePerItem()) + " Points";
                String currencyName = order.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT ? "Vault Money" : "PlayerPoints";
                String paidString = order.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT ? "$" + String.format("%.2f", order.getPaidAmount()) : String.valueOf((int) order.getPaidAmount());
                String totalString = order.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT ? "$" + String.format("%.2f", total) : String.valueOf((int) total) + " Points";
                for (String line : infoLoreCfg) {
                    String processed = line
                            .replace("%required%", String.valueOf(order.getRequiredAmount()))
                            .replace("%price%", priceString)
                            .replace("%currency%", currencyName)
                            .replace("%received%", String.valueOf(order.getReceivedAmount()))
                            .replace("%paid%", paidString)
                            .replace("%total%", totalString);
                    lore.add(ColorUtils.colorize(processed));
                }
            }
            orderMeta.setLore(lore);
            orderItem.setItemMeta(orderMeta);
        }
        if (infoSlot >= 0 && infoSlot < inventory.getSize()) {
            inventory.setItem(infoSlot, orderItem);
        }

        // Nút hủy đơn dựa trên cấu hình
        int cancelSlot = plugin.getConfigManager().getItemSlot("gui.order-manage.cancel-item", 12);
        Material cancelMat = plugin.getConfigManager().getItemMaterial("gui.order-manage.cancel-item", Material.BARRIER);
        String cancelName = plugin.getConfigManager().getItemDisplayName("gui.order-manage.cancel-item", "&cCancel Order");
        List<String> cancelLoreCfg = plugin.getConfigManager().getItemLore("gui.order-manage.cancel-item");
        ItemStack cancelItem = new ItemStack(cancelMat);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(ColorUtils.colorize(cancelName));
            List<String> lore = new ArrayList<>();
            if (cancelLoreCfg != null && !cancelLoreCfg.isEmpty()) {
                lore.addAll(ColorUtils.colorize(cancelLoreCfg));
            }
            // Optionally append dynamic info about items and refunds; this can be configured outside of lore
            cancelMeta.setLore(lore);
            cancelItem.setItemMeta(cancelMeta);
        }
        if (cancelSlot >= 0 && cancelSlot < inventory.getSize()) {
            inventory.setItem(cancelSlot, cancelItem);
        }

        // Nút nhận vật phẩm dựa trên cấu hình
        int collectSlot = plugin.getConfigManager().getItemSlot("gui.order-manage.collect-item", 14);
        Material collectMat = plugin.getConfigManager().getItemMaterial("gui.order-manage.collect-item", Material.CHEST);
        String collectName = plugin.getConfigManager().getItemDisplayName("gui.order-manage.collect-item", "&aCollect Items");
        List<String> collectLoreCfg = plugin.getConfigManager().getItemLore("gui.order-manage.collect-item");
        ItemStack collectItem = new ItemStack(collectMat);
        ItemMeta collectMeta = collectItem.getItemMeta();
        if (collectMeta != null) {
            collectMeta.setDisplayName(ColorUtils.colorize(collectName));
            List<String> lore = new ArrayList<>();
            int availableToCollect = Math.max(0, order.getReceivedAmount() - order.getCollectedAmount());
            if (collectLoreCfg != null && !collectLoreCfg.isEmpty()) {
                for (String line : collectLoreCfg) {
                    String processed = line
                            .replace("%received%", String.valueOf(availableToCollect))
                            .replace("%required%", String.valueOf(order.getRequiredAmount()))
                            .replace("%material%", order.getMaterial().name());
                    lore.add(ColorUtils.colorize(processed));
                }
            } else {
                // Default lore behaviour
                if (availableToCollect > 0) {
                    lore.add(ColorUtils.colorize("&7Click to collect delivered items"));
                    lore.add(ColorUtils.colorize("&aAvailable: &e" + availableToCollect + " " + order.getMaterial().name()));
                } else {
                    lore.add(ColorUtils.colorize("&7No items available to collect"));
                    lore.add(ColorUtils.colorize("&7Wait for delivery or cancel order"));
                }
            }
            collectMeta.setLore(lore);
            collectItem.setItemMeta(collectMeta);
        }
        if (collectSlot >= 0 && collectSlot < inventory.getSize()) {
            inventory.setItem(collectSlot, collectItem);
        }
    }

    public static Order getOrderByPlayer(Player player) {
        return playerManageMap.get(player.getUniqueId());
    }

    public static void removePlayer(Player player) {
        playerManageMap.remove(player.getUniqueId());
    }

    public static void cancelOrder(LeDatOrder plugin, Player player) {
        Order order = getOrderByPlayer(player);
        if (order == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("order.cancel.cannot"));
            new MyOrderGUI(plugin, player).open();
            return;
        }

        // Tính toán số tiền cần refund: chỉ refund phần chưa nhận được hàng
        int itemsNotReceived = Math.max(0, order.getRequiredAmount() - order.getReceivedAmount());
        double refundAmount = itemsNotReceived * order.getPricePerItem();

        // Refund theo loại tiền tệ
        if (refundAmount > 0) {
            if (order.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT) {
                if (plugin.getVaultManager().isEnabled()) {
                    plugin.getVaultManager().deposit(player, refundAmount);
                    String amountString = plugin.getConfigManager().formatCurrencyAmount(refundAmount, order.getCurrencyType());
                    player.sendMessage(plugin.getConfigManager().getMessage("order.cancel.refund", "%amount%", amountString));
                }
            } else {
                if (plugin.getPlayerPointsManager().isEnabled()) {
                    int points = (int) Math.round(refundAmount);
                    if (points > 0) {
                        plugin.getPlayerPointsManager().deposit(player, points);
                        String amountString = plugin.getConfigManager().formatCurrencyAmount(points, order.getCurrencyType());
                        player.sendMessage(plugin.getConfigManager().getMessage("order.cancel.refund", "%amount%", amountString));
                    }
                }
            }
        }

        // Trả lại vật phẩm cho những người đã đóng góp
        int remainingToReturn = order.getReceivedAmount() - order.getCollectedAmount();
        int totalReturned = 0;
        if (remainingToReturn > 0 && !order.getContributions().isEmpty()) {
            for (Map.Entry<UUID, Integer> entry : order.getContributions().entrySet()) {
                UUID contributorId = entry.getKey();
                if (contributorId == null || contributorId.equals(order.getPlayerUUID())) {
                    continue;
                }

                int contributed = entry.getValue();
                if (contributed <= 0) {
                    continue;
                }

                int refundAmountItems = Math.min(contributed, remainingToReturn);
                if (refundAmountItems <= 0) {
                    continue;
                }

                returnItemsToContributor(plugin, contributorId, order.getMaterial(), refundAmountItems);
                remainingToReturn -= refundAmountItems;
                totalReturned += refundAmountItems;

                if (remainingToReturn <= 0) {
                    break;
                }
            }
        }

        order.clearContributions();

        if (totalReturned > 0) {
            player.sendMessage(plugin.getConfigManager().getMessage(
                    "order.cancel.items-returned",
                    "%amount%",
                    String.valueOf(totalReturned),
                    "%material%",
                    order.getMaterial().name()
            ));
        }
        if (remainingToReturn > 0) {
            player.sendMessage(plugin.getConfigManager().getMessage(
                    "order.cancel.items-missing",
                    "%amount%",
                    String.valueOf(remainingToReturn),
                    "%material%",
                    order.getMaterial().name()
            ));
        }

        // Xóa order
        plugin.getOrderManager().removeOrder(order.getId());
        player.sendMessage(plugin.getConfigManager().getMessage("order.cancel.success"));

        // Mở lại GUI My Order
        new MyOrderGUI(plugin, player).open();

        removePlayer(player);
    }

    public static void collectItems(LeDatOrder plugin, Player player) {
        Order order = getOrderByPlayer(player);
        if (order == null) return;

        int availableItems = order.getReceivedAmount() - order.getCollectedAmount();
        if (availableItems <= 0) {
            player.sendMessage(plugin.getConfigManager().getMessage("order.collect.none"));
            return;
        }

        // Tạo ItemStack để gửi cho người chơi
        ItemStack items = new ItemStack(order.getMaterial(), availableItems);
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(items);

        // Nếu không đủ chỗ trong inventory
        if (!leftover.isEmpty()) {
            int notStored = leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
            int collected = Math.max(0, availableItems - notStored);

            // Thả items còn lại xuống đất
            for (ItemStack leftoverItem : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
            }

            player.sendMessage(plugin.getConfigManager().getMessage(
                    "order.collect.partial",
                    "%collected%",
                    String.valueOf(collected),
                    "%material%",
                    order.getMaterial().name()
            ));

            // Cập nhật số lượng đã thu thập (tất cả items đều được "collect", một số rơi xuống đất)
            order.addCollectedAmount(availableItems);
        } else {
            // Lưu lại số lượng đã nhận
            player.sendMessage(plugin.getConfigManager().getMessage(
                    "order.collect.success",
                    "%amount%",
                    String.valueOf(availableItems),
                    "%material%",
                    order.getMaterial().name()
            ));

            // Cập nhật số lượng đã thu thập
            order.addCollectedAmount(availableItems);
        }

        // Kiểm tra nếu đơn hàng đã hoàn thành và tất cả items đã được collect
        if (order.isCompleted() && order.getCollectedAmount() >= order.getReceivedAmount()) {
            plugin.getOrderManager().removeOrder(order.getId());
            player.sendMessage(plugin.getConfigManager().getMessage("order.collect.completed"));
            removePlayer(player);
            new MyOrderGUI(plugin, player).open();
            return;
        }

        order.consumeContributions(availableItems);

        // Lưu thay đổi
        plugin.getOrderManager().saveOrders();

        // Mở lại GUI quản lý order
        new OrderManageGUI(plugin, player, order).open();
    }

    private static void returnItemsToContributor(LeDatOrder plugin, UUID contributorId, Material material, int amount) {
        if (amount <= 0 || material == Material.AIR) {
            return;
        }

        Player contributor = plugin.getServer().getPlayer(contributorId);
        if (contributor != null && contributor.isOnline()) {
            int remaining = amount;
            while (remaining > 0) {
                int stackSize = Math.min(material.getMaxStackSize(), remaining);
                ItemStack stack = new ItemStack(material, stackSize);
                HashMap<Integer, ItemStack> leftover = contributor.getInventory().addItem(stack);
                if (!leftover.isEmpty()) {
                    for (ItemStack item : leftover.values()) {
                        contributor.getWorld().dropItemNaturally(contributor.getLocation(), item);
                    }
                }
                remaining -= stackSize;
            }
            contributor.sendMessage(plugin.getConfigManager().getMessage(
                    "order.cancel.contributor-refund",
                    "%amount%",
                    String.valueOf(amount),
                    "%material%",
                    material.name()
            ));
        } else {
            plugin.getRefundManager().queueRefund(contributorId, material, amount);
        }
    }
}