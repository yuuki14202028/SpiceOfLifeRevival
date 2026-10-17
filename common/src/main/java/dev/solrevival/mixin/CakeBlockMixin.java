package dev.solrevival.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.solrevival.DietManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Cake is eaten as a placed block and never goes through {@link FoodProperties#onConsume}, so it
 * needs its own hook. Each slice counts as eating the cake item. Candle cakes use the same method.
 */
@Mixin(CakeBlock.class)
public abstract class CakeBlockMixin {

    @WrapOperation(
            method = "eat",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"))
    private static void solrevival$diminishCake(FoodData foodData, int nutrition, float saturationModifier,
                                                Operation<Void> original,
                                                LevelAccessor level, BlockPos pos, BlockState state, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            original.call(foodData, nutrition, saturationModifier);
            return;
        }
        FoodProperties slice = new FoodProperties(nutrition,
                FoodConstants.saturationByModifier(nutrition, saturationModifier), false);
        foodData.eat(DietManager.onEat(serverPlayer, new ItemStack(Items.CAKE), slice));
    }
}
