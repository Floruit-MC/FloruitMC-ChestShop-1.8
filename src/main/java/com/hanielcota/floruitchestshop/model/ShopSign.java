package com.hanielcota.floruitchestshop.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@AllArgsConstructor
@Getter
public class ShopSign {
    private final ItemStack item;
    private final double buyPrice;
    private final double sellPrice;
    private final Location location;

    public void applyToSign(Sign sign, String shopName) {
        sign.setLine(0, shopName);
        ItemMeta meta = item.getItemMeta();
        String displayName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : item.getType().name();
        sign.setLine(1, displayName);
        String formattedBuyPrice = String.format("%.1f", buyPrice);
        String formattedSellPrice = String.format("%.1f", sellPrice);

        sign.setLine(2, "§2C §0§l" + formattedBuyPrice + " §7| §cV §0§l" + formattedSellPrice);
        sign.setLine(3, "Quantidade: §5§l" + item.getAmount());
        sign.update();
    }

    public ItemStack getItemClone() {
        return item.clone();
    }

    public int getAmount() {
        return item.getAmount();
    }
}