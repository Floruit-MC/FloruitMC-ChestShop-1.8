package com.hanielcota.floruitchestshop.service.shop.transaction;

import com.hanielcota.floruitchestshop.FloruitChestShop;
import com.hanielcota.floruitchestshop.config.ShopConfig;
import com.hanielcota.floruitchestshop.economy.VaultEconomy;
import com.hanielcota.floruitchestshop.model.ShopSign;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@RequiredArgsConstructor
public class ShopSellService {

    private final VaultEconomy economy;
    private final ShopConfig config;
    private final FloruitChestShop plugin;

    public ShopSellService(FloruitChestShop plugin) {
        this.economy = plugin.getEconomy();
        this.config = plugin.getShopConfig();
        this.plugin = plugin;
    }

    public void sell(Player player, ShopSign shopSign, boolean sellAll) {
        ItemStack itemToSell = shopSign.getItemClone();
        double unitPrice = shopSign.getSellPrice() / shopSign.getAmount();
        String itemName = getItemName(itemToSell);
        PlayerInventory inventory = player.getInventory();

        int amountToSell;
        int availableAmount = countMatchingItems(inventory, itemToSell);
        if (sellAll) {
            amountToSell = availableAmount;
            if (amountToSell == 0) {
                config.sendFormattedMessage(player, config.getNoItemsMessage(), itemName, 0, 0);
                config.playErrorSound(player);
                return;
            }
        } else {
            amountToSell = shopSign.getAmount();
            if (availableAmount < amountToSell) {
                config.sendFormattedMessage(player, config.getNoItemsMessage(), itemName, 0, amountToSell);
                config.playErrorSound(player);
                return;
            }
        }

        // Remover os itens do inventário
        removeItemsFromInventory(inventory, itemToSell, amountToSell);

        double totalPrice = unitPrice * amountToSell;
        // Aplicar bônus com base na permissão do jogador
        double bonusMultiplier = config.getSellBonusMultiplier(player);
        double bonusPercentage = config.getSellBonusPercentage(player);
        totalPrice *= bonusMultiplier;

        economy.deposit(player, totalPrice);
        if (sellAll) {
            config.sendFormattedMessage(player, config.getSellAllMessage(), itemName, totalPrice, amountToSell);
        } else {
            config.sendFormattedMessage(player, config.getSellSuccessMessage(), itemName, totalPrice, amountToSell);
        }
        if (bonusPercentage > 0) {
            config.sendMessage(player, config.formatBonusMessage(config.getBonusAppliedMessage(), bonusPercentage));
        }
        config.playSuccessSound(player);
        logSell(player, itemName, amountToSell, totalPrice);
    }

    private int countMatchingItems(PlayerInventory inventory, ItemStack itemToMatch) {
        int total = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;
            if (config.isIgnoreItemMetadataOnSell()) {
                // Comparar apenas o tipo do material, ignorando metadados
                if (item.getType() == itemToMatch.getType()) {
                    total += item.getAmount();
                }
            } else {
                // Comparar usando isSimilar (inclui metadados)
                if (item.isSimilar(itemToMatch)) {
                    total += item.getAmount();
                }
            }
        }
        return total;
    }

    private void removeItemsFromInventory(PlayerInventory inventory, ItemStack itemToMatch, int amountToRemove) {
        int remaining = amountToRemove;
        for (int i = 0; i < inventory.getSize() && remaining > 0; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) continue;

            boolean matches;
            if (config.isIgnoreItemMetadataOnSell()) {
                matches = item.getType() == itemToMatch.getType();
            } else {
                matches = item.isSimilar(itemToMatch);
            }

            if (!matches) continue;

            int itemAmount = item.getAmount();
            if (itemAmount <= remaining) {
                inventory.setItem(i, null);
                remaining -= itemAmount;
            } else {
                item.setAmount(itemAmount - remaining);
                inventory.setItem(i, item);
                remaining = 0;
            }
        }
    }

    private String getItemName(ItemStack item) {
        return item.getItemMeta() != null && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : item.getType().name();
    }


    private void logSell(Player player, String itemName, int amount, double totalPrice) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        String timestamp = dateFormat.format(new Date());
        String logMessage = String.format("[%s] %s vendeu %d %s por %.1f", timestamp, player.getName(), amount, itemName, totalPrice);
        plugin.getPurchaseLogger().info(logMessage);
    }
}