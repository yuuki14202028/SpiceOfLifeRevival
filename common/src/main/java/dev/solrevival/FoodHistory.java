package dev.solrevival;

import com.mojang.serialization.Codec;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** The most recent foods one player has eaten, oldest first. */
public final class FoodHistory {

    public static final Codec<List<String>> CODEC = Codec.STRING.listOf();

    private final Deque<String> recent = new ArrayDeque<>();

    /** Set whenever the meals change, so the server knows to send them to the client. Starts set. */
    private boolean dirty = true;

    /** How many times this item ID appears in the remembered meals. */
    public int count(String itemId) {
        int n = 0;
        for (String id : recent) {
            if (id.equals(itemId)) n++;
        }
        return n;
    }

    public void add(String itemId, int maxSize) {
        recent.addLast(itemId);
        trim(maxSize);
        dirty = true;
    }

    public void clear() {
        recent.clear();
        dirty = true;
    }

    public List<String> toList() {
        return new ArrayList<>(recent);
    }

    public void setFrom(List<String> ids, int maxSize) {
        recent.clear();
        recent.addAll(ids);
        trim(maxSize);
        dirty = true;
    }

    public void copyFrom(FoodHistory other) {
        setFrom(other.toList(), SolConfig.historySize);
    }

    public int size() {
        return recent.size();
    }

    /** Returns whether the meals changed since the last call, and clears the flag. */
    public boolean consumeDirty() {
        boolean was = dirty;
        dirty = false;
        return was;
    }

    private void trim(int maxSize) {
        while (recent.size() > maxSize) {
            recent.removeFirst();
        }
    }
}
