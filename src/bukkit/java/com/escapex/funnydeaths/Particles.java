package com.escapex.funnydeaths;

import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Method;

/**
 * Particle effect that survives the many Particle enum renamings
 * (1.8/1.9 particle names are completely different from 1.13+ and 1.20+).
 * Everything is done reflectively, so the same jar works everywhere.
 */
public final class Particles {

    private Particles() {
    }

    /** Totem / villager / heart particle puff at the given location. Silently does nothing when unsupported. */
    public static void resurrection(Location location) {
        if (location == null) {
            return;
        }
        try {
            World world = location.getWorld();
            if (world == null) {
                return;
            }
            Class<?> particleClass = Class.forName("org.bukkit.Particle");
            Object particle = resolve(particleClass);
            if (particle == null) {
                return;
            }
            spawn(world, particleClass, particle, location, 25, 0.3, 0.3, 0.3, 0.1);
        } catch (Throwable unsupported) {
            // particles are a nicety, never break the death message for them
        }
    }

    private static Object resolve(Class<?> particleClass) {
        String[] candidates = {
                "TOTEM_OF_UNDYING", // 1.11 - 1.20.x
                "TOTEM",           // 1.13 - 1.20.x alternate naming
                "VILLAGER_HAPPY",  // 1.9 - 1.12
                "HAPPY_VILLAGER",
                "HEART"
        };
        Object[] constants = particleClass.getEnumConstants();
        if (constants == null) {
            return null;
        }
        for (String candidate : candidates) {
            for (Object constant : constants) {
                if (constant instanceof Enum<?> && candidate.equals(((Enum<?>) constant).name())) {
                    return constant;
                }
                if (constant != null && candidate.equals(constant.toString())) {
                    return constant;
                }
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void spawn(World world, Class<?> particleClass, Object particle, Location location,
                              int count, double offsetX, double offsetY, double offsetZ, double speed)
            throws ReflectiveOperationException {
        // 1.9+ : spawnParticle(Particle, Location, int, double, double, double, double)
        // 1.13+: spawnParticle(Particle, Location, int, double, double, double, double, T data)
        // 1.8  : spawnParticle(Particle, Location, int)
        Method full = null;
        Method withData = null;
        Method simple = null;
        for (Method method : world.getClass().getMethods()) {
            if (!"spawnParticle".equals(method.getName())) {
                continue;
            }
            Class<?>[] types = method.getParameterTypes();
            if (types.length != 3 && types.length != 7 && types.length != 8) {
                continue;
            }
            if (types[0] != particleClass || !Location.class.isAssignableFrom(types[1])) {
                continue;
            }
            if (types.length == 7) {
                full = method;
            } else if (types.length == 8) {
                withData = method;
            } else {
                simple = method;
            }
        }
        if (full != null) {
            full.invoke(world, particle, location, Integer.valueOf(count), Double.valueOf(offsetX),
                    Double.valueOf(offsetY), Double.valueOf(offsetZ), Double.valueOf(speed));
        } else if (withData != null) {
            withData.invoke(world, particle, location, Integer.valueOf(count), Double.valueOf(offsetX),
                    Double.valueOf(offsetY), Double.valueOf(offsetZ), Double.valueOf(speed), null);
        } else if (simple != null) {
            simple.invoke(world, particle, location, Integer.valueOf(count));
        }
    }
}
