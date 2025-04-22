package com.hanielcota.floruitchestshop.service.shop;

import com.hanielcota.floruitchestshop.FloruitChestShop;
import com.hanielcota.floruitchestshop.model.ShopSign;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.*;

public class ShopPurchaseManager {
    @Getter
    private final Map<UUID, ShopSign> pendingPurchases = new HashMap<>();
    private final FloruitChestShop plugin;

    public ShopPurchaseManager(FloruitChestShop plugin) {
        this.plugin = plugin;
    }

    public void startPurchase(Player player, ShopSign shopSign) {
        UUID playerId = player.getUniqueId();
        pendingPurchases.put(playerId, shopSign);

        // Enviar mensagem inicial
        plugin.getShopConfig().sendMessage(player, plugin.getShopConfig().formatQuantityMessage(
                plugin.getShopConfig().getEnterQuantityMessage(), shopSign.getAmount()));

        // Calcular ticks com base no tempo configurado (segundos * 20 ticks)
        long timeoutTicks = plugin.getShopConfig().getPurchaseTimeoutSeconds() * 20L;

        // Timeout da compra
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!pendingPurchases.containsKey(playerId)) return;
                Player onlinePlayer = plugin.getServer().getPlayer(playerId);
                if (onlinePlayer != null) {
                    plugin.getShopConfig().sendMessage(onlinePlayer, plugin.getShopConfig().getPurchaseTimeoutMessage());
                    plugin.getShopConfig().playErrorSound(onlinePlayer);
                }
                logCancellation(playerId, "timeout", shopSign);
                pendingPurchases.remove(playerId);
            }
        }.runTaskLater(plugin, timeoutTicks);
    }

    public void cancelPurchase(UUID playerId, String reason, ShopSign shopSign) {
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null) {
            plugin.getShopConfig().sendMessage(player, plugin.getShopConfig().getPurchaseCancelledMessage());
            plugin.getShopConfig().playErrorSound(player);
        }
        logCancellation(playerId, reason, shopSign);
        pendingPurchases.remove(playerId);
    }

    private void logCancellation(UUID playerId, String reason, ShopSign shopSign) {
        Player player = plugin.getServer().getPlayer(playerId);
        String playerName = player != null ? player.getName() : "Unknown";
        String itemName = shopSign.getItemClone().getItemMeta() != null && shopSign.getItemClone().getItemMeta().hasDisplayName()
                ? shopSign.getItemClone().getItemMeta().getDisplayName()
                : shopSign.getItemClone().getType().name();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo")); // Fuso horário de Brasília (UTC-3)
        String timestamp = dateFormat.format(new Date());
        String logMessage = String.format("[%s] %s cancelou a compra de %s (%s)", timestamp, playerName, itemName, reason);
        plugin.getPurchaseLogger().info(logMessage);
    }
}