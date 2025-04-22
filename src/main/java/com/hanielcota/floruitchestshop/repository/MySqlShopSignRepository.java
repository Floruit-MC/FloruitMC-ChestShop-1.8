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
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Getter
public class MySqlShopSignRepository implements ShopSignRepository {
    private final FloruitChestShop plugin;
    private final HikariDataSource dataSource;
    private final Map<Location, ShopSign> shops = new HashMap<>();

    public MySqlShopSignRepository(FloruitChestShop plugin) {
        this.plugin = plugin;
        this.dataSource = setupDataSource();
        if (dataSource == null) {
            throw new RuntimeException("Falha ao inicializar o HikariDataSource para MySQL");
        }
        createTable();
    }

    private HikariDataSource setupDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            String host = plugin.getConfig().getString("mysql.host", "localhost");
            int port = plugin.getConfig().getInt("mysql.port", 3306);
            String database = plugin.getConfig().getString("mysql.database", "minecraft");
            String username = plugin.getConfig().getString("mysql.username", "root");
            String password = plugin.getConfig().getString("mysql.password", "");

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false");
            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000);
            config.setLeakDetectionThreshold(60000);
            return new HikariDataSource(config);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao configurar o HikariDataSource para MySQL: " + e.getMessage());
            return null;
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS shops (" +
                "world VARCHAR(255) NOT NULL, " +
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
            plugin.getLogger().severe("Erro ao criar tabela no MySQL: " + e.getMessage());
        }
    }

    @Override
    public void addShop(ShopSign shop) {
        shops.put(shop.getLocation(), shop);
        // Executar a operação de salvamento de forma assíncrona
        new BukkitRunnable() {
            @Override
            public void run() {
                saveShopToDatabase(shop);
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    public ShopSign getShop(Location location) {
        return shops.get(location);
    }

    @Override
    public void loadShops() {
        if (dataSource == null) return;

        // Carregar de forma assíncrona
        new BukkitRunnable() {
            @Override
            public void run() {
                String sql = "SELECT world, x, y, z, buy_price, sell_price, item FROM shops";
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    Map<Location, ShopSign> loadedShops = new HashMap<>();
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
                        loadedShops.put(location, new ShopSign(item, buyPrice, sellPrice, location));
                    }

                    // Sincronizar com o main thread para atualizar o mapa
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            shops.clear();
                            shops.putAll(loadedShops);
                        }
                    }.runTask(plugin);
                } catch (SQLException e) {
                    plugin.getLogger().severe("Erro ao carregar lojas do MySQL: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    public void saveShops() {
        if (dataSource == null) return;

        // Salvar de forma assíncrona
        new BukkitRunnable() {
            @Override
            public void run() {
                for (ShopSign shop : shops.values()) {
                    saveShopToDatabase(shop);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void saveShopToDatabase(ShopSign shop) {
        String sql = "INSERT INTO shops (world, x, y, z, buy_price, sell_price, item) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE buy_price = ?, sell_price = ?, item = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Location loc = shop.getLocation();
            String itemSerialized = serializeItem(shop.getItemClone());
            pstmt.setString(1, loc.getWorld().getName());
            pstmt.setDouble(2, loc.getX());
            pstmt.setDouble(3, loc.getY());
            pstmt.setDouble(4, loc.getZ());
            pstmt.setDouble(5, shop.getBuyPrice());
            pstmt.setDouble(6, shop.getSellPrice());
            pstmt.setString(7, itemSerialized);

            pstmt.setDouble(8, shop.getBuyPrice());
            pstmt.setDouble(9, shop.getSellPrice());
            pstmt.setString(10, itemSerialized);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar loja no MySQL: " + e.getMessage());
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
}