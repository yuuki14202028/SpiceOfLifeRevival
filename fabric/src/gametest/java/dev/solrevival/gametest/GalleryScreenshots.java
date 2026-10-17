package dev.solrevival.gametest;

import com.mojang.datafixers.util.Pair;
import dev.solrevival.FoodHistory;
import dev.solrevival.FoodHistoryHolder;
import dev.solrevival.SolConfig;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Plays a short scene and saves screenshots for the mod page: the player has had steak three times
 * recently, eats a fourth (slowly, at 50% value), then looks at it in the inventory.
 * Run with ./gradlew :fabric:runClientGameTest; images land in build/run/clientGameTest/screenshots.
 */
public class GalleryScreenshots implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        try {
            playScene(context);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void playScene(ClientGameTestContext context) throws ReflectiveOperationException {
        context.getInput().resizeWindow(1600, 900);
        context.runOnClient(mc -> mc.options.guiScale().set(3));

        try (TestSingleplayerContext world = context.worldBuilder()
                .setUseConsistentSettings(false)
                .adjustSettings(settings -> {
                    settings.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL);
                    settings.setDifficulty(Difficulty.EASY);
                    settings.setSeed("spice of life");
                })
                .create()) {
            world.getServer().runOnServer(GalleryScreenshots::moveToCherryGrove);
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("time set 6000");
            world.getServer().runCommand("weather clear");
            world.getServer().runOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                player.getFoodData().setFoodLevel(6);
                player.getFoodData().setSaturation(0);

                FoodHistory history = FoodHistoryHolder.of(player);
                for (String id : new String[]{"minecraft:bread", "minecraft:cooked_beef", "minecraft:apple",
                        "minecraft:cooked_beef", "minecraft:baked_potato", "minecraft:cooked_beef"}) {
                    history.add(id, SolConfig.historySize);
                }

                player.getInventory().setItem(0, new ItemStack(Items.COOKED_BEEF, 12));
                player.getInventory().setItem(1, new ItemStack(Items.BREAD, 7));
                player.getInventory().setItem(2, new ItemStack(Items.BAKED_POTATO, 9));
                player.getInventory().setItem(3, new ItemStack(Items.APPLE, 5));
                player.getInventory().setItem(4, new ItemStack(Items.CARROT, 14));
                player.getInventory().setItem(5, new ItemStack(Items.COOKED_SALMON, 6));
                player.getInventory().setItem(6, new ItemStack(Items.PUMPKIN_PIE, 3));
                player.getInventory().setSelectedSlot(0);
            });
            context.waitTicks(40);
            context.runOnClient(mc -> mc.gui.toastManager().clear());
            context.takeScreenshot("solrevival_holding");

            // a fourth steak: worth 50% now, and takes twice as long to eat (64 ticks)
            context.getInput().holdKey(options -> options.keyUse);
            context.waitTicks(30);
            context.takeScreenshot("solrevival_eating");
            context.waitTicks(36);
            context.getInput().releaseKey(options -> options.keyUse);
            context.waitTicks(25); // let the crumbs fall; the message stays up for about three seconds
            context.runOnClient(mc -> mc.gui.toastManager().clear());
            context.takeScreenshot("solrevival_tired");

            context.getInput().pressKey(options -> options.keyInventory);
            context.waitForScreen(InventoryScreen.class);
            double[] steak = context.computeOnClient(GalleryScreenshots::steakPosition);
            context.getInput().setCursorPos(steak[0], steak[1]);
            context.waitTicks(10);
            context.takeScreenshot("solrevival_tooltip");
        }
    }

    /** Stands the player on the surface of a nearby cherry grove, looking across it. */
    private static void moveToCherryGrove(MinecraftServer server) {
        ServerLevel level = server.overworld();
        ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
        Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(
                biome -> biome.is(Biomes.CHERRY_GROVE), player.blockPosition(), 6400, 32, 64);
        if (found == null) return;
        // stand on the highest open grass nearby, not under a tree, so the view runs out over the grove
        BlockPos center = found.getFirst();
        BlockPos best = null;
        for (int dx = -48; dx <= 48; dx += 3) {
            for (int dz = -48; dz <= 48; dz += 3) {
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                level.getChunk(new BlockPos(x, 0, z)); // generate it so the height maps are real
                int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                boolean open = ground == top && level.getBlockState(new BlockPos(x, ground - 1, z)).is(Blocks.GRASS_BLOCK);
                if (open && (best == null || ground > best.getY())) {
                    best = new BlockPos(x, ground, z);
                }
            }
        }
        if (best != null) {
            // face back toward the middle of the grove, looking slightly down
            float yaw = (float) Math.toDegrees(Math.atan2(-(center.getX() - best.getX()), center.getZ() - best.getZ()));
            player.teleportTo(level, best.getX() + 0.5, best.getY(), best.getZ() + 0.5, Set.of(), yaw, 15, true);
        }
    }

    /** Where the steak sits in the open inventory, in window coordinates. */
    private static double[] steakPosition(Minecraft mc) throws ReflectiveOperationException {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.gui.screen();
        Field leftPos = AbstractContainerScreen.class.getDeclaredField("leftPos");
        Field topPos = AbstractContainerScreen.class.getDeclaredField("topPos");
        leftPos.setAccessible(true);
        topPos.setAccessible(true);
        for (Slot slot : screen.getMenu().slots) {
            if (slot.getItem().is(Items.COOKED_BEEF)) {
                double guiX = leftPos.getInt(screen) + slot.x + 8;
                double guiY = topPos.getInt(screen) + slot.y + 8;
                double scaleX = (double) mc.getWindow().getScreenWidth() / mc.getWindow().getGuiScaledWidth();
                double scaleY = (double) mc.getWindow().getScreenHeight() / mc.getWindow().getGuiScaledHeight();
                return new double[]{guiX * scaleX, guiY * scaleY};
            }
        }
        throw new IllegalStateException("No steak in the inventory");
    }
}
