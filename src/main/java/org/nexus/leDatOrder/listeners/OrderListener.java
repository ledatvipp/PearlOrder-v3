package org.nexus.leDatOrder.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.nexus.leDatOrder.LeDatOrder;
import org.nexus.leDatOrder.gui.*;
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

        String orderGuiTitleBase = plugin.getConfigManager().getOrderGuiTitle().split("\\(Page")[0].trim();
        if (title.startsWith(ColorUtils.colorize(orderGuiTitleBase))) {
            event.setCancelled(true);
            
            int slot = event.getRawSlot();
            ItemStack clickedItem = event.getCurrentItem();
            
            if (clickedItem == null) return;

            OrderGUI gui = new OrderGUI(plugin, player);

            if (slot == plugin.getConfigManager().getSortItemSlot()) {
                SortType currentType = null;

                boolean foundCurrent = false;
                for (SortType type : SortType.values()) {
                    if (foundCurrent) {
                        currentType = type;
                        break;
                    }
                    if (type == gui.getCurrentSortType()) {
                        foundCurrent = true;
                    }
                }

                if (currentType == null) {
                    currentType = SortType.values()[0];
                }
                
                gui.setCurrentSortType(currentType);
                gui.open();
                player.sendMessage(ColorUtils.colorize("&aSắp xếp theo: &e" + currentType.getDisplayName()));
            } else if (slot == plugin.getConfigManager().getFilterItemSlot()) {
                // Xử lý nút Filter
                OrderType currentType = null;
                
                // Tìm loại lọc tiếp theo
                boolean foundCurrent = false;
                for (OrderType type : OrderType.values()) {
                    if (foundCurrent) {
                        currentType = type;
                        break;
                    }
                    if (type == gui.getCurrentFilterType()) {
                        foundCurrent = true;
                    }
                }
                
                // Nếu đã ở loại cuối cùng, quay lại loại đầu tiên
                if (currentType == null) {
                    currentType = OrderType.values()[0];
                }
                
                gui.setCurrentFilterType(currentType);
                gui.open();
                player.sendMessage(ColorUtils.colorize("&aLọc theo: &e" + currentType.getDisplayName()));
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
        } else if (title.startsWith(ColorUtils.colorize("&6Order -> "))) {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER) {
                event.setCancelled(true);
                player.closeInventory();
                OrderDeliveryGUI.removePlayer(player);
                new OrderGUI(plugin, player).open();
                return;
            }
            event.setCancelled(false);
        } else if (title.equals(ColorUtils.colorize("&6Your Orders"))) {
            event.setCancelled(true);
            
            int slot = event.getRawSlot();
            ItemStack clickedItem = event.getCurrentItem();
            
            if (clickedItem == null) return;
            
            if (slot < 27) {
                // Click vào order của người chơi
                List<Order> playerOrders = plugin.getOrderManager().getOrdersByPlayer(player.getUniqueId());
                if (slot < playerOrders.size()) {
                    Order order = playerOrders.get(slot);
                    new OrderManageGUI(plugin, player, order).open();
                }
            } else if (slot == 31 && clickedItem.getType() == Material.MAP) {
                new CreateOrderGUI(plugin, player).open();
            }
        } else if (title.equals(ColorUtils.colorize("&6Manage Order"))) {
            event.setCancelled(true);
            
            int slot = event.getRawSlot();
            ItemStack clickedItem = event.getCurrentItem();
            
            if (clickedItem == null) return;
            
            if (slot == 12 && clickedItem.getType() == Material.BARRIER) {
                // Hủy bỏ order
                OrderManageGUI.cancelOrder(plugin, player);
            } else if (slot == 14 && clickedItem.getType() == Material.CHEST) {
                // Thu thập items
                OrderManageGUI.collectItems(plugin, player);
            }
        } else if (title.equals(ColorUtils.colorize("&6Create Order"))) {
            event.setCancelled(true);
            
            int slot = event.getRawSlot();
            ItemStack clickedItem = event.getCurrentItem();
            
            if (clickedItem == null) return;
            
            if (slot == 10 && clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
                new MyOrderGUI(plugin, player).open();
            } else if (slot == 12) {
                // Chọn material
                new MaterialSelectGUI(plugin, player).open();
            } else if (slot == 13 && clickedItem.getType() == Material.CHEST) {
                // Nhập số lượng
                player.closeInventory();
                player.sendMessage(ColorUtils.colorize("&aPlease enter the amount in chat:"));
                awaitingChatInput.put(player.getUniqueId(), ChatInputType.AMOUNT);
            } else if (slot == 14 && clickedItem.getType() == Material.SUNFLOWER) {
                // Nhập giá tiền
                player.closeInventory();
                player.sendMessage(ColorUtils.colorize("&aPlease enter the price per item in chat:"));
                awaitingChatInput.put(player.getUniqueId(), ChatInputType.PRICE);
            } else if (slot == 16 && clickedItem.getType() == Material.LIME_STAINED_GLASS_PANE) {
                // Xác nhận tạo order
                CreateOrderGUI.createOrder(plugin, player);
            }
        } else if (title.equals(ColorUtils.colorize("&6Select Material"))) {
            event.setCancelled(true);
            
            int slot = event.getRawSlot();
            ItemStack clickedItem = event.getCurrentItem();
            
            if (clickedItem == null) return;
            
            if (slot < 45) {
                // Chọn material
                Material material = clickedItem.getType();
                CreateOrderGUI.CreateOrderData data = CreateOrderGUI.getCreateData(player);
                if (data != null) {
                    data.setMaterial(material);
                    player.sendMessage(ColorUtils.colorize("&aMaterial set to &e" + material.name()));
                    MaterialSelectGUI.clearSearch(player); // Xóa tìm kiếm khi đã chọn
                    new CreateOrderGUI(plugin, player).open();
                }
            } else if (slot == plugin.getConfigManager().getMaterialSelectFilterItemSlot() && clickedItem.getType() == Material.HOPPER) {
                // Xử lý nút Filter
                MaterialSelectGUI gui = new MaterialSelectGUI(plugin, player);
                OrderType currentType = null;
                
                // Tìm loại filter tiếp theo
                boolean foundCurrent = false;
                for (OrderType type : OrderType.values()) {
                    if (foundCurrent) {
                        currentType = type;
                        break;
                    }
                    if (type == gui.getCurrentFilterType()) {
                        foundCurrent = true;
                    }
                }
                
                // Nếu đã ở loại cuối cùng, quay lại loại đầu tiên
                if (currentType == null) {
                    currentType = OrderType.values()[0]; // Sửa từ ALL thành values()[0]
                }
                
                gui.setFilterType(currentType);
                gui.open();
            } else if (slot == plugin.getConfigManager().getMaterialSelectCommonItemSlot() && clickedItem.getType() == Material.COMPASS) {
                // Hiển thị common materials hoặc tất cả
                MaterialSelectGUI gui = new MaterialSelectGUI(plugin, player);
                gui.toggleCommonMaterials();
                gui.open();
            } else if (slot == plugin.getConfigManager().getMaterialSelectSearchItemSlot() && clickedItem.getType() == Material.OAK_SIGN) {
                // Tìm kiếm
                String currentSearch = MaterialSelectGUI.getSearch(player);
                if (!currentSearch.isEmpty()) {
                    // Nếu đã có tìm kiếm, xóa nó
                    MaterialSelectGUI.clearSearch(player);
                    new MaterialSelectGUI(plugin, player).open();
                    player.sendMessage(ColorUtils.colorize("&aSearch cleared"));
                } else {
                    player.closeInventory();
                    player.sendMessage(ColorUtils.colorize("&aPlease enter search term in chat:"));
                    awaitingChatInput.put(player.getUniqueId(), ChatInputType.MATERIAL_SEARCH);
                }
            } else if (slot == plugin.getConfigManager().getMaterialSelectBackItemSlot() && clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
                MaterialSelectGUI.clearSearch(player);
                new CreateOrderGUI(plugin, player).open();
            } else if (slot == plugin.getConfigManager().getMaterialSelectPreviousPageItemSlot() && clickedItem.getType() == Material.ARROW) {
                MaterialSelectGUI gui = new MaterialSelectGUI(plugin, player);
                gui.previousPage();
                gui.open();
            } else if (slot == plugin.getConfigManager().getMaterialSelectNextPageItemSlot() && clickedItem.getType() == Material.ARROW) {
                MaterialSelectGUI gui = new MaterialSelectGUI(plugin, player);
                gui.nextPage();
                gui.open();
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (awaitingChatInput.containsKey(playerId)) {
            event.setCancelled(true);
            String message = event.getMessage();
            ChatInputType inputType = awaitingChatInput.get(playerId);
            awaitingChatInput.remove(playerId);

            plugin.getFoliaLib().getScheduler().runAtEntity(player, (task) -> {
                if (inputType == ChatInputType.MATERIAL_SEARCH) {
                    MaterialSelectGUI.setSearch(player, message);
                    player.sendMessage(ColorUtils.colorize("&aKết quả tìm kiếm: &e" + message));
                    new MaterialSelectGUI(plugin, player).open();
                    return;
                }
                
                CreateOrderGUI.CreateOrderData data = CreateOrderGUI.getCreateData(player);
                if (data == null) return;
                
                if (inputType == ChatInputType.AMOUNT) {
                    try {
                        int amount = Integer.parseInt(message);
                        if (amount <= 0) {
                            player.sendMessage(ColorUtils.colorize("&cAmount must be greater than 0."));
                        } else {
                            data.setAmount(amount);
                            player.sendMessage(ColorUtils.colorize("&aAmount set to &e" + amount));
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ColorUtils.colorize("&cInvalid number format. Please enter a valid number."));
                    }
                } else if (inputType == ChatInputType.PRICE) {
                    try {
                        double price = Double.parseDouble(message);
                        if (price <= 0) {
                            player.sendMessage(ColorUtils.colorize("&cPrice must be greater than 0."));
                        } else {
                            data.setPricePerItem(price);
                            player.sendMessage(ColorUtils.colorize("&aPrice set to &e$" + String.format("%.2f", price)));
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ColorUtils.colorize("&cInvalid number format. Please enter a valid number."));
                    }
                }
                
                // Mở lại GUI
                new CreateOrderGUI(plugin, player).open();
            });
        }
    }

    private void handleOrderClick(Player player, int slot, OrderGUI orderGUI) {
        // Lấy danh sách order đã lọc và sắp xếp
        List<Order> filteredOrders = plugin.getOrderManager().getSortedOrders(
            orderGUI.getCurrentSortType(),
            orderGUI.getCurrentFilterType());
        
        // Tính toán phân trang
        int startIndex = orderGUI.getCurrentPage() * 45;
        
        // Kiểm tra xem slot có hợp lệ không
        int adjustedSlot = slot + startIndex;
        if (adjustedSlot >= filteredOrders.size()) {
            return;
        }
        
        // Lấy order tương ứng với slot
        Order order = filteredOrders.get(adjustedSlot);
        
        // Kiểm tra xem người chơi có đang cố giao đồ cho chính mình không
        if (order.getPlayerUUID().equals(player.getUniqueId())) {
            player.sendMessage(ColorUtils.colorize("&cBạn không thể giao đồ cho chính mình!"));
            return;
        }
        
        // Mở GUI giao hàng
        new OrderDeliveryGUI(plugin, player, order).open();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        
        if (title.startsWith(ColorUtils.colorize("&6Order -> "))) {
            OrderDeliveryGUI.processDelivery(plugin, player, event.getInventory());
        }
    }
}