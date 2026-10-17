package dev.solrevival.mixin;

import dev.solrevival.FoodHistory;
import dev.solrevival.FoodHistoryHolder;
import dev.solrevival.SolConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gives every player a meal history and saves it with the player's data. */
@Mixin(Player.class)
public abstract class PlayerMixin implements FoodHistoryHolder {

    @Unique
    private static final String SOLREVIVAL$KEY = "solrevival:food_history";

    @Unique
    private final FoodHistory solrevival$foodHistory = new FoodHistory();

    @Override
    public FoodHistory solrevival$getFoodHistory() {
        return solrevival$foodHistory;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void solrevival$save(ValueOutput output, CallbackInfo ci) {
        output.store(SOLREVIVAL$KEY, FoodHistory.CODEC, solrevival$foodHistory.toList());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void solrevival$load(ValueInput input, CallbackInfo ci) {
        input.read(SOLREVIVAL$KEY, FoodHistory.CODEC).ifPresent(ids -> solrevival$foodHistory.setFrom(ids, SolConfig.historySize));
    }
}
