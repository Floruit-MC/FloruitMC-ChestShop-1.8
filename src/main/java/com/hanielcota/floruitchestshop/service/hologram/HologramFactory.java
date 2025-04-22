package com.hanielcota.floruitchestshop.service.hologram;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class HologramFactory {

    public ArmorStand createHologram(Player player, String message) {
        Location location = player.getLocation().add(0, 2.0, 0); // 2 blocos acima da cabeça
        ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setCustomName(message.replace("&", "§"));
        armorStand.setCustomNameVisible(true);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setSmall(true);
        armorStand.setMarker(true);
        return armorStand;
    }
}