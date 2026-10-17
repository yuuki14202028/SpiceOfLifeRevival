package dev.solrevival.compat;

import dev.solrevival.DietManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

/**
 * Loader-independent part of the AppleSkin integration. Does not touch AppleSkin classes, so it is
 * safe to load without AppleSkin installed.
 */
public final class AppleSkinCompat {

    private AppleSkinCompat() {
    }

    /** The food values this player would actually get, using the same math the server applies. */
    public static FoodProperties predict(Player player, ItemStack stack, FoodProperties food) {
        if (player == null || food == null) return food;
        return DietManager.scale(food, DietManager.currentMultiplier(player, stack));
    }
}
