package dev.solrevival.platform.services;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public interface IPlatformHelper {

    /** "Fabric" or "NeoForge". */
    String getPlatformName();

    boolean isModLoaded(String modId);

    /** The game's config folder. */
    Path getConfigDir();

    /** Sends a payload to a player, but only if their client has this mod and can receive it. */
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
}
