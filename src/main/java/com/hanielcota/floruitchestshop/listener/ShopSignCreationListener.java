package com.hanielcota.floruitchestshop.listener;

import com.hanielcota.floruitchestshop.FloruitChestShop;
import com.hanielcota.floruitchestshop.service.shop.creation.ShopSignCreator;
import com.hanielcota.floruitchestshop.service.shop.ShopSignManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@RequiredArgsConstructor
public class ShopSignCreationListener implements Listener {

    private final FloruitChestShop plugin;
    private final ShopSignManager manager;
    private final ShopSignCreator creator;

    public ShopSignCreationListener(FloruitChestShop plugin) {
        this.plugin = plugin;
        this.manager = new ShopSignManager(plugin.getShopSignRepository());
        this.creator = new ShopSignCreator(manager, plugin.getShopConfig());
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;

        Player player = event.getPlayer();
        if (!plugin.getAdminModeManager().isInAdminMode(player.getUniqueId())) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        manager.setPendingSign(player, sign);
        creator.startCreation(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        manager.processChatInput(
                event.getPlayer(),
                event.getMessage(),
                plugin.getShopConfig().getShopName(),
                plugin.getShopConfig());
    }
}