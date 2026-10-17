package dev.solrevival;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;

/**
 * /solrevival — lists your recent meals and, if you hold food, how much it is worth right now.
 * Anyone can run it; it only reads the player's own data.
 * /solrevival reset [players] — forgets meals, like the original's /spiceoflife reset. Operators only.
 */
public final class SolCommands {

    private SolCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(Constants.MOD_ID)
                .executes(SolCommands::showHistory)
                .then(Commands.literal("reset")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> reset(ctx.getSource(), List.of(ctx.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> reset(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"))))));
    }

    private static int showHistory(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
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
    }

    private static int reset(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer target : targets) {
            FoodHistoryHolder.of(target).clear(); // the next tick sends the empty history to the client
        }
        if (targets.size() == 1) {
            Component name = targets.iterator().next().getDisplayName();
            source.sendSuccess(() -> Component.translatableWithFallback("command.solrevival.reset.single",
                    "Reset the meal history of %s", name), true);
        } else {
            source.sendSuccess(() -> Component.translatableWithFallback("command.solrevival.reset.multiple",
                    "Reset the meal history of %s players", targets.size()), true);
        }
        return targets.size();
    }
}
