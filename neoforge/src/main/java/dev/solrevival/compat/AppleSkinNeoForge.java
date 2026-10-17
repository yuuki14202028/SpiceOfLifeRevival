package dev.solrevival.compat;

import net.neoforged.neoforge.common.NeoForge;
import squeek.appleskin.api.event.FoodValuesEvent;

/**
 * Makes AppleSkin's tooltip and HUD previews show diminished food values.
 * Only load this class when AppleSkin is present.
 */
public final class AppleSkinNeoForge {

    private AppleSkinNeoForge() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(FoodValuesEvent.class, event -> event.modifiedFoodProperties =
                AppleSkinCompat.predict(event.player, event.itemStack, event.modifiedFoodProperties));
    }
}
