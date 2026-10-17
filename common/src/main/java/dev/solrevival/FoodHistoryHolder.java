package dev.solrevival;

/** Implemented on every Player by {@link dev.solrevival.mixin.PlayerMixin}. */
public interface FoodHistoryHolder {

    FoodHistory solrevival$getFoodHistory();

    static FoodHistory of(Object player) {
        return ((FoodHistoryHolder) player).solrevival$getFoodHistory();
    }
}
