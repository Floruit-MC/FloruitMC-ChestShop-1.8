package com.hanielcota.floruitchestshop.service.hologram;

import com.hanielcota.floruitchestshop.FloruitChestShop;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class HologramAnimator {
    private final FloruitChestShop plugin;

    public void animate(ArmorStand hologram, String message, long durationTicks) {
        final String[] colors = {"§e", "§d", "§a", "§c", "§a"}; // Amarelo -> Roxo -> Verde -> Azul Escuro -> Verde

        new BukkitRunnable() {
            int tick = 0;
            int colorIndex = 0;

            @Override
            public void run() {
                if (hologram.isDead()) {
                    cancel();
                    return;
                }

                // Atualiza a cor do holograma
                hologram.setCustomName(colors[colorIndex] + message.replace("&", "§").substring(2));
                colorIndex = (colorIndex + 1) % colors.length;
                tick++;

                // Calcula quantas atualizações de cor cabem na duração total
                // Cada cor deve ser exibida por (durationTicks / colors.length) ticks
                if (tick >= durationTicks / colors.length) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, durationTicks / colors.length); // Cada cor é exibida por (durationTicks / colors.length) ticks
    }
}