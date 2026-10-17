package dev.solrevival;

import dev.solrevival.network.SyncDietPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class SolRevivalFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CommonClass.init();
        PayloadTypeRegistry.clientboundPlay().register(SyncDietPayload.TYPE, SyncDietPayload.STREAM_CODEC);
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> SolCommands.register(dispatcher));
    }
}
