package com.hanielcota.floruitchestshop.service.shop;

import com.hanielcota.floruitchestshop.config.ShopConfig;
import com.hanielcota.floruitchestshop.model.ShopSign;
import com.hanielcota.floruitchestshop.repository.ShopSignRepository;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ShopSignManager {

    private final ShopSignRepository repository;
    private final Map<UUID, ItemStack> pendingCreations = new HashMap<>();
    private final Map<UUID, Sign> pendingSigns = new HashMap<>();

    public void startCreationProcess(Player player, ItemStack item) {
        pendingCreations.put(player.getUniqueId(), item);
    }

    public void setPendingSign(Player player, Sign sign) {
        pendingSigns.put(player.getUniqueId(), sign);
    }

    public void processChatInput(Player player, String message, String shopName, ShopConfig config) {
        UUID playerId = player.getUniqueId();
        if (!pendingCreations.containsKey(playerId) || !pendingSigns.containsKey(playerId)) {
            return;
        }

        String[] parts = message.split(" ");
        if (parts.length != 3) {
            player.sendMessage(config.getInvalidFormatMessage());
            config.playErrorSound(player);
            return;
        }

        double buyPrice;
        double sellPrice;
        int amount;
        try {
            buyPrice = Double.parseDouble(parts[0]);
            sellPrice = Double.parseDouble(parts[1]);
            amount = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(config.getInvalidNumberMessage());
            config.playErrorSound(player);
            return;
        }

        if (buyPrice < 0 || sellPrice < 0) {
            player.sendMessage(config.getInvalidPriceMessage());
            config.playErrorSound(player);
            return;
        }

        if (amount < 1 || amount > 64) {
            player.sendMessage(config.getInvalidAmountMessage());
            config.playErrorSound(player);
            return;
        }

        ItemStack item = pendingCreations.get(playerId);
        item.setAmount(amount);
        Sign sign = pendingSigns.get(playerId);
        ShopSign shopSign = new ShopSign(item, buyPrice, sellPrice, sign.getLocation());
        shopSign.applyToSign(sign, shopName);
        repository.addShop(shopSign);

        pendingCreations.remove(playerId);
        pendingSigns.remove(playerId);
        config.sendFormattedMessage(player, config.getShopCreatedMessage(), getItemName(item), buyPrice, amount);
        config.playSuccessSound(player);
    }

    private String getItemName(ItemStack item) {
        return item.getItemMeta() != null && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : item.getType().name();
    }
}