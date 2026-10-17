package dev.solrevival.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vanilla starts the chewing sounds and particles a fixed share into {@link Consumable#consumeTicks()}.
 * When eating has been stretched, that would leave the first part of a long meal silent, so measure
 * from the real duration instead.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @WrapOperation(
            method = "onUseTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/component/Consumable;shouldEmitParticlesAndSounds(I)Z"))
    private boolean solrevival$chewThroughLongerMeals(Consumable consumable, int ticksRemaining, Operation<Boolean> original,
                                                      Level level, LivingEntity entity, int ticksRemainingArg) {
        int duration = ((ItemStack) (Object) this).getUseDuration(entity);
        if (duration <= consumable.consumeTicks()) {
            return original.call(consumable, ticksRemaining);
        }
        int usedFor = duration - ticksRemaining;
        return usedFor > (int) (duration * 0.21875F) && ticksRemaining % 4 == 0;
    }
}
