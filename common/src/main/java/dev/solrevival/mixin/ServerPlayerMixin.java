package dev.solrevival.mixin;

import dev.solrevival.FoodHistory;
import dev.solrevival.FoodHistoryHolder;
import dev.solrevival.SolConfig;
import dev.solrevival.network.SyncDietPayload;
import dev.solrevival.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When a player respawns or returns from the End, the game builds a new player object and calls
 * {@code restoreFrom}. Copy the meal history across: always when leaving the End, and on death
 * only if the config says so.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void solrevival$copyHistory(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        if (restoreAll || SolConfig.keepHistoryOnDeath) {
            FoodHistoryHolder.of(this).copyFrom(FoodHistoryHolder.of(oldPlayer));
        }
    }

    /** Sends the meals to the client whenever they have changed, and once when the player object is new. */
    @Inject(method = "tick", at = @At("HEAD"))
    private void solrevival$syncHistory(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        FoodHistory history = FoodHistoryHolder.of(self);
        if (history.consumeDirty()) {
            Services.PLATFORM.sendToPlayer(self, new SyncDietPayload(history.toList(), SolConfig.rules()));
        }
    }
}
