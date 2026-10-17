package dev.solrevival;

import dev.solrevival.compat.AppleSkinNeoForge;
import dev.solrevival.network.SyncDietPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(Constants.MOD_ID)
public class SolRevivalNeoForge {

    public SolRevivalNeoForge(IEventBus modEventBus) {
        CommonClass.init();
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1")
                // optional: players without the mod can still join; they just get no client-side extras
                .optional()
                .playToClient(SyncDietPayload.TYPE, SyncDietPayload.STREAM_CODEC,
                        (payload, context) -> ClientDietState.update(payload.history(), payload.rules())));
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class,
                event -> SolCommands.register(event.getDispatcher()));
        if (ModList.get().isLoaded("appleskin")) {
            AppleSkinNeoForge.register(); // separate class so AppleSkin is never touched when absent
        }
    }
}
