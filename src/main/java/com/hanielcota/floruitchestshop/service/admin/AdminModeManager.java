package com.hanielcota.floruitchestshop.service.admin;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class AdminModeManager {

    private final Set<UUID> adminModePlayers = new HashSet<>();

    public void toggleAdminMode(UUID playerId) {
        if (adminModePlayers.contains(playerId)) {
            adminModePlayers.remove(playerId);
            return;
        }
        adminModePlayers.add(playerId);
    }

    public boolean isInAdminMode(UUID playerId) {
        return adminModePlayers.contains(playerId);
    }
}