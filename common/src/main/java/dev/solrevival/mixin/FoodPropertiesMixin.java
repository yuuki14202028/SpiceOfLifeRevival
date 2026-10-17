package dev.solrevival.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.solrevival.DietManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * When a player finishes eating, vanilla calls {@code player.getFoodData().eat(this)} inside
 * {@link FoodProperties#onConsume}. We swap in reduced food values there, on the server only.
 * WrapOperation (rather than Redirect) lets other mods hook the same call.
 */
@Mixin(FoodProperties.class)
public abstract class FoodPropertiesMixin {

    @WrapOperation(
            method = "onConsume",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
    private void solrevival$applyDiminishingReturns(FoodData foodData, FoodProperties food, Operation<Void> original,
                                                    Level level, LivingEntity entity, ItemStack stack, Consumable consumable) {
        if (entity instanceof ServerPlayer player) {
            food = DietManager.onEat(player, stack, food);
        } else if (level.isClientSide() && entity instanceof Player player && player.isLocalPlayer()) {
            DietManager.onEatClient(stack);
        }
        original.call(foodData, food);
    }
}
