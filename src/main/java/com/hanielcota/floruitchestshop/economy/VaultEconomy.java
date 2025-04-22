package com.hanielcota.floruitchestshop.economy;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class VaultEconomy {

    private Economy economy;
    private final JavaPlugin plugin;

    public VaultEconomy(JavaPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }

        economy = rsp.getProvider();
    }

    public boolean isEnabled() {
        return economy != null;
    }

    public boolean has(Player player, double amount) {
        if (!isEnabled()) return false;
        return economy.has(player, amount);
    }

    public void withdraw(Player player, double amount) {
        if (!isEnabled()) return;
        economy.withdrawPlayer(player, amount);
    }

    public void deposit(Player player, double amount) {
        if (!isEnabled()) return;
        economy.depositPlayer(player, amount);
    }
}