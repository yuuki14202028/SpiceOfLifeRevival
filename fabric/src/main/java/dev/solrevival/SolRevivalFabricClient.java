package dev.solrevival;

import dev.solrevival.client.FoodTooltip;
import dev.solrevival.network.SyncDietPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class SolRevivalFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncDietPayload.TYPE,
                (payload, context) -> ClientDietState.update(payload.history(), payload.rules()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientDietState.reset());
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) ->
                FoodTooltip.append(stack, Minecraft.getInstance().player, lines));
    }
}
