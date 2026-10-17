package dev.solrevival;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * /solrevival — lists your recent meals and, if you hold food, how much it is worth right now.
 * Anyone can run it; it only reads the player's own data.
 */
public final class SolCommands {

    private SolCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(Constants.MOD_ID).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            FoodHistory history = FoodHistoryHolder.of(player);
            List<String> meals = history.toList();

            String list = meals.isEmpty() ? "-" : String.join(", ", meals.reversed());
            ctx.getSource().sendSuccess(() -> Component.translatableWithFallback(
                    "command.solrevival.history",
                    "Recent meals (%s/%s, newest first): %s",
                    meals.size(), SolConfig.historySize, list), false);

            ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!held.isEmpty()) {
                int percent = (int) Math.round(DietManager.currentMultiplier(player, held) * 100);
                ctx.getSource().sendSuccess(() -> Component.translatableWithFallback(
                        "command.solrevival.held",
                        "%s is worth %s%% right now",
                        held.getHoverName(), percent), false);
            }
            return meals.size();
        }));
    }
}
