package com.hanielcota.floruitchestshop.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import com.hanielcota.floruitchestshop.FloruitChestShop;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@CommandAlias("shopsign")
@CommandPermission("floruitchestshop.create")
@AllArgsConstructor
public class ShopSignCommand extends BaseCommand {

    private final FloruitChestShop plugin;

    @Subcommand("admin")
    public void onAdmin(Player player) {
        plugin.getAdminModeManager().toggleAdminMode(player.getUniqueId());

        if (plugin.getAdminModeManager().isInAdminMode(player.getUniqueId())) {
            plugin.getShopConfig().sendMessage(player, plugin.getShopConfig().getAdminModeEnabledMessage());
            plugin.getShopConfig().playSuccessSound(player);
            return;
        }

        plugin.getShopConfig().sendMessage(player, plugin.getShopConfig().getAdminModeDisabledMessage());
        plugin.getShopConfig().playSuccessSound(player);
    }
}