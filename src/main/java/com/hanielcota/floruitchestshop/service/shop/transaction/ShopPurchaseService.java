package com.hanielcota.floruitchestshop.service.shop.transaction;

import com.hanielcota.floruitchestshop.FloruitChestShop;
import com.hanielcota.floruitchestshop.config.ShopConfig;
import com.hanielcota.floruitchestshop.economy.VaultEconomy;
import com.hanielcota.floruitchestshop.model.ShopSign;
import com.hanielcota.floruitchestshop.service.hologram.HologramManager;
import com.hanielcota.floruitchestshop.service.shop.ShopPurchaseManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@RequiredArgsConstructor
public class ShopPurchaseService {

    private final VaultEconomy economy;
    private final ShopConfig config;
    private final FloruitChestShop plugin;
    private final HologramManager hologramManager;
    private final ShopPurchaseManager purchaseManager;

    public ShopPurchaseService(FloruitChestShop plugin) {
        this.economy = plugin.getEconomy();
        this.config = plugin.getShopConfig();
        this.plugin = plugin;
        this.hologramManager = plugin.getHologramManager();
        this.purchaseManager = plugin.getShopPurchaseManager();
    }

    public void startPurchase(Player player, ShopSign shopSign) {
        purchaseManager.startPurchase(player, shopSign);
    }

    public void processPurchase(Player player, String message) {
        ShopSign shopSign = purchaseManager.getPendingPurchases().get(player.getUniqueId());
        if (shopSign == null) return;

        // Permitir cancelamento
        if (message.equalsIgnoreCase("cancelar")) {
            purchaseManager.cancelPurchase(player.getUniqueId(), "manual", shopSign);
            return;
        }

        int requestedAmount;
        try {
            requestedAmount = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            config.sendMessage(player, config.formatQuantityMessage(config.getInvalidQuantityMessage(), shopSign.getAmount()));
            config.playErrorSound(player);
            purchaseManager.cancelPurchase(player.getUniqueId(), "invalid_quantity", shopSign);
            return;
        }

        if (requestedAmount < 1) {
            config.sendMessage(player, config.formatQuantityMessage(config.getMaxQuantityMessage(), shopSign.getAmount()));
            config.playErrorSound(player);
            purchaseManager.cancelPurchase(player.getUniqueId(), "invalid_quantity", shopSign);
            return;
        }

        // Mover a lógica de compra para o thread principal
        Bukkit.getScheduler().runTask(plugin, () -> {
            ItemStack item = shopSign.getItemClone();
            item.setAmount(requestedAmount);
            double unitPrice = shopSign.getBuyPrice() / shopSign.getAmount();
            double totalPrice = unitPrice * requestedAmount;

            // Aplicar desconto com base na permissão do jogador
            double discountMultiplier = config.getBuyDiscountMultiplier(player);
            double discountPercentage = config.getBuyDiscountPercentage(player);
            totalPrice *= discountMultiplier;
            String itemName = getItemName(item);

            if (!economy.has(player, totalPrice)) {
                config.sendFormattedMessage(player, config.getNoMoneyMessage(), itemName, totalPrice, requestedAmount);
                config.playErrorSound(player);
                purchaseManager.cancelPurchase(player.getUniqueId(), "insufficient_funds", shopSign);
                return;
            }

            if (player.getInventory().firstEmpty() == -1) {
                config.sendFormattedMessage(player, config.getFullInventoryMessage(), itemName, totalPrice, requestedAmount);
                config.playErrorSound(player);
                purchaseManager.cancelPurchase(player.getUniqueId(), "full_inventory", shopSign);
                return;
            }

            hologramManager.showHologram(player, config.getBuyingMessage(), 20L);
            economy.withdraw(player, totalPrice);
            player.getInventory().addItem(item);
            config.sendFormattedMessage(player, config.getPurchaseSuccessMessage(), itemName, totalPrice, requestedAmount);
            if (discountPercentage > 0) {
                config.sendMessage(player, config.formatDiscountMessage(config.getDiscountAppliedMessage(), discountPercentage));
            }
            config.playSuccessSound(player);
            logPurchase(player, itemName, requestedAmount, totalPrice);
            purchaseManager.getPendingPurchases().remove(player.getUniqueId());
        });
    }

    private String getItemName(ItemStack item) {
        return item.getItemMeta() != null && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : item.getType().name();
    }

    private void logPurchase(Player player, String itemName, int amount, double totalPrice) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        String timestamp = dateFormat.format(new Date());
        String logMessage = String.format("[%s] %s comprou %d %s por %.1f", timestamp, player.getName(), amount, itemName, totalPrice);
        plugin.getPurchaseLogger().info(logMessage);
    }
}