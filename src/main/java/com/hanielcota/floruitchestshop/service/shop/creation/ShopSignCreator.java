package com.hanielcota.floruitchestshop.service.shop.creation;

import com.hanielcota.floruitchestshop.config.ShopConfig;
import com.hanielcota.floruitchestshop.service.shop.ShopSignManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class ShopSignCreator {
    private final ShopSignManager manager;
    private final ShopConfig config;

    public void startCreation(Player player) {
        if (!player.hasPermission("shopsign.create")) {
            player.sendMessage(config.getNoPermissionMessage());
            config.playErrorSound(player);
            return;
        }

        if (!player.isSneaking()) {
            player.sendMessage(config.getNoShiftMessage());
            config.playErrorSound(player);
            return;
        }

        ItemStack item = player.getInventory().getItemInHand();
        if (item.getType().equals(Material.AIR)) {
            player.sendMessage(config.getNoItemMessage());
            config.playErrorSound(player);
            return;
        }

        manager.startCreationProcess(player, item.clone());
        player.sendMessage(config.getInputPromptMessage());
    }
}