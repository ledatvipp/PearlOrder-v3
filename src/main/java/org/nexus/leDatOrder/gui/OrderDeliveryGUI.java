package org.nexus.leDatOrder.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.models.Order;
import org.nexus.leDatOrder.utils.ColorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class OrderDeliveryGUI {
    private static final Map<UUID, Order> playerDeliveryMap = new HashMap<>();

    private final LeDatOrder plugin;
    private final Player player;
    private final Order order;
    private Inventory inventory;

    public OrderDeliveryGUI(LeDatOrder plugin, Player player, Order order) {
        this.plugin = plugin;
        this.player = player;
        this.order = order;
        playerDeliveryMap.put(player.getUniqueId(), order);
    }

    public void open() {
        createInventory();
        updateInventory();
        player.openInventory(inventory);
    }

    private void createInventory() {
        String title = plugin.getConfigManager().getOrderDeliveryGuiTitle()
                .replace("%player%", order.getPlayerName());
        int size = plugin.getConfigManager().getOrderDeliveryGuiSize();
        inventory = Bukkit.createInventory(null, size, title);
    }

    private void updateInventory() {
        inventory.clear();

        // Border items
        ItemStack borderItem = createBorderItem();
        List<Integer> borderSlots = plugin.getConfigManager().getOrderDeliveryBorderSlots();
        for (int slot : borderSlots) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, borderItem.clone());
            }
        }

        // Order info item
        ItemStack infoItem = new ItemStack(order.getMaterial());
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            String displayName = plugin.getConfigManager().getOrderDeliveryOrderInfoDisplayName()
                    .replace("%player%", order.getPlayerName());
            infoMeta.setDisplayName(ColorUtils.colorize(displayName));

            List<String> lore = new ArrayList<>();
            for (String line : plugin.getConfigManager().getOrderDeliveryOrderInfoLore()) {
                String priceString = plugin.getConfigManager().formatCurrencyAmount(order.getPricePerItem(), order.getCurrencyType());
                String totalString = plugin.getConfigManager().formatCurrencyAmount(order.getRequiredAmount() * order.getPricePerItem(), order.getCurrencyType());
                String processed = line
                        .replace("%player%", order.getPlayerName())
                        .replace("%material%", order.getMaterial().name())
                        .replace("%required%", String.valueOf(order.getRequiredAmount()))
                        .replace("%received%", String.valueOf(order.getReceivedAmount()))
                        .replace("%remaining%", String.valueOf(Math.max(0, order.getRequiredAmount() - order.getReceivedAmount())))
                        .replace("%price%", priceString)
                        .replace("%total%", totalString)
                        .replace("%currency%", plugin.getConfigManager().getCurrencyDisplayName(order.getCurrencyType()));
                lore.add(ColorUtils.colorize(processed));
            }
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        int infoSlot = plugin.getConfigManager().getOrderDeliveryOrderInfoSlot();
        if (infoSlot >= 0 && infoSlot < inventory.getSize()) {
            inventory.setItem(infoSlot, infoItem);
        }

        // Back button
        ItemStack backItem = new ItemStack(plugin.getConfigManager().getOrderDeliveryBackItemMaterial());
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ColorUtils.colorize(plugin.getConfigManager().getOrderDeliveryBackItemDisplayName()));
            List<String> lore = new ArrayList<>();
            for (String line : plugin.getConfigManager().getOrderDeliveryBackItemLore()) {
                lore.add(ColorUtils.colorize(line));
            }
            backMeta.setLore(lore);
            backItem.setItemMeta(backMeta);
        }
        int backSlot = plugin.getConfigManager().getOrderDeliveryBackItemSlot();
        if (backSlot >= 0 && backSlot < inventory.getSize()) {
            inventory.setItem(backSlot, backItem);
        }
    }

    private ItemStack createBorderItem() {
        ItemStack borderItem = new ItemStack(plugin.getConfigManager().getOrderDeliveryBorderItemMaterial());
        ItemMeta meta = borderItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().getOrderDeliveryBorderItemDisplayName());
            List<String> lore = plugin.getConfigManager().getOrderDeliveryBorderItemLore();
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            borderItem.setItemMeta(meta);
        }
        return borderItem;
    }

    public static Order getOrderByPlayer(Player player) {
        return playerDeliveryMap.get(player.getUniqueId());
    }

    public static void removePlayer(Player player) {
        playerDeliveryMap.remove(player.getUniqueId());
    }

    public static void processDelivery(LeDatOrder plugin, Player player, Inventory inventory) {
        Order order = getOrderByPlayer(player);
        if (order == null) return;

        // Kiểm tra các item trong inventory
        int deliveredAmount = 0;
        Map<Integer, ItemStack> returnItems = new HashMap<>();
        int remainingNeeded = Math.max(0, order.getRequiredAmount() - order.getReceivedAmount());
        int excessAmount = 0;
        List<ItemStack> overflowItems = new ArrayList<>();

        Set<Integer> protectedSlots = new HashSet<>(plugin.getConfigManager().getOrderDeliveryBorderSlots());
        protectedSlots.add(plugin.getConfigManager().getOrderDeliveryOrderInfoSlot());
        protectedSlots.add(plugin.getConfigManager().getOrderDeliveryBackItemSlot());

        for (int i = 0; i < inventory.getSize(); i++) {
            if (protectedSlots.contains(i)) {
                continue;
            }
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                if (Tag.SHULKER_BOXES.isTagged(item.getType())) {
                    ShulkerExtractionResult shulkerExtraction = extractFromShulker(item, order.getMaterial(), remainingNeeded);
                    if (shulkerExtraction.deliveredAmount() > 0) {
                        deliveredAmount += shulkerExtraction.deliveredAmount();
                        remainingNeeded = Math.max(0, remainingNeeded - shulkerExtraction.deliveredAmount());
                    }

                    if (!shulkerExtraction.overflowItems().isEmpty()) {
                        overflowItems.addAll(shulkerExtraction.overflowItems());
                        for (ItemStack overflow : shulkerExtraction.overflowItems()) {
                            excessAmount += overflow.getAmount();
                        }
                    }

                    returnItems.put(i, shulkerExtraction.updatedShulker());
                } else if (item.getType() == order.getMaterial()) {
                    int canTake = Math.min(item.getAmount(), remainingNeeded);
                    deliveredAmount += canTake;
                    remainingNeeded = Math.max(0, remainingNeeded - canTake);

                    int leftover = item.getAmount() - canTake;
                    if (leftover > 0) {
                        ItemStack excessStack = item.clone();
                        excessStack.setAmount(leftover);
                        returnItems.put(i, excessStack);
                        excessAmount += leftover;
                    }
                } else {
                    // Item không đúng loại, trả lại cho người chơi
                    returnItems.put(i, item);
                }
            }
        }

        // Nếu không có item nào đúng loại, trả lại tất cả và đóng GUI
        if (deliveredAmount == 0) {
            for (Map.Entry<Integer, ItemStack> entry : returnItems.entrySet()) {
                player.getInventory().addItem(entry.getValue());
            }
            for (ItemStack overflow : overflowItems) {
                player.getInventory().addItem(overflow);
            }
            removePlayer(player);
            return;
        }

        // Tính toán số lượng thừa cần trả lại
        int actualDelivered = deliveredAmount;

        inventory.clear();

        // Items sẽ được lưu trữ trong order để người tạo order có thể collect sau
        // Không cần gửi trực tiếp cho người tạo order nữa

        // Cập nhật order và trả tiền cho người chơi
        double paymentAmount = order.getPricePerItem() * actualDelivered;
        if (actualDelivered > 0) {
            plugin.getOrderManager().deliverItems(player, order, actualDelivered);
        }

        // Trả tiền cho người giao hàng dựa trên loại tiền tệ
        if (paymentAmount > 0) {
            if (order.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT) {
                if (plugin.getVaultManager().isEnabled()) {
                    plugin.getVaultManager().deposit(player, paymentAmount);
                    String amountString = plugin.getConfigManager().formatCurrencyAmount(paymentAmount, order.getCurrencyType());
                    player.sendMessage(plugin.getConfigManager().getMessage("delivery.payment-received", "%amount%", amountString));
                }
            } else {
                if (plugin.getPlayerPointsManager().isEnabled()) {
                    int points = (int) Math.round(paymentAmount);
                    if (points > 0) {
                        plugin.getPlayerPointsManager().deposit(player, points);
                        String amountString = plugin.getConfigManager().formatCurrencyAmount(points, order.getCurrencyType());
                        player.sendMessage(plugin.getConfigManager().getMessage("delivery.payment-received", "%amount%", amountString));
                    }
                }
            }
        }

        // Kiểm tra nếu đơn hàng đã hoàn thành
        if (order.getReceivedAmount() >= order.getRequiredAmount()) {
            player.sendMessage(plugin.getConfigManager().getMessage("delivery.order-filled"));

            Player orderOwner = plugin.getServer().getPlayer(order.getPlayerUUID());
            if (orderOwner != null && orderOwner.isOnline()) {
                String ownerMessage = plugin.getConfigManager().getMessage(
                        "delivery.owner-complete",
                        "%material%",
                        order.getMaterial().name()
                );
                plugin.getFoliaLib().getScheduler().runAtEntity(orderOwner, (task) -> orderOwner.sendMessage(ownerMessage));
            }
        }

        // Trả lại các item không đúng loại
        for (Map.Entry<Integer, ItemStack> entry : returnItems.entrySet()) {
            player.getInventory().addItem(entry.getValue());
        }
        for (ItemStack overflow : overflowItems) {
            player.getInventory().addItem(overflow);
        }

        // Trả lại số lượng thừa nếu có
        if (excessAmount > 0) {
            player.sendMessage(plugin.getConfigManager().getMessage(
                    "delivery.excess-returned",
                    "%amount%",
                    String.valueOf(excessAmount),
                    "%material%",
                    order.getMaterial().name()
            ));
        }

        removePlayer(player);

        // Hiển thị thông báo thành công
        showDeliveryAnimation(plugin, player, paymentAmount);
    }

    private static void showDeliveryAnimation(LeDatOrder plugin, Player player, double amount) {
        // Mở lại GUI Order ngay lập tức
        plugin.getFoliaLib().getScheduler().runAtEntity(player, (task) -> {
            new OrderGUI(plugin, player).open();
        });
    }

    private static ShulkerExtractionResult extractFromShulker(ItemStack shulkerItem, Material targetMaterial, int remainingNeeded) {
        ItemMeta baseMeta = shulkerItem.getItemMeta();
        if (!(baseMeta instanceof BlockStateMeta blockStateMeta)) {
            return new ShulkerExtractionResult(shulkerItem, 0, new ArrayList<>());
        }

        if (!(blockStateMeta.getBlockState() instanceof ShulkerBox shulkerBox)) {
            return new ShulkerExtractionResult(shulkerItem, 0, new ArrayList<>());
        }

        if (remainingNeeded <= 0) {
            return new ShulkerExtractionResult(shulkerItem, 0, new ArrayList<>());
        }

        Inventory boxInventory = shulkerBox.getInventory();
        int extracted = 0;
        List<ItemStack> overflowItems = new ArrayList<>();

        for (int slot = 0; slot < boxInventory.getSize(); slot++) {
            ItemStack content = boxInventory.getItem(slot);
            if (content == null || content.getType() != targetMaterial) {
                continue;
            }

            int deliverable = Math.max(0, Math.min(content.getAmount(), remainingNeeded - extracted));
            int overflowCount = content.getAmount() - deliverable;

            if (deliverable > 0) {
                extracted += deliverable;
            }

            if (overflowCount > 0) {
                ItemStack overflowStack = content.clone();
                overflowStack.setAmount(overflowCount);
                overflowItems.add(overflowStack);
            }

            boxInventory.setItem(slot, null);

            if (extracted >= remainingNeeded) {
                break;
            }
        }

        blockStateMeta.setBlockState(shulkerBox);
        shulkerItem.setItemMeta(blockStateMeta);

        return new ShulkerExtractionResult(shulkerItem, extracted, overflowItems);
    }

    private record ShulkerExtractionResult(ItemStack updatedShulker, int deliveredAmount, List<ItemStack> overflowItems) {
    }
}
