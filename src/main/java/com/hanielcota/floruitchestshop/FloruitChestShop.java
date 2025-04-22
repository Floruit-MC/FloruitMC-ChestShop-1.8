package com.hanielcota.floruitchestshop;

import co.aikar.commands.PaperCommandManager;
import com.hanielcota.floruitchestshop.commands.ShopSignCommand;
import com.hanielcota.floruitchestshop.config.ShopConfig;
import com.hanielcota.floruitchestshop.economy.VaultEconomy;
import com.hanielcota.floruitchestshop.listener.ShopSignCreationListener;
import com.hanielcota.floruitchestshop.listener.ShopSignInteractionListener;
import com.hanielcota.floruitchestshop.repository.MySqlShopSignRepository;
import com.hanielcota.floruitchestshop.repository.ShopSignRepository;
import com.hanielcota.floruitchestshop.service.admin.AdminModeManager;
import com.hanielcota.floruitchestshop.service.hologram.HologramManager;
import com.hanielcota.floruitchestshop.service.shop.ShopPurchaseManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Getter
public final class FloruitChestShop extends JavaPlugin {

    private VaultEconomy economy;
    private ShopConfig shopConfig;
    private ShopSignRepository shopSignRepository;
    private AdminModeManager adminModeManager;
    private PaperCommandManager commandManager;
    private HologramManager hologramManager;
    private ShopPurchaseManager shopPurchaseManager;
    private Logger purchaseLogger;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        shopConfig = new ShopConfig(this);
        adminModeManager = new AdminModeManager();
        hologramManager = new HologramManager(this);
        shopPurchaseManager = new ShopPurchaseManager(this);
        setupPurchaseLogger();

        if (!setupEconomy()) {
            getLogger().severe("Vault não encontrado! Desativando plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupRepository()) {
            getLogger().severe("Falha ao inicializar o repositório! Desativando plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        shopSignRepository.loadShops();
        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        if (shopSignRepository == null) return;
        shopSignRepository.saveShops();
        shopSignRepository.close();
    }

    private boolean setupEconomy() {
        economy = new VaultEconomy(this);
        return economy.isEnabled();
    }

    private boolean setupRepository() {
        try {
            shopSignRepository = new MySqlShopSignRepository(this);
            return true;
        } catch (Exception e) {
            getLogger().severe("Erro ao configurar o repositório MySQL: " + e.getMessage());
            return false;
        }
    }

    private void setupPurchaseLogger() {
        try {
            File logDir = new File(getDataFolder(), "logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            purchaseLogger = Logger.getLogger("PurchaseHistory");
            FileHandler fileHandler = new FileHandler(logDir + "/purchase_history.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            purchaseLogger.addHandler(fileHandler);
            purchaseLogger.setUseParentHandlers(false); // Evita duplicação no console
        } catch (Exception e) {
            getLogger().severe("Erro ao configurar o logger de compras: " + e.getMessage());
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ShopSignCreationListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopSignInteractionListener(this), this);
    }

    private void registerCommands() {
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new ShopSignCommand(this));
    }
}