package com.hanielcota.floruitchestshop.repository;

import com.hanielcota.floruitchestshop.FloruitChestShop;
import com.hanielcota.floruitchestshop.model.ShopSign;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Getter
public class SqliteShopSignRepository implements ShopSignRepository {

    private final FloruitChestShop plugin;
    private final HikariDataSource dataSource;
    private final Map<Location, ShopSign> shops = new HashMap<>();

    public SqliteShopSignRepository(FloruitChestShop plugin) {
        this.plugin = plugin;
        this.dataSource = setupDataSource();

        if (dataSource != null) {
            createTable();
            return;
        }
        throw new RuntimeException("Falha ao inicializar o HikariDataSource");
    }

    private HikariDataSource setupDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/shops.db");
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000); // 30 segundos
            config.setLeakDetectionThreshold(60000); // Detectar vazamentos após 60 segundos
            return new HikariDataSource(config);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao configurar o HikariDataSource: " + e.getMessage());
            return null;
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS shops (" +
                "world TEXT NOT NULL, " +
                "x DOUBLE NOT NULL, " +
                "y DOUBLE NOT NULL, " +
                "z DOUBLE NOT NULL, " +
                "buy_price DOUBLE NOT NULL, " +
                "sell_price DOUBLE NOT NULL, " +
                "item TEXT NOT NULL, " +
                "PRIMARY KEY (world, x, y, z))";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao criar tabela: " + e.getMessage());
        }
    }

    @Override
    public void addShop(ShopSign shop) {
        shops.put(shop.getLocation(), shop);
        saveShopToDatabase(shop);
    }

    @Override
    public ShopSign getShop(Location location) {
        return shops.get(location);
    }

    @Override
    public void loadShops() {
        if (dataSource == null) return;

        String sql = "SELECT world, x, y, z, buy_price, sell_price, item FROM shops";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Location location = new Location(
                        Bukkit.getWorld(rs.getString("world")),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z")
                );
                double buyPrice = rs.getDouble("buy_price");
                double sellPrice = rs.getDouble("sell_price");
                ItemStack item = deserializeItem(rs.getString("item"));
                shops.put(location, new ShopSign(item, buyPrice, sellPrice, location));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao carregar lojas: " + e.getMessage());
        }
    }

    @Override
    public void saveShops() {
        if (dataSource == null) return;

        for (ShopSign shop : shops.values()) {
            saveShopToDatabase(shop);
        }
    }

    private void saveShopToDatabase(ShopSign shop) {
        String sql = "INSERT OR REPLACE INTO shops (world, x, y, z, buy_price, sell_price, item) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Location loc = shop.getLocation();
            pstmt.setString(1, loc.getWorld().getName());
            pstmt.setDouble(2, loc.getX());
            pstmt.setDouble(3, loc.getY());
            pstmt.setDouble(4, loc.getZ());
            pstmt.setDouble(5, shop.getBuyPrice());
            pstmt.setDouble(6, shop.getSellPrice());
            pstmt.setString(7, serializeItem(shop.getItemClone()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar loja: " + e.getMessage());
        }
    }

    private String serializeItem(ItemStack item) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", item);
        return config.saveToString();
    }

    private ItemStack deserializeItem(String yaml) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(yaml);
            return config.getItemStack("item");
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().severe("Erro ao deserializar item: " + e.getMessage());
            return new ItemStack(org.bukkit.Material.AIR);
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public Map<Location, ShopSign> getShops() {
        return shops;
    }
}