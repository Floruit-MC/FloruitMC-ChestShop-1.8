package com.hanielcota.floruitchestshop.config;

import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class ShopConfig {
    private final JavaPlugin plugin;
    private final String shopName;
    private final String noPermissionMessage;
    private final String noShiftMessage;
    private final String noItemMessage;
    private final String inputPromptMessage;
    private final String invalidFormatMessage;
    private final String invalidNumberMessage;
    private final String invalidPriceMessage;
    private final String invalidAmountMessage;
    private final String shopCreatedMessage;
    private final String noMoneyMessage;
    private final String fullInventoryMessage;
    private final String purchaseSuccessMessage;
    private final String noItemsMessage;
    private final String sellSuccessMessage;
    private final String adminModeEnabledMessage;
    private final String adminModeDisabledMessage;
    private final String buyingMessage;
    private final String enterQuantityMessage;
    private final String invalidQuantityMessage;
    private final String maxQuantityMessage;
    private final String purchaseCancelledMessage;
    private final String sellAllMessage;
    private final String purchaseTimeoutMessage;
    private final String purchaseTimeoutWarningMessage;
    private final String bonusAppliedMessage;
    private final String discountAppliedMessage;
    private final int purchaseTimeoutSeconds;
    private final List<Bonus> sellBonuses;
    private final List<Discount> buyDiscounts;
    private final boolean ignoreItemMetadataOnSell;

    public ShopConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        shopName = config.getString("shop-name", "Loja do Servidor");
        noPermissionMessage = config.getString("messages.no-permission", "&cVocê não tem permissão!");
        noShiftMessage = config.getString("messages.no-shift", "&cUse shift enquanto segura o item!");
        noItemMessage = config.getString("messages.no-item", "&cSegure um item na mão!");
        inputPromptMessage = config.getString("messages.input-prompt", "&eDigite no chat: [preço de compra] [preço de venda] [quantidade]");
        invalidFormatMessage = config.getString("messages.invalid-format", "&cFormato inválido! Use: [compra] [venda] [quantidade]");
        invalidNumberMessage = config.getString("messages.invalid-number", "&cNúmeros inválidos!");
        invalidPriceMessage = config.getString("messages.invalid-price", "&cPreços não podem ser negativos!");
        invalidAmountMessage = config.getString("messages.invalid-amount", "&cQuantidade deve ser entre 1 e 64!");
        shopCreatedMessage = config.getString("messages.shop-created", "&aLoja criada para {item}!");
        noMoneyMessage = config.getString("messages.no-money", "&cVocê não tem {price} para comprar {amount} {item}!");
        fullInventoryMessage = config.getString("messages.full-inventory", "&cSeu inventário está cheio para {amount} {item}!");
        purchaseSuccessMessage = config.getString("messages.purchase-success", "&aVocê comprou {amount} {item} por {price}!");
        noItemsMessage = config.getString("messages.no-items", "&cVocê não tem {amount} {item} para vender!");
        sellSuccessMessage = config.getString("messages.sell-success", "&aVocê vendeu {amount} {item} por {price}!");
        adminModeEnabledMessage = config.getString("messages.admin-mode-enabled", "&aModo administrador ativado!");
        adminModeDisabledMessage = config.getString("messages.admin-mode-disabled", "&eModo administrador desativado!");
        buyingMessage = config.getString("messages.buying", "&eEstou comprando... &d(:");
        enterQuantityMessage = config.getString("messages.enter-quantity", "&eDigite a quantidade que deseja comprar (máximo {max}):");
        invalidQuantityMessage = config.getString("messages.invalid-quantity", "&cQuantidade inválida! Digite um número entre 1 e {max}.");
        maxQuantityMessage = config.getString("messages.max-quantity", "&cVocê só pode comprar até {max} itens!");
        purchaseCancelledMessage = config.getString("messages.purchase-cancelled", "&cCompra cancelada!");
        sellAllMessage = config.getString("messages.sell-all", "&aVocê vendeu todos os {item} do seu inventário por {price}!");
        purchaseTimeoutMessage = config.getString("messages.purchase-timeout", "&cTempo esgotado! Compra cancelada.");
        purchaseTimeoutWarningMessage = config.getString("messages.purchase-timeout-warning", "&eFaltam 5 segundos para cancelar a compra!");
        bonusAppliedMessage = config.getString("messages.bonus-applied", "&aVocê recebeu um bônus de {percentage}%!");
        discountAppliedMessage = config.getString("messages.discount-applied", "&aVocê recebeu um desconto de {percentage}%!");
        purchaseTimeoutSeconds = config.getInt("purchase-timeout-seconds", 30);
        ignoreItemMetadataOnSell = config.getBoolean("ignore-item-metadata-on-sell", true);

        // Carregar bônus de venda
        sellBonuses = new ArrayList<>();
        ConfigurationSection sellBonusesSection = config.getConfigurationSection("sell-bonuses");
        if (sellBonusesSection != null) {
            for (String key : sellBonusesSection.getKeys(false)) {
                String permission = sellBonusesSection.getString(key + ".permission");
                int priority = sellBonusesSection.getInt(key + ".priority");
                double percentage = sellBonusesSection.getDouble(key + ".percentage");
                sellBonuses.add(new Bonus(permission, priority, percentage));
            }
        }

        // Carregar descontos de compra
        buyDiscounts = new ArrayList<>();
        ConfigurationSection buyDiscountsSection = config.getConfigurationSection("buy-discounts");
        if (buyDiscountsSection != null) {
            for (String key : buyDiscountsSection.getKeys(false)) {
                String permission = buyDiscountsSection.getString(key + ".permission");
                int priority = buyDiscountsSection.getInt(key + ".priority");
                double percentage = buyDiscountsSection.getDouble(key + ".percentage");
                buyDiscounts.add(new Discount(permission, priority, percentage));
            }
        }
    }

    public String formatMessage(String message, String item, double price, int amount) {
        return message.replace("{item}", item).replace("{price}", String.format("%.1f", price)).replace("{amount}", String.valueOf(amount)).replace("&", "§");
    }

    public String formatQuantityMessage(String message, int max) {
        return message.replace("{max}", String.valueOf(max)).replace("&", "§");
    }

    public String formatBonusMessage(String message, double percentage) {
        return message.replace("{percentage}", String.format("%.1f", percentage)).replace("&", "§");
    }

    public String formatDiscountMessage(String message, double percentage) {
        return message.replace("{percentage}", String.format("%.1f", percentage)).replace("&", "§");
    }

    public void sendFormattedMessage(Player player, String message, String item, double price, int amount) {
        player.sendMessage(formatMessage(message, item, price, amount));
    }

    public void sendMessage(Player player, String message) {
        player.sendMessage(message.replace("&", "§"));
    }

    public void playSuccessSound(Player player) {
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);
    }

    public void playErrorSound(Player player) {
        player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0f, 0.5f);
    }

    // Classes internas para bônus e descontos
    @Getter
    public static class Bonus {
        private final String permission;
        private final int priority;
        private final double percentage;

        public Bonus(String permission, int priority, double percentage) {
            this.permission = permission;
            this.priority = priority;
            this.percentage = percentage;
        }
    }

    @Getter
    public static class Discount {
        private final String permission;
        private final int priority;
        private final double percentage;

        public Discount(String permission, int priority, double percentage) {
            this.permission = permission;
            this.priority = priority;
            this.percentage = percentage;
        }
    }

    // Métodos para calcular bônus e descontos
    public double getSellBonusMultiplier(Player player) {
        return sellBonuses.stream().filter(bonus -> player.hasPermission(bonus.getPermission())).max(Comparator.comparingInt(Bonus::getPriority)).map(bonus -> 1.0 + (bonus.getPercentage() / 100.0)).orElse(1.0);
    }

    public double getSellBonusPercentage(Player player) {
        return sellBonuses.stream().filter(bonus -> player.hasPermission(bonus.getPermission())).max(Comparator.comparingInt(Bonus::getPriority)).map(Bonus::getPercentage).orElse(0.0);
    }

    public double getBuyDiscountMultiplier(Player player) {
        return buyDiscounts.stream().filter(discount -> player.hasPermission(discount.getPermission())).max(Comparator.comparingInt(Discount::getPriority)).map(discount -> 1.0 - (discount.getPercentage() / 100.0)).orElse(1.0);
    }

    public double getBuyDiscountPercentage(Player player) {
        return buyDiscounts.stream().filter(discount -> player.hasPermission(discount.getPermission())).max(Comparator.comparingInt(Discount::getPriority)).map(Discount::getPercentage).orElse(0.0);
    }
}