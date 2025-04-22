package com.hanielcota.floruitchestshop.listener;

import com.hanielcota.floruitchestshop.FloruitChestShop;
import com.hanielcota.floruitchestshop.model.ShopSign;
import com.hanielcota.floruitchestshop.service.shop.transaction.ShopPurchaseService;
import com.hanielcota.floruitchestshop.service.shop.transaction.ShopSellService;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@RequiredArgsConstructor
public class ShopSignInteractionListener implements Listener {

    private final FloruitChestShop plugin;
    private final ShopPurchaseService purchaseService;
    private final ShopSellService sellService;

    public ShopSignInteractionListener(FloruitChestShop plugin) {
        this.plugin = plugin;
        this.purchaseService = new ShopPurchaseService(plugin);
        this.sellService = new ShopSellService(plugin);
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handlePurchase(event);
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleSell(event);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getShopPurchaseManager().getPendingPurchases().containsKey(event.getPlayer().getUniqueId())) return;
        purchaseService.processPurchase(event.getPlayer(), event.getMessage());
        event.setCancelled(true);
    }

    private void handlePurchase(PlayerInteractEvent event) {
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        if (!sign.getLine(0).contains(plugin.getShopConfig().getShopName())) return;

        if (plugin.getAdminModeManager().isInAdminMode(event.getPlayer().getUniqueId())) return;

        ShopSign shopSign = plugin.getShopSignRepository().getShop(sign.getLocation());
        if (shopSign == null) {
            plugin.getShopConfig().sendMessage(event.getPlayer(), "§cEsta loja não está registrada!");
            plugin.getShopConfig().playErrorSound(event.getPlayer());
            return;
        }

        purchaseService.startPurchase(event.getPlayer(), shopSign);
    }

    private void handleSell(PlayerInteractEvent event) {
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        if (!sign.getLine(0).contains(plugin.getShopConfig().getShopName())) return;

        if (plugin.getAdminModeManager().isInAdminMode(event.getPlayer().getUniqueId())) return;

        ShopSign shopSign = plugin.getShopSignRepository().getShop(sign.getLocation());
        if (shopSign == null) {
            plugin.getShopConfig().sendMessage(event.getPlayer(), "§cEsta loja não está registrada!");
            plugin.getShopConfig().playErrorSound(event.getPlayer());
            return;
        }

        boolean sellAll = event.getPlayer().isSneaking();
        sellService.sell(event.getPlayer(), shopSign, sellAll);
    }
}