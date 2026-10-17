package dev.solrevival;

import dev.solrevival.platform.Services;

/** Shared startup, called from each loader's entry point. */
public final class CommonClass {

    private CommonClass() {
    }

    public static void init() {
        SolConfig.load(Services.PLATFORM.getConfigDir());
        Constants.LOG.info("{} loaded on {}", Constants.MOD_NAME, Services.PLATFORM.getPlatformName());
    }
}
