package dev.solrevival;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Applies the diminishing returns when a player eats, and answers "how much is this food worth to
 * this player right now" on either side. On the client the answer comes from what the server sent.
 */
public final class DietManager {

    private DietManager() {
    }

    public static String idOf(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    /** The rules for this player's side, or null on a client whose server has not sent any. */
    @Nullable
    public static DietRules rulesFor(Player player) {
        return player.level().isClientSide() ? ClientDietState.rules() : SolConfig.rules();
    }

    private static FoodHistory historyFor(Player player) {
        return player.level().isClientSide() ? ClientDietState.history() : FoodHistoryHolder.of(player);
    }

    /**
     * The share of value this player would get from this food right now, 1.0 when unknown.
     * On the client only the local player's meals are known.
     */
    public static double currentMultiplier(Player player, ItemStack stack) {
        DietRules rules = rulesFor(player);
        if (rules == null || (player.level().isClientSide() && !player.isLocalPlayer())) return 1.0;
        return rules.multiplierFor(idOf(stack), historyFor(player));
    }

    /** Food values reduced by a multiplier. Nutrition stays at least 1 until the food is worth nothing. */
    public static FoodProperties scale(FoodProperties food, double multiplier) {
        if (multiplier >= 1.0) return food;
        int nutrition = (int) Math.round(food.nutrition() * multiplier);
        if (multiplier > 0.0 && food.nutrition() > 0) {
            nutrition = Math.max(1, nutrition); // a little is better than nothing until fully bored
        }
        float saturation = (float) (food.saturation() * multiplier);
        return new FoodProperties(nutrition, saturation, food.canAlwaysEat());
    }

    /** How long this player takes to eat this food, given its vanilla duration. */
    public static int eatingTicks(Player player, ItemStack stack, int baseTicks) {
        DietRules rules = rulesFor(player);
        if (rules == null) return baseTicks;
        return rules.eatingTicks(baseTicks, currentMultiplier(player, stack));
    }

    /**
     * Called on the server just before a food is added to the player's hunger bar.
     * Records the meal and returns the (possibly reduced) food values to apply.
     */
    public static FoodProperties onEat(ServerPlayer player, ItemStack stack, FoodProperties food) {
        String id = idOf(stack);
        DietRules rules = SolConfig.rules();
        if (rules.exemptItems().contains(id)) {
            return food;
        }

        FoodHistory history = FoodHistoryHolder.of(player);
        double multiplier = rules.multiplierFor(id, history);
        history.add(id, rules.historySize());

        if (multiplier >= 1.0) {
            return food;
        }

        if (SolConfig.showMessages) {
            int percent = (int) Math.round(multiplier * 100);
            Component name = stack.getHoverName();
            Component message = percent > 0
                    ? Component.translatableWithFallback("message.solrevival.bored",
                    "You're getting tired of %s (%s%% value)", name, percent)
                    : Component.translatableWithFallback("message.solrevival.sick_of",
                    "You're sick of %s. It gives you nothing.", name);
            player.sendSystemMessage(message, true);
        }

        return scale(food, multiplier);
    }

    /**
     * Called on the client when the local player finishes eating. Records the meal straight away so
     * the next bite is timed correctly even before the server's update arrives.
     */
    public static void onEatClient(ItemStack stack) {
        DietRules rules = ClientDietState.rules();
        if (rules == null) return;
        String id = idOf(stack);
        if (!rules.exemptItems().contains(id)) {
            ClientDietState.history().add(id, rules.historySize());
        }
    }
}
