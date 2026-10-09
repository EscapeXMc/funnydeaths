package com.escapex.funnydeaths;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Task scheduling that works on Spigot/CraftBukkit/Paper and on Folia-style
 * regionised servers (Folia, Leaves) which reject the legacy Bukkit scheduler.
 */
public final class SchedulerCompat {

    private SchedulerCompat() {
    }

    /**
     * Runs the task on the thread that owns the given entity.
     * Falls back to the Folia global region scheduler and finally to a direct call.
     */
    public static void runEntity(Plugin plugin, Entity entity, Runnable task) {
        try {
            Bukkit.getScheduler().runTask(plugin, task);
            return;
        } catch (Throwable legacyUnsupported) {
            // Folia-style server: no legacy scheduler available
        }
        if (entity != null) {
            try {
                Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
                Method run = scheduler.getClass().getMethod("run", Plugin.class, Runnable.class, Runnable.class);
                run.invoke(scheduler, plugin, task, null);
                return;
            } catch (Throwable ignored) {
                // try the global region scheduler next
            }
        }
        try {
            Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Method run = scheduler.getClass().getMethod("run", Plugin.class, Runnable.class);
            run.invoke(scheduler, plugin, task);
            return;
        } catch (Throwable ignored) {
            // last resort: run inline, the workload is tiny
        }
        task.run();
    }
}
