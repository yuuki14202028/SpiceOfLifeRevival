package dev.solrevival.client;

import dev.solrevival.ClientDietState;
import dev.solrevival.DietManager;
import dev.solrevival.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Adds one short line to a food's tooltip once it has lost value, e.g. "Value: 50%".
 * Food at full value gets nothing, and so does every food until the server has sent its rules.
 * With AppleSkin installed this stays out of the way: AppleSkin already shows the reduced values.
 */
public final class FoodTooltip {

    private static final boolean APPLESKIN_LOADED = Services.PLATFORM.isModLoaded("appleskin");

    private FoodTooltip() {
    }

    public static void append(ItemStack stack, @Nullable Player player, List<Component> lines) {
        if (APPLESKIN_LOADED || player == null || !stack.has(DataComponents.FOOD) || ClientDietState.rules() == null) return;

        double multiplier = DietManager.currentMultiplier(player, stack);
        if (multiplier >= 1.0) return;

        int percent = (int) Math.round(multiplier * 100);
        lines.add(Component.translatableWithFallback("tooltip.solrevival.value", "Value: %s%%", percent)
                .withStyle(percent > 0 ? ChatFormatting.YELLOW : ChatFormatting.RED));
    }
}
