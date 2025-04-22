package com.hanielcota.floruitchestshop.service.hologram;

import com.hanielcota.floruitchestshop.FloruitChestShop;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HologramManager {
    private final FloruitChestShop plugin;
    private final HologramAnimator animator;
    @Getter
    private final Map<UUID, ArmorStand> activeHolograms = new HashMap<>();

    public HologramManager(FloruitChestShop plugin) {
        this.plugin = plugin;
        this.animator = new HologramAnimator(plugin);
        startCleanupTask();
    }

    public void showHologram(Player player, String message, long durationTicks) {
        UUID playerId = player.getUniqueId();
        ArmorStand existingHologram = activeHolograms.get(playerId);

        if (existingHologram != null && !existingHologram.isDead()) {
            existingHologram.remove();
        }

        final ArmorStand hologram = createHologram(player, message);
        activeHolograms.put(playerId, hologram);

        // Animar o holograma com as cores
        animator.animate(hologram, message, durationTicks);

        // Remover o holograma após a duração
        new BukkitRunnable() {
            @Override
            public void run() {
                removeHologram(playerId);
            }
        }.runTaskLater(plugin, durationTicks);
    }

    public void removeHologram(UUID playerId) {
        ArmorStand hologram = activeHolograms.remove(playerId);
        if (hologram == null || hologram.isDead()) return;
        hologram.remove();
    }

    private ArmorStand createHologram(Player player, String message) {
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

    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                activeHolograms.entrySet().removeIf(entry -> {
                    ArmorStand hologram = entry.getValue();
                    if (hologram.isDead()) return true;
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player == null || !player.isOnline()) {
                        hologram.remove();
                        return true;
                    }
                    return false;
                });
            }
        }.runTaskTimer(plugin, 0L, 200L); // Executa a cada 10 segundos (200 ticks)
    }
}