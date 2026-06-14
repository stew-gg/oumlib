package dev.oum.oumlib.text.placeholder.bridge;

import dev.oum.oumlib.text.placeholder.PlaceholderRegistry;
import org.bukkit.plugin.Plugin;

public final class PapiHelper {
    private PapiHelper() {
    }

    public static void register(Plugin plugin, PlaceholderRegistry registry) {
        for (String namespace : registry.getNamespaces().keySet()) {
            PapiPlaceholderBridge.register(plugin, registry, namespace);
        }
    }
}
