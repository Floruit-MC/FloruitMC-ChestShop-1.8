package com.hanielcota.floruitchestshop.repository;

import com.hanielcota.floruitchestshop.model.ShopSign;
import org.bukkit.Location;

import java.util.Map;

public interface ShopSignRepository {
    void addShop(ShopSign shop);

    ShopSign getShop(Location location);

    void loadShops();

    void saveShops();

    void close();

    Map<Location, ShopSign> getShops();
}