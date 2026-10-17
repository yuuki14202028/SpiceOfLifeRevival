package dev.solrevival;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * What the client knows about the local player's diet, as last sent by the server.
 * Empty until the server sends something, so a server without this mod changes nothing on the client.
 */
public final class ClientDietState {

    private static final FoodHistory HISTORY = new FoodHistory();
    @Nullable
    private static DietRules rules;

    private ClientDietState() {
    }

    public static FoodHistory history() {
        return HISTORY;
    }

    @Nullable
    public static DietRules rules() {
        return rules;
    }

    public static void update(List<String> history, DietRules newRules) {
        rules = newRules;
        HISTORY.setFrom(history, newRules.historySize());
    }

    /** Called on disconnect. */
    public static void reset() {
        rules = null;
        HISTORY.clear();
    }
}
