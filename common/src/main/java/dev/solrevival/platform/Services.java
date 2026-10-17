package dev.solrevival.platform;

import dev.solrevival.Constants;
import dev.solrevival.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

/** Loads the Fabric or NeoForge implementation of the platform helpers at runtime. */
public final class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
