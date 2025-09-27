package org.nexus.leDatOrder.listeners;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.gui.CreateOrderGUI;
import org.nexus.leDatOrder.gui.MaterialSelectGUI;
import org.nexus.leDatOrder.gui.MyOrderGUI;
import org.nexus.leDatOrder.gui.OrderDeliveryGUI;
import org.nexus.leDatOrder.gui.OrderGUI;
import org.nexus.leDatOrder.gui.OrderManageGUI;
import org.nexus.leDatOrder.models.Order;
import org.nexus.leDatOrder.models.OrderType;
import org.nexus.leDatOrder.models.SortType;
import org.nexus.leDatOrder.utils.ColorUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderListener implements Listener {
    private final LeDatOrder plugin;
    private final Map<UUID, ChatInputType> awaitingChatInput = new HashMap<>();

    private enum ChatInputType {
        AMOUNT,
        PRICE,
        MATERIAL_SEARCH
    }

    public OrderListener(LeDatOrder plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        String strippedTitle = stripColor(title);

        // Lấy tiêu đề từ config (đã strip màu)
        String orderTitleTemplate = plugin.getConfigManager().getOrderGuiTitle();
        String orderPrefix = stripColor(orderTitleTemplate).split("\\(")[0].trim();

        String myOrderTitle = stripColor(plugin.getConfigManager().getMyOrderGuiTitle());
        String manageOrderTitle = stripColor(plugin.getConfigManager().getOrderManageGuiTitle());
        String createOrderTitle = stripColor(plugin.getConfigManager().getCreateOrderGuiTitle());
        String materialSelectTitle = stripColor(plugin.getConfigManager().getMaterialSelectGuiTitle());
        String orderDeliveryPrefix = stripColor(plugin.getConfigManager().getOrderDeliveryGuiTitle())
                .replace("%player%", "").trim();

        // ==== ORDER LIST (có phân trang) ====
        if (strippedTitle.startsWith(orderPrefix)) {
            event.setCancelled(true);

            int slot = event.getRawSlot();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) return;

            OrderGUI gui = new OrderGUI(plugin, player);

            if (slot == plugin.getConfigManager().getSortItemSlot()) {
                SortType[] all = SortType.values();
                SortType cur = gui.getCurrentSortType();
                int idx = 0;
                for (int i = 0; i < all.length; i++) if (all[i] == cur) { idx = i; break; }
                gui.setCurrentSortType(all[(idx + 1) % all.length]);
                gui.open();
                player.sendMessage(ColorUtils.colorize("&aSắp xếp theo: &e" + gui.getCurrentSortType().getDisplayName()));

            } else if (slot == plugin.getConfigManager().getFilterItemSlot()) {
                OrderType[] all = OrderType.values();
                OrderType cur = gui.getCurrentFilterType();
                int idx = 0;
                for (int i = 0; i < all.length; i++) if (all[i] == cur) { idx = i; break; }
                gui.setCurrentFilterType(all[(idx + 1) % all.length]);
                gui.open();
                player.sendMessage(ColorUtils.colorize("&aLọc theo: &e" + gui.getCurrentFilterType().getDisplayName()));

            } else if (slot == plugin.getConfigManager().getRefreshItemSlot()) {
                gui.open();

            } else if (slot == plugin.getConfigManager().getSearchItemSlot()) {
                new MaterialSelectGUI(plugin, player).open();

            } else if (slot == plugin.getConfigManager().getCreateItemSlot()) {
                new MyOrderGUI(plugin, player).open();

            } else if (slot == plugin.getConfigManager().getPreviousPageItemSlot()) {
                gui.previousPage();
                gui.open();

            } else if (slot == plugin.getConfigManager().getNextPageItemSlot()) {
                gui.nextPage();
                gui.open();

            } else if (slot < 45) {
                handleOrderClick(player, slot, gui);
            }
            return;
        }

        // ==== ORDER DELIVERY (cho phép thao tác) ====
        if (strippedTitle.startsWith(orderDeliveryPrefix)) {
            // Nút “Quay lại”
            ItemStack current = event.getCurrentItem();
            int backSlotDefault = 40; // 45-slot GUI => slot 40
            int backSlot = backSlotDefault;
            Material backMat = Material.BARRIER;
            try {
                backMat = plugin.getConfigManager().getItemMaterial("gui.order-delivery.back-item", Material.BARRIER);
                // Nếu bạn có getter slot tổng quát, thay bằng: backSlot = plugin.getConfigManager().getItemSlot("gui.order-delivery.back-item", 40);
            } catch (Exception ignored) {}
            if (current != null && current.getType() == backMat && event.getRawSlot() == backSlot) {
                event.setCancelled(true);
                player.closeInventory();
                OrderDeliveryGUI.removePlayer(player);
                new OrderGUI(plugin, player).open();
                return;
            }
            event.setCancelled(false);
            return;
        }

        // ==== MY ORDERS ====
        if (strippedTitle.equalsIgnoreCase(myOrderTitle)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            if (slot < 27) {
                List<Order> list = plugin.getOrderManager().getOrdersByPlayer(player.getUniqueId());
                if (slot < list.size()) {
                    new OrderManageGUI(plugin, player, list.get(slot)).open();
                }
            } else {
                int createSlot = 31; // mặc định
                Material createMat = plugin.getConfigManager().getItemMaterial("gui.my-order.create-item", Material.MAP);
                if (slot == createSlot && clicked.getType() == createMat) {
                    new CreateOrderGUI(plugin, player).open();
                }
            }
            return;
        }

        // ==== MANAGE ORDER ====
        if (strippedTitle.equalsIgnoreCase(manageOrderTitle)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            int cancelSlot = 12;
            Material cancelMat = plugin.getConfigManager().getItemMaterial("gui.order-manage.cancel-item", Material.BARRIER);
            if (slot == cancelSlot && clicked.getType() == cancelMat) {
                OrderManageGUI.cancelOrder(plugin, player);
                return;
            }

            int collectSlot = 14;
            Material collectMat = plugin.getConfigManager().getItemMaterial("gui.order-manage.collect-item", Material.CHEST);
            if (slot == collectSlot && clicked.getType() == collectMat) {
                OrderManageGUI.collectItems(plugin, player);
            }
            return;
        }

        // ==== CREATE ORDER ====
        if (strippedTitle.equalsIgnoreCase(createOrderTitle)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            int backSlot = 10;
            Material backMat = plugin.getConfigManager().getItemMaterial("gui.create-order.back-item", Material.RED_STAINED_GLASS_PANE);
            if (slot == backSlot && clicked.getType() == backMat) {
                new MyOrderGUI(plugin, player).open();
                return;
            }

            int materialSlot = 12;
            if (slot == materialSlot && clicked.getType() == plugin.getConfigManager().getItemMaterial("gui.create-order.material-item", Material.STONE)) {
                new MaterialSelectGUI(plugin, player).open();
                return;
            }

            int amountSlot = 13;
            if (slot == amountSlot && clicked.getType() == plugin.getConfigManager().getItemMaterial("gui.create-order.amount-item", Material.CHEST)) {
                player.closeInventory();
                player.sendMessage(plugin.getConfigManager().getMessage("input.amount-prompt"));
                awaitingChatInput.put(player.getUniqueId(), ChatInputType.AMOUNT);
                return;
            }

            int priceSlot = 14;
            if (slot == priceSlot && clicked.getType() == plugin.getConfigManager().getItemMaterial("gui.create-order.price-item", Material.SUNFLOWER)) {
                player.closeInventory();
                player.sendMessage(plugin.getConfigManager().getMessage("input.price-prompt"));
                awaitingChatInput.put(player.getUniqueId(), ChatInputType.PRICE);
                return;
            }

            int confirmSlot = 16;
            if (slot == confirmSlot && clicked.getType() == plugin.getConfigManager().getItemMaterial("gui.create-order.confirm-item", Material.LIME_STAINED_GLASS_PANE)) {
                CreateOrderGUI.createOrder(plugin, player);
                return;
            }

            // Toggle tiền tệ
            int currencySlot = plugin.getConfigManager().getCreateOrderCurrencyItemSlot();
            Material currencyMat = plugin.getConfigManager().getCreateOrderCurrencyItemMaterial();
            if (slot == currencySlot && clicked.getType() == currencyMat) {
                CreateOrderGUI.CreateOrderData data = CreateOrderGUI.getCreateData(player);
                if (data != null) {
                    boolean vaultOK = plugin.getVaultManager().isEnabled();
                    boolean ppOK = plugin.getPlayerPointsManager().isEnabled();
                    if (!vaultOK && !ppOK) {
                        player.sendMessage(plugin.getConfigManager().getMessage("currency.no-system-available"));
                        return;
                    }
                    data.toggleCurrencyType();
                    if (data.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT && !vaultOK) {
                        data.toggleCurrencyType();
                        player.sendMessage(plugin.getConfigManager().getMessage("currency.vault-unavailable-switch"));
                    } else if (data.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.PLAYERPOINTS && !ppOK) {
                        data.toggleCurrencyType();
                        player.sendMessage(plugin.getConfigManager().getMessage("currency.playerpoints-unavailable-switch"));
                    }
                    String name = data.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT ? "Vault Money" : "PlayerPoints";
                    player.sendMessage(plugin.getConfigManager().getMessage("currency.type-changed", "%type%", name));
                    new CreateOrderGUI(plugin, player).open();
                }
            }
            return;
        }

        // ==== MATERIAL SELECT ====
        if (strippedTitle.equalsIgnoreCase(materialSelectTitle)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            if (slot < 45) {
                Material mat = clicked.getType();
                CreateOrderGUI.CreateOrderData data = CreateOrderGUI.getCreateData(player);
                if (data != null) {
                    data.setMaterial(mat);
                    player.sendMessage(ColorUtils.colorize("&aĐã chọn vật liệu: &e" + mat.name()));
                    MaterialSelectGUI.clearSearch(player);
                    new CreateOrderGUI(plugin, player).open();
                }
                return;
            }

            int filterSlot = plugin.getConfigManager().getMaterialSelectFilterItemSlot();
            if (slot == filterSlot && clicked.getType() == plugin.getConfigManager().getMaterialSelectFilterItemMaterial()) {
                MaterialSelectGUI gui = new MaterialSelectGUI(plugin, player);
                OrderType cur = gui.getCurrentFilterType();
                OrderType[] all = OrderType.values();
                int idx = 0;
                for (int i = 0; i < all.length; i++) if (all[i] == cur) { idx = i; break; }
                gui.setFilterType(all[(idx + 1) % all.length]);
                gui.open();
                return;
            }

            int commonSlot = plugin.getConfigManager().getMaterialSelectCommonItemSlot();
            if (slot == commonSlot && clicked.getType() == plugin.getConfigManager().getMaterialSelectCommonItemMaterial()) {
                MaterialSelectGUI gui = new MaterialSelectGUI(plugin, player);
                gui.toggleCommonMaterials();
                gui.open();
                return;
            }

            int searchSlot = plugin.getConfigManager().getMaterialSelectSearchItemSlot();
            if (slot == searchSlot && clicked.getType() == plugin.getConfigManager().getMaterialSelectSearchItemMaterial()) {
                String curSearch = MaterialSelectGUI.getSearch(player);
                if (!curSearch.isEmpty()) {
                    MaterialSelectGUI.clearSearch(player);
                    new MaterialSelectGUI(plugin, player).open();
                    player.sendMessage(ColorUtils.colorize("&aĐã xóa tìm kiếm"));
                } else {
                    player.closeInventory();
                    player.sendMessage(ColorUtils.colorize("&aNhập từ khóa tìm kiếm trong chat:"));
                    awaitingChatInput.put(player.getUniqueId(), ChatInputType.MATERIAL_SEARCH);
                }
                return;
            }

            int backSlot = plugin.getConfigManager().getMaterialSelectBackItemSlot();
            if (slot == backSlot && clicked.getType() == plugin.getConfigManager().getMaterialSelectBackItemMaterial()) {
                MaterialSelectGUI.clearSearch(player);
                new CreateOrderGUI(plugin, player).open();
                return;
            }

            int prevSlot = plugin.getConfigManager().getMaterialSelectPreviousPageItemSlot();
            if (slot == prevSlot && clicked.getType() == plugin.getConfigManager().getMaterialSelectPreviousPageItemMaterial()) {
                MaterialSelectGUI gui = new MaterialSelectGUI(plugin, player);
                gui.previousPage();
                gui.open();
                return;
            }

            int nextSlot = plugin.getConfigManager().getMaterialSelectNextPageItemSlot();
            if (slot == nextSlot && clicked.getType() == plugin.getConfigManager().getMaterialSelectNextPageItemMaterial()) {
                MaterialSelectGUI gui = new MaterialSelectGUI(plugin, player);
                gui.nextPage();
                gui.open();
            }
            return;
        }
    }

    /**
     * Mở GUI giao hàng khi click vào 1 order trên danh sách.
     * slot: vị trí ô được click trong 45 ô đầu (vùng hiển thị order).
     */
    private void handleOrderClick(Player player, int slot, OrderGUI gui) {
        // Lấy danh sách order đã lọc & sắp xếp theo trạng thái hiện tại của GUI
        List<Order> orders = plugin.getOrderManager().getSortedOrders(
                gui.getCurrentSortType(),
                gui.getCurrentFilterType()
        );

        // Phân trang: mỗi trang hiển thị tối đa 45 item (0..44)
        int startIndex = gui.getCurrentPage() * 45;
        int index = startIndex + slot;

        if (index < 0 || index >= orders.size()) {
            return; // click vào ô trống
        }

        Order order = orders.get(index);

        // Không cho tự giao cho chính mình
        if (order.getPlayerUUID().equals(player.getUniqueId())) {
            player.sendMessage(ColorUtils.colorize("&cBạn không thể giao đồ cho chính mình!"));
            return;
        }

        // Nếu bạn đã có cơ chế escrow và muốn chặn đơn chưa khóa tiền:
        // if (!order.isEscrowed()) {
        //     player.sendMessage(ColorUtils.colorize("&eĐơn này chưa khóa tiền. Vui lòng bảo chủ đơn hủy và tạo lại!"));
        //     return;
        // }

        new OrderDeliveryGUI(plugin, player, order).open();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!awaitingChatInput.containsKey(playerId)) return;

        event.setCancelled(true);
        String message = event.getMessage();
        ChatInputType type = awaitingChatInput.remove(playerId);

        plugin.getFoliaLib().getScheduler().runAtEntity(player, (task) -> {
            if (type == ChatInputType.MATERIAL_SEARCH) {
                MaterialSelectGUI.setSearch(player, message);
                player.sendMessage(ColorUtils.colorize("&aKết quả tìm kiếm: &e" + message));
                new MaterialSelectGUI(plugin, player).open();
                return;
            }

            CreateOrderGUI.CreateOrderData data = CreateOrderGUI.getCreateData(player);
            if (data == null) return;

            if (type == ChatInputType.AMOUNT) {
                try {
                    int amount = Integer.parseInt(message.replaceAll("[^0-9]", ""));
                    if (amount <= 0) {
                        player.sendMessage(ColorUtils.colorize("&cSố lượng phải > 0."));
                    } else {
                        data.setAmount(amount);
                        player.sendMessage(ColorUtils.colorize("&aĐã đặt số lượng: &e" + amount));
                    }
                } catch (NumberFormatException ex) {
                    player.sendMessage(ColorUtils.colorize("&cSai định dạng số. Vui lòng nhập số nguyên."));
                }
            } else if (type == ChatInputType.PRICE) {
                try {
                    String norm = message.replace(",", ".").replaceAll("[^0-9.]", "");
                    double price = Double.parseDouble(norm);
                    if (price <= 0D) {
                        player.sendMessage(ColorUtils.colorize("&cGiá phải > 0."));
                    } else {
                        data.setPricePerItem(price);
                        String pretty = (data.getCurrencyType() == org.nexus.leDatOrder.enums.CurrencyType.VAULT)
                                ? ("$" + String.format("%.2f", price))
                                : ((int) Math.round(price) + " Points");
                        player.sendMessage(ColorUtils.colorize("&aĐã đặt giá: &e" + pretty));
                    }
                } catch (NumberFormatException ex) {
                    player.sendMessage(ColorUtils.colorize("&cSai định dạng số. Ví dụ hợp lệ: 12.5 hoặc 12,5"));
                }
            }

            new CreateOrderGUI(plugin, player).open();
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String stripped = stripColor(event.getView().getTitle());
        String deliveryPrefix = stripColor(plugin.getConfigManager().getOrderDeliveryGuiTitle())
                .replace("%player%", "").trim();

        if (stripped.startsWith(deliveryPrefix)) {
            OrderDeliveryGUI.processDelivery(plugin, player, event.getInventory());
        }
    }

    private String stripColor(String input) {
        if (input == null) return "";
        return ChatColor.stripColor(input);
    }
}
