package dev.solrevival.mixin;

import dev.solrevival.DietManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Food that has lost value takes longer to eat. Runs on both sides; the client uses the meals the
 * server sent, so the eating animation and the server agree on when the food is done.
 */
@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "getUseDuration", at = @At("RETURN"), cancellable = true)
    private void solrevival$slowerEating(ItemStack stack, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        if (user instanceof Player player && stack.has(DataComponents.FOOD)) {
            cir.setReturnValue(DietManager.eatingTicks(player, stack, cir.getReturnValueI()));
        }
    }
}
