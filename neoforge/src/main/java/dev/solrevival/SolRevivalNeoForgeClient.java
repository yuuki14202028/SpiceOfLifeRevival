package dev.solrevival;

import dev.solrevival.client.FoodTooltip;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class SolRevivalNeoForgeClient {

    public SolRevivalNeoForgeClient(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> ClientDietState.reset());
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class,
                event -> FoodTooltip.append(event.getItemStack(), event.getEntity(), event.getToolTip()));
    }
}
