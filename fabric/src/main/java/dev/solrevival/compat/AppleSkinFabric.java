package dev.solrevival.compat;

import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;

/** AppleSkin entrypoint: makes its tooltip and HUD previews show diminished food values. */
public class AppleSkinFabric implements AppleSkinApi {

    @Override
    public void registerEvents() {
        FoodValuesEvent.EVENT.register(event -> event.modifiedFoodComponent =
                AppleSkinCompat.predict(event.player, event.itemStack, event.modifiedFoodComponent));
    }
}
