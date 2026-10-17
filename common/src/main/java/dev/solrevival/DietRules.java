package dev.solrevival;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The parts of the config that decide how much a food is worth and how long it takes to eat.
 * The server sends its copy to each client so both sides agree.
 */
public record DietRules(int historySize, List<Double> multipliers, Set<String> exemptItems,
                        double eatingSpeedModifier, double maxEatingTimeMultiplier) {

    public static DietRules fromConfig() {
        return new DietRules(SolConfig.historySize, SolConfig.multipliers, SolConfig.exemptItems,
                SolConfig.eatingSpeedModifier, SolConfig.maxEatingTimeMultiplier);
    }

    /** The share of a food's value you get, given the meals you remember. */
    public double multiplierFor(String itemId, FoodHistory history) {
        if (exemptItems.contains(itemId)) return 1.0;
        int index = Math.min(history.count(itemId), multipliers.size() - 1);
        return multipliers.get(index);
    }

    /**
     * Stretches the time to eat a food that has lost value, with the original Spice of Life's formula
     * {@code duration / multiplier ^ eatingSpeedModifier}: at 50% value it takes twice as long by default.
     * Capped at {@link #maxEatingTimeMultiplier} times the normal time, where 0 means no cap.
     */
    public int eatingTicks(int baseTicks, double multiplier) {
        if (eatingSpeedModifier <= 0.0 || multiplier >= 1.0 || baseTicks <= 0) return baseTicks;
        double divisor = Math.pow(multiplier, eatingSpeedModifier);
        // the original made worthless food take Short.MAX_VALUE ticks, practically forever
        double ticks = divisor > 0.0 ? baseTicks / divisor : Short.MAX_VALUE;
        if (maxEatingTimeMultiplier > 0.0) ticks = Math.min(ticks, baseTicks * maxEatingTimeMultiplier);
        return (int) Math.min(Short.MAX_VALUE, Math.round(ticks));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(historySize);
        buf.writeVarInt(multipliers.size());
        multipliers.forEach(buf::writeDouble);
        buf.writeVarInt(exemptItems.size());
        exemptItems.forEach(buf::writeUtf);
        buf.writeDouble(eatingSpeedModifier);
        buf.writeDouble(maxEatingTimeMultiplier);
    }

    public static DietRules read(FriendlyByteBuf buf) {
        int historySize = buf.readVarInt();
        int count = buf.readVarInt();
        List<Double> multipliers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) multipliers.add(buf.readDouble());
        int exemptCount = buf.readVarInt();
        Set<String> exempt = new HashSet<>();
        for (int i = 0; i < exemptCount; i++) exempt.add(buf.readUtf());
        return new DietRules(historySize, List.copyOf(multipliers), Set.copyOf(exempt),
                buf.readDouble(), buf.readDouble());
    }
}
