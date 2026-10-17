package dev.solrevival;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Settings, read from config/solrevival.properties.
 * The file is created with defaults on first launch. Edit it and restart the game or server.
 */
public final class SolConfig {

    /** How many recent meals each player remembers. */
    public static int historySize = 12;

    /**
     * How much of a food's value you get, by how many times it already appears in your recent meals.
     * Index 0 = not eaten recently, index 1 = eaten once recently, and so on. The last value repeats.
     */
    public static List<Double> multipliers = List.of(1.0, 1.0, 0.75, 0.5, 0.25, 0.0);

    /** Keep a player's meal history when they die. The original Spice of Life reset it (food.history.persists.through.death=false). */
    public static boolean keepHistoryOnDeath = true;

    /** Show a message above the hotbar when a food has lost value. */
    public static boolean showMessages = true;

    /** Item IDs that never lose value and are not remembered (for example minecraft:golden_apple). */
    public static Set<String> exemptItems = Set.of();

    /**
     * Food that has lost value takes longer to eat: time = normal / multiplier ^ this, as in the
     * original mod (its food.eating.speed.modifier, default 1). 0 turns it off.
     */
    public static double eatingSpeedModifier = 1.0;

    /** The longest eating can get, as a multiple of the normal time. 0 means no cap, like the original. */
    public static double maxEatingTimeMultiplier = 8.0;

    private static DietRules rules = DietRules.fromConfig();

    private SolConfig() {
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve(Constants.MOD_ID + ".properties");
        Properties props = new Properties();

        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (IOException e) {
                Constants.LOG.error("Could not read {}, using defaults", file, e);
            }
        }

        historySize = Math.max(1, parseInt(props.getProperty("history_size"), historySize));
        multipliers = parseMultipliers(props.getProperty("multipliers"), multipliers);
        keepHistoryOnDeath = parseBool(props.getProperty("keep_history_on_death"), keepHistoryOnDeath);
        showMessages = parseBool(props.getProperty("show_messages"), showMessages);
        exemptItems = parseSet(props.getProperty("exempt_items"));
        eatingSpeedModifier = Math.max(0.0, parseDouble(props.getProperty("eating_speed_modifier"), eatingSpeedModifier));
        maxEatingTimeMultiplier = Math.max(0.0, parseDouble(props.getProperty("max_eating_time_multiplier"), maxEatingTimeMultiplier));
        rules = DietRules.fromConfig();

        save(file);
        Constants.LOG.info("Loaded config: history {} meals, multipliers {}", historySize, multipliers);
    }

    /** The loaded settings that decide food value and eating time, as sent to clients. */
    public static DietRules rules() {
        return rules;
    }

    private static void save(Path file) {
        String content = """
                # Spice of Life: Revival
                # Restart the game or server after editing.

                # How many recent meals each player remembers.
                history_size=%d

                # Share of a food's value you get, by how many times it is already in your recent meals.
                # 1st value = not eaten recently, 2nd = eaten once, ... The last value repeats.
                multipliers=%s

                # Keep meal history when a player dies.
                keep_history_on_death=%b

                # Show a message above the hotbar when a food has lost value.
                show_messages=%b

                # Comma-separated item IDs that never lose value, e.g. minecraft:golden_apple,minecraft:enchanted_golden_apple
                exempt_items=%s

                # Food that has lost value takes longer to eat: time = normal time / value ^ this.
                # 1.0 = at 50%% value it takes twice as long (the original mod's default). 0 = off.
                eating_speed_modifier=%s

                # The longest eating can take, as a multiple of the normal time. 0 = no cap (the original
                # mod's default, which makes food worth 0%% take about half an hour to eat).
                max_eating_time_multiplier=%s
                """.formatted(
                historySize,
                String.join(",", multipliers.stream().map(String::valueOf).toList()),
                keepHistoryOnDeath,
                showMessages,
                String.join(",", exemptItems),
                eatingSpeedModifier,
                maxEatingTimeMultiplier);
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write(content);
            }
        } catch (IOException e) {
            Constants.LOG.error("Could not write {}", file, e);
        }
    }

    private static int parseInt(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            Constants.LOG.warn("Invalid number '{}' in config, using {}", value, fallback);
            return fallback;
        }
    }

    private static double parseDouble(String value, double fallback) {
        if (value == null) return fallback;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            Constants.LOG.warn("Invalid number '{}' in config, using {}", value, fallback);
            return fallback;
        }
    }

    private static boolean parseBool(String value, boolean fallback) {
        return value == null ? fallback : Boolean.parseBoolean(value.trim());
    }

    private static List<Double> parseMultipliers(String value, List<Double> fallback) {
        if (value == null || value.isBlank()) return fallback;
        List<Double> result = new ArrayList<>();
        for (String part : value.split(",")) {
            try {
                double d = Double.parseDouble(part.trim());
                result.add(Math.max(0.0, Math.min(1.0, d)));
            } catch (NumberFormatException e) {
                Constants.LOG.warn("Invalid multipliers '{}' in config, using defaults", value);
                return fallback;
            }
        }
        return result.isEmpty() ? fallback : List.copyOf(result);
    }

    private static Set<String> parseSet(String value) {
        Set<String> result = new HashSet<>();
        if (value == null) return result;
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return Set.copyOf(result);
    }
}
