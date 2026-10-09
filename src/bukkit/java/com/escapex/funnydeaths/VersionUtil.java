package com.escapex.funnydeaths;

import org.bukkit.Bukkit;

/**
 * Version detection that understands both the old scheme (1.8.9 ... 1.21.11)
 * and the new year-based scheme (26.1, 26.1.1, 26.1.2, 26.2, 26.3 ...).
 */
public final class VersionUtil {

    private static final String RAW;
    private static final int[] VERSION;

    static {
        String raw = "";
        int[] parsed = new int[] {Integer.MAX_VALUE, 0, 0};
        try {
            raw = Bukkit.getBukkitVersion();
        } catch (Throwable ignored) {
            // never fails plugin load
        }
        if (raw == null) {
            raw = "";
        }
        String base = raw;
        int dash = base.indexOf('-');
        if (dash >= 0) {
            base = base.substring(0, dash);
        }
        if (!base.isEmpty()) {
            String[] parts = base.split("\\.");
            try {
                if (parts.length >= 3) {
                    parsed = new int[] {Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])};
                } else if (parts.length == 2) {
                    parsed = new int[] {Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 0};
                }
            } catch (NumberFormatException ignored) {
                // fall through, safe defaults above
            }
        }
        RAW = raw;
        VERSION = parsed;
    }

    private VersionUtil() {
    }

    /** Raw version string reported by the server, e.g. "1.8.8-R0.1-SNAPSHOT" or "26.3-R0.1-SNAPSHOT". */
    public static String raw() {
        return RAW;
    }

    public static int major() {
        return VERSION[0];
    }

    public static int minor() {
        return VERSION[1];
    }

    public static int patch() {
        return VERSION[2];
    }

    /**
     * True when the running server is at least major.minor.
     * Works for both version schemes: 26.3 is "greater" than any 1.x version.
     */
    public static boolean isAtLeast(int major, int minor) {
        if (VERSION[0] != major) {
            return VERSION[0] > major;
        }
        return VERSION[1] >= minor;
    }

    /** Hex colours (&#RRGGBB) exist since 1.16. */
    public static boolean supportsHexColors() {
        return isAtLeast(1, 16);
    }

    /** The Particle API only exists from 1.9 upwards. */
    public static boolean supportsParticles() {
        try {
            Class.forName("org.bukkit.Particle");
            return true;
        } catch (Throwable notAvailable) {
            return false;
        }
    }

    /** Folia and its forks (Leaves, ...) refuse the legacy Bukkit scheduler. */
    public static boolean isFoliaFamily() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (Throwable notRegionized) {
            // fall through to the server name check
        }
        try {
            String name = Bukkit.getServer().getName();
            return name != null && name.toLowerCase().contains("folia");
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Human readable platform line for the console. */
    public static String describe() {
        String serverName = "unknown";
        try {
            serverName = Bukkit.getServer().getName();
        } catch (Throwable ignored) {
        }
        return serverName + " / API " + (RAW.isEmpty() ? "unknown" : RAW);
    }
}
