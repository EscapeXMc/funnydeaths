package com.escapex.funnydeaths;

import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Normalises config keys against the damage cause / entity type renames that happened
 * over the years so a config written on 1.8.9 keeps working on 26.3 (and vice versa).
 */
public final class Names {

    private static final Map<String, String> CAUSE_ALIASES = new HashMap<String, String>();
    private static final Map<String, String> ENTITY_ALIASES = new HashMap<String, String>();

    static {
        // damage cause renames across versions
        cause("ENTITY_ATTACK", "PLAYER_ATTACK");
        cause("EXPLOSION", "BLOCK_EXPLOSION");
        cause("LAVA", "HOT_FLOOR");
        cause("FALLING_BLOCK", "FALLING_ANVIL");
        cause("DROWNING", "DROWN");
        cause("STARVATION", "STARVE");
        cause("FIRE_TICK", "ON_FIRE");
        cause("MAGIC", "INDIRECT_MAGIC");
        cause("VOID", "OUT_OF_WORLD");
        cause("SUFFOCATION", "IN_WALL");
        // entity type renames across versions
        entity("PIG_ZOMBIE", "ZOMBIFIED_PIGLIN");
        entity("MUSHROOM_COW", "MOOSHROOM");
        entity("SNOWMAN", "SNOW_GOLEM");
        entity("PRIMED_TNT", "TNT");
        entity("BOAT", "OAK_BOAT");
    }

    private Names() {
    }

    private static void cause(String modern, String legacyOrFork) {
        CAUSE_ALIASES.put(modern, legacyOrFork);
        CAUSE_ALIASES.put(legacyOrFork, modern);
    }

    private static void entity(String legacy, String modern) {
        // both spellings collapse onto the legacy name so that a config written on an
        // old server and one written on a new server resolve to the same bucket
        ENTITY_ALIASES.put(legacy, legacy);
        ENTITY_ALIASES.put(modern, legacy);
    }

    /**
     * Canonical damage cause name for a config key, or null when this server knows neither name.
     */
    public static String cause(String key) {
        if (key == null) {
            return null;
        }
        String normalized = key.trim().toUpperCase(Locale.ROOT);
        if (exists(normalized)) {
            return normalized;
        }
        String alias = CAUSE_ALIASES.get(normalized);
        if (alias != null && exists(alias)) {
            return alias;
        }
        return null;
    }

    /**
     * Canonical mob name for a config key; unknown keys are returned trimmed/upper-cased.
     */
    public static String entity(String key) {
        if (key == null) {
            return "";
        }
        String normalized = key.trim().toUpperCase(Locale.ROOT);
        String alias = ENTITY_ALIASES.get(normalized);
        return alias != null ? alias : normalized;
    }

    private static boolean exists(String enumName) {
        try {
            EntityDamageEvent.DamageCause.valueOf(enumName);
            return true;
        } catch (IllegalArgumentException unknown) {
            return false;
        }
    }
}
