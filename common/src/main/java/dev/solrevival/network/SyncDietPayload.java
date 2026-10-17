package dev.solrevival.network;

import dev.solrevival.Constants;
import dev.solrevival.DietRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Server to client: the player's recent meals and the server's rules.
 * The client needs both to stretch eating time, fill in tooltips and feed AppleSkin.
 */
public record SyncDietPayload(List<String> history, DietRules rules) implements CustomPacketPayload {

    public static final Type<SyncDietPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "sync_diet"));

    public static final StreamCodec<FriendlyByteBuf, SyncDietPayload> STREAM_CODEC =
            CustomPacketPayload.codec(SyncDietPayload::write, SyncDietPayload::read);

    private static SyncDietPayload read(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<String> history = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            history.add(buf.readUtf());
        }
        return new SyncDietPayload(history, DietRules.read(buf));
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(history.size());
        history.forEach(buf::writeUtf);
        rules.write(buf);
    }

    @Override
    public Type<SyncDietPayload> type() {
        return TYPE;
    }
}
